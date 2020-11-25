package Ejercicios.Ejemplos;

import jade.core.Agent;

public class AgentTry extends Agent {

    @Override
    protected void setup() {
        System.out.format("Hola, soy el agente %s\n", getLocalName());

        addBehaviour(new ContadorExternoCyclicBehaviour(this));

        super.setup();
    }

    @Override
    protected void takeDown() {
        System.out.format("El agente %s muere\n", getLocalName());
        super.takeDown();
    }

}
