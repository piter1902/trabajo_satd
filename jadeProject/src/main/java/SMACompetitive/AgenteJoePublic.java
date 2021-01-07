package main.java.SMACompetitive;

import main.java.Timeout.TimeoutAdapter;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class AgenteJoePublic extends Agent implements AgenteSimulacion {

    private final static Logger log = LogManager.getLogger(AgenteJoePublic.class);

    // Probability to be converted to a resistance agent
    private int probabilityToConvertResistance;

    // Probability to be converted to a system agent
    private int probabilityToConvertSystem;

    // Random Number generator
    private Random random;

    // Thread de peticiones tipo inform
    private Thread informThread;

    // AID from the architect. Is requeired when converting to a TEAM
    private AID architectAID;

    @Override
    protected void setup() {
        super.setup();
        Object[] args = getArguments();
        // The probability is a random number from 1 to 100
        random = new Random(System.nanoTime());
        this.probabilityToConvertResistance = random.nextInt(100);
        random = new Random(System.nanoTime());
        this.probabilityToConvertSystem = random.nextInt(100);
        // Thread que escucha el mensaje de tipo INFORM
        // TODO: Esto esta aqui un poco de gratis, igual es incompatible con los demas
        informThread = new Thread(() -> {
            // Esperamos el mensaje de kill
            ACLMessage aclMessage = this.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            TimeoutAdapter.sendACKBack(aclMessage.getSender(), this);
            log.info("Agente JoePublic " + getName() + " es eliminado ");
            // Eliminamos al agente
            this.doDelete();
        });
        // Agent barrier
        // TODO: Se puede negociar donde esperar la barrera
        ACLMessage aclMessage = this.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE));
        TimeoutAdapter.sendACKBack(aclMessage.getSender(), this);
        // Obtain architect AID
        architectAID = aclMessage.getSender();
        log.info("Agente JoePublic " + getName() + " sale de la barrera ");
        addBehaviour(new AgenteJoePublicBehaviour(probabilityToConvertResistance, probabilityToConvertSystem));
    }

    @Override
    public void recalcBonus(int incremento) {

    }

    @Override
    public int getBonus() {
        return 0;
    }


    @Override
    protected void takeDown() {
        if (informThread.isInterrupted()) {
            informThread.interrupt();
            try {
                informThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        super.takeDown();
    }

    public AID getArchitectAID() {
        return architectAID;
    }
}
