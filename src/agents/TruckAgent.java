package agents;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.time.LocalDateTime;
import java.util.*;

import utils.*;



public class TruckAgent extends BaseAgent {

    String PortCode;
    String NextDest;
    LocalDateTime ArrivalDate;
    private List<Container> container;
    private AID managerAgent;
    private TransportState state;

    protected void setup() {
        //ARGS:
        //!!!!STATE, PORTCODE, DATE, DEST, MANIFEST!!!!!!
        //MANIFEST form: [c1,c2,c3]

        Object[] args = getArguments();
        state = TransportState.valueOf((String)args[0]);
        PortCode = (String) args[1];
        ArrivalDate = LocalDateTime.parse((String)args[2]);

        container = new ArrayList<>();
        container = Container.StringToContainers((String) args[3]);

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
        System.out.println("Truck Agent " + getAID().getName() + " is ready.");

        addBehaviour(new GetPortManagerAgent());
        addBehaviour(new RequestAppointment());

    }

    protected void takeDown() {
        System.out.println("Truck Agent " + getAID().getName() + " is terminating.");
        this.doDelete();
    }

    private class GetPortManagerAgent extends OneShotBehaviour {
        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(PortCode+"-Manager");
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                managerAgent = (result[0].getName());
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }

        }
    }

    private class RequestAppointment extends OneShotBehaviour {
        public void action() {
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.addReceiver(managerAgent);
            if(state == TransportState.DROPOFF){
                message.setContent("SCHEDULE!!!Truck!!!DROPOFF!!!"+ArrivalDate.toString()+
                        "!!!"+ NextDest +"!!!"+ container.toString());
            }

            if(state == TransportState.PICKUP){
                message.setContent("SCHEDULE!!!Truck!!!PICKUP!!!"+ArrivalDate.toString()+
                        "!!!"+ NextDest +"!!!"+ container.toString());

            }

            if(state == TransportState.DROPOFFandPICKUP){
                message.setContent("SCHEDULE!!!Truck!!!DROPOFFandPICKUP!!!"+ArrivalDate.toString()+
                        "!!!"+ NextDest +"!!!"+ container.toString());

            }

            send(message);
            System.out.println("Truck Agent sent Truck arrival appointment.");

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

            System.out.println("Appointment confirmed.");

            addBehaviour(new SendArrival());

        }
    }

    private class SendArrival extends OneShotBehaviour {
        public void action() {
            System.out.println("Truck Agent sent Truck arrival notification.");

            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.addReceiver(managerAgent);
            if(state == TransportState.DROPOFF){
                message.setContent("ARRIVAL!!!Truck!!!DROPOFF!!!"+ArrivalDate.toString()+
                        "!!!"+ NextDest +"!!!"+ container.toString());
                send(message);

            }

            if(state == TransportState.PICKUP){
                message.setContent("ARRIVAL!!!Truck!!!PICKUP!!!"+ArrivalDate.toString()+
                        "!!!"+ NextDest);
                send(message);

                ACLMessage response = myAgent.blockingReceive();
                if (response != null) {
                    String strMsg = response.getContent();
                    String[] contents = deserializeMessage(response);
                    container = Container.StringToContainers(contents[1]);

                } else {
                    block();
                }
            }

            if(state == TransportState.DROPOFFandPICKUP){
                message.setContent("ARRIVAL!!!Truck!!!DROPOFFandPICKUP!!!"+ArrivalDate.toString()+
                        "!!!"+ NextDest +"!!!"+ container.toString());
                send(message);

                ACLMessage response = myAgent.blockingReceive();
                if (response != null) {
                    String strMsg = response.getContent();
                    String[] contents = deserializeMessage(response);
                    container = Container.StringToContainers(contents[1]);

                } else {
                    block();
                }

            }

            System.out.println("Truck Agent leaving.");
            takeDown();
        }
    }
}
