package SMACompetitive;

import jade.core.Agent;

import static SMACompetitive.Constants.INITIAL_BONUS;

public class AgenteResistencia extends Agent implements AgenteSimulacion {

    private int bonus;

    private String name;

    @Override
    protected void setup() {
        super.setup();
        Object[] args = getArguments();
        this.name = (String) args[0];
        this.bonus = INITIAL_BONUS;
    }

    protected void recalcBonus(int bonus) {
        // TODO: Recalc bonus
    }
}
