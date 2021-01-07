package main.java.SMACompetitive;

import main.java.Timeout.TimeoutAdapter;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Random;

import static main.java.SMACompetitive.Constants.INITIAL_BONUS;

public class AgenteJoePublicBehaviour extends SimpleBehaviour {

    private final static Logger log = LogManager.getLogger(AgenteJoePublicBehaviour.class);

    // Probabilities of conversion
    private int probabilityResistance;
    private int probabilitySystem;


    public AgenteJoePublicBehaviour(int probabilityResistance, int probabilitySystem) {
        this.probabilityResistance = probabilityResistance;
        this.probabilitySystem = probabilitySystem;
    }

    @Override
    public void action() {
        // Solo se procesan las peticiones de tipo (PROPOSE)
        // Siempre está a la espera de que le pidan reclutamiento
        ACLMessage aclMessage = this.myAgent.blockingReceive(
                MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
        AID sender = aclMessage.getSender();
        TimeoutAdapter.sendACKBack(sender, this.myAgent);

        Constants.AGENT_MESSAGE message = null;
        try {
            message = (Constants.AGENT_MESSAGE) aclMessage.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }

        if (message != null) {
            log.info("Mensaje recibido a " + myAgent.getName() + ": " + message);
            switch (message) {
                case RESISTANCE_RECRUITE:
                    // Están intentando reclutar de la resistencia
                    Constants.JOEPUBLIC_RESPONSE response = resistConversion(Constants.TEAM.RESISTANCE);
                    sendResponseToConverter(response, sender);
                    break;
                case SYSTEM_RECRUITE:
                    // Intento de reclutamiento de sistema
                    response = resistConversion(Constants.TEAM.SYSTEM);
                    sendResponseToConverter(response, sender);
                    break;
                case CONVERT_TO_RESISTANCE:
                    log.info("Agente JP " + myAgent.getName() + " convirtiendo a RESISTENCIA");
                    // Cambiar comportamiento a resistencia
                    // TODO: Tiene sentido pero no se si es asi
                    myAgent.addBehaviour(new AgenteResistenciaBehaviour(INITIAL_BONUS, ((AgenteJoePublic) myAgent).getArchitectAID()));
                    myAgent.removeBehaviour(this);
                    break;
                case CONVERT_TO_SYSTEM:
                    // Cambiar comportamiento a sistema
                    log.info("Agente JP " + myAgent.getName() + " convirtiendo a SISTEMA");

                    // TODO: Tiene sentido pero no se si es asi
                    myAgent.addBehaviour(new AgenteSistemaBehaviour(INITIAL_BONUS, ((AgenteJoePublic) myAgent).getArchitectAID()));
                    myAgent.removeBehaviour(this);
                    break;
            }
        } else {
            // Mensaje erróneo -> es descartado
        }
    }

    private void sendResponseToConverter(Constants.JOEPUBLIC_RESPONSE decision, AID sender) {
        ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
        aclMessage.addReceiver(sender);
        try {
            aclMessage.setContentObject(decision);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TimeoutAdapter.sendWithTimeout(aclMessage, this.myAgent);
    }

    /**
     * Checks if the JP agent will JOIN the converter team
     *
     * @param team converter TEAM
     * @return YES if it will convert. No if NOT. ORACLE if it's the oracle
     */
    private Constants.JOEPUBLIC_RESPONSE resistConversion(Constants.TEAM team) {
        // TODO: buscar un mecanismo más sofisticado para resistirse
        Random randomTemp = new Random(System.nanoTime());
        // TODO: se puede asi?
        if (myAgent instanceof AgenteOraculo) {
            // Se comprueba si es el oraculo
            log.info("Agente JP " + myAgent.getName() + " es ORACULO");
            return Constants.JOEPUBLIC_RESPONSE.ORACULO;
        }
        switch (team) {
            case RESISTANCE:
                if (this.probabilityResistance > randomTemp.nextInt(100)) {
                    log.info("Agente JP " + myAgent.getName() + " admite conversion a RESISTENCIA");
                    return Constants.JOEPUBLIC_RESPONSE.YES;
                }
                break;
            case SYSTEM:
                if (this.probabilitySystem > randomTemp.nextInt(100)) {
                    log.info("Agente JP " + myAgent.getName() + " admite conversion a SISTEMA");
                    return Constants.JOEPUBLIC_RESPONSE.YES;
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + team);
        }
        // ON this case, it won't convert
        return Constants.JOEPUBLIC_RESPONSE.NO;
    }

    @Override
    public boolean done() {
        return false;
    }
}
