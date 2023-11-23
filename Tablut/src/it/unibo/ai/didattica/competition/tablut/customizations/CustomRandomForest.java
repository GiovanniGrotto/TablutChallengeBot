package it.unibo.ai.didattica.competition.tablut.customizations;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.EvaluationUtils;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.trees.RandomForest;
import weka.core.*;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.File;
import java.util.ArrayList;


public class CustomRandomForest {

    static public RandomForest rf = new RandomForest();
    static public Classifier randomForest = CustomRandomForest.importModel(System.getProperty("user.dir")+ File.separator + "trainedModel.model");
    static public EvaluationUtils ev = new EvaluationUtils();

    static public Evaluation evaluation;

    static public Instances data_no_label;

    static public Instances data;

    static {
        //System.out.println(System.getProperty("user.dir"));
        //System.out.println(System.getProperty("user.home"));
        //System.out.println(System.getProperty("user.dir")+ File.separator + "trainedModel.model");
    }

    static {
        try {
            String filename=System.getProperty("user.dir")+ File.separator + "serialized_data.arff";
            //System.out.println(filename);
            data = new DataSource(filename).getDataSet();
            data.setClassIndex(0);
            //System.out.println(System.getProperty("user.dir"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            String filename=System.getProperty("user.dir")+ File.separator + "serialized_data.arff";
            //System.out.println(filename);
            data_no_label = new DataSource(filename).getDataSet();
            data_no_label.setClassIndex(0);
            //System.out.println(System.getProperty("user.dir"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Instance serializeState(State state, Instance instance) {
        // Assuming the board is 9x9
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                State.Pawn cell = state.getBoard()[i][j];
                Integer index = (i * 9 + j) * 3;
                if (cell == State.Pawn.WHITE) {
                    instance.setValue(index, 1);
                    instance.setValue(index+1, 0);
                    instance.setValue(index+2, 0);

                } else if (cell == State.Pawn.BLACK) {
                    instance.setValue(index, 0);
                    instance.setValue(index+1, 1);
                    instance.setValue(index+2, 0);

                } else if (cell == State.Pawn.KING) {
                    instance.setValue(index, 0);
                    instance.setValue(index+1, 0);
                    instance.setValue(index+2, 1);

                } else {
                    instance.setValue(index, 0);
                    instance.setValue(index+1, 0);
                    instance.setValue(index+2, 0);

                }
            }
        }

