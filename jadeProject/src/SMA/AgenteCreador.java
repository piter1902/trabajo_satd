package SMA;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AgenteCreador extends Agent {

    private Map<String, String> map;

    @Override
    protected void setup() {
        map = new HashMap<>();
        try {
            crearAgentes();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        super.setup();
    }

    private void crearAgentes() throws StaleProxyException {
        // TODO: Crear agentes y pasar por parametro a quien deben enviar cosas
        // Agentes PredictorScorer (MLP, J48, NaiveBayes)
        map.put("MLP_PS", generateNewAgent("MLP", AgentePredictorScorer.class, null));
        map.put("J48_PS", generateNewAgent("J48", AgentePredictorScorer.class, null));
        map.put("NBa_PS", generateNewAgent("Naive Bayes", AgentePredictorScorer.class, null));

        // Agentes de los modelos (entrenamiento con la primera particion)
        map.put("Normalizar_MLP", generateNewAgent("Normalizar_MLP", AgenteNormalizarMLP.class, map.get("MLP_PS")));
        // TODO: Meter los otros agentes aqui

        // Agente de normalizar la segunda particion
        map.put("AplicarNormalizado", generateNewAgent("AplicarNormalizado", AgenteNormalizar.class, map.get("MLP_PS")));

        // Agente de particion
        // Se le envia un map<String, list<String>> para denotar: partition1, partition2
        Map<String, String[]> argument = new HashMap<>();
        argument.put("partition1", generateList(map.get("Normalizar_MLP"))); //TODO: Incluir el resto
        argument.put("partition2", generateList(map.get("J48_PS"), map.get("NBa_PS"), map.get("AplicarNormalizado")));
        map.put("Partition", generateNewAgent("Partition", AgenteParticion.class, argument));

        // Agente de lectura de fichero
        map.put("Lectura", generateNewAgent("Lectura", AgenteArchivo.class, map.get("Partition")));
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
        ac.start();
        return ac.getName();
    }
}
