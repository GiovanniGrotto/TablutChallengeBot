package it.unibo.ai.didattica.competition.tablut.customizations;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.EvaluationUtils;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.ArrayList;


public class CustomRandomForest {

    static public RandomForest rf = new RandomForest();
    static public EvaluationUtils ev = new EvaluationUtils();




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


    public static void main(String[] argv) throws Exception {

        long startTime = System.currentTimeMillis();
        long endTime;
        //Carico il dataset
        DataSource source = new DataSource("Tablut/src/it/unibo/ai/didattica/competition/tablut/customizations/serialized_data.arff");
        Instances data = source.getDataSet();
        //Setto quale attributo corrisponde alla label
        data.setClassIndex(0);
        //Shuffle del dataset
        data.randomize(new java.util.Random());


        //Divido in trainSet e testSet
        double splitPercentage=0.8;
        int trainSize = (int) Math.round(data.size()* splitPercentage );
        Instances trainData= new Instances( data, 0, trainSize );
        Instances testData= new Instances(data, trainSize, data.size()-trainSize );

        endTime = System.currentTimeMillis();
        System.out.println("Processed data in " + (endTime - startTime) + "ms");

        //Training del modello
        //rf.buildClassifier(new Instances(data));

        //MSRE dopo il training su tutto il trainSet
        startTime=System.currentTimeMillis();
        ArrayList<Prediction> predictions=ev.getTrainTestPredictions(rf,data,testData);
        endTime = System.currentTimeMillis();
        System.out.println("Trained model in " + (endTime - startTime) + "ms");
        double msre=MeanSquareRootErrorCalculator.calculateMSRE(predictions);
        System.out.println("MSRE training 100%: " + msre);

        //MSRE dopo il training su {splitPercentage}% del trainSet
        startTime=System.currentTimeMillis();
        predictions=ev.getTrainTestPredictions(rf,trainData,testData);
        endTime = System.currentTimeMillis();
        System.out.println("Trained model in " + (endTime - startTime) + "ms");
        msre=MeanSquareRootErrorCalculator.calculateMSRE(predictions);
        System.out.println("MSRE training 80%: " + msre);



        //quando abbiamo finito lo esportiamo
//        exportModel(rf);
    }
}
