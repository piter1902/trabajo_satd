package SMA;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AgenteParticion extends Agent {

    private static final double PORCENTAJE_PARTICION = 0.8;

    @Override
    protected void setup() {
        addBehaviour(new PartitionBehaviour());
        super.setup();
    }

    private static class PartitionBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            ACLMessage aclMessage = this.myAgent.blockingReceive();
            String data = aclMessage.getContent();

            List<String> lines = Arrays.asList(data.split("\n"));
            String firstLine = lines.remove(0);

            // Partition
            Collections.shuffle(lines);
            int partitionIndex = (int) (lines.size() * PORCENTAJE_PARTICION);
            List<String> partition1 = lines.subList(0, partitionIndex);
            List<String> partition2 = lines.subList(partitionIndex, lines.size());
        }

    }
}
