package main.java.SMACompetitive;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import main.java.Timeout.TimeoutAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;

class AgenteArquitectoBehaviour extends SimpleBehaviour {

    private static Logger log = null;
    static {
        InputStream stream = AgenteArquitectoBehaviour.class.getClassLoader().
                getResourceAsStream("main/resources/logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(stream);
            log = Logger.getLogger(AgenteArquitectoBehaviour.class.getName());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Lista de main.java.agentes (las tuplas son: nombre de agente, direccionAID.string)
    private final Map<Constants.TEAM, Map<String, String>> agentMap;
    // Lista de los main.java.agentes que estan ocupados (en batalla o en reclutamiento)
    private final List<String> busyAgents;
    // Flag de fin del juego
    private boolean gameOver;
    // Flag de oraculo
    private final boolean oraculoFound;
    //Stats singleton
    private final SimulationStats stats = SimulationStats.getInstance();

    public AgenteArquitectoBehaviour(Map<String, String> agentsResistencia,
                                     Map<String, String> agentsSistema,
                                     Map<String, String> agentsJoePublic) {
        gameOver = false;
        oraculoFound = false;

        agentMap = new HashMap<>();
        agentMap.put(Constants.TEAM.RESISTANCE, agentsResistencia);
        agentMap.put(Constants.TEAM.SYSTEM, agentsSistema);
        agentMap.put(Constants.TEAM.JOEPUBLIC, agentsJoePublic);

        busyAgents = new ArrayList<>();
    }

    @Override
    public void action() {
        // Solo se procesan las peticiones de acciones (REQUEST)
        ACLMessage aclMessage = this.myAgent.blockingReceive(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        AID sender = aclMessage.getSender();
        TimeoutAdapter.sendACKBack(sender, this.myAgent);

        GameMessage gameMessage = null;
        try {
            gameMessage = (GameMessage) aclMessage.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }

        if (gameMessage != null) {
            Constants.ARQUITECT_MESSAGE message = (Constants.ARQUITECT_MESSAGE) gameMessage.getMessage();
            log.info("Mensaje recibido: " + message + " por -------> " + sender);
            switch (message) {
                case GET_GAME_STATUS:
                    // Devolver el estado del juego
                    stats.increasNumberOfGetGameStatus();
                    getGameStatus(sender);
                    break;

                case GET_RESISTANCE_MATCH:
                    // Duelo con la resistencia
                    stats.increaseNumberOfBattlesSystem();
                    if (this.busyAgents.contains(sender.getName())) {
                        // El arquitecto ha emparejado a este agente con otro
                        agentInUse(sender);
                    } else {
                        getResistanceMatch(sender);
                    }
                    break;

                case GET_SYSTEM_MATCH:
                    // Duelo con el sistema
                    stats.increaseNumberOfBattlesResistance();
                    if (this.busyAgents.contains(sender.getName())) {
                        // El arquitecto ha emparejado a este agente con otro
                        agentInUse(sender);
                    } else {
                        getSystemMatch(sender);
                    }
                    break;

                case WIN:
                    // Alguien ha ganado la batalla [win()]
                    String winner = sender.getName();
                    String loser = gameMessage.getContent();
                    log.info("WIN CASE - winner: " + winner + " loser: " + loser);
                    // Obtain the team of the winner
                    if (agentMap.get(Constants.TEAM.RESISTANCE).containsValue(winner)) {
                        stats.increaseNumberOfResistanceWins();
                    } else {
                        stats.increaseNumberOfSystemWins();
                    }
                    battleEnd(loser, winner);
                    break;

                case DEFEAT:
                    // Alguien ha perdido la batalla [defeat()]
                    winner = gameMessage.getContent();
                    loser = sender.getName();
                    log.info("DEFEAT CASE - winner: " + winner + " loser: " + loser);

                    // Obtain the team of the winner
                    if (agentMap.get(Constants.TEAM.RESISTANCE).containsValue(winner)) {
                        stats.increaseNumberOfResistanceWins();
                    } else {
                        stats.increaseNumberOfSystemWins();
                    }
                    battleEnd(loser, winner);
                    break;

                case TIE:
                    // Ha habido empate [tie()]
                    stats.increaseNumberOfTies();
                    String agentName = gameMessage.getContent();
                    log.info("TIE CASE: " + agentName + " VS " + sender.getName());
                    battleEndTie(agentName, sender.getName());
                    break;

                case GET_JOEPUBLIC_AGENT:
                    // Conseguir un agente tipo JoePublic
                    getJoePublic(sender);
                    break;

                case CONVERT_JOEPUBLIC_TO_RESISTANCE:
                    //Añadir el agente de tipo JoePublic a resistencia
                    stats.increaseNumberOfResistanceConversions();
                    agentName = gameMessage.getContent();
                    convertPublicToResistencia(agentName);
                    ACLMessage response;
                    break;

                case CONVERT_JOEPUBLIC_TO_SYSTEM:
                    // Añadir el agente de tipo JoePublic a sistema
                    stats.increaseNumberOfSystemConversions();
                    agentName = gameMessage.getContent();
                    convertPublicToSystem(agentName);
                    break;

                case NOT_CONVERTED:
                    // Los main.java.agentes de la resistencia no matan a los main.java.agentes JoePublic. Se libera
                    agentName = gameMessage.getContent();
                    // Lo liberamos
                    busyAgents.remove(agentName);
                    break;

                case ORACULO_FOUND_RESISTANCE:
                    // El equipo RESISTANCE ha encontrado al oraculo
                    stats.setOraculoFound(true);
                    stats.setTeamOraculoFound(Constants.TEAM.RESISTANCE);
                    stats.setNameOfOraculoDiscover(sender.getName());
                    agentName = gameMessage.getContent();
                    oraculoFound(agentName, Constants.TEAM.RESISTANCE);
                    break;

                case ORACULO_FOUND_SYSTEM:
                    // El equipo SYSTEM ha encontrado al oraculo
                    stats.setOraculoFound(true);
                    stats.setTeamOraculoFound(Constants.TEAM.SYSTEM);
                    stats.setNameOfOraculoDiscover(sender.getName());
                    agentName = gameMessage.getContent();
                    oraculoFound(agentName, Constants.TEAM.SYSTEM);
                    break;

                case KILL_JOEPUBLIC:
                    // Mensaje de agente del sistema que indica que se debe eliminar un JoePublic
                    stats.increaseNumberOfSystemJPKills();
                    agentName = gameMessage.getContent();
                    removeAgentFromMap(agentName, Constants.TEAM.JOEPUBLIC);
                    busyAgents.remove(agentName);
                    killAgent(agentName);
                    break;

                default:
                    // Error?
                    System.err.println("Mensaje no reconocido: " + message);
                    break;
            }
        } else {
            // message is null -> Error en el mensaje -> Se descarta
        }
        // Calculo del nuevo estado (si el numero de main.java.agentes de alguno de los dos bandos es 0)
        gameOver = agentMap.get(Constants.TEAM.RESISTANCE).entrySet().size() == 0
                || agentMap.get(Constants.TEAM.SYSTEM).entrySet().size() == 0;
        // TODO: Cambiar esto por lo comentado de arriba
//        gameOver = agentMap.get(Constants.TEAM.JOEPUBLIC).entrySet().size() == 0;
        if (gameOver) {
            gameOver();
            // Se muestran resultados
            printMatrixStats();
        }
    }

    /**
     * Muestra estadísticas del juego a partir del singletone SimulationStat
     */
    private void printMatrixStats() {
        // Estos valores se deben aplicar al finalizar el juego
        stats.setNumberAgentsJoePublic(agentMap.get(Constants.TEAM.JOEPUBLIC).size());
        stats.setNumberAgentsResistance(agentMap.get(Constants.TEAM.RESISTANCE).size());
        stats.setNumberAgentsSystem(agentMap.get(Constants.TEAM.SYSTEM).size());
        stats.printStats();
        stats.showGUI();
        stats.reset();
    }

    private void agentInUse(AID sender) {
        ACLMessage response = new ACLMessage(ACLMessage.CANCEL);
        response.addReceiver(sender);
        try {
            GameMessage gm = new GameMessage(Constants.AGENT_MESSAGE.IS_IN_USE);
            response.setContentObject(gm);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TimeoutAdapter.sendWithTimeout(response, this.myAgent, "AGENT IN USE TO " + sender.getName());
    }

    /**
     * Se elimina a todos los main.java.agentes existentes
     */
    private void gameOver() {
        ACLMessage response = new ACLMessage(ACLMessage.INFORM);
        if (agentMap.get(Constants.TEAM.RESISTANCE).entrySet().size() == 0) {
            // En este caso, ha ganado el sistema
            for (String agentName : agentMap.get(Constants.TEAM.SYSTEM).values()) {
                response.addReceiver(new AID(agentName, AID.ISGUID));
            }
        } else {
            // En caso contrario, ha ganado la resistencia
            for (String agentName : agentMap.get(Constants.TEAM.RESISTANCE).values()) {
                response.addReceiver(new AID(agentName, AID.ISGUID));
            }
        }
        // También se debe comprobar que no quede ningún agente de tipo JoePublic
        for (String agentName : agentMap.get(Constants.TEAM.JOEPUBLIC).values()) {
            response.addReceiver(new AID(agentName, AID.ISGUID));
        }
        try {
            GameMessage gm = new GameMessage(Constants.AGENT_MESSAGE.KILL);
            response.setContentObject(gm);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TimeoutAdapter.sendWithTimeout(response, this.myAgent, "GAME OVER ...");
    }

    private void oraculoFound(String agentName, Constants.TEAM team) {
        ACLMessage response;
        removeAgentFromMap(agentName, Constants.TEAM.JOEPUBLIC);
        busyAgents.remove(agentName);
        killAgent(agentName);
        String lider = null;
        switch (team) {
            case RESISTANCE:
                lider = agentMap.get(Constants.TEAM.RESISTANCE).get(Constants.NEO_NAME);
                break;
            case SYSTEM:
                lider = agentMap.get(Constants.TEAM.SYSTEM).get(Constants.SMITH_NAME);
                break;
        }

        if (lider != null) {
            response = new ACLMessage(ACLMessage.AGREE);
            response.addReceiver(new AID(lider, AID.ISGUID));
            try {
                GameMessage gm = new GameMessage(Constants.AGENT_MESSAGE.ADD_BONUS_ORACULO);
                response.setContentObject(gm);
            } catch (IOException e) {
                e.printStackTrace();
            }
            TimeoutAdapter.sendWithTimeout(response, this.myAgent, "ORACLE FOUND ...");
        }
    }

    private void convertPublicToSystem(String agentName) {
        ACLMessage response;
        convertJoePublic(agentName, Constants.TEAM.SYSTEM);
        // TODO: Se ha modificado el tipo de mensaje a PROPOSE ya que JP solo espera ese tipo
        response = new ACLMessage(ACLMessage.PROPOSE);
        response.addReceiver(new AID(agentName, AID.ISGUID));
        try {
            GameMessage gm = new GameMessage(Constants.AGENT_MESSAGE.CONVERT_TO_SYSTEM);
            response.setContentObject(gm);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TimeoutAdapter.sendWithTimeout(response, this.myAgent, "CONVERT PUBLIC TO SYSTEM TO " + agentName);
    }

    private void convertPublicToResistencia(String agentName) {
        convertJoePublic(agentName, Constants.TEAM.RESISTANCE);
        // TODO: Se ha modificado el tipo de mensaje a PROPOSE ya que JP solo espera ese tipo
        ACLMessage response = new ACLMessage(ACLMessage.PROPOSE);
        response.addReceiver(new AID(agentName, AID.ISGUID));
        try {
            GameMessage gm = new GameMessage(Constants.AGENT_MESSAGE.CONVERT_TO_RESISTANCE);
            response.setContentObject(gm);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TimeoutAdapter.sendWithTimeout(response, this.myAgent, "CONVERT PUBLIC TO RESISTANCE TO " + agentName);
    }

    private void convertJoePublic(String JPName, Constants.TEAM team) {
        removeAgentFromMap(JPName, Constants.TEAM.JOEPUBLIC);
        busyAgents.remove(JPName);
        addAgentToMap(JPName, team);
    }

    private void getJoePublic(AID sender) {
        String joePublicAgent = lookupAgent(Constants.TEAM.JOEPUBLIC);
        this.busyAgents.add(joePublicAgent);
        ACLMessage response = new ACLMessage(ACLMessage.REQUEST);
        response.addReceiver(sender);
        GameMessage gm = new GameMessage(null, joePublicAgent);
        try {
            response.setContentObject(gm);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TimeoutAdapter.sendWithTimeout(response, this.myAgent, "GET JOEPULIC FROM " + sender.getName());
    }

    /**
     * Funcion que libera a los agentes de la batalla. Elimina al agente perdedor.
     *
     * @param defeated agente perdedor de la batalla.
     * @param winner   agente ganador de la batalla.
     */
    private void battleEnd(String defeated, String winner) {
        // Se elimina al agente que ha sido ganado
        killAgent(defeated);
        // Se desbloquean los main.java.agentes que estaban como ocupados
        busyAgents.remove(defeated);
        busyAgents.remove(winner);
    }

    private void battleEndTie(String agentName, String senderName) {
        // Se desbloquean los main.java.agentes que estaban como ocupados
        busyAgents.remove(agentName);
        busyAgents.remove(senderName);
    }

    private void killAgent(String agentName) {
        ACLMessage killMessage = new ACLMessage(ACLMessage.INFORM);
        try {
            GameMessage gm = new GameMessage(Constants.AGENT_MESSAGE.KILL);
            killMessage.setContentObject(gm);
        } catch (IOException e) {
            e.printStackTrace();
        }
        killMessage.addReceiver(new AID(agentName, AID.ISGUID));
        // Se elimina el agente de la lista de main.java.agentes
        if (agentMap.get(Constants.TEAM.RESISTANCE).containsValue(agentName)) {
            removeAgentFromMap(agentName, Constants.TEAM.RESISTANCE);
        } else if (agentMap.get(Constants.TEAM.SYSTEM).containsValue(agentName)) {
            removeAgentFromMap(agentName, Constants.TEAM.SYSTEM);
        }
        // Se envia el mensaje
        TimeoutAdapter.sendWithTimeout(killMessage, this.myAgent, "KILL AGENT " + agentName);
    }

    private void removeAgentFromMap(String agentName, Constants.TEAM team) {
        for (String key : agentMap.get(team).keySet()) {
            if (agentMap.get(team).get(key).equals(agentName)) {
                // Es el que buscamos. Lo eliminamos
                agentMap.get(team).remove(key);
                break;
            }
        }
    }

    private void addAgentToMap(String agentName, Constants.TEAM team) {
        agentMap.get(team).put(UUID.randomUUID().toString(), agentName);
    }

    private void getSystemMatch(AID sender) {
        String systemAgent = lookupAgent(Constants.TEAM.SYSTEM);
        if (systemAgent != null) {
            // Add to busy list
            this.busyAgents.add(systemAgent);
            this.busyAgents.add(sender.getName());
            getAgentMatch(sender, systemAgent);
        } else {
            // else ->  No tengo oponente para luchar :(
            noOpponentToBattle(sender);
        }
    }

    private void getResistanceMatch(AID sender) {
        String resistanceAgent = lookupAgent(Constants.TEAM.RESISTANCE);
        if (resistanceAgent != null) {
            // Add to busy list
            this.busyAgents.add(resistanceAgent);
            this.busyAgents.add(sender.getName());
            getAgentMatch(sender, resistanceAgent);
        } else {
            // else ->  No tengo oponente para luchar :(
            noOpponentToBattle(sender);
        }
    }

    private void noOpponentToBattle(AID sender) {
        // No hay oponente
        ACLMessage aclMessage = new ACLMessage(ACLMessage.CANCEL);
        GameMessage gm = new GameMessage(null, null);
        aclMessage.addReceiver(sender);
        try {
            aclMessage.setContentObject(gm);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TimeoutAdapter.sendWithTimeout(aclMessage, this.myAgent, "NO HAY OPONENTE PARA LUCHAR");
    }

    private void getAgentMatch(AID sender, String resistanceAgent) {
        ACLMessage response = new ACLMessage(ACLMessage.REQUEST);
        response.addReceiver(sender);
        GameMessage gm = new GameMessage(null, resistanceAgent);
        try {
            response.setContentObject(gm);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TimeoutAdapter.sendWithTimeout(response, this.myAgent, "GET AGENT MATCH " + sender.getName());
    }

    private String lookupAgent(Constants.TEAM team) {
        String agentName = null;
        List<Map.Entry<String, String>> agents = new ArrayList<>();
        switch (team) {
            case RESISTANCE:
                agents = new ArrayList<>(agentMap.get(Constants.TEAM.RESISTANCE).entrySet());
                break;

            case SYSTEM:
                agents = new ArrayList<>(agentMap.get(Constants.TEAM.SYSTEM).entrySet());
                break;

            case JOEPUBLIC:
                agents = new ArrayList<>(agentMap.get(Constants.TEAM.JOEPUBLIC).entrySet());
                break;

            default:
                break;
        }
        Collections.shuffle(agents);
        for (int i = 0; i < agents.size(); i++) {
            String posibleAgentName = agents.get(i).getValue();
            if (!busyAgents.contains(posibleAgentName)) {
                // No esta ocupado. Nos vale.
                agentName = posibleAgentName;
                break;
            }
            // Esta ocupado. Repetimos
        }
        return agentName;
    }

    private void getGameStatus(AID sender) {
        int nResis = agentMap.get(Constants.TEAM.RESISTANCE).entrySet().size();
        int nSist = agentMap.get(Constants.TEAM.SYSTEM).entrySet().size();
        int nJoeP = agentMap.get(Constants.TEAM.JOEPUBLIC).entrySet().size();
        GameStatus gameStatus = new GameStatus(this.oraculoFound, nResis, nSist, nJoeP);
        ACLMessage response = new ACLMessage(ACLMessage.REQUEST);
        response.addReceiver(sender);
        try {
            response.setContentObject(gameStatus);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TimeoutAdapter.sendWithTimeout(response, this.myAgent, "SEND GAME STATUS TO " + sender.getName());
    }

    @Override
    public boolean done() {
        return gameOver;
    }
}
