import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;

public class DA extends Agent {
    private boolean isAvailable = true;  // Set based on the agent's status or conditions
    private boolean routeRequestReceived = false;
    private String capacity;
    
    protected void setup() {
    	
    	//Get capacity argument
    	Object[] args = getArguments();
        if (args != null && args.length > 0) {
            capacity = (String) args[0];  
            System.out.println(getLocalName() + ": Capacity set to " + capacity);
        } else {
            System.err.println("Error: No capacity provided. Defaulting to 100.");
            capacity = "100";  // Default capacity if none provided
        }
        
        registerAgent();
        setupRequestResponder();
        listenForRouteRequest();
    }

    // Register agent to DF
    private void registerAgent() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("DeliveryAgent");
        sd.setName(getLocalName() + "-Delivery");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Respond to initial request from MRA
    // AGREE and INFORM capacity
    private void setupRequestResponder() {
        MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        addBehaviour(new AchieveREResponder(this, template) {
            protected ACLMessage handleRequest(ACLMessage request) {
                ACLMessage reply = request.createReply();
                if (isAvailable) {
                    reply.setPerformative(ACLMessage.AGREE);
                    System.out.println(getLocalName() + ": Available for routing tasks.");
                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    System.out.println(getLocalName() + ": Not available for routing tasks.");
                }
                return reply;
            }

            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
                if (response.getPerformative() == ACLMessage.AGREE) {
                    ACLMessage inform = request.createReply();
                    inform.setPerformative(ACLMessage.INFORM);
                    inform.setContent(capacity);
                    inform.addUserDefinedParameter("customConversationId", "capacity-inform");
                    System.out.println(inform.getConversationId());
                    return inform;
                } else {
                    return null;  // No further action required if refused
                }
            }
        });
    }

    // Listen for MRA REQUEST to send REQUEST for route
    private void listenForRouteRequest() {
        MessageTemplate mt = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST)
        );
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive(mt);
                if (msg != null && msg.getContent().equals("Please request your route") && !routeRequestReceived) {
                    System.out.println(getLocalName() + ": Received route request from MRA.");
                    // Request the route details from MRA
                    requestRouteFromMRA();
                    routeRequestReceived = true; // Set the flag to indicate that a route request has been processed
                } else {
                    block();
                }
            }
        });
    }

    // REQUEST route
    private void requestRouteFromMRA() {
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.addReceiver(new AID("MRA", AID.ISLOCALNAME));
        request.setContent("Requesting route details");
        request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);  
        request.setConversationId("route-request");
        send(request);
        System.out.println(getLocalName() + ": Requested route details from MRA.");
        listenForRouteDetails();
    }

    // Listen for route details
    // Trigger perform task
    private void listenForRouteDetails() {
        MessageTemplate mt = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchConversationId("route-details")  // Ensuring we're matching the right conversation ID
        );
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    System.out.println(getLocalName() + ": Received route details from MRA.");
                    performTask(msg.getContent());
                } else {
                    block();
                }
            }
        });
    }

    
    //Perform task
    private void performTask(String routeDetails) {
        System.out.println(getLocalName() + ": Performing task based on route: " + routeDetails);
        
        //TASK
        
        // Assuming the task is completed successfully
        ACLMessage completionMsg = new ACLMessage(ACLMessage.INFORM);
        completionMsg.addReceiver(new AID("MRA", AID.ISLOCALNAME));
        completionMsg.setContent("Task completed successfully.");
        completionMsg.setConversationId("task-completed");
        send(completionMsg);
        System.out.println(getLocalName() + ": Sent task completion message to MRA.");
        routeRequestReceived = false; // Reset the flag after performing the task
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