        // Append the turn
        if (state.getTurn()== State.Turn.WHITE){
            instance.setValue(instance.numAttributes()-1, 1);
        }
        else {
            instance.setValue(instance.numAttributes()-1, 0);
        }
        return instance;
    }

    public static Instance createStateInstance(State state) {
        // Create a Weka Instance
        Instance instance = new DenseInstance(data_no_label.numAttributes());
        instance.setDataset(data_no_label);
        Instance newInstance = serializeState(state, instance);

        return newInstance;
    }

    public static double evaluate(State state) throws Exception {
        Instance instance= createStateInstance( state );

        return randomForest.classifyInstance(instance);
    }

    public static void exportModel(Classifier trainedModel){
        try {
            SerializationHelper.write("trainedModel.model", trainedModel);
            //System.out.println("Model saved successfully.");
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


        //Divido in trainSet e testSet
        double splitPercentage=0.7;
        int trainSize = (int) Math.round(data.size()* splitPercentage );
        Instances testData= new Instances( data, 0, trainSize );
        //Instances testData= new Instances(data, trainSize, data.size()-trainSize );
//
//        endTime = System.currentTimeMillis();
//        System.out.println("Processed data in " + (endTime - startTime) + "ms");

        //Training del modello
        rf.setMaxDepth(100);
        startTime=System.currentTimeMillis();
        rf.buildClassifier(new Instances(data));
        endTime = System.currentTimeMillis();
        System.out.println("Trained model in " + (endTime - startTime) + "ms");
        //trainSetMSRE();
        //testSetMSRE(testData);
    }

    public static void trainSetMSRE() throws Exception {
        ArrayList<Prediction> predictions=ev.getTrainTestPredictions(rf,data,data);
        double msre=MeanSquareRootErrorCalculator.calculateMSRE(predictions);
        System.out.println("MSRE on train set: " + msre);
    }

    public static void testSetMSRE(Instances testSet) throws Exception {
        ArrayList<Prediction> predictions=ev.getTrainTestPredictions(rf,data,testSet);
        double msre=MeanSquareRootErrorCalculator.calculateMSRE(predictions);
        System.out.println("MSRE on test set: " + msre);
    }

    public static void tuneRandomForest() throws Exception {
        DataSource source = new DataSource("Tablut/src/it/unibo/ai/didattica/competition/tablut/customizations/serialized_data.arff");
        Instances data = source.getDataSet();
        data.setClassIndex(0);
        data.randomize(new java.util.Random());
//
//        //TUNING NUMBER OF TREES ----------> DEFAULT IS FINE
//        int[] numTreesValues = {10, 25, 50, 75, 100, 150, 200};
//        for (int numTrees : numTreesValues) {
//            RandomForest randomForest = new RandomForest();
//            randomForest.setNumIterations(numTrees);
//
//            // Train the model
//            randomForest.buildClassifier(data);
//
//            System.out.println("Number of Trees: " + numTrees);
//            ArrayList<Prediction> predictions=ev.getTrainTestPredictions(randomForest,data,data);
//            double msre=MeanSquareRootErrorCalculator.calculateMSRE(predictions);
//            System.out.println("MSRE with "+numTrees+" trees: " + msre);
//            System.out.println("-------------------------");
//        }

        //TUNING MAX DEPTH OF TREES ----------> 100 IS FINE
        int[] maxDepthValues = {100, 200, 300};
        for (int maxDepth : maxDepthValues) {
            RandomForest randomForest = new RandomForest();
            randomForest.setMaxDepth(maxDepth);

            // Train the model
            randomForest.buildClassifier(data);

            System.out.println("Max depth: " + maxDepth);
            ArrayList<Prediction> predictions=ev.getTrainTestPredictions(randomForest,data,data);
            double msre=MeanSquareRootErrorCalculator.calculateMSRE(predictions);
            System.out.println("MSRE: " + msre);
            System.out.println("-------------------------");
        }
    }

    public static void crossValidation() throws Exception {
        evaluation=new Evaluation(data);
        evaluation.crossValidateModel(rf, data, 10, new java.util.Random(1));
        System.out.println("Mean Squared Error: " + evaluation.meanAbsoluteError());
        System.out.println("Root Mean Squared Error: " + evaluation.rootMeanSquaredError());
    }

    public static void trainNN() throws Exception {

        long startTime;
        long endTime;
        //Carico il dataset
        DataSource source = new DataSource("Tablut/src/it/unibo/ai/didattica/competition/tablut/customizations/serialized_data.arff");
        Instances data = source.getDataSet();
        //Setto quale attributo corrisponde alla label
        data.setClassIndex(0);
        //Shuffle del dataset
        data.randomize(new java.util.Random());


        MultilayerPerceptron neuralNetwork = new MultilayerPerceptron();

        // Set the options for the neural network
        neuralNetwork.setOptions(weka.core.Utils.splitOptions("-L 0.001 -M 0.9 -N 400 -V 0 -S 0 -E 20 -H \"150,20\""));

        // Solver "adam" is not directly available in Weka, but the above options should be a good starting point.
        // Please refer to the Weka documentation for more details on available options.

        // Train the neural network
        startTime=System.currentTimeMillis();
        System.out.println("Start time: "+startTime);
        neuralNetwork.buildClassifier(data);
        endTime=System.currentTimeMillis();
        System.out.println("End time: "+endTime);

        evaluation=new Evaluation(data);
        evaluation.crossValidateModel(neuralNetwork, data, 10, new java.util.Random(1));
        System.out.println("Mean Squared Error: " + evaluation.meanAbsoluteError());
        System.out.println("Root Mean Squared Error: " + evaluation.rootMeanSquaredError());

    }


    public static void main(String[] argv) throws Exception {
        //tuneRandomForest();
        //trainModel();
        //crossValidation();
        //quando abbiamo finito lo esportiamo
        //exportModel(rf);
    }
}
