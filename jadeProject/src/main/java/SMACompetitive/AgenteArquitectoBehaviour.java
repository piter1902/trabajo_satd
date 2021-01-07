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
import java.util.*;

class AgenteArquitectoBehaviour extends SimpleBehaviour {

    private final static Logger log = LogManager.getLogger(AgenteArquitectoBehaviour.class);


    // Lista de main.java.agentes (las tuplas son: nombre de agente, direccionAID.string)
    private final Map<Constants.TEAM, Map<String, String>> agentMap;
    // Lista de los main.java.agentes que estan ocupados (en batalla o en reclutamiento)
    private final List<String> busyAgents;
    // Flag de fin del juego
    private boolean gameOver;
    // Flag de oraculo
    private boolean oraculoFound;

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

        Constants.ARQUITECT_MESSAGE message = null;
        try {
            message = (Constants.ARQUITECT_MESSAGE) aclMessage.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }

        if (message != null) {
            log.info("Mensaje recibido: " + message);
            switch (message) {
                case GET_GAME_STATUS:
                    // Devolver el estado del juego
                    getGameStatus(aclMessage, sender);
                    break;

                case GET_RESISTANCE_MATCH:
                    // Duelo con la resistencia
                    if (this.busyAgents.contains(sender.getName())) {
                        // El arquitecto ha emparejado a este agente con otro
                        agentInUse(sender);
                    } else {
                        getResistanceMatch(sender);
                    }
                    break;

                case GET_SYSTEM_MATCH:
                    // Duelo con el sistema
                    if (this.busyAgents.contains(sender.getName())) {
                        // El arquitecto ha emparejado a este agente con otro
                        agentInUse(sender);
                    } else {
                        getSystemMatch(sender);
                    }
                    break;

                case WIN:
                    // Alguien ha ganado la batalla [win()]
                    // TODO: Comprobar si esto funciona si no -> enviar una tupla
                    String agentName = aclMessage.getContent();
                    battleEnd(agentName, sender.getName());
                    break;

                case TIE:
                    // Ha habido empate [tie()]
                    // TODO: Comprobar si esto funciona si no -> enviar una tupla
                    agentName = aclMessage.getContent();
                    battleEndTie(agentName, sender.getName());
                    break;

                case GET_JOEPUBLIC_AGENT:
                    // Conseguir un agente tipo JoePublic
                    getJoePublic(sender);
                    break;

                case CONVERT_JOEPUBLIC_TO_RESISTANCE:
                    //Añadir el agente de tipo JoePublic a resistencia
                    // TODO: Comprobar si esto funciona si no -> enviar una tupla
                    agentName = aclMessage.getContent();
                    convertPublicToResistencia(agentName);
                    ACLMessage response;
                    break;

                case CONVERT_JOEPUBLIC_TO_SYSTEM:
                    // Añadir el agente de tipo JoePublic a sistema
                    // TODO: Comprobar si esto funciona si no -> enviar una tupla
                    agentName = aclMessage.getContent();
                    convertPublicToSystem(agentName);
                    break;

                case NOT_CONVERTED:
                    // Los main.java.agentes de la resistencia no matan a los main.java.agentes JoePublic. Se libera
                    agentName = aclMessage.getContent();
                    // Lo liberamos
                    busyAgents.remove(agentName);
                    break;

                case ORACULO_FOUND_RESISTANCE:
                    // El equipo RESISTANCE ha encontrado al oraculo
                    // TODO: Comprobar si esto funciona si no -> enviar una tupla
                    agentName = aclMessage.getContent();
                    oraculoFound(agentName, Constants.TEAM.RESISTANCE);
                    break;

                case ORACULO_FOUND_SYSTEM:
                    // El equipo SYSTEM ha encontrado al oraculo
                    // TODO: Comprobar si esto funciona si no -> enviar una tupla
                    agentName = aclMessage.getContent();
                    oraculoFound(agentName, Constants.TEAM.SYSTEM);
                    break;

                case KILL_JOEPUBLIC:
                    // Mensaje de agente del sistema que indica que se debe eliminar un JoePublic
                    agentName = aclMessage.getContent();
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
        if (gameOver) {
            // TODO: Hacer cosas (o creamos otro behaviour?)
            gameOver();
        }
    }

    private void agentInUse(AID sender) {
        ACLMessage response = new ACLMessage(ACLMessage.CANCEL);
        response.addReceiver(sender);
        try {
            response.setContentObject(Constants.AGENT_MESSAGE.IS_IN_USE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TimeoutAdapter.sendWithTimeout(response, this.myAgent);
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
            response.setContentObject(Constants.AGENT_MESSAGE.KILL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TimeoutAdapter.sendWithTimeout(response, this.myAgent);
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
                response.setContentObject(Constants.AGENT_MESSAGE.ADD_BONUS_ORACULO);
            } catch (IOException e) {
                e.printStackTrace();
            }
            TimeoutAdapter.sendWithTimeout(response, this.myAgent);
        }
    }

    private void convertPublicToSystem(String agentName) {
        ACLMessage response;
        convertJoePublic(agentName, Constants.TEAM.SYSTEM);
        // TODO: Se ha modificado el tipo de mensaje a PROPOSE ya que JP solo espera ese tipo
        response = new ACLMessage(ACLMessage.PROPOSE);
        response.addReceiver(new AID(agentName, AID.ISGUID));
        try {
            response.setContentObject(Constants.AGENT_MESSAGE.CONVERT_TO_SYSTEM);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TimeoutAdapter.sendWithTimeout(response, this.myAgent);
    }

    private void convertPublicToResistencia(String agentName) {
        convertJoePublic(agentName, Constants.TEAM.RESISTANCE);
        // TODO: Se ha modificado el tipo de mensaje a PROPOSE ya que JP solo espera ese tipo
        ACLMessage response = new ACLMessage(ACLMessage.PROPOSE);
        response.addReceiver(new AID(agentName, AID.ISGUID));
        try {
            response.setContentObject(Constants.AGENT_MESSAGE.CONVERT_TO_RESISTANCE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TimeoutAdapter.sendWithTimeout(response, this.myAgent);
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
        response.setContent(joePublicAgent);
        TimeoutAdapter.sendWithTimeout(response, this.myAgent);
    }

    private void battleEnd(String agentName, String senderName) {
        // Se elimina al agente que ha sido ganado
        killAgent(agentName);
        // Se desbloquean los main.java.agentes que estaban como ocupados
        busyAgents.remove(agentName);
        busyAgents.remove(senderName);
    }

    private void battleEndTie(String agentName, String senderName) {
        // Se desbloquean los main.java.agentes que estaban como ocupados
        busyAgents.remove(agentName);
        busyAgents.remove(senderName);
    }

    private void killAgent(String agentName) {
        ACLMessage killMessage = new ACLMessage(ACLMessage.INFORM);
        try {
            killMessage.setContentObject(Constants.AGENT_MESSAGE.KILL);
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
        TimeoutAdapter.sendWithTimeout(killMessage, this.myAgent);
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
        }
    }

    private void getResistanceMatch(AID sender) {
        String resistanceAgent = lookupAgent(Constants.TEAM.RESISTANCE);
        if (resistanceAgent != null) {
            // Add to busy list
            this.busyAgents.add(resistanceAgent);
            this.busyAgents.add(sender.getName());
            getAgentMatch(sender, resistanceAgent);
        }
    }

    private void getAgentMatch(AID sender, String resistanceAgent) {
        ACLMessage response = new ACLMessage(ACLMessage.REQUEST);
        response.addReceiver(sender);
        response.setContent(resistanceAgent);
        TimeoutAdapter.sendWithTimeout(response, this.myAgent);
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
            agentName = agents.get(i).getValue();
            if (!busyAgents.contains(agentName)) {
                // No esta ocupado. Nos vale.
                break;
            }
            // Esta ocupado. Repetimos
        }
        return agentName;
    }

    private void getGameStatus(ACLMessage aclMessage, AID sender) {
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
        TimeoutAdapter.sendWithTimeout(aclMessage, this.myAgent);
    }

    @Override
    public boolean done() {
        return gameOver;
    }
}
