package agents;

import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.List;

public class DFAgent extends Agent{
    List<AID> ManagerAgents = new ArrayList<AID>();
    protected void setup() {

        // Register the DF service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("DF");
        sd.setName("DF");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Add the behaviour
        addBehaviour(new SendManagerAgent());
    }
    private class SendManagerAgent extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // CFP Message received. Process it
                String PortCode = msg.getContent();
                ACLMessage reply = msg.createReply();
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType(PortCode);
                template.addServices(sd);

                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    for (DFAgentDescription dfAgentDescription : result) {
                        ManagerAgents.add(dfAgentDescription.getName());
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }

                if(ManagerAgents.size() > 0) {
                    String replyMsg = null;
                    replyMsg = ManagerAgents.toString();

                    // The requested PortCode is available
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(replyMsg);
                }
                else{
                    // The requested PortCode not available
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }


}