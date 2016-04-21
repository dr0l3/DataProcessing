package Pipeline;

import Core.ClassifierEvalDescriptionTriplet;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.RemoveByName;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Created by Rune on 29-03-2016.
 */
public class RandomForestGridSearch implements Callable<ArrayList<ClassifierEvalDescriptionTriplet>> {
    private Instances dataset;
    private String regex;
    private Filter filter;
    private int crossValidationNumberOfFolds;

    public RandomForestGridSearch(Instances dataset, String regex, int crossValidationNumberOfFolds) {
        this.dataset = dataset;
        this.regex = regex;
        this.crossValidationNumberOfFolds = crossValidationNumberOfFolds;
    }

    public RandomForestGridSearch(Instances dataset, Filter filter, int crossValidationNumberOfFolds) {
        this.dataset = dataset;
        this.filter = filter;
        this.crossValidationNumberOfFolds = crossValidationNumberOfFolds;
    }

    @Override
    public ArrayList<ClassifierEvalDescriptionTriplet> call() throws Exception {
        ArrayList<ClassifierEvalDescriptionTriplet> triplets = new ArrayList<>();

        double numberOfTrees = 10;
        for (int i = 0; i < 10; i++) {
            String[] options = weka.core.Utils.splitOptions("-I " + (int) numberOfTrees +" -K 0 -S 1");
            RandomForest rf = new RandomForest();
            rf.setOptions(options);
            FilteredClassifier filterRf = new FilteredClassifier();
            //filter.setInputFormat(dataset);
            if(filter == null) {
                RemoveByName filter = new RemoveByName();
                filter.setExpression(regex);
            }
            filterRf.setFilter(filter);
            filterRf.setClassifier(rf);
            filterRf.buildClassifier(dataset);

            Evaluation eval = new Evaluation(dataset);
            eval.crossValidateModel(filterRf, dataset, crossValidationNumberOfFolds, new Random());
            triplets.add(new ClassifierEvalDescriptionTriplet("RF("+numberOfTrees+")"+filter.getClass().getSimpleName(), eval,filterRf));

            numberOfTrees = numberOfTrees * 1.5;
        }

        System.out.println("RandomForest with filter: "+regex+ " Done!");

        return triplets;
    }
}
