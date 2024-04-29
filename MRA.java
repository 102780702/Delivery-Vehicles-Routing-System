package cos30018.assign;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;

public class MRA extends Agent implements Serializable {
	private static final long serialVersionUID = 1L;
    private Map<String, Integer> capacityMap; // Map to store the capacities of delivery agents
    
    protected void setup() {
        capacityMap = new HashMap<>();
        
        // Add behavior to handle incoming messages
        addBehaviour(new HandleRequests());
    }
    
    private class HandleRequests extends CyclicBehaviour implements Serializable {
    	private static final long serialVersionUID = 1L;
        public void action() {
            // Define the message template to match incoming requests
            MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            
            // Receive the request message
            ACLMessage request = receive(template);
            
            if (request != null) {
                // Step 1: MRA gets parcel information from the database
                String parcelInfo = getParcelInfoFromDatabase();
                
                // Step 2: MRA sends a request to DA to request to send parcel
                ACLMessage sendParcelRequest = new ACLMessage(ACLMessage.REQUEST);
                sendParcelRequest.setContent(parcelInfo);
                sendParcelRequest.addReceiver(request.getSender());
                send(sendParcelRequest);
                
                // Step 3: The DA requests route from MRA (implicit in the request from DA)
                
                // Step 4: The DA informs its capacity to MRA
                capacityMap.put(request.getSender().getLocalName(), Integer.parseInt(request.getContent()));
                
                // Step 5: Perform Genetic Algorithm to create the route
                String optimizedRoute = performGeneticAlgorithm();
                
                // Step 5: The MRA informs route to DA
                ACLMessage informRoute = new ACLMessage(ACLMessage.INFORM);
                informRoute.setContent(optimizedRoute);
                informRoute.addReceiver(request.getSender());
                send(informRoute);
                
                // Wait for response from DA
                ACLMessage response = blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                
                if (response != null) {
                    // Step 6: The DA informs MRA that it has completed the task
                    if (response.getContent().equals("Task Completed")) {
                        System.out.println("Delivery agent " + response.getSender().getLocalName() + " has completed the task.");
                    } else {
                        System.out.println("Delivery agent " + response.getSender().getLocalName() + " failed to complete the task.");
                    }
                } else {
                    System.out.println("No response from delivery agent.");
                }
            }
            else {
                // If no message received, block the behavior
                block();
            }
        }
    }
    
    private String getParcelInfoFromDatabase() {
        // Implement this method to retrieve parcel information from the database
        // For demonstration purposes, a placeholder string is returned
        return "Parcel information from the database";
    }
    
    private String performGeneticAlgorithm() {
        return "route";
    }
}

