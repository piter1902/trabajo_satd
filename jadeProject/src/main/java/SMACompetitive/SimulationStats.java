package main.java.SMACompetitive;

public class SimulationStats {

    // Singleton
    private static final SimulationStats instance = new SimulationStats();
    // Display GUI
    private final SimulationStatsGUI simulationStatsGUI;
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

    private SimulationStats() {
        this.simulationStatsGUI = new SimulationStatsGUI();
    }

    public static SimulationStats getInstance() {
        return instance;
    }

    // Display GUI
    public void showGUI(Constants.TEAM winnerTeam) {
        String result = createStatString();
        this.simulationStatsGUI.setTextOnArea(result);
        this.simulationStatsGUI.setSize(400, 400);
        this.simulationStatsGUI.setContentPane(this.simulationStatsGUI.$$$getRootComponent$$$());
        this.simulationStatsGUI.setTitle(String.format("Winner: %s", winnerTeam.name()));
        this.simulationStatsGUI.setVisible(true);
    }

    /**
     * Resetea la instancia del singletone
     * Sólo es útil si se ejecuta varias veces el programa sin volver a compilar
     */
    public void reset() {
        setNameOfOraculoDiscover("");
        setOraculoFound(false);
        setTeamOraculoFound(null);
        setNumberOfBattlesSystem(0);
        setNumberAgentsSystem(0);
        setNumberOfBattlesResistance(0);
        setNumberOfGetGameStatus(0);
        setNumberOfResistanceConversions(0);
        setNumberOfSystemConversions(0);
        setNumberOfSystemJPKills(0);
        setNumberOfResistanceWins(0);
        setNumberOfSystemWins(0);
        setNumberOfTies(0);
        setNumberOfResistanceRecluitments(0);
        setNumberOfSystemRecluitments(0);
    }

    // Getters
    public int getNumberOfResistanceConversions() {
        return numberOfResistanceConversions;
    }

    public void setNumberOfResistanceConversions(int numberOfResistanceConversions) {
        this.numberOfResistanceConversions = numberOfResistanceConversions;
    }

    public int getNumberOfSystemConversions() {
        return numberOfSystemConversions;
    }

    public void setNumberOfSystemConversions(int numberOfSystemConversions) {
        this.numberOfSystemConversions = numberOfSystemConversions;
    }

    public int getNumberOfResistanceRecluitments() {
        return numberOfResistanceRecluitments;
    }

    public void setNumberOfResistanceRecluitments(int numberOfResistanceRecluitments) {
        this.numberOfResistanceRecluitments = numberOfResistanceRecluitments;
    }

    public int getNumberOfSystemRecluitments() {
        return numberOfSystemRecluitments;
    }

    public void setNumberOfSystemRecluitments(int numberOfSystemRecluitments) {
        this.numberOfSystemRecluitments = numberOfSystemRecluitments;
    }

    public int getNumberOfSystemJPKills() {
        return numberOfSystemJPKills;
    }

    public void setNumberOfSystemJPKills(int numberOfSystemJPKills) {
        this.numberOfSystemJPKills = numberOfSystemJPKills;
    }

    public int getNumberOfBattlesResistance() {
        return numberOfBattlesResistance;
    }

    public void setNumberOfBattlesResistance(int numberOfBattlesResistance) {
        this.numberOfBattlesResistance = numberOfBattlesResistance;
    }

    public int getNumberOfBattlesSystem() {
        return numberOfBattlesSystem;
    }

    public void setNumberOfBattlesSystem(int numberOfBattlesSystem) {
        this.numberOfBattlesSystem = numberOfBattlesSystem;
    }

    public int getNumberOfResistanceWins() {
        return numberOfResistanceWins;
    }

    public void setNumberOfResistanceWins(int numberOfResistanceWins) {
        this.numberOfResistanceWins = numberOfResistanceWins;
    }

    public int getNumberOfSystemWins() {
        return numberOfSystemWins;
    }

    // Setters

    public void setNumberOfSystemWins(int numberOfSystemWins) {
        this.numberOfSystemWins = numberOfSystemWins;
    }

    public int getNumberOfTies() {
        return numberOfTies;
    }

    public void setNumberOfTies(int numberOfTies) {
        this.numberOfTies = numberOfTies;
    }

    public int getNumberAgentsResistance() {
        return numberAgentsResistance;
    }

    public void setNumberAgentsResistance(int numberAgentsResistance) {
        this.numberAgentsResistance = numberAgentsResistance;
    }

    public int getNumberAgentsSystem() {
        return numberAgentsSystem;
    }

    public void setNumberAgentsSystem(int numberAgentsSystem) {
        this.numberAgentsSystem = numberAgentsSystem;
    }

    public int getNumberAgentsJoePublic() {
        return numberAgentsJoePublic;
    }

    public void setNumberAgentsJoePublic(int numberAgentsJoePublic) {
        this.numberAgentsJoePublic = numberAgentsJoePublic;
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

    public boolean isOraculoFound() {
        return oraculoFound;
    }

    public void setOraculoFound(boolean oraculoFound) {
        this.oraculoFound = oraculoFound;
    }

    public int getNumberOfGetGameStatus() {
        return numberOfGetGameStatus;
    }

    public void setNumberOfGetGameStatus(int numberOfGetGameStatus) {
        this.numberOfGetGameStatus = numberOfGetGameStatus;
    }

    // Increasers

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
        String result = createStatString();
        System.out.println(result);
    }


    private String createStatString() {
        String result = "*".repeat(66) + "\n";
        result += "*".repeat(28) + "Game Stats" + "*".repeat(28) + "\n";
        result += String.format("%-45s %20s\n", "* Peticiones de estado de juego:", numberOfGetGameStatus);
        result += String.format("%-45s %20s\n", "* Conversiones conseguidas por Resistencia:", numberOfResistanceConversions);
        result += String.format("%-45s %20s\n", "* Conversiones conseguidas por Sistema:", numberOfSystemConversions);
        result += String.format("%-45s %20s\n", "* Intentos de conversion por Resistencia:", numberOfResistanceRecluitments);
        result += String.format("%-45s %20s\n", "* Intentos de conversion por Sistema:", numberOfSystemRecluitments);
        result += String.format("%-45s %20s\n", "* JoePublic eliminados por Sistema:", numberOfSystemJPKills);
        result += String.format("%-45s %20s\n", "* Peticiones de batalla por Resistencia:", numberOfBattlesResistance);
        result += String.format("%-45s %20s\n", "* Peticiones de batalla por Sistema:", numberOfBattlesSystem);
        result += String.format("%-45s %20s\n", "* Victorias de Resistencia:", numberOfResistanceWins);
        result += String.format("%-45s %20s\n", "* Victorias de Sistema:", numberOfSystemWins);
        result += String.format("%-45s %20s\n", "* Numero de empates:", numberOfTies);
        if (isOraculoFound()) {
            result += String.format("* ORACULO ENCONTRADO por %s (%s)\n", nameOfOraculoDiscover, teamOraculoFound);
        } else {
            result += "* ORACULO NO ENCONTRADO\n";
        }
        result += String.format("%-45s %20s\n", "* Numero de Agentes Resistencia restantes:", numberAgentsResistance);
        result += String.format("%-45s %20s\n", "* Numero de Agentes Sistema restantes:", numberAgentsSystem);
        result += String.format("%-45s %20s\n", "* Numero de Agentes JoePublic restantes:", numberAgentsJoePublic);
        result += "*".repeat(28) + "Game Stats" + "*".repeat(28) + "\n";
        return result;
    }
}
