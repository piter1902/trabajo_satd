package SMA;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AgenteNormalizar extends Agent {

    private List<AID> receivers;

    @Override
    protected void setup() {
        getReceivers();
        addBehaviour(new normalizarBehaviour(receivers));
        super.setup();
    }

    private void getReceivers() {
        Object[] args = getArguments();
        receivers = new ArrayList<>();
        // args es la lista de receptores
        for (Object o : args) {
            if (o instanceof String) {
                String s = (String) o;
//                System.out.println(s);
                receivers.add(new AID(s, AID.ISGUID));
            }
        }
    }

    private static class normalizarBehaviour extends OneShotBehaviour {

        private final List<AID> receivers;

        public normalizarBehaviour(List<AID> receivers) {
            this.receivers = receivers;
        }

        @Override
        public void action() {
            ACLMessage aclMessage = this.myAgent.blockingReceive();
            Instances wekaDataset = null;
            try {
                wekaDataset = (Instances) aclMessage.getContentObject();
                System.out.format("Agente %s: test dataset received to normalize (%d rows)\n", myAgent.getName(), wekaDataset.numInstances());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // normalize dataset
            Normalize filter = new Normalize();
            try {
                filter.setInputFormat(wekaDataset);
                wekaDataset = Filter.useFilter(wekaDataset, filter);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            ACLMessage sendMessage = new ACLMessage(ACLMessage.REQUEST);
            receivers.forEach(sendMessage::addReceiver);
            try {
                sendMessage.setContentObject(wekaDataset);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.myAgent.send(sendMessage);
            System.out.format("Agent %s: message sent to receivers\n", this.myAgent.getName());

        }
    }
}
