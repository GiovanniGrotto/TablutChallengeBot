package it.unibo.ai.didattica.competition.tablut.customizations;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.EvaluationUtils;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.trees.RandomForest;
import weka.core.*;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.ArrayList;


public class CustomRandomForest {

    static public RandomForest rf = new RandomForest();

    static public Classifier randomForest = CustomRandomForest.importModel("trainedModel.model");
    static public EvaluationUtils ev = new EvaluationUtils();

    static public Instances data_no_label;

    static public Instances data;

    static {
        try {
            data_no_label = new DataSource("serialized_data.arff").getDataSet();
            data_no_label.setClassIndex(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            data = new DataSource("serialized_data.arff").getDataSet();
            data.setClassIndex(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<Integer> serializeState(State state) {
        ArrayList<Integer> newState = new ArrayList<>();

        // Assuming the board is 9x9
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                State.Pawn cell = state.getBoard()[i][j];
                if (cell == State.Pawn.WHITE) {
                    newState.add(1);
                    newState.add(0);
                    newState.add(0);
                } else if (cell == State.Pawn.BLACK) {
                    newState.add(0);
                    newState.add(1);
                    newState.add(0);
                } else if (cell == State.Pawn.KING) {
                    newState.add(0);
                    newState.add(0);
                    newState.add(1);
                } else {
                    newState.add(0);
                    newState.add(0);
                    newState.add(0);
                }
            }
        }

        // Append the turn
        if (state.getTurn()== State.Turn.WHITE){
            newState.add(1);
        }
        else {
            newState.add(0);
        }
        return newState;
    }

    public static Instance createStateInstance(State state) {
        // Create a Weka Instance
        Instance instance = new DenseInstance(data_no_label.numAttributes());
        instance.setDataset(data_no_label);
        ArrayList<Integer> stateList= serializeState(state);

        // Set attribute values from the State object
        for (int i = 1; i < 244; i++) {
            instance.setValue(i, stateList.get(i));
        }

        return instance;
    }

    public static double evaluate(State state) throws Exception {
        Instance instance= createStateInstance( state );

        return randomForest.classifyInstance(instance);
    }

    public static void exportModel(Classifier trainedModel){
        try {
            SerializationHelper.write("trainedModel.model", trainedModel);
            System.out.println("Model saved successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Classifier importModel(String filename){
        try {
            // Load the pre-trained model (replace filename with the actual filename you used to save the model, ex. "your_model.model")
            Classifier loadedModel = (Classifier) SerializationHelper.read(filename);
            System.out.println("Model loaded successfully.");

            // Now you can use 'loadedModel' to make predictions.
            // Example: double prediction = loadedModel.classifyInstance(instanceToPredict);
            return loadedModel;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static long calculatePredictionTime(Classifier classifier, Instance instance) {
        long startTime = System.currentTimeMillis();
        try {
            classifier.classifyInstance(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    public static void trainModel() throws Exception {
        long startTime;
        long endTime;
        //Carico il dataset
        DataSource source = new DataSource("Tablut/src/it/unibo/ai/didattica/competition/tablut/customizations/serialized_data.arff");
        Instances data = source.getDataSet();
        //Setto quale attributo corrisponde alla label
        data.setClassIndex(0);
        //Shuffle del dataset
        data.randomize(new java.util.Random());


//        //Divido in trainSet e testSet
//        double splitPercentage=0.8;
//        int trainSize = (int) Math.round(data.size()* splitPercentage );
//        Instances trainData= new Instances( data, 0, trainSize );
//        Instances testData= new Instances(data, trainSize, data.size()-trainSize );
//
//        endTime = System.currentTimeMillis();
//        System.out.println("Processed data in " + (endTime - startTime) + "ms");

        //Training del modello
        startTime=System.currentTimeMillis();
        rf.buildClassifier(new Instances(data));
        endTime = System.currentTimeMillis();
        System.out.println("Trained model in " + (endTime - startTime) + "ms");
    }

    public static void trainSetMSRE() throws Exception {
        ArrayList<Prediction> predictions=ev.getTrainTestPredictions(rf,data,data);
        double msre=MeanSquareRootErrorCalculator.calculateMSRE(predictions);
        System.out.println("MSRE training 100%: " + msre);
    }


    public static void main(String[] argv) throws Exception {

        trainModel();
        //quando abbiamo finito lo esportiamo
        exportModel(rf);
    }
}
