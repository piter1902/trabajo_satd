package SMACompetitive;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import static SMACompetitive.Constants.INITIAL_BONUS;

public class AgenteSistema extends Agent implements AgenteSimulacion {

    private int bonus;

    private String name;

    // Thread de peticiones tipo inform
    private Thread informThread;

    @Override
    protected void setup() {
        super.setup();
        Object[] args = getArguments();
        this.name = (String) args[0];
        this.bonus = INITIAL_BONUS;
        // Thread que escucha el mensaje de tipo INFORM
        // TODO: Esto esta aqui un poco de gratis, igual es incompatible con los demas
        informThread = new Thread(() -> {
            // Esperamos el mensaje de kill
            this.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            // Eliminamos al agente
            this.takeDown();
        });
        informThread.start();
        addBehaviour(new AgenteSistemaBehaviour(this.bonus, new AID((String) args[1], AID.ISGUID)));
    }

    @Override
    protected void takeDown() {
        super.takeDown();
        try {
            informThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
