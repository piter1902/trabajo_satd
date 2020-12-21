package SMACompetitive;

import jade.core.Agent;

import java.util.Random;

public class AgenteJoePublic extends Agent implements AgenteSimulacion {

    // Probability to be converted to a resistance agent
    private int probabilityToConvertResistance;

    // Random Number generator
    private Random random;

    @Override
    protected void setup() {
        super.setup();
        random = new Random(System.nanoTime());
        this.probabilityToConvertResistance = random.nextInt(100);
    }
}
