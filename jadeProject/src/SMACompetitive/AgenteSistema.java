package SMACompetitive;

import jade.core.Agent;

import static SMACompetitive.Constants.INITIAL_BONUS;

public class AgenteSistema extends Agent implements AgenteSimulacion {

    private int bonus;

    private String name;

    @Override
    protected void setup() {
        super.setup();
        this.name = (String) getArguments()[0];
        this.bonus = INITIAL_BONUS;
    }

    protected void recalcBonus(int bonus) {
        // TODO: Recalc bonus
    }
}
