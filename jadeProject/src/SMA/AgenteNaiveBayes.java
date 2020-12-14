package SMA;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AgenteNaiveBayes extends Agent {

    private List<AID> receivers;

    @Override
    protected void setup() {
        getReceivers();
        addBehaviour(new NaiveBayesBehaviour(receivers));
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

    private static class NaiveBayesBehaviour extends OneShotBehaviour {

        private final List<AID> receivers;

        public NaiveBayesBehaviour(List<AID> receivers) {
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


            // Constuimos el clasificador (J48)
            NaiveBayes naiveBayes = new NaiveBayes();
            try {
                naiveBayes.buildClassifier(wekaDataset);
            } catch (Exception exception) {
                exception.printStackTrace();
            }


            // Send to receivers
            ACLMessage sendMessage = new ACLMessage(ACLMessage.REQUEST);
            receivers.forEach(sendMessage::addReceiver);
            try {
                sendMessage.setContentObject(naiveBayes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.myAgent.send(sendMessage);
            // Timeout protocol
            TimeoutAdapter.sendWithTimeout(sendMessage, this.myAgent);

            System.out.format("Agent %s: message sent to receivers\n", this.myAgent.getName());

        }
    }
}
