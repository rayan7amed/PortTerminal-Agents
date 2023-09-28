package agents;

import jade.content.ContentManager;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public abstract class BaseAgent extends Agent {
    String PortCode;
    ACLMessage message;

    public void print(String str){
        wait(3);
        System.out.println(this.getLocalName()+" -> "+ str+".\n");
    }

    public AID getAIDByName(String Name){
        AID result = null;

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(Name);
        template.addServices(sd);

        try {
            DFAgentDescription[] results = DFService.search(this, template);
            if (results.length > 0)
                result = (results[0].getName());
            else
            {
                print(Name+" not found");
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        return result;
    }


    public String[] deserializeMessage(ACLMessage message){
        return message.getContent().split("!!!");
    }

    public static void wait(int sec) {
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

