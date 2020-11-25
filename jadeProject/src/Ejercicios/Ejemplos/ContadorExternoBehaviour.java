package Ejercicios.Ejemplos;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;

public class ContadorExternoBehaviour extends Behaviour {

    private int contador = 12;
    private final Agent agent;

    public ContadorExternoBehaviour(Agent agent) {
        this.agent = agent;
    }

    @Override
    public void onStart() {
        System.out.format("El comportamiento tipo Behaviour del agente %s ha empezado", agent.getLocalName());
        super.onStart();
    }

    @Override
    public void action() {
        // Acciones a realizar (comportamiento)
        System.out.format("\t%d\n", contador);
        contador--;
    }

    @Override
    public boolean done() {
        // Condición de fin de comportamiento
        boolean fin = contador == 0;
        if (fin) {
            agent.doDelete();
        }
        return fin;
    }
}
