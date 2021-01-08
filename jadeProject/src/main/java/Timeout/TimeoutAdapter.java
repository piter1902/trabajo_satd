package main.java.Timeout;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TimeoutAdapter {

    // Numero máximo de reintentos de envíos
    private static final int MAX_COUNT = 10;

    private final static Logger log = Logger.getLogger(TimeoutAdapter.class.getName());

    public static void sendWithTimeout(ACLMessage msg, Agent agent, String... context) {
        jade.util.leap.Iterator it = msg.getAllReceiver();
        List<AID> receptoresRestantes = new ArrayList<>();
        it.forEachRemaining(o -> receptoresRestantes.add((AID) o));
        int count_sends = MAX_COUNT;
        // TODO: Enviamos antes de esperar una respuesta
        agent.send(msg);
        do {
            ACLMessage received = agent.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM), 5000);
            if (received != null) {
                // Esto es el ACK
                log.severe(String.format("Agente %s: ACK received from %s . CONTEXT -------------> %s\n", agent.getName(), received.getSender().getName(), context[0]));
                receptoresRestantes.remove(received.getSender());
            } else {
                // Esto es otra cosa. Volvemos a intentarlo
                log.warning(String.format("Agente %s: ACK not received. Trying %d more times. CONTEXT -------------> %s\n", agent.getName(), count_sends, context[0]));
                count_sends--;
                // Se envia el mensaje a los que no han enviado ACK
                msg.clearAllReceiver();
                receptoresRestantes.forEach(msg::addReceiver);
                agent.send(msg);
            }
        } while (count_sends >= 0 && !receptoresRestantes.isEmpty());
    }

    // Para el caso que se necesitan mandar 2 mensajes (se podría generalizar)
    public static void sendWithTimeout2Messages(ACLMessage msg1, ACLMessage msg2, Agent agent) {
        jade.util.leap.Iterator it1 = msg1.getAllReceiver();
        jade.util.leap.Iterator it2 = msg2.getAllReceiver();
        List<AID> receptoresRestantes1 = new ArrayList<>();
        List<AID> receptoresRestantes2 = new ArrayList<>();
        it1.forEachRemaining(o -> receptoresRestantes1.add((AID) o));
        it2.forEachRemaining(o -> receptoresRestantes2.add((AID) o));
        int count_sends = MAX_COUNT;
        do {
            ACLMessage received = agent.blockingReceive(1000);
            if (received != null && received.getPerformative() == ACLMessage.CONFIRM) {
                // Esto es el ACK
                AID sender = received.getSender();
                log.severe(String.format("Agente %s: ACK received from %s\n", agent.getName(), received.getSender().getName()));
                if (receptoresRestantes1.contains(sender)) {
                    receptoresRestantes1.remove(sender);
                } else receptoresRestantes2.remove(sender);
            } else {
                // Esto es otra cosa. Volvemos a intentarlo
                log.warning(String.format("Agente %s: ACK not received. Trying %d more times.\n", agent.getName(), count_sends));
                count_sends--;
                // Se envia el mensaje a los que no han enviado ACK
                msg1.clearAllReceiver();
                receptoresRestantes1.forEach(msg1::addReceiver);

                msg2.clearAllReceiver();
                receptoresRestantes2.forEach(msg2::addReceiver);
                // Send messages back
                agent.send(msg1);
                agent.send(msg2);
            }
        } while (count_sends >= 0 && (!receptoresRestantes1.isEmpty() || !receptoresRestantes2.isEmpty()));
    }

    public static void sendACKBack(AID sender, Agent agent) {
        ACLMessage ackMessage = new ACLMessage(ACLMessage.CONFIRM);
        ackMessage.addReceiver(sender);
        agent.send(ackMessage);
    }
}
