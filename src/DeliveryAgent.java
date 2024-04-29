import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.FailureException;

public class DeliveryAgent extends Agent {

    protected void setup() {
        System.out.println(getLocalName() + ": waiting for requests...");
        // Message template to listen only for messages matching the correct interaction protocol and performative
        MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        // Add the AchieveREResponder behaviour which implements the responder role
        addBehaviour(new AchieveREResponder(this, null) {
            
            protected ACLMessage prepareResponse(ACLMessage request) throws
                    NotUnderstoodException, RefuseException {
                System.out.println("Agent " + getLocalName() + ": REQUEST received from " + request.getSender().getName() + ". Action is " + request.getContent());
                // Agree to perform the action
                System.out.println("Agent " + getLocalName() + ": Agree");
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                return agree;
            
                // Inform about capacity
                System.out.println("Agent " + getLocalName() + ": Informing about capacity");
                ACLMessage capacityInform = request.createReply();
                capacityInform.setPerformative(ACLMessage.INFORM);
                capacityInform.setContent("Capacity: 100"); // You can set the actual capacity here
                myAgent.send(capacityInform);

                // Request route
                System.out.println("Agent " + getLocalName() + ": Requesting route");
                ACLMessage routeRequest = new ACLMessage(ACLMessage.REQUEST);
                routeRequest.addReceiver(request.getSender());
                routeRequest.setContent("Requesting route");
                myAgent.send(routeRequest);

            

            // If the agent agreed to the request received, then it has to perform the associated action and return the result of the action
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
                // Perform the action (dummy method)
                if (performAction()) {
                    System.out.println("Agent "+getLocalName()+": Agree");
                    ACLMessage inform = request.createReply();
                    inform.setPerformative(ACLMessage.INFORM);
                    inform.setContent(String.valueOf(Math.random()));
                    return inform;
                } else {
                    // Action failed
                    System.out.println("Agent "+getLocalName()+": Refuse");
					throw new FailureException("check‐failed");
                }
            }
        }
    });

    // Dummy method for performing action
    private boolean performAction() {
        return true; // Return true if action succeeds, false otherwise
    }
}
}