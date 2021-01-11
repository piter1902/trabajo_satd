package main.java.SMACompetitive;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import main.java.Timeout.TimeoutAdapter;

import java.util.logging.Logger;

public class AgenteSmith extends AgenteSistema {

    private Logger logger = Logger.getLogger(AgenteSmith.class.getName());

    private Thread agreeThread;

    @Override
    protected void setup() {
        agreeThread = new Thread(() -> {
            // Esperamos el mensaje de oraculo
            ACLMessage aclMessage = this.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.AGREE));
            TimeoutAdapter.sendACKBack(aclMessage.getSender(), this);
            logger.info("Recibido el bonus del Oraculo");
            // Recalculamos el bonus
            recalcBonus(+5);
        });
        agreeThread.start();
        super.setup();
    }

    @Override
    protected void takeDown() {
        if (!agreeThread.isInterrupted()) {
            agreeThread.interrupt();
            try {
                agreeThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        super.takeDown();
    }

    @Override
    public void recalcBonus(int incremento) {
        synchronized (this) {
            this.bonus += incremento;
            if (this.bonus >= Constants.MAX_BONUS_NEO_SMITH) {
                this.bonus = Constants.MAX_BONUS_NEO_SMITH;
            } else if (this.bonus <= Constants.MIN_BONUS) {
                this.bonus = Constants.MIN_BONUS;
            }
        }
    }

}