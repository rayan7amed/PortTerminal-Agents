package agents;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.time.LocalDateTime;
import java.util.*;

import jade.lang.acl.MessageTemplate;
import utils.*;



public class ShipAgent extends BaseAgent {

    String PortCode;
    String NextDest;

    LocalDateTime ArrivalDate;
    private List<Container> Manifest;
    private AID managerAgent;
    private TransportState state;

    ACLMessage message;
    protected void setup() {
        //ARGS:
        //STATE, PORTCODE, DATE, DEST, MANIFEST//
        //MANIFEST form: [c1,c2,c3]

        Object[] args = getArguments();
        state = TransportState.valueOf((String)args[0]);
        PortCode = (String) args[1];
        ArrivalDate = LocalDateTime.parse((String)args[2]);
        NextDest = (String) args[3];

        Manifest = new ArrayList<>();
        Manifest = Container.StringToContainers((String) args[4]);

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();

        sd.setType(this.getLocalName());
        sd.setName(this.getLocalName());

        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        managerAgent = getAIDByName(PortCode+"-Manager");
        print("is ready");

        addBehaviour(new RequestAppointment());

    }

    protected void takeDown() {
        print("is terminating");
        super.takeDown();
    }

    private class RequestAppointment extends OneShotBehaviour {
        public void action() {
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.addReceiver(managerAgent);

            if(state == TransportState.DROPOFF){
                message.setContent("SCHEDULE!!!Ship!!!DROPOFF!!!"+ArrivalDate.toString()+
                        "!!!"+NextDest+"!!!"+ Manifest.toString());
            }

            if(state == TransportState.PICKUP){
                message.setContent("SCHEDULE!!!Ship!!!PICKUP!!!"+ArrivalDate.toString()+
                        "!!!"+NextDest+"!!!"+ Manifest.toString());

            }

            if(state == TransportState.DROPOFFandPICKUP){
                message.setContent("SCHEDULE!!!Ship!!!DROPOFFandPICKUP!!!"+ArrivalDate.toString()+
                        "!!!"+NextDest+"!!!"+ Manifest.toString());

            }

            print("sent ship arrival appointment");
            send(message);

            ACLMessage response = myAgent.blockingReceive();
            if(response != null){
                if(response.getContent().startsWith("REJECTION")) {
                    String[] contents = deserializeMessage(response);
                    ArrivalDate = LocalDateTime.parse(contents[1]);
                }
            }
            else{
                block();
            }

            print("Appointment confirmed");
            addBehaviour(new SendArrival());

        }
    }

    private class SendArrival extends OneShotBehaviour {
        public void action() {
            print("sent ship arrival notification");

            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.addReceiver(managerAgent);

            /*"ARRIVAL!!!Ship!!!MODE!!!DATE!!!Dest!!!Containers"*/
            if(state == TransportState.DROPOFF){
                message.setContent("ARRIVAL!!!Ship!!!DROPOFF!!!"+ArrivalDate.toString()+
                        "!!!"+NextDest+"!!!"+ Manifest.toString());
                send(message);

            }

            if(state == TransportState.PICKUP){
                message.setContent("ARRIVAL!!!Ship!!!PICKUP!!!"+ArrivalDate.toString()+
                        "!!!"+NextDest);
                send(message);

                print("is waiting to load containers");

                ACLMessage response = myAgent.blockingReceive();
                if (response != null) {
                    String[] contents = deserializeMessage(response);
                    Manifest = Container.StringToContainers(contents[1]);

                } else {
                    block();
                }
            }

            if(state == TransportState.DROPOFFandPICKUP){
                message.setContent("ARRIVAL!!!Ship!!!DROPOFFandPICKUP!!!"+ArrivalDate.toString()+
                        "!!!"+NextDest+"!!!"+ Manifest.toString());
                send(message);

                ACLMessage response = myAgent.blockingReceive();
                if (response != null) {
                    String[] contents = deserializeMessage(response);
                    Manifest = Container.StringToContainers(contents[1]);

                } else {
                    block();
                }

            }

            print("is leaving");
            takeDown();
        }
    }
}
