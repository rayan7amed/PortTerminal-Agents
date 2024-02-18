package agents;


import GUI.ManagerGUI;
import utils.*;

import jade.core.*;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.*;

public class ManagerAgent extends BaseAgent{
    AID WaterCrane, LandCrane, YardCrane;

    List<Appointment> WaterCraneSchedule = new ArrayList<>();
    List<Appointment> LandCraneSchedule = new ArrayList<>();

    private static boolean guiClosed = false;
    static final ManagerGUI gui = new ManagerGUI();

    @Override
    protected void setup() {
        //ARGS:
        //PORTCODE
        Object[] args = getArguments();
        PortCode = (String) args[0];

        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(PortCode+"-Manager");
        sd.setName(PortCode+"-Manager");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }


//        gui.setVisible(true);
//        if (!guiClosed) {
//            synchronized (gui) {
//                try {
//                    gui.wait();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        print("is ready");

        addBehaviour(new GetCranes());
        addBehaviour(new HandleMessages());
    }

    protected void takeDown() {
        print("is terminating");
        super.takeDown();
    }


    private class GetCranes extends OneShotBehaviour {
        public void action(){
            DFAgentDescription template1 = new DFAgentDescription();
            DFAgentDescription template2 = new DFAgentDescription();
            DFAgentDescription template3 = new DFAgentDescription();

            ServiceDescription sd1 = new ServiceDescription();
            sd1.setType(PortCode+"-LandCrane");
            template1.addServices(sd1);

            ServiceDescription sd2 = new ServiceDescription();
            sd2.setType(PortCode+"-WaterCrane");
            template2.addServices(sd2);

            ServiceDescription sd3 = new ServiceDescription();
            sd3.setType(PortCode+"-YardCrane");
            template3.addServices(sd3);
            int counter = 0;
            try {
                DFAgentDescription[] result = new DFAgentDescription[0];
                while (counter < 3)
                {
                    result = DFService.search(myAgent, template1);
                    if(result.length == 1) {
                        LandCrane = result[0].getName();
                        counter++;
                    }

                    result = DFService.search(myAgent, template2);
                    if(result.length == 1){
                        WaterCrane = result[0].getName();
                        counter++;
                    }

                    result = DFService.search(myAgent, template3);
                    if(result.length == 1){
                        YardCrane = result[0].getName();
                        counter++;
                    }

                    if(counter != 3)
                        counter = 0;

                }
                print("got all agents");
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
    }

    private class HandleMessages extends OneShotBehaviour {
        public void  action(){

            message = myAgent.blockingReceive();

            print("received a message from: "+message.getSender().getLocalName());
            if (message != null) {

                String[] contents = deserializeMessage(message);
                if (Objects.equals(contents[0], "SCHEDULE")) {
                    addBehaviour(new AddAppointment());
                }

                if (Objects.equals(contents[0], "ARRIVAL")) {

                    if (contents[1].startsWith("Truck")) {
                        addBehaviour(new HandleTruckArrival());

                    }
                    if (contents[1].startsWith("Ship")) {
                        addBehaviour(new HandleShipArrival());

                    }
                    if (contents[1].startsWith("Train")) {
                        addBehaviour(new HandleTrainArrival());
                    }
                }

            } else {
                block();
            }
        }


    }

    private class AddAppointment extends OneShotBehaviour {
        boolean Available = true;
        public void  action(){

            if(message.getContent().startsWith("SCHEDULE"))
            {
                ACLMessage reply = message.createReply();
                AID sender = message.getSender();
                /* TYPE!!!MODE!!! */
                String[] contents = deserializeMessage(message);

                String type = contents[1];
                LocalDateTime arrivalDateTime = LocalDateTime.parse(contents[3]);
                int numberOfContainers = 0;
                if(Objects.equals(contents[2],"DROPOFF"))
                     numberOfContainers += Container.StringToContainers(contents[5]).size();

                if(Objects.equals(contents[2],"PICKUP"))
                    numberOfContainers += StorageBay.GetContainersByDest(contents[4]).size();

                if(Objects.equals(contents[2],"DROPOFFandPICKUP"))
                {
                    numberOfContainers += StorageBay.GetContainersByDest(contents[4]).size();
                    numberOfContainers += Container.StringToContainers(contents[5]).size();;
                }
                int minutes = numberOfContainers * 15;
                if(Objects.equals(contents[1], "Ship"))
                {

                    for (Appointment apt : WaterCraneSchedule)
                        if (apt.Arrival.isBefore(arrivalDateTime.plusMinutes(minutes)) || apt.Departure.isAfter(arrivalDateTime))
                            Available = false;

                    if(Available) {
                        WaterCraneSchedule.add(new Appointment(arrivalDateTime, minutes, contents[1], sender, contents[5]));
                        reply.setContent("CONFIRMATION!!!ADDED TO SCHEDULE");
                    }
                    else {
                        LocalDateTime closestDate = null;
                        for (Appointment apt : WaterCraneSchedule) {
                            if (apt.Arrival.isBefore(arrivalDateTime.plusMinutes(minutes)) && apt.Departure.isAfter(arrivalDateTime)) {
                                if (closestDate == null || apt.Departure.isBefore(closestDate)) {
                                    closestDate = apt.Departure.plusMinutes(15);
                                }
                            }
                        }
                        if (closestDate != null) {
                            reply.setContent("REJECTION!!!"+closestDate.toString());
                            WaterCraneSchedule.add(new Appointment(closestDate, minutes, contents[1], sender, contents[5]));
                        }

                    }
                }
                else {
                    for (Appointment apt : LandCraneSchedule)
                        if (apt.Arrival.isBefore(arrivalDateTime.plusMinutes(minutes)) || apt.Departure.isAfter(arrivalDateTime))
                            Available = false;

                    if(Available){
                        LandCraneSchedule.add(new Appointment(arrivalDateTime, minutes, contents[1], sender, contents[5]));
                        reply.setContent("CONFIRMATION!!!ADDED TO SCHEDULE");
                    }
                    else {
                        LocalDateTime closestDate = null;
                        for (Appointment apt : LandCraneSchedule) {
                            if (apt.Arrival.isBefore(arrivalDateTime.plusMinutes(minutes)) && apt.Departure.isAfter(arrivalDateTime)) {
                                //Overlapping period found, update the closest available date
                                if (closestDate == null || apt.Departure.isBefore(closestDate) ) {
                                    closestDate = apt.Departure.plusMinutes(15);
                                }
                            }
                        }
                        if (closestDate != null) {
                            reply.setContent("REJECTION!!!"+closestDate.toString());
                            LandCraneSchedule.add(new Appointment(closestDate, minutes, contents[1], sender, contents[6]));
                        }
                    }
                }
                print("added appointment to schedule");
                print("sending appointment confirmation to: " + message.getSender().getLocalName());
                send(reply);
            }
            else {
                print("ERROR with schedule");
            }
            addBehaviour(new HandleMessages());
        }
    }
    private class HandleShipArrival extends OneShotBehaviour {

        public void  action(){
            AID sender = message.getSender();
            ACLMessage reply = message.createReply();

            String[] contents = deserializeMessage(message);
            String type = contents[1];
            String mode = contents[2];
            String date = contents[3];
            String dest = contents[4];

            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.addReceiver(WaterCrane);


            if(Objects.equals(mode, "DROPOFF")){
                String containers = contents[5];
                message.setContent(sender.getLocalName()+"!!!DROPOFF!!!"+containers);
            }
            if(Objects.equals(mode, "PICKUP")){
                List<Container> containers = new ArrayList<>();

                if(StorageBay.terminalLayout.length > 0)
                    containers = StorageBay.GetContainersByDest(dest);
                message.setContent(sender.getLocalName()+"!!!PICKUP!!!"+containers.toString());
            }
            if(Objects.equals(mode, "DROPOFFandPICKUP")){
                String dropcontainers = contents[5];
                List<Container> pickcontainers = StorageBay.GetContainersByDest(dest);
                message.setContent(sender.getLocalName()+"!!!DROPOFFandPICKUP!!!"+dropcontainers+"!!!"+pickcontainers.toString());
            }
            send(message);
            addBehaviour(new HandleMessages());
        }

    }
    private class HandleTruckArrival extends OneShotBehaviour {

        public void  action(){
            String msg = message.getContent();
            AID sender = message.getSender();
            ACLMessage reply = message.createReply();


            String[] contents = deserializeMessage(message);
            String type = contents[1];
            String mode = contents[2];
            String date = contents[3];
            String dest = contents[4];
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.addReceiver(LandCrane);
            if(Objects.equals(contents[2], "DROPOFF")){
                String containers = contents[5];
                message.setContent(sender.getLocalName()+"!!!DROPOFF!!!"+containers);
            }
            if(Objects.equals(contents[2], "PICKUP")){
                List<Container> containers = StorageBay.GetContainersByDest(dest);
                //GET containers
                message.setContent(sender.getLocalName()+"!!!PICKUP!!!"+containers.toString());
            }
            if(Objects.equals(contents[2], "DROPOFFandPICKUP")){
                String dropcontainers = contents[5];
                List<Container> pickcontainers = StorageBay.GetContainersByDest(dest);
                //GET Cons
                message.setContent(sender.getLocalName()+"!!!DROPOFFandPICKUP!!!"+dropcontainers.toString()+pickcontainers.toString());
            }
            send(message);
            addBehaviour(new HandleMessages());
        }
    }
    private class HandleTrainArrival extends OneShotBehaviour {

        public void  action(){
            AID sender = message.getSender();
            ACLMessage reply = message.createReply();

            String[] contents = deserializeMessage(message);
            String type = contents[1];
            String mode = contents[2];
            String date = contents[3];
            String dest = contents[4];
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.addReceiver(LandCrane);
            if(Objects.equals(contents[2], "DROPOFF")){
                String containers = contents[5];
                message.setContent(sender.getLocalName()+"!!!DROPOFF!!!"+containers);
            }
            if(Objects.equals(contents[2], "PICKUP")){
                List<Container> containers = StorageBay.GetContainersByDest(dest);
                message.setContent(sender.getLocalName()+"!!!PICKUP!!!"+containers.toString());
            }
            if(Objects.equals(contents[2], "DROPOFFandPICKUP")){
                String dropcontainers = contents[5];
                List<Container> pickcontainers = StorageBay.GetContainersByDest(dest);
                message.setContent(sender.getLocalName()+"!!!DROPOFFandPICKUP!!!"+dropcontainers.toString()+pickcontainers.toString());
            }
            send(message);
            addBehaviour(new HandleMessages());
        }
    }


    public static void closeGui() {
        guiClosed = true;
        synchronized (gui) {
            gui.notify();
        }
    }
}