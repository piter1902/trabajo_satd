package main.java.SMACompetitive;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import main.java.Timeout.TimeoutAdapter;

import java.io.IOException;
import java.util.Random;
import java.util.logging.Logger;

class AgenteResistenciaBehaviour extends SimpleBehaviour {

    public static final int WAITING_TIME = 100;

    public static final int GAMESTATUS_WAITING_TIME = 5000;

    private final static Logger log = Logger.getLogger(AgenteResistenciaBehaviour.class.getName());

    // Direccion del arquitecto
    private final AID arquitectAID;
    // Componente aleatorio
    private final Random random;
    // End of game
    private boolean endOfGame;

    AgenteResistenciaBehaviour(int bonus, AID arquitect) {
        this.endOfGame = false;
        this.arquitectAID = arquitect;
        this.random = new Random(System.nanoTime());
    }

    @Override
    public void action() {
        // Comprobacion de que no se ha terminado el juego
        // TODO: Esto no debería existir
//        this.endOfGame = checkGameOver();

        if (!endOfGame) {
            // Pedimos y recibimos el estado de la simulación
            requestGameStatus();
            GameStatus gameStatus = getGameStatusMessage();

            if (gameStatus != null) {
                // El juego no ha terminado
                int nJP = gameStatus.getnJoePublic();
                int nRe = gameStatus.getnResistencia();
                int nSi = gameStatus.getnSistema();
                boolean oraculo = gameStatus.isOraculoFound();

                // Comprobacion de REQUEST (para saber si quieren luchar con nosotros)
                AID oponentAID = anyoneWantsToBattle();
                // oponentAID sera null si no hay una batalla pendiente
                if (oponentAID != null) {
                    // Estamos en batalla por peticion de los otros
                    log.info("Agente Resistencia " + myAgent.getName() + " observa que " + oponentAID.getName() + " quiere luchar");
                    sendBonusToOpponent(oponentAID);
                    // Esperamos la respuesta del agente oponente
                    Constants.BATTLE_RESPONSE response = getBattleResponse();
                    log.info("Agente Resistencia " + myAgent.getName() + ". Resultado batalla: " + response);
                    switch (response) {
                        case WIN:
                            // Recalcular bonus
                            ((AgenteSimulacion) this.myAgent).recalcBonus(+1);
                            break;

                        case DEFEAT:
                            // Hemos perdido, seremos eliminados por el arquitecto
                            defeated();
                            break;

                        case TIE:
                            // Empate, recalcular bonus
                            ((AgenteSimulacion) this.myAgent).recalcBonus(-1);
                            break;
                    }
                } else {
                    System.out.println("Estoy en el caso de que IGUAL recluto");
                    // Acciones normales (tener en cuenta al orcaulo?)
                    if (true/*nJP > 0*/) {
                        // Intentamos reclutar
                        log.info("Agente Resistencia " + myAgent.getName() + " comienza a reclutar");
                        requestJoePublicAgent();
                        String guid = getJoePublicID();
                        // Peticion de reclutamiento al agente
                        recluteAgent(guid);
                        // Respuesta del agente JoePublic
                        Constants.JOEPUBLIC_RESPONSE response = getJoePublicResponse();
                        sendJoePublicResponse(guid, response);
                    } else {
                        log.info("Agente Resistencia " + myAgent.getName() + " quiere luchar");
                        // Habrá que luchar
                        sendRequestForMatch();
                        // Respuesta del arquitecto
                        String agentName = getMatchedAgent();
                        if (agentName != null) {
                            log.info("Agente Resistencia " + myAgent.getName() + " pide batalla a " + agentName);
                            // Hay emparejamiento
                            int enemyBonus = getEnemyBonus(agentName);
                            // Calculo de la batalla
                            Constants.BATTLE_RESPONSE result = battleIntern(enemyBonus);
                            log.info("Agente Resistencia " + myAgent.getName() + " resultado de batalla: " + result);
                            // Enviamos el resultado al arquitecto
                            sendResultToArchitect(agentName, result);
                            // Enviamos el resultado al otro agente
                            sendResultToOponent(agentName, result);
                            // Acciones a realizar
                            switch (result) {
                                case WIN:
                                    // Recalcular bonus
                                    ((AgenteSimulacion) this.myAgent).recalcBonus(+1);
                                    break;
                                case DEFEAT:
                                    // Esperamos la eliminacion
                                    defeated();
                                    break;
                                case TIE:
                                    // Empate
                                    ((AgenteSimulacion) this.myAgent).recalcBonus(-1);
                                    break;
                            }
                        }
                        // else -> CANCEL
                    }
                }
            }
        }

    }

