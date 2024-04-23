import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.Behaviour;


public class SenderAgent extends Agent {

    protected void setup() {
        System.out.println("SenderAgent " + getAID().getName() + " is ready.");

        addBehaviour(new SendMessagesBehaviour());
    }

    private class SendMessagesBehaviour extends Behaviour {
        private int counter = 0;

        public void action() {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setContent("Hello ReceiverAgent!");

            AID receiver = new AID("ReceiverAgent", AID.ISLOCALNAME);
            msg.addReceiver(receiver);

            send(msg);

            System.out.println("Sent message " + (++counter));

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public boolean done() {
            return false;
        }
    }
}
