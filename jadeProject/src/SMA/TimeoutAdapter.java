package SMA;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import java.util.*;

public class TimeoutAdapter {

    private static final int MAX_COUNT = 10;

    // TODO: Aqui falla por el Iterator
    public static void sendWithTimeout(ACLMessage msg, Agent agent) {
        jade.util.leap.Iterator it = msg.getAllReceiver();
        List<AID> receptoresRestantes = new ArrayList<>();
        it.forEachRemaining(o -> receptoresRestantes.add((AID) o));
        // TODO: Se puede modificar esta cantidad en base al numero de receptores
        int count_sends = MAX_COUNT;
        do {
            ACLMessage received = agent.blockingReceive(1000);
            if (received != null && received.getPerformative() == ACLMessage.CONFIRM) {
                // Esto es el ACK
                System.out.format("Agente %s: ACK received from %s\n", agent.getName(), received.getSender().getName());
                // TODO: Son los AID unicos?
                receptoresRestantes.remove(received.getSender());
                break;
            } else {
                // Esto es otra cosa. Volvemos a intentarlo
                System.out.format("Agente %s: ACK not received. Trying %d more times.\n", agent.getName(), count_sends);
                count_sends--;
                agent.send(msg);
            }
        } while (count_sends >= 0 && !receptoresRestantes.isEmpty());
    }

    public static void sendACKBack(AID sender, Agent agent) {
        ACLMessage ackMessage = new ACLMessage(ACLMessage.CONFIRM);
        ackMessage.addReceiver(sender);
        agent.send(ackMessage);
    }
}
