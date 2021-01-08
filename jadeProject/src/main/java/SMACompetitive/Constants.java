package main.java.SMACompetitive;

public class Constants {
    public static final int INITIAL_BONUS = 50;
    public static final int MAX_BONUS = 90;
    public static final int MAX_BONUS_NEO_SMITH = 99;

    // Equipos
    public enum TEAM {
        RESISTANCE, SYSTEM, JOEPUBLIC
    }

    // Tipos de mensaje para el arquitecto
    public enum ARQUITECT_MESSAGE implements GAME_MESSAGE {
        GET_GAME_STATUS, GET_RESISTANCE_MATCH, GET_SYSTEM_MATCH,
        WIN, TIE, GET_JOEPUBLIC_AGENT, CONVERT_JOEPUBLIC_TO_RESISTANCE, CONVERT_JOEPUBLIC_TO_SYSTEM, NOT_CONVERTED,
        ORACULO_FOUND_RESISTANCE, ORACULO_FOUND_SYSTEM, KILL_JOEPUBLIC
    }

    // Tipos de mensaje entre los main.java.agentes
    public enum AGENT_MESSAGE implements GAME_MESSAGE {
        KILL, RESISTANCE_RECRUITE, SYSTEM_RECRUITE, CONVERT_TO_RESISTANCE, CONVERT_TO_SYSTEM, ADD_BONUS_ORACULO, BATTLE, IS_IN_USE
    }

    // Tipos de respuesta del agente JoePublic
    public enum JOEPUBLIC_RESPONSE implements GAME_MESSAGE {
        YES, NO, ORACULO
    }

    // Tipos de respuesta en las batallas
    public enum BATTLE_RESPONSE implements GAME_MESSAGE {
        WIN, DEFEAT, TIE
    }

    // Interfaz comun de los mensajes
    public interface GAME_MESSAGE {
    }

    public static final String NEO_NAME = "Neo";
    public static final String SMITH_NAME = "Smith";
}
