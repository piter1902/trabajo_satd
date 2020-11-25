package SMA;

import jade.core.AID;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgenteArchivo extends GuiAgent {

    // GUI
    private JfrmAgenteArchivo formArchivo;

    // File
    private BufferedReader file;

    @Override
    protected void setup() {
        super.setup();
        initForm();
    }

    private void initForm() {
        this.formArchivo = new JfrmAgenteArchivo(this);
        this.formArchivo.setContentPane(this.formArchivo.$$$getRootComponent$$$());
        this.formArchivo.pack();
        this.formArchivo.setVisible(true);
        this.formArchivo.setSize(400, 400);
    }

    @Override
    protected void onGuiEvent(GuiEvent guiEvent) {
        // Tratamiento del evento
        ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);//se define objeto de tipo mensaje

        try {
            String ruta = formArchivo.obtenerRuta();
            System.out.println(ruta);
            file = new BufferedReader(new FileReader(ruta)); //Se lee el archivo
            msg.setContent(convertir(file));//se le añade el contenido al objeto de tipo mensaje, convirtiendo el Buffer en un String
            msg.addReceiver(new AID("dm", AID.ISLOCALNAME));//AID= Agent identification, se le añade a quien se le envia
            send(msg); //el agente actual envia el mensaje
            file.close();//se cierra el archivo
        } catch (IOException ex) {
            Logger.getLogger(agentes.AgenteArchivo.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(formArchivo, ex.getMessage());
        }
    }

    /*
     * Convierte un elemento Buffer a un String y lo retorna
     */
    public String convertir(BufferedReader file) throws IOException {
        String temp;//Almacena la linea leida del file
        String cadena = "";//cadena final
        while ((temp = file.readLine()) != null) {
            cadena = cadena + temp + "\n";
        }
        return cadena;
    }
}
