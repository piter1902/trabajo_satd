package Ejercicios.Ejemplos;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;

public class ContadorExternoCyclicBehaviour extends CyclicBehaviour {
    private int contador = 12;

    private final Agent agent;

    public ContadorExternoCyclicBehaviour(Agent agent) {
        super();
        this.agent = agent;
    }

    @Override
    public void onStart() {
        System.out.format("El comportamiento tipo CyclicBehaviour del agente %s ha empezado", agent.getLocalName());
        super.onStart();
    }

    @Override
    public void action() {
        // Acciones a realizar
        System.out.println("\t" + contador);
        contador--;
        if (contador == 0) {
            agent.doDelete();
        }
    }



}
