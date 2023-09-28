package simulation;

import GUI.StorageBayGUI;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import jade.core.Runtime;
import jade.wrapper.ContainerController;

import utils.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.time.LocalDateTime;
import java.util.List;



public class Simulation {
    public static void main(String[] args) {
        StorageBay storageBay = new StorageBay();

        //Randomly generating containers
        RandomContainerGenerator rand = new RandomContainerGenerator();
        List<Container> GeneratedContainers = rand.GenerateContainers(394);
        //Adding randomly generated containers to storage bay
        for(Container con : GeneratedContainers)
            StorageBay.addContainer(con);

        List<Container> GeneratedContainersGdans = rand.GenerateContainersByDest(6,"GDANSK");
        for(Container con : GeneratedContainersGdans)
            StorageBay.addContainer(con);

        StorageBayGUI storageBayGUI = new StorageBayGUI();
        storageBayGUI.setVisible(true);


        System.out.println("Number of containers in storage bay "+StorageBay.GetNumberOfContainers());
//        wait(10);
        String hostname = null;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            hostname = localHost.getHostName();

        } catch (UnknownHostException e){
            e.printStackTrace();
        }

        // Start the JADE runtime
        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl();
//        profile.setParameter(Profile.GUI, "true"); // Start the GUI
        profile.setParameter(Profile.MAIN_HOST, hostname); // Host for the main container
        profile.setParameter(Profile.MAIN_PORT, "1099"); // Port for the main container

        // Create the main container
        ContainerController mainContainer = runtime.createMainContainer(profile);

        // Create the agent containers
//        AgentContainer container1 = createContainer(runtime, "Container1");
//        AgentContainer container2 = createContainer(runtime, "Container2");
//        AgentContainer container3 = createContainer(runtime, "Container3");
//        AgentContainer container4 = createContainer(runtime, "Container4");
//        AgentContainer container5 = createContainer(runtime, "Container5");

        // Create and start your agents
        try {
            Object[] ManagerArgs = new Object[1];
            ManagerArgs[0] = "SIM";
            AgentController ManagerAgent = mainContainer.createNewAgent("ManagerAgent", "agents.ManagerAgent", ManagerArgs);
            ManagerAgent.start();

            Object[] YardCraneArgs = new Object[1];
            YardCraneArgs[0] = "SIM";
            AgentController YardCraneAgent = mainContainer.createNewAgent("YardCraneAgent", "agents.YardCraneAgent", YardCraneArgs);
            YardCraneAgent.start();

            Object[] LandCraneArgs = new Object[2];
            LandCraneArgs[0] = "Land";
            LandCraneArgs[1] = "SIM";
            AgentController LandCraneAgent = mainContainer.createNewAgent("LandCraneAgent", "agents.CraneAgent", LandCraneArgs);
            LandCraneAgent.start();

            Object[] WaterCraneArgs = new Object[2];
            WaterCraneArgs[0] = "Water";
            WaterCraneArgs[1] = "SIM";
            AgentController WaterCraneAgent = mainContainer.createNewAgent("WaterCraneAgent", "agents.CraneAgent", WaterCraneArgs);
            WaterCraneAgent.start();

            wait(4);

            System.out.println("Port Terminal is ready!!!");

            ////STATE, PORTCODE, DATE, DEST, MANIFEST////
            //MANIFEST form: [c1,c2,c3]
            Object[] ShipArgs = new Object[6];
            ShipArgs[0] = "PICKUP";
            ShipArgs[1] = "SIM";
            ShipArgs[2] = LocalDateTime.now().toString();
            ShipArgs[3] = "GDANSK";
            ShipArgs[4] = RandomContainerGenerator.GenerateContainers(10).toString();
            ShipArgs[5] = "ShipAgent1";
            AgentController ShipAgent1 = mainContainer.createNewAgent("ShipAgent1", "agents.ShipAgent", ShipArgs);
            ShipAgent1.start();

//            wait(80);
//            Object[] TrainArgs = new Object[6];
//            TrainArgs[0] = "DROPOFF";
//            TrainArgs[1] = "SIM";
//            TrainArgs[2] = LocalDateTime.now().toString();
//            TrainArgs[3] = "JEDDAH";
//            TrainArgs[4] = RandomContainerGenerator.GenerateContainers(10).toString();
//            TrainArgs[5] = "TrainAgent1";
//            AgentController TrainAgent1 = mainContainer.createNewAgent("TrainAgent1", "agents.TrainAgent", TrainArgs);
//            TrainAgent1.start();

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

    }

    private static AgentContainer createContainer(Runtime runtime, String containerName) {
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.CONTAINER_NAME, containerName);

        return runtime.createAgentContainer(profile);
    }

    public static void wait(int sec)
    {
        try
        {
            Thread.sleep(sec * 1000L);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }
}
