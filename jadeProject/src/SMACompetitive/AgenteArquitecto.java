package SMACompetitive;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.*;

public class AgenteArquitecto extends Agent {

    @Override
    protected void setup() {
        try {
            crearAgentes();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        super.setup();
        this.addBehaviour(new AgenteArquitectoBehaviour(null, null, null));
    }

    private void crearAgentes() throws StaleProxyException {
        // TODO: Crear agentes y pasar por parametro a quien deben enviar cosas
        // TODO: Create ResistanceAgents
        // TODO: Create SystemAgents
        // TODO: Create JoePublicAgents
        // TODO: Create Oraculo

        // TODO: Start simulation
        // TODO: AgentController.start()
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
        // TODO: Crear con los containers
        ContainerController cc = getContainerController();
        String agentName = name != null ? name : UUID.randomUUID().toString();
        AgentController ac = cc.createNewAgent(agentName, classObject.getName(), args);
        // TODO: De esta manera tienen ventaja los agentes creados primero.
        // TODO: Posible implementación de barrera
        ac.start();
        return ac.getName();
    }

}
