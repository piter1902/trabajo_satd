package SMACompetitive;

public class Constants {
    public static final int INITIAL_BONUS = 50;
    public static final int MAX_BONUS = 90;
    public static final int MAX_BONUS_NEO_SMITH = 99;

    // Equipos
    public enum TEAM {
        RESISTANCE, SYSTEM, JOEPUBLIC
    }

    // Tipos de mensaje para el arquitecto
    public enum ARQUITECT_MESSAGE {
        GET_GAME_STATUS, GET_RESISTANCE_MATCH, GET_SYSTEM_MATCH,
        WIN, TIE, GET_JOEPUBLIC_AGENT, CONVERT_JOEPUBLIC, ORACULO_FOUND
    }

    // Tipos de mensaje entre los agentes
    public enum AGENT_MESSAGE {
        KILL
    }
}
