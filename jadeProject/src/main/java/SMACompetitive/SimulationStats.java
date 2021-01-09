package main.java.SMACompetitive;

public class SimulationStats {

    // Variables to log
    // Número de peticiones del estado del juego
    // TODO:puede ser interesante separarlo en RESISTANCE | SYSTEM
    private int numberOfGetGameStatus;
    // JP reclutados
    private int numberOfResistanceConversions;
    private int numberOfSystemConversions;
    // Numero de intento de reclutamiento de JP+
    private int numberOfResistanceRecluitments;
    private int numberOfSystemRecluitments;
    // JP asesinados por el sistema
    private int numberOfSystemJPKills;
    // Numero de batallas solicitadas por resistencia
    private int numberOfBattlesResistance;
    // Numero de batallas solicitadas por sistema
    private int numberOfBattlesSystem;
    // Numero de victorias en batallas
    private int numberOfResistanceWins;
    private int numberOfSystemWins;
    private int numberOfTies;
    // Numero de agentes de ambos bandos
    private int numberAgentsResistance;
    private int numberAgentsSystem;
    private int numberAgentsJoePublic;
    // Oraculo ha sido encontrado
    private boolean oraculoFound = false;
    private Constants.TEAM teamOraculoFound;
    private String nameOfOraculoDiscover;


    // Singleton
    private static final SimulationStats instance = new SimulationStats();

    private SimulationStats() {
    }

    public static SimulationStats getInstance() {
        return instance;
    }

    // Getters
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

    public int getNumberOfSystemJPKills() {
        return numberOfSystemJPKills;
    }

    public int getNumberOfBattlesResistance() {
        return numberOfBattlesResistance;
    }

    public int getNumberOfBattlesSystem() {
        return numberOfBattlesSystem;
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

    public Constants.TEAM getTeamOraculoFound() {
        return teamOraculoFound;
    }

    public String getNameOfOraculoDiscover() {
        return nameOfOraculoDiscover;
    }

    public boolean isOraculoFound() {
        return oraculoFound;
    }

    public int getNumberOfGetGameStatus() {
        return numberOfGetGameStatus;
    }

    //Setters

    public void setOraculoFound(boolean oraculoFound) {
        this.oraculoFound = oraculoFound;
    }

    public void setTeamOraculoFound(Constants.TEAM teamOraculoFound) {
        this.teamOraculoFound = teamOraculoFound;
    }

    public void setNameOfOraculoDiscover(String nameOfOraculoDiscover) {
        this.nameOfOraculoDiscover = nameOfOraculoDiscover;
    }

    public void setNumberAgentsResistance(int numberAgentsResistance) {
        this.numberAgentsResistance = numberAgentsResistance;
    }

    public void setNumberAgentsSystem(int numberAgentsSystem) {
        this.numberAgentsSystem = numberAgentsSystem;
    }

    public void setNumberAgentsJoePublic(int numberAgentsJoePublic) {
        this.numberAgentsJoePublic = numberAgentsJoePublic;
    }

    //increasers

    public void increaseNumberOfResistanceConversions() {
        this.numberOfResistanceConversions++;
    }

    public void increaseNumberOfSystemConversions() {
        this.numberOfSystemConversions++;
    }

    public void increaseNumberOfResistanceRecluitments() {
        this.numberOfResistanceRecluitments++;
    }

    public void increaseNumberOfSystemRecluitments() {
        this.numberOfSystemRecluitments++;
    }

    public void increaseNumberOfSystemJPKills() {
        this.numberOfSystemJPKills++;
    }

    public void increaseNumberOfBattlesResistance() {
        this.numberOfBattlesResistance++;
    }

    public void increaseNumberOfBattlesSystem() {
        this.numberOfBattlesSystem++;
    }

    public void increaseNumberOfResistanceWins() {
        this.numberOfResistanceWins++;
    }

    public void increaseNumberOfSystemWins() {
        this.numberOfSystemWins++;
    }

    public void increaseNumberOfTies() {
        this.numberOfTies++;
    }

    public void increasNumberOfGetGameStatus() {
        this.numberOfGetGameStatus++;
    }

    public void printStats() {
        System.out.println("*".repeat(66));
        System.out.println("*".repeat(28) + "Game Stats" + "*".repeat(28));
        System.out.format("%-45s %20s\n", "* Peticiones de estado de juego:", numberOfGetGameStatus);
        System.out.format("%-45s %20s\n", "* Conversiones conseguidas por Resistencia:", numberOfResistanceConversions);
        System.out.format("%-45s %20s\n","* Conversiones conseguidas por Sistema:", numberOfSystemConversions);
        System.out.format("%-45s %20s\n","* Intentos de conversion por Resistencia:", numberOfResistanceRecluitments);
        System.out.format("%-45s %20s\n","* Intentos de conversion por Sistema:", numberOfSystemRecluitments);
        System.out.format("%-45s %20s\n","* JoePublic eliminados por Sistema:", numberOfSystemJPKills);
        System.out.format("%-45s %20s\n","* Peticiones de batalla por Resistencia:", numberOfBattlesResistance);
        System.out.format("%-45s %20s\n","* Peticiones de batalla por Sistema:", numberOfBattlesSystem);
        System.out.format("%-45s %20s\n","* Victorias de Resistencia:", numberOfResistanceWins);
        System.out.format("%-45s %20s\n","* Victorias de Sistema:", numberOfSystemWins);
        System.out.format("%-45s %20s\n","* Numero de empates:", numberOfTies);
        if (isOraculoFound()) {
            System.out.format("* ORACULO ENCONTRADO por %s (%s)\n", nameOfOraculoDiscover, teamOraculoFound);
        } else {
            System.out.format("* ORACULO NO ENCONTRADO\n");
        }
        System.out.format("%-45s %20s\n","* Numero de Agentes Resistencia restantes:", numberAgentsResistance);
        System.out.format("%-45s %20s\n","* Numero de Agentes Sistema restantes:", numberAgentsSystem);
        System.out.format("%-45s %20s\n","* Numero de Agentes JoePublic restantes:", numberAgentsJoePublic);
        System.out.println("*".repeat(28) + "Game Stats" + "*".repeat(28));
    }
}
