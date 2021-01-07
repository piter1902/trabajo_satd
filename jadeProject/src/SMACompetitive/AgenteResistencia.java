package SMACompetitive;

import Timeout.TimeoutAdapter;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static SMACompetitive.Constants.INITIAL_BONUS;
import static SMACompetitive.Constants.MAX_BONUS;

public class AgenteResistencia extends Agent implements AgenteSimulacion {

    private final static Logger log = LogManager.getLogger(AgenteResistencia.class);

    protected int bonus;

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
        addBehaviour(new AgenteResistenciaBehaviour(this.bonus, aclMessage.getSender()));
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
            this.bonus = MAX_BONUS;
        }
    }

    @Override
    public int getBonus() {
        return this.bonus;
    }
}
