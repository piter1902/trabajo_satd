package main.java.SMACompetitive;

public abstract class SimulationStats {

    // Variables to log
    // JP reclutados
    private int numberOfResistanceConversions;
    private int numberOfSystemConversions;
    // Numero de intentos de reclutamiento de JP+
    private int numberOfResistanceRecluitments;
    private int numberOfSystemRecluitments;
    private int numberOfRecluitments;
    // JP asesinados por el sistema
    private int numberOfSystemJPKills;
    // Numero de batallas
    private int numberOfBattles;
    // Numero de victorias en batallas
    private int numberOfResistanceWins;
    private int numberOfSystemWins;
    private int numberOfTies;
    // Numero de agentes de ambos bandos
    private int numberAgentsResistance;
    private int numberAgentsSystem;
    private int numberAgentsJoePublic;
    // Oraculo ha sido encontrado
    private boolean oraculoFound;
    private Constants.TEAM teamOraculoFound;
    private String nameOfOraculoDiscover;


    // Singleton
    private SimulationStats instance;

    private SimulationStats() {

    }

    public SimulationStats getInstance() {
        return instance;
    }

    // Getters y incrementers

    public int getNumberOfResistanceConversions() {
        return numberOfResistanceConversions;
    }

    public int getNumberOfSystemConversions() {
        return numberOfSystemConversions;
    }

    public int getNumberOfResistanceRecluitments() {
        return numberOfResistanceRecluitments;
    }

    public int getNumberOfSystemRecluitments() {
        return numberOfSystemRecluitments;
    }

    public int getNumberOfRecluitments() {
        return numberOfRecluitments;
    }

    public int getNumberOfSystemJPKills() {
        return numberOfSystemJPKills;
    }

    public int getNumberOfBattles() {
        return numberOfBattles;
    }

    public int getNumberOfResistanceWins() {
        return numberOfResistanceWins;
    }

    public int getNumberOfSystemWins() {
        return numberOfSystemWins;
    }

    public int getNumberOfTies() {
        return numberOfTies;
    }

    public int getNumberAgentsResistance() {
        return numberAgentsResistance;
    }

    public int getNumberAgentsSystem() {
        return numberAgentsSystem;
    }

    public int getNumberAgentsJoePublic() {
        return numberAgentsJoePublic;
    }

    public boolean isOraculoFound() {
        return oraculoFound;
    }

    public void setOraculoFound(boolean oraculoFound) {
        this.oraculoFound = oraculoFound;
    }

    public Constants.TEAM getTeamOraculoFound() {
        return teamOraculoFound;
    }

    public void setTeamOraculoFound(Constants.TEAM teamOraculoFound) {
        this.teamOraculoFound = teamOraculoFound;
    }

    public String getNameOfOraculoDiscover() {
        return nameOfOraculoDiscover;
    }

    public void setNameOfOraculoDiscover(String nameOfOraculoDiscover) {
        this.nameOfOraculoDiscover = nameOfOraculoDiscover;
    }
}
