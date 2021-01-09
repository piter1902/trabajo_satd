package main.java.SMACompetitive;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import main.java.Timeout.TimeoutAdapter;

import java.util.*;
import java.util.logging.Logger;


public class AgenteArquitecto extends Agent {

    private final static Logger log = Logger.getLogger(AgenteArquitecto.class.getName());

    private String architectName;

    private static final int NUMBER_JOEPUBLIC = 10;

    //  System Agents
    private Map<String, String> systemMap;
    // Resistance Agents
    private Map<String, String> resistanceMap;
    // JoePublic Agents
    private Map<String, String> joePublicMap;

    @Override
    protected void setup() {
        super.setup();
        systemMap = new HashMap<>();
        resistanceMap = new HashMap<>();
        joePublicMap = new HashMap<>();
        // Obtain architect name
        this.architectName = getName();
        log.info("Nombre de arquitecto: " + architectName);
        try {
            crearAgentes();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        // Agents barrier
        sendStartMessageToAgents();
        this.addBehaviour(new AgenteArquitectoBehaviour(resistanceMap, systemMap, joePublicMap));
    }

    /**
     * Starts the Agent barrier
     */
    private void sendStartMessageToAgents() {
        ACLMessage aclMessage = new ACLMessage(ACLMessage.PROPAGATE);
        List<String> agentsName = getAllAgentsName();
        log.info("Opening barrier: " + agentsName.toString());

        for (String name : agentsName) {
            aclMessage.addReceiver(new AID(name, AID.ISGUID));
        }
        TimeoutAdapter.sendWithTimeout(aclMessage, this, "OPENING BARRIER");
    }

    /**
     * Obtains and mix all agents name
     *
     * @return List that contains all agents name (shuffled)
     */
    private List<String> getAllAgentsName() {
        List<String> names = new ArrayList<>();
        // Obtain system agents
        for (String key : systemMap.keySet()) {
            names.add(systemMap.get(key));
        }
        // Obtain resistance agents
        for (String key : resistanceMap.keySet()) {
            names.add(resistanceMap.get(key));
        }
        // Obtain JP agents
        for (String key : joePublicMap.keySet()) {
            names.add(joePublicMap.get(key));
        }
        Collections.shuffle(names);
        return names;
    }

    private void crearAgentes() throws StaleProxyException {
        log.info("Comienza creacion de agentes ... ");
        createResistanceAgents();
        createSystemAgents();
        createJoePublicAgents();
    }


    private void createJoePublicAgents() throws StaleProxyException {
        // Create NUMBER_JOEPUBLIC - 1 Joe Public agents
        for (int i = 0; i < NUMBER_JOEPUBLIC - 1; i++) {
            joePublicMap.put(String.format("JP%s_KY", i), generateNewAgent("JP" + i, AgenteJoePublic.class, architectName));
        }
        // One of them will be the oracle
        joePublicMap.put("Oracle_KY", generateNewAgent("Oracle", AgenteOraculo.class, architectName));
    }

    private void createSystemAgents() throws StaleProxyException {
        systemMap.put(Constants.SMITH_NAME, generateNewAgent(Constants.SMITH_NAME, AgenteSmith.class, Constants.SMITH_NAME, architectName));
        systemMap.put("Torrente_KY", generateNewAgent("Torrente", AgenteSistema.class, "Torrente", architectName));
        systemMap.put("Terminator_KY", generateNewAgent("Terminator", AgenteSistema.class, "Terminator", architectName));
    }

    private void createResistanceAgents() throws StaleProxyException {
        resistanceMap.put(Constants.NEO_NAME, generateNewAgent(Constants.NEO_NAME, AgenteNeo.class, Constants.NEO_NAME, architectName));
        resistanceMap.put("Morfeo_KY", generateNewAgent("Morfeo", AgenteResistencia.class, "Morfeo", architectName));
        resistanceMap.put("Triniti_KY", generateNewAgent("Triniti", AgenteResistencia.class, "Triniti", architectName));

    }

    private String[] generateList(String... list) {
        return list;
    }

    /**
     * Returns GlobalName of created Agent
     *
     * @param name        of created agent
     * @param classObject of Agent
     * @param args        to pass to created agent
     * @return Global name of created agent
     * @throws StaleProxyException
     */
    private String generateNewAgent(String name, Class classObject, Object... args) throws StaleProxyException {
        ContainerController cc = getContainerController();
        String agentName = name != null ? name : UUID.randomUUID().toString();
        // TODO: El nombre se pasa en los argumentos?? Creo que no
        AgentController ac = cc.createNewAgent(agentName, classObject.getName(), args);
        ac.start();
        return ac.getName();
    }

}
