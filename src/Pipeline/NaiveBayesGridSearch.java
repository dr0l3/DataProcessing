package Pipeline;

import Core.ClassifierEvalDescriptionTriplet;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
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
public class NaiveBayesGridSearch implements Callable<ArrayList<ClassifierEvalDescriptionTriplet>> {
    private Instances dataset;
    private String regex;
    private Filter filter;
    private int numberOfCrossValidationFolds;

    public NaiveBayesGridSearch(Instances dataset, String regex, int numberOfCrossValidationFolds) {
        this.dataset = dataset;
        this.regex = regex;
        this.numberOfCrossValidationFolds = numberOfCrossValidationFolds;
    }

    public NaiveBayesGridSearch(Instances dataset, Filter filter, int numberOfCrossValidationFolds) {
        this.dataset = dataset;
        this.filter = filter;
        this.numberOfCrossValidationFolds = numberOfCrossValidationFolds;
    }

    @Override
    public ArrayList<ClassifierEvalDescriptionTriplet> call() throws Exception {
        ArrayList<ClassifierEvalDescriptionTriplet> triplets = new ArrayList<>();

        String[] options = weka.core.Utils.splitOptions("-K");

        NaiveBayes naiveBayes = new NaiveBayes();
        naiveBayes.setOptions(options);

        FilteredClassifier filternb = new FilteredClassifier();
        if(filter == null) {
            RemoveByName filter = new RemoveByName();
            filter.setExpression(regex);
        }
        //filter.setInputFormat(dataset);
        filternb.setFilter(filter);
        filternb.setClassifier(naiveBayes);

        filternb.buildClassifier(dataset);

        Evaluation eval = new Evaluation(dataset);
        eval.crossValidateModel(filternb,dataset, numberOfCrossValidationFolds, new Random());

        triplets.add(new ClassifierEvalDescriptionTriplet("NaiveBayes(-K)",eval,filternb));

        naiveBayes = new NaiveBayes();
        naiveBayes.setOptions(weka.core.Utils.splitOptions(""));

        filternb = new FilteredClassifier();
        filternb.setFilter(filter);
        filternb.setClassifier(naiveBayes);

        filternb.buildClassifier(dataset);

        eval = new Evaluation(dataset);
        eval.crossValidateModel(filternb,dataset, numberOfCrossValidationFolds, new Random());

        triplets.add(new ClassifierEvalDescriptionTriplet("NaiveBayes()",eval,filternb));

        System.out.println("NaiveBayesTest with filter "+regex+" Done!");

        return triplets;
    }
}
