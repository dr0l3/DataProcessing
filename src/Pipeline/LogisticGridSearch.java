package Pipeline;

import Core.ClassifierEvalDescriptionTriplet;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.RemoveByName;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Created by Rune on 29-03-2016.
 */
public class LogisticGridSearch implements Callable<ArrayList<ClassifierEvalDescriptionTriplet>> {
    private Instances dataset;
    private String regex;
    private Filter filter;
    private int numberOfCrossValidationFolds;

    public LogisticGridSearch(Instances dataset, Filter filter, int numberOfCrossValidationFolds) {
        this.dataset = dataset;
        this.filter = filter;
        this.numberOfCrossValidationFolds = numberOfCrossValidationFolds;
    }

    public LogisticGridSearch(Instances dataset, String regex, int numberOfCrossValidationFolds) {
        this.dataset = dataset;
        this.regex = regex;
        this.numberOfCrossValidationFolds = numberOfCrossValidationFolds;
    }

    @Override
    public ArrayList<ClassifierEvalDescriptionTriplet> call() throws Exception {
        ArrayList<ClassifierEvalDescriptionTriplet> triplets = new ArrayList<>();

        double ridge = 1;

        for (int i = 0; i < 10; i++) {
            String[] options = weka.core.Utils.splitOptions("-R "+ridge+ " -M -1");
            Logistic logistic = new Logistic();
            logistic.setOptions(options);

            FilteredClassifier filterlog = new FilteredClassifier();
            //filter.setInputFormat(dataset);
            if(filter == null) {
                RemoveByName filter = new RemoveByName();
                filter.setExpression(regex);
            }
            filterlog.setFilter(filter);
            filterlog.setClassifier(logistic);

            filterlog.buildClassifier(dataset);

            Evaluation eval = new Evaluation(dataset);
            eval.crossValidateModel(filterlog,dataset, numberOfCrossValidationFolds, new Random());
            triplets.add(new ClassifierEvalDescriptionTriplet("Logistic("+ridge+").Filter:"+filter.getClass().getSimpleName(), eval,filterlog));
            ridge = ridge/10;
        }

        System.out.println("LogisticTest Done!");

        return triplets;
    }
}
