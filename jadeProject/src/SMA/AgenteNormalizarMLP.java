package SMA;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import weka.classifiers.meta.MultiClassClassifier;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

public class AgenteNormalizarMLP extends Agent {

    @Override
    protected void setup() {
        addBehaviour(new normalizarYMLPBehaviour());
        super.setup();
    }

    private static class normalizarYMLPBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            ACLMessage aclMessage = this.myAgent.blockingReceive();
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

            // TODO: Send dynamically mlp object to next node


        }
    }
}
