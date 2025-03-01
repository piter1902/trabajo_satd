package main.java.SMACompetitive;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import main.java.Timeout.TimeoutAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class AgenteJoePublic extends Agent implements AgenteSimulacion {

    private static Logger log = null;

    static {
        InputStream stream = AgenteJoePublic.class.getClassLoader().
                getResourceAsStream("main/resources/logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(stream);
            log = Logger.getLogger(AgenteJoePublic.class.getName());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    // Bonus
    private int bonus;

    @Override
    protected void setup() {
        super.setup();
        Object[] args = getArguments();
        // The probability is a random number from 1 to 100
        random = new Random(System.nanoTime());
        this.probabilityToConvertResistance = random.nextInt(100);
        random = new Random(System.nanoTime());
        this.probabilityToConvertSystem = random.nextInt(100);
        this.bonus = Constants.INITIAL_BONUS;
        // Thread que escucha el mensaje de tipo INFORM
        informThread = new Thread(() -> {
            // Esperamos el mensaje de kill
            ACLMessage aclMessage = this.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            TimeoutAdapter.sendACKBack(aclMessage.getSender(), this);
            log.info("Agente JoePublic " + getName() + " es eliminado ");
            // Eliminamos al agente
            this.doDelete();
        });
        // Agent barrier
        informThread.start();
        ACLMessage aclMessage = this.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE));
        TimeoutAdapter.sendACKBack(aclMessage.getSender(), this);
        // Obtain architect AID
        architectAID = aclMessage.getSender();
        log.info("Agente JoePublic " + getName() + " sale de la barrera ");
        addBehaviour(new AgenteJoePublicBehaviour(probabilityToConvertResistance, probabilityToConvertSystem));
    }

    @Override
    public void recalcBonus(int incremento) {
        this.bonus += incremento;
        if (this.bonus >= Constants.MAX_BONUS) {
            this.bonus = Constants.MAX_BONUS;
        } else if (this.bonus <= Constants.MIN_BONUS) {
            this.bonus = Constants.MIN_BONUS;
        }
    }

    @Override
    public int getBonus() {
        return this.bonus;
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
