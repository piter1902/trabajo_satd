package main.java.SMA;

import jade.core.AID;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import main.java.Timeout.TimeoutAdapter;
import weka.core.Instances;
import weka.core.converters.Saver;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgenteArchivo extends GuiAgent {

    // GUI
    private JfrmAgenteArchivo formArchivo;

    // Lista de receptores
    private List<AID> receivers;

    @Override
    protected void setup() {
        super.setup();
        initForm();
        getReceivers();
    }

    private void getReceivers() {
        Object[] args = getArguments();
        receivers = new ArrayList<>();
        // args es la lista de receptores
        for (Object o : args) {
            if (o instanceof String) {
                String s = (String) o;
//                System.out.println(s);
                receivers.add(new AID(s, AID.ISGUID));
            }
        }
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
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);        // Se define objeto de tipo mensaje

        try {
            String ruta = formArchivo.obtenerRuta();
            System.out.println(ruta);
            // File
            BufferedReader file = new BufferedReader(new FileReader(ruta));        // Se lee el archivo
            Instances wekaDataset;
            if (ruta.matches(".*\\.arff$")) {
                // Es un conjunto de weka
                wekaDataset = new Instances(file);
//                saveToFile(wekaDataset, new CSVSaver());
            } /*else if (ruta.matches(".*\\.csv$")) {
                // Es un conjunto CSV separado por , y con cabecera
                CSVLoader loader = new CSVLoader();
                loader.setSource(new File(ruta));
                loader.setFieldSeparator(",");
                wekaDataset = loader.getDataSet();
                saveToFile(wekaDataset, new ArffSaver());
                // El problema es el convertidor de Weka. Usando otro igual funciona.
                // Recogemos cable
            }*/ else {
                throw new RuntimeException("El fichero no es ARFF");
            }
            // Se añade el dataset al mensaje
            msg.setContentObject(wekaDataset);
            // AID = Agent identification, se le añade a quien se le envia (lista de receptores)
            receivers.forEach(msg::addReceiver);
            send(msg);
            System.out.format("Message sent from %s to %s\n", getLocalName(), receivers.toString());
            // main.java.Timeout protocol
            TimeoutAdapter.sendWithTimeout(msg, this);
            file.close();                                           // Se cierra el archivo
        } catch (IOException ex) {
            Logger.getLogger(main.java.agentes.AgenteArchivo.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(formArchivo, ex.getMessage());
        }
    }

    private void saveToFile(Instances instances, Saver saver) {
        try {
            saver.setInstances(instances);
            saver.setFile(new File("/Users/pedroalluetamargo/Desktop/" + UUID.randomUUID().toString() + saver.getFileExtension()));
            saver.writeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}