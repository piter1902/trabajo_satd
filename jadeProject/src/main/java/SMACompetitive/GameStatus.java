package main.java.SMACompetitive;

import java.io.Serializable;

public class GameStatus implements Serializable {

    private final boolean oraculoFound;

    private final int nResistencia;

    private final int nSistema;

    private final int nJoePublic;

    public GameStatus(boolean oraculoFound, int nResistencia, int nSistema, int nJoePublic) {
        this.oraculoFound = oraculoFound;
        this.nResistencia = nResistencia;
        this.nSistema = nSistema;
        this.nJoePublic = nJoePublic;
    }

    public boolean isOraculoFound() {
        return oraculoFound;
    }

    public int getnResistencia() {
        return nResistencia;
    }

    public int getnSistema() {
        return nSistema;
    }

    public int getnJoePublic() {
        return nJoePublic;
    }

    @Override
    public String toString() {
        return "GameStatus{" +
                "oraculoFound=" + oraculoFound +
                ", nResistencia=" + nResistencia +
                ", nSistema=" + nSistema +
                ", nJoePublic=" + nJoePublic +
                '}';
    }
}
