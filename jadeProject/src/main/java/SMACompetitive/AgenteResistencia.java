package main.java.SMACompetitive;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import main.java.Timeout.TimeoutAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class AgenteResistencia extends Agent implements AgenteSimulacion {

    private static Logger log = null;
    static {
        InputStream stream = AgenteResistencia.class.getClassLoader().
                getResourceAsStream("main/resources/logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(stream);
            log = Logger.getLogger(AgenteResistencia.class.getName());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected int bonus;

    private String name;

    // Thread de peticiones tipo inform
    private Thread informThread;

    @Override
    protected void setup() {
        super.setup();
        Object[] args = getArguments();
        this.name = (String) args[0];
        this.bonus = Constants.INITIAL_BONUS;
        // Thread que escucha el mensaje de tipo INFORM
        // TODO: Esto esta aqui un poco de gratis, igual es incompatible con los demas
        informThread = new Thread(() -> {
            // Esperamos el mensaje de kill
            ACLMessage aclMessage = this.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            TimeoutAdapter.sendACKBack(aclMessage.getSender(), this);
            log.info("Agente Resistencia " + getName() + " es eliminado");
            // Eliminamos al agente
            this.doDelete();
        });
        informThread.start();
        // Agent Barrier
        // TODO: Se puede negociar donde esperar la barrera
        ACLMessage aclMessage = this.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE));
        TimeoutAdapter.sendACKBack(aclMessage.getSender(), this);
        log.info("Agente Resistencia " + getName() + " sale de la barrera ");
        addBehaviour(new AgenteResistenciaBehaviour_2(this.bonus, aclMessage.getSender()));
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
}
