package SMACompetitive;

import java.io.Serializable;

public class GameStatus implements Serializable {

    private boolean oraculoFound;

    private int nResistencia;

    private int nSistema;

    private int nJoePublic;

    public GameStatus(boolean oraculoFound, int nResistencia, int nSistema, int nJoePublic) {
        this.oraculoFound = oraculoFound;
        this.nResistencia = nResistencia;
        this.nSistema = nSistema;
        this.nJoePublic = nJoePublic;
    }

    public boolean isOraculoFound() {
        return oraculoFound;
    }

    public void setOraculoFound(boolean oraculoFound) {
        this.oraculoFound = oraculoFound;
    }

    public int getnResistencia() {
        return nResistencia;
    }

    public void setnResistencia(int nResistencia) {
        this.nResistencia = nResistencia;
    }

    public int getnSistema() {
        return nSistema;
    }

    public void setnSistema(int nSistema) {
        this.nSistema = nSistema;
    }

    public int getnJoePublic() {
        return nJoePublic;
    }

    public void setnJoePublic(int nJoePublic) {
        this.nJoePublic = nJoePublic;
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
