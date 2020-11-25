package SMA;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

public class AgenteArchivo extends GuiAgent {

    private JfrmAgenteArchivo formArchivo;

    @Override
    protected void setup() {
        super.setup();
        this.formArchivo = new JfrmAgenteArchivo(this);
        this.formArchivo.pack();
        this.formArchivo.setVisible(true);
        this.formArchivo.setSize(400, 400);
    }

    @Override
    protected void onGuiEvent(GuiEvent guiEvent) {

    }
}
