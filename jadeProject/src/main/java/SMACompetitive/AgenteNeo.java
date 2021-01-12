package main.java.SMACompetitive;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import main.java.Timeout.TimeoutAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class AgenteNeo extends AgenteResistencia {

    private static Logger log = null;

    static {
        InputStream stream = AgenteNeo.class.getClassLoader().
                getResourceAsStream("main/resources/logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(stream);
            log = Logger.getLogger(AgenteNeo.class.getName());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Thread agreeThread;

    @Override
    protected void setup() {
        agreeThread = new Thread(() -> {
            // Esperamos el mensaje de oraculo
            ACLMessage aclMessage = this.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.AGREE));
            TimeoutAdapter.sendACKBack(aclMessage.getSender(), this);
            log.info("Recibido el bonus del Oraculo");
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