    private void sendResultToOponent(String agentName, Constants.BATTLE_RESPONSE result) {
        ACLMessage aclMessage = new ACLMessage(ACLMessage.PROPOSE);
        aclMessage.addReceiver(new AID(agentName, AID.ISGUID));
        try {
            // Se invierte el resultado para enviarselo al oponente
            Constants.BATTLE_RESPONSE response;
            if (result == Constants.BATTLE_RESPONSE.WIN) {
                response = Constants.BATTLE_RESPONSE.DEFEAT;
            } else if (result == Constants.BATTLE_RESPONSE.DEFEAT) {
                response = Constants.BATTLE_RESPONSE.WIN;
            } else {
                // Es un empate
                response = Constants.BATTLE_RESPONSE.TIE;
            }
            GameMessage gm = new GameMessage(response);
            aclMessage.setContentObject(gm);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TimeoutAdapter.sendWithTimeout(aclMessage, this.myAgent, "SENDING BATTLE RESULTS TO " + agentName);
    }

    private void sendResultToArchitect(String agentName, Constants.BATTLE_RESPONSE result) {
        ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
        aclMessage.addReceiver(arquitectAID);
        GameMessage gm = new GameMessage(Constants.ARQUITECT_MESSAGE.WIN);
        try {
            aclMessage.setContentObject(gm);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String loser;
        if (result == Constants.BATTLE_RESPONSE.WIN) {
            // Hemos ganado
            loser = agentName;
        } else if (result == Constants.BATTLE_RESPONSE.TIE) {
            // Empate (se envia el agente oponente)
            loser = agentName;
        } else {
            // Hemos perdido
            loser = this.myAgent.getName();
        }
        // Se envía el agente a ELIMINAR
        gm.setContent(loser);
        TimeoutAdapter.sendWithTimeout(aclMessage, this.myAgent, "SENDING BATTLE RESULTS TO ARCHITECT");
    }

    private Constants.BATTLE_RESPONSE battleIntern(int enemyBonus) {
        boolean enemyIsBigger = enemyBonus > ((AgenteSimulacion) this.myAgent).getBonus();
        int bonusFinal = enemyIsBigger ? enemyBonus - ((AgenteSimulacion) this.myAgent).getBonus() : ((AgenteSimulacion) this.myAgent).getBonus() - enemyBonus;
        int randomNum = this.random.nextInt(bonusFinal);
        Constants.BATTLE_RESPONSE result;
        if (bonusFinal - randomNum > 5) {
            // Gana el de mas bonus
            result = enemyIsBigger ? Constants.BATTLE_RESPONSE.DEFEAT : Constants.BATTLE_RESPONSE.WIN;
        } else {
            // Tablas
            result = Constants.BATTLE_RESPONSE.TIE;
        }
        return result;
    }

    private int getEnemyBonus(String agentName) {
        ACLMessage aclMessage = new ACLMessage(ACLMessage.PROPOSE);
        aclMessage.addReceiver(new AID(agentName, AID.ISGUID));
        TimeoutAdapter.sendWithTimeout(aclMessage, this.myAgent, "REQUEST BONUS TO " + agentName);
        ACLMessage response = this.myAgent.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
        TimeoutAdapter.sendACKBack(response.getSender(), this.myAgent);
        int enemyBonus = 0;
        try {
            GameMessage gm = (GameMessage) response.getContentObject();
            enemyBonus = Integer.parseInt(gm.getContent());
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        return enemyBonus;
    }

    private String getMatchedAgent() {
        MessageTemplate template = new MessageTemplate((MessageTemplate.MatchExpression) aclMessage -> {
            int performative = aclMessage.getPerformative();
            return performative == ACLMessage.CANCEL || performative == ACLMessage.REQUEST;
        });

        ACLMessage aclMessage = this.myAgent.blockingReceive(template);
        TimeoutAdapter.sendACKBack(aclMessage.getSender(), this.myAgent);

        if (aclMessage.getPerformative() == ACLMessage.REQUEST) {
            // Nos han dado al oponente
            String oponent = null;
            try {
                GameMessage gm = (GameMessage) aclMessage.getContentObject();
                oponent = gm.getContent();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            return oponent;
        }
        // Already in use or end of game -> no hacer nada
        return null;

    }

    private void sendRequestForMatch() {
        ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
        aclMessage.addReceiver(arquitectAID);
        try {
            GameMessage gm = new GameMessage(Constants.ARQUITECT_MESSAGE.GET_SYSTEM_MATCH);
            aclMessage.setContentObject(gm);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TimeoutAdapter.sendWithTimeout(aclMessage, this.myAgent, "REQUEST MATCH");
    }

    private void defeated() {
        ACLMessage aclMessage = this.myAgent.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        TimeoutAdapter.sendACKBack(aclMessage.getSender(), this.myAgent);
        // Fin de la partida
        this.endOfGame = true;
    }

    private Constants.BATTLE_RESPONSE getBattleResponse() {
        ACLMessage aclMessage = this.myAgent.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
        TimeoutAdapter.sendACKBack(aclMessage.getSender(), this.myAgent);
        Constants.BATTLE_RESPONSE response = null;
        try {
            GameMessage gm = (GameMessage) aclMessage.getContentObject();
            response = (Constants.BATTLE_RESPONSE) gm.getMessage();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        return response;
    }

    private void sendBonusToOpponent(AID oponentAID) {
        ACLMessage aclMessage = new ACLMessage(ACLMessage.PROPOSE);
        aclMessage.addReceiver(oponentAID);
        GameMessage gm = new GameMessage(Constants.AGENT_MESSAGE.BATTLE, String.valueOf(((AgenteSimulacion) this.myAgent).getBonus()));
        try {
            aclMessage.setContentObject(gm);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TimeoutAdapter.sendWithTimeout(aclMessage, this.myAgent, "SENDING BONUS INFO TO " + oponentAID.getName());
    }

    private boolean checkGameOver() {
        ACLMessage aclMessage = this.myAgent.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.INFORM), WAITING_TIME);
        boolean isGameOver = aclMessage != null;
        if (isGameOver) {
            Constants.AGENT_MESSAGE contentObject = null;
            try {
                GameMessage gm = (GameMessage) aclMessage.getContentObject();
                contentObject = (Constants.AGENT_MESSAGE) gm.getMessage();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            if (contentObject == Constants.AGENT_MESSAGE.KILL) {
                // Se ha terminado la simulacion
                TimeoutAdapter.sendACKBack(aclMessage.getSender(), this.myAgent);
            }
        }
        return isGameOver;
    }

    private void sendJoePublicResponse(String guid, Constants.JOEPUBLIC_RESPONSE response) {
        Constants.ARQUITECT_MESSAGE type = null;
        if (response == Constants.JOEPUBLIC_RESPONSE.YES) {
            type = Constants.ARQUITECT_MESSAGE.CONVERT_JOEPUBLIC_TO_RESISTANCE;
        } else if (response == Constants.JOEPUBLIC_RESPONSE.NO) {
            // El agente de la resistencia deja en paz al joepublic
            type = Constants.ARQUITECT_MESSAGE.NOT_CONVERTED;
        } else if (response == Constants.JOEPUBLIC_RESPONSE.ORACULO) {
            type = Constants.ARQUITECT_MESSAGE.ORACULO_FOUND_RESISTANCE;
        }
        manipulateJoePublic(guid, type);
    }

    private void manipulateJoePublic(String guid, Constants.ARQUITECT_MESSAGE type) {
        ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
        aclMessage.addReceiver(arquitectAID);
        GameMessage gm = new GameMessage(type, guid);
        try {
            aclMessage.setContentObject(gm);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TimeoutAdapter.sendWithTimeout(aclMessage, this.myAgent, "JP DECISION TO ARCHITECT: " + type);
    }

    private Constants.JOEPUBLIC_RESPONSE getJoePublicResponse() {
        ACLMessage aclMessage = this.myAgent.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
        TimeoutAdapter.sendACKBack(aclMessage.getSender(), this.myAgent);
        Constants.JOEPUBLIC_RESPONSE response = null;
        try {
            GameMessage gm = (GameMessage) aclMessage.getContentObject();
            response = (Constants.JOEPUBLIC_RESPONSE) gm.getMessage();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        return response;
    }

    private void recluteAgent(String guid) {
        ACLMessage aclMessage = new ACLMessage(ACLMessage.PROPOSE);
        aclMessage.addReceiver(new AID(guid, AID.ISGUID));
        try {
            GameMessage gm = new GameMessage(Constants.AGENT_MESSAGE.RESISTANCE_RECRUITE);
            aclMessage.setContentObject(gm);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TimeoutAdapter.sendWithTimeout(aclMessage, this.myAgent, "RECLUTE AGENT " + guid);
    }

    private String getJoePublicID() {
        ACLMessage aclMessage = this.myAgent.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        TimeoutAdapter.sendACKBack(aclMessage.getSender(), this.myAgent);
        String joePublic = null;
        try {
            GameMessage gm = (GameMessage) aclMessage.getContentObject();
            joePublic = gm.getContent();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        return joePublic;
    }

    private void requestJoePublicAgent() {
        ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
        aclMessage.addReceiver(arquitectAID);
        try {
            GameMessage gm = new GameMessage(Constants.ARQUITECT_MESSAGE.GET_JOEPUBLIC_AGENT);
            aclMessage.setContentObject(gm);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TimeoutAdapter.sendWithTimeout(aclMessage, this.myAgent, "REQUEST JP TO ARCHITECT");
    }

    private AID anyoneWantsToBattle() {
        // TODO: Comprobar que no hay deadlocks
        ACLMessage aclMessage = this.myAgent.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE), WAITING_TIME);
        // aclMessage == null si no hay peticiones de batalla
        boolean wantToBattle = aclMessage != null;
        if (wantToBattle) {
            TimeoutAdapter.sendACKBack(aclMessage.getSender(), this.myAgent);
            return aclMessage.getSender();
        }
        return null;
    }

    private GameStatus getGameStatusMessage() {
        // Se espera la confirmacion de la petición
        ACLMessage aclMessage = this.myAgent.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        // Se ha recibido el mensaje
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
            GameMessage gm = new GameMessage(Constants.ARQUITECT_MESSAGE.GET_GAME_STATUS);
            aclMessage.setContentObject(gm);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TimeoutAdapter.sendWithTimeout(aclMessage, this.myAgent, "GAME STATUS TO ARCHITECT");
    }

    @Override
    public boolean done() {
        return endOfGame;
    }
}
