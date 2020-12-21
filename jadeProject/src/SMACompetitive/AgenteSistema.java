package SMACompetitive;

import jade.core.Agent;

import static SMACompetitive.Constants.INITIAL_BONUS;

public class AgenteSistema extends Agent implements AgenteSimulacion {

    private int bonus;

    @Override
    protected void setup() {
        super.setup();
        this.bonus = INITIAL_BONUS;
    }
}
