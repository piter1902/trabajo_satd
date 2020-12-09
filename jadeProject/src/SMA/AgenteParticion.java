package SMA;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import weka.core.Instances;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class AgenteParticion extends Agent {

    private static final double PORCENTAJE_PARTICION = 0.8;

    private final Map<String, List<AID>> receivers = new HashMap<>();

    @Override
    protected void setup() {
        getReceivers();
        addBehaviour(new PartitionBehaviour(receivers.get("partition1"), receivers.get("partition2")));
        super.setup();
    }

    private void getReceivers() {
        // Obtenemos el diccionario de receptores
        List<Object> arguments = Arrays.asList(getArguments());
        // Es una lista de 1 elemento (Map<String,List<String>>)
        if (arguments.get(0) instanceof Map) {
            Map<String, String[]> receptores = (Map<String, String[]>) arguments.get(0);
            receptores.forEach((listName, stringList) -> {
                List<AID> listReceivers = Arrays.asList(stringList).stream().map(s -> new AID(s, AID.ISGUID)).collect(Collectors.toList());
                receivers.put(listName, listReceivers);
            });
        }
    }

    private static class PartitionBehaviour extends OneShotBehaviour {

        private final List<AID> partition1;
        private final List<AID> partition2;

        public PartitionBehaviour(List<AID> partition1, List<AID> partition2) {
            this.partition1 = partition1;
            this.partition2 = partition2;
        }

        @Override
        public void action() {
            // Aqui siempre se espera un mensaje de tipo REQUEST
            // Funciona como un Blocking Receive
            ACLMessage aclMessage = this.myAgent.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

            // Send ACK to sender
            TimeoutAdapter.sendACKBack(aclMessage.getSender(), this.myAgent);

            // Parse Message
            Instances wekaDataset;
            try {
                wekaDataset = ((Instances) aclMessage.getContentObject());
            } catch (UnreadableException e) {
                throw new RuntimeException("No se puede obtener el dataset del mensaje");
            }

            System.out.format("Message received at %s : %s (rows)\n", myAgent.getName(), wekaDataset.numInstances());



            // Randomize dataset
            wekaDataset.randomize(new Random(System.nanoTime()));

            // Partition
            int partition1_size = (int) (wekaDataset.numInstances() * PORCENTAJE_PARTICION);
            int partition2_size = wekaDataset.numInstances() - partition1_size;

            Instances wekaDataset1 = new Instances(wekaDataset, 0, partition1_size);
            Instances wekaDataset2 = new Instances(wekaDataset, partition1_size, partition2_size);

            System.out.format("El conjunto de datos partition1 tiene %d filas\n", wekaDataset1.numInstances());
            System.out.format("El conjunto de datos partition2 tiene %d filas\n", wekaDataset2.numInstances());

            // Como en el ejemplo de AgenteDM
            wekaDataset1.setClassIndex(wekaDataset1.numAttributes() - 1);
            wekaDataset2.setClassIndex(wekaDataset2.numAttributes() - 1);

            // Add receivers

            System.out.format("Conjuntos de datos enviados.\n");

            ACLMessage partition1Message = new ACLMessage(ACLMessage.REQUEST);
            ACLMessage partition2Message = new ACLMessage(ACLMessage.REQUEST);
            try {
                partition1Message.setContentObject(wekaDataset1);
                partition2Message.setContentObject(wekaDataset2);
            } catch (IOException e) {
                e.printStackTrace();
            }
            partition1.forEach(partition1Message::addReceiver);
            partition2.forEach(partition2Message::addReceiver);

            // Send messages
            myAgent.send(partition1Message);
            myAgent.send(partition2Message);

            // Timeout protocol
            TimeoutAdapter.sendWithTimeout2Messages(partition1Message, partition2Message, this.myAgent);

        }



    }
}
