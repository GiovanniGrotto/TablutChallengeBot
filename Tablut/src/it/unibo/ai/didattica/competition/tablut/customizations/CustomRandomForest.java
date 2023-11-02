package it.unibo.ai.didattica.competition.tablut.customizations;

import weka.classifiers.evaluation.EvaluationUtils;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomForest.*;
import weka.core.Instances;
import weka.core.WekaPackageManager;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.ArrayList;


public class CustomRandomForest {

    static public RandomForest rf = new RandomForest();

    static public EvaluationUtils ev = new EvaluationUtils();


    public static void main(String[] argv) throws Exception {
        DataSource source = new DataSource("C:\\Users\\Utente ASUS\\Desktop\\TablutChallengeBot\\Tablut\\src\\it\\unibo\\ai\\didattica\\competition\\tablut\\customizations\\db.arff");
        Instances data = source.getDataSet();
        data.setClassIndex(data.numAttributes() - 1);
        rf.buildClassifier(new Instances(data));

        ArrayList<Prediction> res=ev.getTrainTestPredictions(rf,data,data);
        System.out.print(res);
    }
}
