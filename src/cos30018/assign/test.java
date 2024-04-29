package cos30018.assign;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class test extends Agent {
    
    protected void setup() {
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.CFP) {
                        System.out.println("Received CFP from " + msg.getSender().getName());
                        System.out.println("Responding with 'hi'");
                        
                        // Create response message
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("hi");
                        
                        // Send response
                        send(reply);
                    }
                }
                else {
                    block();
                }
            }
        });
    }
}

