package Delivery.system;


import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.util.leap.Serializable;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;

public class DeliveryAgent extends Agent implements Serializable{

    protected void setup() {
        System.out.println("Agent " + getLocalName() + " waiting for requests...");
        
        // Register as "Delivery Agent"
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("DeliveryAgent");
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        

        // Define the message template to match incoming requests
        MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        
        // Add the AchieveREResponder behavior which implements the responder role
        addBehaviour(new AchieveREResponder(this, template) {
        	protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
                System.out.println("Agent " + getLocalName() + ": REQUEST received from " + request.getSender().getName() + ". Action is " + request.getContent());
                
                // Send another request to MRA for route
                ACLMessage routeRequest = new ACLMessage(ACLMessage.REQUEST);
                routeRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                routeRequest.setContent("Request for route");
                routeRequest.addReceiver(new AID("MRA", AID.ISLOCALNAME));
                send(routeRequest);
                
                // Inform capacity to MRA
                informCapacity(request.getContent());
                
                // Agree to the request
                System.out.println("Agent " + getLocalName() + ": Agree");
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                return agree;
            }

            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
            	

                if (response.getPerformative() == ACLMessage.INFORM) {
                    System.out.println("Agent " + getLocalName() + ": Information received from MRA: " + response.getContent());
                    // Send route to UI
                    sendRouteToUI(response.getContent());
                    // Inform MRA of task completion
                    informTaskCompletion(request.getSender());
                    // Return INFORM message
                    ACLMessage inform = request.createReply();
                    inform.setPerformative(ACLMessage.INFORM);
                    inform.setContent("Task Completed");
                    return inform;
                } else {
                    throw new FailureException("unexpected-error");
                }
            }
        });
    }
    

    private void informCapacity(String capacity) {
        ACLMessage informCapacity = new ACLMessage(ACLMessage.INFORM);
        informCapacity.setContent(capacity);
        informCapacity.addReceiver(new AID("MRA", AID.ISLOCALNAME));
        send(informCapacity);
    }

    private void sendRouteToUI(String route) {
        // Send route information to UI
        // Implement this method according to your UI communication method
    }

    private void informTaskCompletion(AID receiver) {
        ACLMessage informCompletion = new ACLMessage(ACLMessage.INFORM);
        informCompletion.setContent("Task Completed");
        informCompletion.addReceiver(receiver);
        send(informCompletion);
    }
}
