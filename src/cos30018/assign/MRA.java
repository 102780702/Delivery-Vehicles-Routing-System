package cos30018.assign;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.domain.FIPANames;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class MRA extends Agent {
    private Map<AID, Integer> daCapacities = new HashMap<>();
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
                        // Generate routes
                        generateRoutes();
                    }
                }
            } else {
                block();
            }
        }
    }

    // Method to generate route based on informed capacities and inform DAs
    private void generateRoutes() {
        // Dummy implementation for now
        System.out.println("MRA: Generating routes based on informed capacities...");
        for (Map.Entry<AID, Integer> entry : daCapacities.entrySet()) {
            AID daAgent = entry.getKey();
            int capacity = entry.getValue();

            // Here you can implement your logic to generate routes based on capacity
            String route = "Dummy route for " + daAgent.getName();

            // Inform DA about the route
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(daAgent);
            msg.setContent(route);
            send(msg);
            System.out.println("MRA: Informed " + daAgent.getName() + " about the route: " + route);
        }
    }
}



