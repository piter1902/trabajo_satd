package main.java.SMA;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import main.java.Timeout.TimeoutAdapter;
import weka.classifiers.meta.MultiClassClassifier;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AgenteNormalizarMLP extends Agent {

    private List<AID> receivers;

    @Override
    protected void setup() {
        getReceivers();
        addBehaviour(new normalizarYMLPBehaviour(receivers));
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

    private static class normalizarYMLPBehaviour extends OneShotBehaviour {

        private final List<AID> receivers;

        public normalizarYMLPBehaviour(List<AID> receivers) {
            this.receivers = receivers;
        }

        @Override
        public void action() {
            ACLMessage aclMessage = this.myAgent.blockingReceive();
            // Reply with ACK
            TimeoutAdapter.sendACKBack(aclMessage.getSender(), this.myAgent);
            Instances wekaDataset = null;
            try {
                wekaDataset = (Instances) aclMessage.getContentObject();
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.format("Agent %s received %d rows\n", myAgent.getName(), wekaDataset.numInstances());

            // normalize dataset
            Normalize filter = new Normalize();
            try {
                filter.setInputFormat(wekaDataset);
                wekaDataset = Filter.useFilter(wekaDataset, filter);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            // Constuimos el clasificador (Multi Layer Perceptron)
            MultiClassClassifier mlp = new MultiClassClassifier();
            try {
                mlp.buildClassifier(wekaDataset);
            } catch (Exception exception) {
                exception.printStackTrace();
            }


            // Send to receivers
            ACLMessage sendMessage = new ACLMessage(ACLMessage.REQUEST);
            receivers.forEach(sendMessage::addReceiver);
            try {
                sendMessage.setContentObject(mlp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.myAgent.send(sendMessage);
            // main.java.Timeout protocol
            System.out.format("Agent %s: sending model to receivers\n", this.myAgent.getName());
            TimeoutAdapter.sendWithTimeout(sendMessage, this.myAgent);

            System.out.format("Agent %s: message sent to receivers\n", this.myAgent.getName());

        }
    }
}
