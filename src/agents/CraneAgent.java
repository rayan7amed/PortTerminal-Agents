package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Objects;

public class CraneAgent extends BaseAgent {
    String PortCode;
    String Type;

    AID managerAgent;
    AID yardCraneAgent;
    AID Transporter;
    ACLMessage message;
    boolean isCraneAvailable;

    Agent ThisAgent;

    protected void setup() {
        //ARGS:
        //Type,PortCode
        //"Water" or "Land"
        Object[] args = getArguments();
        Type = (String) args[0];
        PortCode = (String) args[1];
        PortCode = PortCode.trim();
        Type = Type.trim();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        if(Objects.equals(Type, "Water")){
            sd.setType(PortCode+"-WaterCrane");
            sd.setName(PortCode+"-WaterCrane");
        }
        else{
            sd.setType(PortCode+"-LandCrane");
            sd.setName(PortCode+"-LandCrane");
        }

        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }


        managerAgent = getAIDByName(PortCode+"-Manager");
        yardCraneAgent = getAIDByName(PortCode+"-YardCrane");

        if(managerAgent != null && yardCraneAgent != null)
            print("got all agents");
        else
            takeDown();

        print("is ready");
        isCraneAvailable = true;

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

                if (Objects.equals(managerAgent,message.getSender())) {
                    addBehaviour(new HandleRequests());
                    return;
                }
            } else {
                block();
            }
            addBehaviour(new HandleMessages());
        }
    }

    private class HandleRequests extends OneShotBehaviour{
        public void action() {

            if (message != null) {

                String[] contents = deserializeMessage(message);
                Transporter = getAIDByName(contents[0].trim());
                String mode = contents[1];
                if(Transporter == null)
                    return;

                ACLMessage toYardCraneAgent = new ACLMessage(ACLMessage.INFORM);
                toYardCraneAgent.addReceiver(yardCraneAgent);

//                ACLMessage reply = message.createReply();
//                reply.setPerformative(ACLMessage.INFORM);
//                reply.setContent("crane-assigned");
//                send(reply);

                if(Objects.equals(mode, "DROPOFF")){
                    String dropoffContainers = contents[2];
                    toYardCraneAgent.setContent("DROPOFF!!!"+dropoffContainers+"!!!"+Transporter.toString());
                    print("unloading containers");
                    addBehaviour(new HandleMessages());
                    print("unloaded containers");

                    print("sent departure clearance to: "+Transporter.getLocalName());
                    ACLMessage departureClearance = new ACLMessage(ACLMessage.INFORM);
                    departureClearance.addReceiver(Transporter);
                    send(departureClearance);

                }

                if(Objects.equals(mode, "PICKUP")){
                    String pickupContainers = contents[2];
                    toYardCraneAgent.setContent("PICKUP!!!"+pickupContainers+"!!!"+Transporter.toString());

                    addBehaviour(new WaitForAndLoadContainers());
                }

                if(Objects.equals(mode, "DROPOFFandPICKUP")){
                    String dropoffContainers = contents[2];
                    String pickupContainers = contents[3];
                    toYardCraneAgent.setContent("DROPOFFandPICKUP!!!"+dropoffContainers.toString()+"!!!"+pickupContainers.toString()+"!!!"+Transporter);
                    print("unloaded and waiting for pickup containers "+yardCraneAgent.getLocalName());

                    addBehaviour(new WaitForAndLoadContainers());
                }
                print("sent to: "+yardCraneAgent.getLocalName());
                send(toYardCraneAgent);

            }
            else {
                block();
            }

        }
    }

    private class WaitForAndLoadContainers extends OneShotBehaviour{
        public void action(){
            print("waiting for pickup containers from "+yardCraneAgent.getLocalName());
            MessageTemplate mt = MessageTemplate.MatchSender(yardCraneAgent);
            ACLMessage msg = myAgent.blockingReceive(mt);
            ACLMessage toTransporter = new ACLMessage(ACLMessage.INFORM);
            toTransporter.addReceiver(Transporter);

            if (msg != null) {

                String containers = msg.getContent();

                print("loading containers");

                toTransporter.setContent("LOADED!!!"+containers);
                send(toTransporter);

                print("sent departure clearance to: "+Transporter.getLocalName());
                ACLMessage departureClearance = new ACLMessage(ACLMessage.INFORM);
                departureClearance.addReceiver(Transporter);
                send(departureClearance);

                addBehaviour(new HandleMessages());
            } else {
                block();
            }
            addBehaviour(new HandleMessages());
            }
        }

    }





