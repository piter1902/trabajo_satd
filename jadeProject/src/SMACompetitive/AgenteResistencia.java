package SMACompetitive;

import Timeout.TimeoutAdapter;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.Random;

import static SMACompetitive.Constants.INITIAL_BONUS;

public class AgenteResistencia extends Agent implements AgenteSimulacion {

    private int bonus;

    private String name;

    @Override
    protected void setup() {
        super.setup();
        Object[] args = getArguments();
        this.name = (String) args[0];
        this.bonus = INITIAL_BONUS;
        addBehaviour(new AgenteResistenciaBehaviour(this.bonus, new AID((String) args[1], AID.ISGUID)));
    }

    protected void recalcBonus(int bonus) {
        // TODO: Recalc bonus
    }

    private static class AgenteResistenciaBehaviour extends SimpleBehaviour {

        public static final int BATTLE_REQUEST_WAITING_TIME = 100;

        // Direccion del arquitecto
        private final AID arquitectAID;
        // Componente aleatorio
        private final Random random;
        // Bonus
        private int bonus;
        // End of game
        private boolean endOfGame;

        AgenteResistenciaBehaviour(int bonus, AID arquitect) {
            this.bonus = bonus;
            this.endOfGame = false;
            this.arquitectAID = arquitect;
            this.random = new Random(System.nanoTime());
        }

        @Override
        public void action() {
            // Pedimos y recibimos el estado de la simulación
            requestGameStatus();
            GameStatus gameStatus = getGameStatusMessage();
            int nJP = gameStatus.getnJoePublic();
            int nRe = gameStatus.getnResistencia();
            int nSi = gameStatus.getnSistema();
            boolean oraculo = gameStatus.isOraculoFound();

            // Comprobacion de REQUEST (para saber si quieren luchar con nosotros)
            boolean wantToBattle = anyoneWantsToBattle();
            if (wantToBattle) {
                // Estamos en batalla por peticion de los otros
            } else {
                // Acciones normales (tener en cuenta al orcaulo?)
                if (nJP > 0) {
                    // Intentamos reclutar
                    requestJoePublicAgent();
                    String guid = getJoePublicID();
                    // Peticion de reclutamiento al agente
                    recluteAgent(guid);
                    // Respuesta del agente JoePublic
                    Constants.JOEPUBLIC_RESPONSE response = getJoePublicResponse();
                    sendJoePublicResponse(guid, response);
                } else {
                    // Habrá que luchar
                }
            }
        }

        private void sendJoePublicResponse(String guid, Constants.JOEPUBLIC_RESPONSE response) {
            Constants.ARQUITECT_MESSAGE type = null;
            if (response == Constants.JOEPUBLIC_RESPONSE.YES) {
                type = Constants.ARQUITECT_MESSAGE.CONVERT_JOEPUBLIC_TO_RESISTANCE;
            } else if (response == Constants.JOEPUBLIC_RESPONSE.NO) {
                type = Constants.ARQUITECT_MESSAGE.NOT_CONVERTED;
            } else if (response == Constants.JOEPUBLIC_RESPONSE.ORACULO) {
                type = Constants.ARQUITECT_MESSAGE.ORACULO_FOUND_RESISTANCE;
            }
            manipulateJoePublic(guid, type);
        }

        private void manipulateJoePublic(String guid, Constants.ARQUITECT_MESSAGE type) {
            ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
            aclMessage.addReceiver(arquitectAID);
            try {
                aclMessage.setContentObject(type);
            } catch (IOException e) {
                e.printStackTrace();
            }
            aclMessage.setContent(guid);
            TimeoutAdapter.sendWithTimeout(aclMessage, this.myAgent);
        }

        private Constants.JOEPUBLIC_RESPONSE getJoePublicResponse() {
            ACLMessage aclMessage = this.myAgent.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
            TimeoutAdapter.sendACKBack(aclMessage.getSender(), this.myAgent);
            Constants.JOEPUBLIC_RESPONSE response = null;
            try {
                response = (Constants.JOEPUBLIC_RESPONSE) aclMessage.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            return response;
        }

        private void recluteAgent(String guid) {
            ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
            aclMessage.addReceiver(new AID(guid, AID.ISGUID));
            try {
                aclMessage.setContentObject(Constants.AGENT_MESSAGE.RECRUITE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            TimeoutAdapter.sendWithTimeout(aclMessage, this.myAgent);
        }

        private String getJoePublicID() {
            ACLMessage aclMessage = this.myAgent.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
            TimeoutAdapter.sendACKBack(aclMessage.getSender(), this.myAgent);
            return aclMessage.getContent();
        }

        private void requestJoePublicAgent() {
            ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
            aclMessage.addReceiver(arquitectAID);
            try {
                aclMessage.setContentObject(Constants.ARQUITECT_MESSAGE.GET_JOEPUBLIC_AGENT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            TimeoutAdapter.sendWithTimeout(aclMessage, this.myAgent);
        }

        private boolean anyoneWantsToBattle() {
            // TODO: Comprobar que no hay deadlocks
            ACLMessage aclMessage = this.myAgent.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), BATTLE_REQUEST_WAITING_TIME);
            // aclMessage == null si no hay peticiones de batalla
            boolean wantToBattle = aclMessage != null;
            if (wantToBattle) {
                TimeoutAdapter.sendACKBack(aclMessage.getSender(), this.myAgent);
            }
            return wantToBattle;
        }

        private GameStatus getGameStatusMessage() {
            // Se espera la confirmacion de la petición
            ACLMessage aclMessage = this.myAgent.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
            TimeoutAdapter.sendACKBack(aclMessage.getSender(), this.myAgent);
            GameStatus gameStatus = null;
            try {
                gameStatus = (GameStatus) aclMessage.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            return gameStatus;
        }

        private void requestGameStatus() {
            ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
            aclMessage.addReceiver(arquitectAID);
            try {
                aclMessage.setContentObject(Constants.ARQUITECT_MESSAGE.GET_GAME_STATUS);
            } catch (IOException e) {
                e.printStackTrace();
            }
            TimeoutAdapter.sendWithTimeout(aclMessage, this.myAgent);
        }

        @Override
        public boolean done() {
            return endOfGame;
        }
    }
}
