package cos30018.assign;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;

import java.util.Random;

public class DA extends Agent {
    private int capacity;

    protected void setup() {
        // Generate a random capacity value
        Random rand = new Random();
        capacity = rand.nextInt(100) + 1; // Generates a random integer between 1 and 100 (inclusive)

        System.out.println(getLocalName() + ": Waiting for routing requests...");
        // Message template to listen only for messages matching the correct interaction protocol and performative
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        // Add the AchieveREResponder behaviour which implements the responder role in a FIPA_REQUEST interaction protocol
        addBehaviour(new AchieveREResponder(this, template) {
            protected ACLMessage prepareResponse(ACLMessage request) throws
            NotUnderstoodException, RefuseException {
                System.out.println(getLocalName() + ": REQUEST received from " +
                        request.getSender().getName() + ". Content is: " + request.getContent());

                // Inform MRA that the request has been taken and provide capacity information
                System.out.println(getLocalName() + ": Agreeing to take the request and informing capacity to MRA.");
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);

                // Include capacity information in the message content
                String capacityMsg = "Request taken. Capacity: " + capacity;
                agree.setContent(capacityMsg);

                // Informing capacity to MRA
                System.out.println(getLocalName() + ": Capacity information sent to MRA: " + capacityMsg);

                return agree;
            }

            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response)
                    throws FailureException {
                // Extract route from MRA's response
                String route = response.getContent();
                // Perform the action of providing optimized route information
                System.out.println(getLocalName() + ": Received route from MRA: " + route);
                // Inform MRA that the request has been completed
                System.out.println(getLocalName() + ": Informing MRA that the request has been completed.");
                ACLMessage inform = request.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                inform.setContent("Request completed"); // Informing MRA about request completion
                return inform;
            }
        });
    }
}
