package SMACompetitive;

import Timeout.TimeoutAdapter;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AgenteNeo extends AgenteResistencia {

    private Thread agreeThread;

    @Override
    protected void setup() {
        agreeThread = new Thread(() -> {
            // Esperamos el mensaje de oraculo
            ACLMessage aclMessage = this.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.AGREE));
            TimeoutAdapter.sendACKBack(aclMessage.getSender(), this);
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
        this.bonus += incremento;
        if (this.bonus >= Constants.MAX_BONUS_NEO_SMITH) {
            this.bonus = Constants.MAX_BONUS_NEO_SMITH;
        }
    }

}
