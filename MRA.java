import jade.core.Agent;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.HashSet;
import java.util.List;

import com.google.gson.Gson;

public class MRA extends Agent {
	private Map<AID, String> daCapacities = new HashMap<>();
    private Set<AID> agreeDAs = new HashSet<>();
    private Set<AID> routerequestDAs = new HashSet<>();
    private boolean allResponsesReceived = false;
    private Map<AID, Boolean> routeRequestSent = new HashMap<>();

    private Map<Coordinate, Integer> fileData = new HashMap<>();
    Set<Coordinate> usedCoordinates = new HashSet<>(); // address that already taken by other DA

    protected void setup() {
    	
        resetState();

        // read txt file
        String fileName = "coordinates.txt";
        fileData = readCoordinatesFromFile(fileName);

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
        // Add behavior to handle route request messages from DAs
        addBehaviour(new RouteRequestListener(this));
    }


    // Listen for CFP message
    private class TriggerListener extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.CFP));
            if (msg != null) {
                System.out.println("MRA: Received CFP from " + msg.getSender().getName());
                addBehaviour(new RequestBehaviour());
                addBehaviour(new CapacityCollectingBehaviour());
                
                
            } else {
                block();
            }
        }
    }

    // Send broadcast REQUEST 
    private class RequestBehaviour extends OneShotBehaviour {
        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("DeliveryAgent");
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                if (result.length > 0) {
                    System.out.println("MRA: Broadcasting request to " + result.length + " Delivery Agents (DAs).");
                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                    for (DFAgentDescription dfd : result) {
                        msg.addReceiver(dfd.getName());
                    }
                    msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                    msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
                    msg.setContent("Available for VRP?");

                    addBehaviour(new RequestInitiator(myAgent, msg));
                } else {
                    System.out.println("MRA: No Delivery Agents (DAs) found.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //Handle response from DA
    private class RequestInitiator extends AchieveREInitiator {
        public RequestInitiator(Agent a, ACLMessage msg) {
            super(a, msg);
        }

        protected void handleAgree(ACLMessage agree) {
            System.out.println("MRA: " + agree.getSender().getName() + " has agreed to the request.");
            agreeDAs.add(agree.getSender());
        }

        protected void handleRefuse(ACLMessage refuse) {
            System.out.println("MRA: " + refuse.getSender().getName() + " refused the request.");
        }

        protected void handleAllResultNotifications(Vector notifications) {
            System.out.println("MRA: All responses received from DAs.");
            allResponsesReceived = true;
            addBehaviour(new CapacityCollectingBehaviour()); 
        }
    }

    
    // Receive Capacity from DA and send REQUEST to send route
    private class CapacityCollectingBehaviour extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = receive(mt);
            if (msg != null) {
                if (!msg.getConversationId().equals("route-request")) {
                    processCapacityMessage(msg);
                    if (!routeRequestSent.containsKey(msg.getSender()) || !routeRequestSent.get(msg.getSender())) {
                        sendRouteRequest(msg.getSender());
                    }
                } 
            } else {
                block();
            }
        }
    }


    private void processCapacityMessage(ACLMessage msg) {
        String capacity = msg.getContent();  // Directly using the string content
        daCapacities.put(msg.getSender(), capacity);
        System.out.println("Received capacity " + capacity + " from " + msg.getSender().getName());
    }


    private void sendRouteRequest(AID receiver) {
        ACLMessage request = new ACLMessage(ACLMessage.INFORM);
        request.addReceiver(receiver);
        request.setContent("Please request your route");
        request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);  
        request.setConversationId("route-request");
        send(request);
        System.out.println("MRA: Requested " + receiver.getLocalName() + " to request their route.");
    }

    // Listens for route request
    // Triggers initial route generation once all DAs have requested for route and informed capacity
    private class RouteRequestListener extends CyclicBehaviour {
        public RouteRequestListener(Agent a) {
            super(a);
        }

        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId("route-request")
            );

            ACLMessage request = myAgent.receive(mt);
            if (request != null) {
                System.out.println("MRA: Received route request from " + request.getSender().getName());
                routerequestDAs.add(request.getSender());

                // Check if all DAs have sent a request
                if (routerequestDAs.equals(daCapacities.keySet())) {
                    System.out.println("MRA: Received route request from all DAs");
                    startRouteGeneration();
                }
            } else {
                block();  // Block the behaviour until the next message is received
            }
        }
    }

    
    private void startRouteGeneration() {
        while (usedCoordinates.size() != fileData.size()) 
        {
            // Iterate over all entries in the daCapacities map
            for (Map.Entry<AID, String> entry : daCapacities.entrySet()) {
                AID daAgent = entry.getKey();
                Integer capacity = Integer.parseInt(entry.getValue());

                /*  seperate location and generate route */
                List<Coordinate> notYetOptimize = new ArrayList<>();
                int tempTotalCapacity = 0;
                for (Map.Entry<Coordinate, Integer> entry2 : fileData.entrySet()) {
                    // if didnt exceed DA's capacity && havent taken by other DA, then this address will be assigned to current DA
                    if(tempTotalCapacity + entry2.getValue() <= capacity && !usedCoordinates.contains(entry2.getKey()) ) {
                        tempTotalCapacity += entry2.getValue();
                        notYetOptimize.add(entry2.getKey());
                        usedCoordinates.add(entry2.getKey());
                    }
                    else {
                        break; // stop iteration if current DA no capacity left or no more parcel to sent
                    }
                }

                /*  optimize the route  */
                Coordinate centerWarehouse = new Coordinate(1.532302, 110.357173);
                // in case no more parcel to are in the list: in 2nd batch, maybe all the parcel has been taken and no more parcel left for other agent
                // if no parcel, just skip the optimize and sent inform process to avoid error 
                if (notYetOptimize.size() != 0) {
                    notYetOptimize.add(0, centerWarehouse);
                    List<Integer> distances = calculateDistances(notYetOptimize);
                    printDistanceReference(notYetOptimize, distances); // print distance between address, can delete, for understanding and debug purpose

                    // format distances data so algorithm can work
                    int numberOfCoordinates = notYetOptimize.size();
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

                    List<Coordinate> optimizedRoute = new ArrayList<>(); 
                    for (int i: resultList) {
                        optimizedRoute.add(notYetOptimize.get(i));
                    }

                    // Create a new ACL message for each DA
                    ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                    inform.addReceiver(daAgent);
                    String route = "Route for " + daAgent.getLocalName() + " with capacity " + capacity;
                    // convert optimize route to json format and sent it to DA
                    Gson gson = new Gson();
                    String optimizedRouteJson = gson.toJson(optimizedRoute);
                    inform.setContent(optimizedRouteJson);
                    inform.setConversationId("route-response"); 

                    this.send(inform); 
                    System.out.println("MRA: Route assigned to " + daAgent.getLocalName() + ": " + route);
                }
                
            }

            // initialize all DA status
            for (AID daAgent : daCapacities.keySet()) {
                routeRequestSent.put(daAgent, false);
            }
        }
    	
    }

    
    private void resetState() {
        daCapacities.clear();
        agreeDAs.clear();
        allResponsesReceived = false;  // Resetting all the state variables
        routeRequestSent.clear(); // Clear routeRequestSent map
        System.out.println("MRA: State has been reset for a new cycle.");
    }


    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}
