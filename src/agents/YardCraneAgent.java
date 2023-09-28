package agents;

import GUI.StorageBayGUI;
import utils.*;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.List;
import java.util.Objects;

public class YardCraneAgent extends BaseAgent {
    AID managerAgent;

    protected void setup() {
        Object[] args = getArguments();

        PortCode = (String) args[0];
        PortCode = PortCode.trim();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();

        sd.setType(PortCode+"-YardCrane");
        sd.setName(PortCode+"-YardCrane");


        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        managerAgent = getAIDByName(PortCode+"-Manager");

        print("is ready");
        addBehaviour(new HandleMessages());
    }

    protected void takeDown() {
        print("is terminating");
        super.takeDown();
    }

    private class HandleMessages extends OneShotBehaviour {
        public void action(){
            message = myAgent.blockingReceive();
            if (message != null) {
                print("received a message from: "+message.getSender().getLocalName());
                addBehaviour(new HandleCraneRequests());
            } else {
                block();
            }
        }
    }
    private class HandleCraneRequests extends OneShotBehaviour{
        public void action() {

            if (message != null) {
                String msg = message.getContent();
                String[] contents = deserializeMessage(message);
                String mode = contents[0];

                ACLMessage reply = message.createReply();

                if(Objects.equals(mode, "DROPOFF")){
                    String dropoffContainers = contents[1];
                    List<Container> containersToDropOff = Container.StringToContainers(dropoffContainers);

                    print(Integer.toString(containersToDropOff.size()) + " to stack");
                    print(Integer.toString(StorageBay.GetNumberOfContainers()) + " containers in StorageBay");

                    print("stacking containers");
                    for (Container container:containersToDropOff) {
                        int moves = StorageBay.addContainer(container);
                        print("stacked container "+ container + " with "+ Integer.toString(moves) + " moves");
                        int[] pos = new int[2];
                        pos = StorageBay.GetContainersPosition(container);
                        StorageBayGUI.updateCounts(pos);
                    }

                }

                if(Objects.equals(mode, "PICKUP")){

                    String pickupContainers = contents[1];
                    List<Container> containersToPickUp = Container.StringToContainers(pickupContainers);

                    print(Integer.toString(containersToPickUp.size()) + " to unstack");
                    print(Integer.toString(StorageBay.GetNumberOfContainers()) + " containers in StorageBay");

                    reply.setContent(pickupContainers.toString());

                    print("unstacking containers");
                    for (Container container:containersToPickUp) {

                        int[] pos = new int[2];
                        pos = StorageBay.GetContainersPosition(container);

                        int moves = StorageBay.removeContainer(container);
                        StorageBayGUI.updateCounts(pos);
                        print("unstacked container "+ container + " with "+ Integer.toString(moves) + " moves");
                    }

                    print("sent to:" + message.getSender().getLocalName());
                    send(reply);
                }

                if(Objects.equals(mode, "DROPOFFandPICKUP")){

                    String dropoffContainers = contents[1];
                    String pickupContainers = contents[2];
                    List<Container> containersToPickUp= Container.StringToContainers(pickupContainers);
                    List<Container> containersToDropOff= Container.StringToContainers(dropoffContainers);

                    print(Integer.toString(containersToPickUp.size()) + " to unstack");
                    print(Integer.toString(containersToDropOff.size()) + " to stack");
                    print(Integer.toString(StorageBay.GetNumberOfContainers()) + " containers in StorageBay");
                    reply.setContent(pickupContainers.toString());

                    print("unstacking containers");
                    for (Container container:containersToPickUp) {
                        StorageBay.removeContainer(container);
                    }
                    print("stacking containers");
                    for (Container container:containersToDropOff) {
                        StorageBay.addContainer(container);
                    }
                    print("sent to: " + message.getSender().getLocalName());
                    send(reply);
                }

            }
            else {
                block();
            }
            print("containers in storage: "+StorageBay.GetNumberOfContainers());
            addBehaviour(new HandleMessages());
        }
    }

}

