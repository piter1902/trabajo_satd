package SMA;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

public class AgenteNormalizar extends Agent {

    @Override
    protected void setup() {
        addBehaviour(new normalizarBehaviour());
        super.setup();
    }

    private static class normalizarBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            ACLMessage aclMessage = this.myAgent.blockingReceive();
            Instances wekaDataset = null;
            try {
                wekaDataset = (Instances) aclMessage.getContentObject();
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

            // TODO: Send dynamically normalized dataset object to next node

        }
    }
}
