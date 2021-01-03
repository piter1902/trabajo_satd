package SMACompetitive;

import Timeout.TimeoutAdapter;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import static SMACompetitive.Constants.INITIAL_BONUS;
import static SMACompetitive.Constants.MAX_BONUS;

public class AgenteSistema extends Agent implements AgenteSimulacion {

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
            // Eliminamos al agente
            this.doDelete();
        });
        informThread.start();
        addBehaviour(new AgenteSistemaBehaviour(this.bonus, new AID((String) args[1], AID.ISGUID)));
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
