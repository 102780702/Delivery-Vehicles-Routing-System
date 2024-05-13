import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.domain.FIPANames;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.List;

public class MRA extends Agent {
    private Map<AID, Integer> daCapacities = new HashMap<>();
    private Map<AID, List<Map<Coordinate, Integer>>> coordinateInfoMaps = new HashMap<>(); // DAs area
    private Map<Coordinate, Integer> locationAndParcelNeedToBeDelivery = new HashMap<>(); // file data
    private Set<AID> requestSenders = new HashSet<>();
    private boolean triggered = false;

    protected void setup() {
    	// Register the MRA agent with the DF service
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("MRA");
        sd.setName(getLocalName() + "-MRA");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Add behavior to handle CFP messages from dummy agents
        addBehaviour(new TriggerListener());
    }

    // Inner class to handle CFP messages from dummy agents
    private class TriggerListener extends jade.core.behaviours.CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                if (msg.getPerformative() == ACLMessage.CFP && !triggered) {
                    System.out.println("MRA: Received CFP from " + msg.getSender().getName());
                    triggered = true;
                    startActions();
                }
            } else {
                block();
            }
        }
    }

    // Start MRA actions after being triggered by a CFP
    private void startActions() {

    	// Collect delivery data
        String fileName = "coordinates.txt";
        locationAndParcelNeedToBeDelivery = readCoordinatesFromFile(fileName);

        // Search for agents providing the "DeliveryAgent" service
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("DeliveryAgent");
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this, template);
            if (result.length > 0) {
                System.out.println("MRA: Broadcasting request to " + result.length + " Delivery Agents (DAs).");

                // Create a REQUEST message
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);

                // Add all DAs as receivers
                for (DFAgentDescription dfd : result) {
                    AID aid = dfd.getName();
                    msg.addReceiver(aid);
                }

                // Set the interaction protocol
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                // Specify the reply deadline (10 seconds)
                msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
                // Set message content
                msg.setContent("VRP request");

                // Initialize the AchieveREInitiator behavior and add it to the agent
                addBehaviour(new AchieveREInitiator(this, msg) {
                    // Method to handle an agree message from a DA
                    protected void handleAgree(ACLMessage agree) {
                        System.out.println("MRA: " + agree.getSender().getName() + " has agreed to the request.");
                    }

                    // Method to handle requests from DAs
                    protected void handleRequest(ACLMessage request) {
                        if (request.getPerformative() == ACLMessage.REQUEST) {
                            System.out.println("MRA: Received request from " + request.getSender().getName());
                            // Add sender to set of request senders
                            requestSenders.add(request.getSender());

                            // agree msg to request senders?

                        } else {
                            System.out.println("MRA: Unexpected message received from " + request.getSender().getName());
                        }
                    }

                    // Method to handle a refuse message from a DA
                    protected void handleRefuse(ACLMessage refuse) {
                        System.out.println("MRA: " + refuse.getSender().getName() + " refused to process the request.");
                    }

                    // Method to handle a failure message (failure in delivering the message)
                    protected void handleFailure(ACLMessage failure) {
                        if (failure.getSender().equals(myAgent.getAMS())) {
                            // FAILURE notification from the JADE runtime: the receiver (DA) does not exist
                            System.out.println("MRA: Responder does not exist.");
                        } else {
                            System.out.println("MRA: " + failure.getSender().getName() + " failed to process the request.");
                        }
                    }

                    // Method that is invoked when notifications have been received from all responders
                    // expected, but run the above one 
                    protected void handleAllResultNotifications(jade.util.leap.List notifications) {
                        System.out.println("MRA: Received responses from all responders.");

                        // Add behavior to handle incoming capacity inform messages from DAs
                        addBehaviour(new CapacityInformListener());
                    }
                });
            } else {
                System.out.println("MRA: No Delivery Agents (DAs) found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Inner class to handle incoming capacity inform messages from DAs
    private class CapacityInformListener extends jade.core.behaviours.CyclicBehaviour {
        private Set<AID> informSenders = new HashSet<>();

        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                if (msg.getPerformative() == ACLMessage.INFORM) {
                    // Process capacity inform message
                    System.out.println("MRA: Received capacity information from " + msg.getSender().getName());
                    System.out.println("MRA: Capacity: " + msg.getContent());

                    // Store the capacity
                    AID daAgent = msg.getSender();
                    int capacity = Integer.parseInt(msg.getContent());
                    daCapacities.put(daAgent, capacity);

                    // Add sender to set of inform senders
                    informSenders.add(daAgent);

                    // Check if all request senders have informed their capacities
                    if (informSenders.equals(requestSenders)) {
                        // All DAs that sent a request have informed their capacities
                        System.out.println("MRA: All DAs that sent a request have informed their capacities.");

                        // according to DA's capacity seperate delivery coordinate
                        for (AID aid : daCapacities.keySet()) {
                            List<Map<Coordinate, Integer>> listOfDeliveyLocation = new ArrayList<>();
                            Map<Coordinate, Integer> map = new HashMap<>();
                            listOfDeliveyLocation.add(map);
                            coordinateInfoMaps.put(aid, listOfDeliveyLocation); // put empty map ready to store delivery areas
                        }

                        // assign delivery address to each area, make sure not exceed agent capacity
                        int taken = 0;
                        for (Map.Entry<AID, List<Map<Coordinate, Integer>>> entry : coordinateInfoMaps.entrySet()) {
                            AID agentId = entry.getKey();
                            
                            Integer agentCapacity = daCapacities.get(agentId);
                            
                             // assigning items to DA
                            int tempTotalCapacity = 0;
                            for (Map.Entry<Coordinate, Integer> coordinateEntry : locationAndParcelNeedToBeDelivery.entrySet().stream().skip(taken).collect(Collectors.toList())) {
                                Coordinate coordinate = coordinateEntry.getKey();
                                Integer deliveryItemsCapacity = coordinateEntry.getValue();
                                
                                if (tempTotalCapacity + deliveryItemsCapacity <= agentCapacity) {
                                    tempTotalCapacity += deliveryItemsCapacity;
                                    List<Map<Coordinate, Integer>> tempList = entry.getValue();
                                    Map<Coordinate, Integer> tempMap = new HashMap<>();
                                    tempMap.put(coordinate, deliveryItemsCapacity);
                                    tempList.add(tempMap);
                                    taken++;
                                } 
                                else {
                                    break;
                                }
                            }
                        }

                        // Generate routes
                        generateRoutes(coordinateInfoMaps);
                    }
                }
            } else {
                block();
            }
        }
    }

    // Method to generate route based on informed capacities and inform DAs
    private void generateRoutes(Map<AID, List<Map<Coordinate, Integer>>> coordinateInfoMaps) {
        // Dummy implementation for now
        // System.out.println("MRA: Generating routes based on informed capacities...");
        // for (Map.Entry<AID, Integer> entry : daCapacities.entrySet()) {
        //     AID daAgent = entry.getKey();
        //     int capacity = entry.getValue();

        //     // Here you can implement your logic to generate routes based on capacity
        //     String route = "Dummy route for " + daAgent.getName();

        for (Map.Entry<AID, List<Map<Coordinate, Integer>>> entry : coordinateInfoMaps.entrySet()) {
            AID agentId = entry.getKey();

            List<Map<Coordinate, Integer>> areaList = entry.getValue();

            // Algrotrithm here
            for (Map<Coordinate, Integer> area: areaList) {

                List<Coordinate> coordinateList = new ArrayList<>();
                List<List<Coordinate>> listOfShortestPath = new ArrayList<>();

                for (Map.Entry<Coordinate, Integer> entry1 : area.entrySet()) {
                    Coordinate coordinate = entry1.getKey();
                    coordinateList.add(coordinate);
                }
                Coordinate centerWarehouse = new Coordinate(1.532302, 110.357173);
                coordinateList.add(0, centerWarehouse);
    
                // function to calculate distance between each address
                List<Integer> distances = calculateDistances(coordinateList);
                printDistanceReference(coordinateList, distances); // print distance between address, can delete, for understanding and debug purpose
                
                // format distances data so algorithm can work
                int numberOfCoordinates = coordinateList.size();
                int[][] travelPrices = new int[numberOfCoordinates][numberOfCoordinates];
                int index = 0;
                for (int i = 0; i < numberOfCoordinates; i++) {
                    for (int j = 0; j < numberOfCoordinates; j++) {
                        if (i != j) {
                            if (travelPrices[i][j] == 0) {
                                travelPrices[i][j] = distances.get(index);
                                travelPrices[j][i] = travelPrices[i][j];
                                index ++;
                            }
                        }
                    }
                }
    
                printTravelPrices(travelPrices,numberOfCoordinates); // print matrix, can delete, for understanding and debug purpose
    
                // algorithm functions
                UberSalesmensch geneticAlgorithm = new UberSalesmensch(numberOfCoordinates, SelectionType.ROULETTE, travelPrices, 0, 0);
                SalesmanGenome result = geneticAlgorithm.optimize();
    
                // print address index, can delete, for understanding and debug purpose
                List<Integer> resultList = convertSalesmanGenomeToIntList(result);
                System.out.println(resultList);
    
                // Store the shortest path into this list and ready to pass to DA
                List<Coordinate> shortestPath = new ArrayList<>(); 
                for (int i: resultList) {
                    shortestPath.add(coordinateList.get(i));
                }
                
                listOfShortestPath.add(shortestPath);
            }

            // pass listOfShortestPath to DA, which mean da should hava a variable to store this, datatype: List<List<Coordinate>>
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(agentId);
            msg.setContent(listOfShortestPath); // something like this? need to convert to string ?
            send(msg);
            System.out.println("MRA: Informed " + agentId.getName() + " about the route: " + listOfShortestPath); 
        }
            
    }

    private static int calculateDistance(Coordinate loc1, Coordinate loc2) {
        double lat1 = Math.toRadians(loc1.getLatitude());
        double lon1 = Math.toRadians(loc1.getLongitude());
        double lat2 = Math.toRadians(loc2.getLatitude());
        double lon2 = Math.toRadians(loc2.getLongitude());
        
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        
        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // earth radius in meters
        double radius = 6371000; // 6371 km converted to meters
        
        return (int) Math.round(radius * c);
    }
    

    public static List<Integer> calculateDistances(List<Coordinate> locations) {
        List<Integer> distances = new ArrayList<>();
        int n = locations.size();
        
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Coordinate loc1 = locations.get(i);
                Coordinate loc2 = locations.get(j);
                int distance = calculateDistance(loc1, loc2);
                distances.add(distance);
            }
        }
        
        return distances;
    }

    public static void printDistanceReference(List<Coordinate> locations, List<Integer> distances) {
        int n = locations.size();
        int index = 0;
        
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Coordinate loc1 = locations.get(i);
                Coordinate loc2 = locations.get(j);
                System.out.println("[ " +loc1.getLatitude() + ", " + loc1.getLongitude() + " ] and [" +loc2.getLatitude() + ", " + loc2.getLongitude() + "]: " + distances.get(index).toString() + "m");
                index++;
            }
        }
    }

    public static void printTravelPrices(int[][] travelPrices, int numberOfCoordinates){
        for(int i = 0; i<numberOfCoordinates; i++){
            for(int j=0; j<numberOfCoordinates; j++){
                System.out.print(travelPrices[i][j]);
                if(travelPrices[i][j]/10 == 0)
                    System.out.print("  ");
                else
                    System.out.print(' ');
            }
            System.out.println("");
        }
    }

    public static List<Integer> convertSalesmanGenomeToIntList(SalesmanGenome genome) {
        List<Integer> integerSequence = new ArrayList<>();
        
        integerSequence.add(genome.getStartingCity());
        
        for (int gene : genome.getGenome()) {
            integerSequence.add(gene);
        }
        
        integerSequence.add(genome.getStartingCity());
        
        return integerSequence;
    }

    public static Map<Coordinate, Integer> readCoordinatesFromFile(String fileName) 
    {
        Map<Coordinate, Integer> coordinateInfoMap = new HashMap<>();
        BufferedReader reader;
        try 
        {
            reader = new BufferedReader(new FileReader(fileName));
            String line = reader.readLine();
            while (line != null) 
            {
                String[] parts = line.split(",");
                if (parts.length == 3) 
                {
                    double latitude = Double.parseDouble(parts[0]);
                    double longitude = Double.parseDouble(parts[1]);
                    int capacity = Integer.parseInt(parts[2]);
                    Coordinate coordinate = new Coordinate(latitude, longitude);
                    coordinateInfoMap.put(coordinate, capacity);
                } 
                else 
                {
                    System.err.println("Invalid format: " + line);
                }
                line = reader.readLine();
            }
            reader.close();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return coordinateInfoMap;
    }
}