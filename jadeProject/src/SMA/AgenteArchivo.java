package SMA;

import jade.core.AID;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.CSVSaver;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgenteArchivo extends GuiAgent {

    private static final int MAX_COUNT = 10;
    // GUI
    private JfrmAgenteArchivo formArchivo;

    // File
    private BufferedReader file;

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
            file = new BufferedReader(new FileReader(ruta));        // Se lee el archivo
            Instances wekaDataset = null;
            if (ruta.matches(".*\\.arff$")) {
                // Es un conjunto de weka
                wekaDataset = new Instances(file);
                saveCSV(wekaDataset);
            } else if (ruta.matches(".*\\.csv$")) {
                // Es un conjunto CSV separado por ; y con cabecera
                CSVLoader loader = new CSVLoader();
                loader.setSource(new File(ruta));
                loader.setFieldSeparator(",");
                wekaDataset = loader.getDataSet();
                // TODO: Comprobar el por qué falla.
                //  Utilizar: https://stackoverflow.com/questions/15637553/weka-simplekmeans-cannot-handle-string-attributes
            } else {
                throw new RuntimeException("El fichero no es CSV ni ARFF");
            }
            // Se añade el dataset al mensaje
            msg.setContentObject(wekaDataset);
            // AID = Agent identification, se le añade a quien se le envia (lista de receptores)
            receivers.forEach(msg::addReceiver);
            send(msg);
            System.out.format("Message sent from %s to %s\n", getLocalName(), receivers.toString());
            // Timeout protocol
            TimeoutAdapter.sendWithTimeout(msg, this);
            file.close();                                           // Se cierra el archivo
        } catch (IOException ex) {
            Logger.getLogger(agentes.AgenteArchivo.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(formArchivo, ex.getMessage());
        }
    }

    private void saveCSV(Instances instances) {
        CSVSaver csvSaver = new CSVSaver();
        try {
            csvSaver.setInstances(instances);
            csvSaver.setFile(new File("/Users/pedroalluetamargo/Desktop/vehiculos.csv"));
            csvSaver.writeBatch();
        } catch (IOException e) {
            e.printStackTrace();
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