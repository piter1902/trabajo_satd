package SMA;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
            this.myAgent.blockingReceive(1000);
            String data = aclMessage.getContent();

            List<String> lines = Arrays.asList(data.split("\n"));
            // Quitamos la cabecera
            // TODO: Falla aqui (unsupported operation exception). Es probable que sea por el conjunto de datos weka.
            String firstLine = lines.remove(0);

            // Partition
            Collections.shuffle(lines);
            int partitionIndex = (int) (lines.size() * PORCENTAJE_PARTICION);

            List<String> partition1 = lines.subList(0, partitionIndex);
            // Reduce to one string (concatenate)
            String p1 = partition1.stream().reduce("", (s, acum) -> s + acum);

            List<String> partition2 = lines.subList(partitionIndex, lines.size());
            // Reduce to one string (concatenate)
            String p2 = partition2.stream().reduce("", (s, acum) -> s + acum);

            // TODO: Send dynamically Weka Datasets (DataSource)
            InputStream inputStream1 = new ByteArrayInputStream(p1.getBytes());
            InputStream inputStream2 = new ByteArrayInputStream(p2.getBytes());


            ConverterUtils.DataSource dataSource1 = new ConverterUtils.DataSource(inputStream1);
            ConverterUtils.DataSource dataSource2 = new ConverterUtils.DataSource(inputStream2);

            Instances wekaDataset1 = null;
            Instances wekaDataset2 = null;
            try {
                wekaDataset1 = dataSource1.getDataSet();
                wekaDataset2 = dataSource2.getDataSet();
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            // Como en el ejemplo de AgenteDM
            wekaDataset1.setClassIndex(wekaDataset1.numAttributes() - 1);
            wekaDataset2.setClassIndex(wekaDataset2.numAttributes() - 1);


        }

    }
}
