package SMA;

import jade.core.behaviours.OneShotBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

import java.util.Random;

public class AgentePredictorScorer extends GuiAgent {

    private Instances validationDataset;
    private AbstractClassifier classifier;
    private JfrmAgentePredictionScorer formResults;

    @Override
    protected void setup() {
        initForm();
        addBehaviour(new predictionScorerBehaviour());
        super.setup();
    }

    private void initForm() {
        this.formResults = new JfrmAgentePredictionScorer();
        this.formResults.setContentPane(this.formResults.$$$getRootComponent$$$());
        this.formResults.pack();
        this.formResults.setSize(400, 400);
    }

    @Override
    protected void onGuiEvent(GuiEvent guiEvent) {

    }

    private class predictionScorerBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            // Recepcion de los dos parametros (dataset y modelo)
            for (int i = 0; i < 2; i++) {
                ACLMessage aclMessage = this.myAgent.blockingReceive();
                Object content = null;
                try {
                    content = aclMessage.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
                if (content instanceof Instances) {
                    // Es el dataset
                    validationDataset = (Instances) content;
                } else if (content instanceof AbstractClassifier) {
                    // Es el modelo
                    classifier = (AbstractClassifier) content;
                }
            }

            // Evaluacion del modelo recibido
            Evaluation evaluation;
            try {
                evaluation = new Evaluation(validationDataset);
                evaluation.crossValidateModel(classifier, validationDataset, 10, new Random(System.nanoTime()));
                //Obtenemos resultados
                String results = String.format("\nResultados %s\n========\n", this.myAgent.getLocalName());
                results = results + ("# instancias clasificadas " + (int) evaluation.numInstances() + "\n");
                results = results + ("% instancias correctamente clasificadas " + evaluation.pctCorrect() + "\n");
                results = results + ("# instancias correctamente clasificadas " + (int) evaluation.correct() + "\n");
                results = results + ("% instancias incorrectamente clasificadas " + evaluation.pctIncorrect() + "\n");
                results = results + ("# instancias incorrectamente clasificadas " + (int) evaluation.incorrect() + "\n");
                results = results + ("Media del error absoluto " + evaluation.meanAbsoluteError() + "\n");
                results = results + (evaluation.toMatrixString("Matrix de confusion"));
                formResults.setTextOnArea(results);
                // Mostramos la pantalla de resultados
                formResults.setVisible(true);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

        }
    }
}
