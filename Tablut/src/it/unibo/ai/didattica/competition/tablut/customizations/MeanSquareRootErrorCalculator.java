package it.unibo.ai.didattica.competition.tablut.customizations;

import weka.classifiers.evaluation.Prediction;

import java.util.List;

public class MeanSquareRootErrorCalculator {

    public static double calculateMSRE(List<Prediction> instances) {
        int n = instances.size();
        double sumOfSquaredDifferences = 0.0;

        for (Prediction instance : instances) {


            double actual = instance.actual();
            double predicted = instance.predicted();
            double squaredDifference = Math.pow(actual - predicted, 2);
            sumOfSquaredDifferences += squaredDifference;
        }

        double meanSquaredDifference = sumOfSquaredDifferences / n;

        return Math.sqrt(meanSquaredDifference);
    }
}