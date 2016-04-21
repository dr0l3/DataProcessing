package Pipeline;

import Core.ClassifierEvalDescriptionTriplet;
import Core.ClassifierEvalDescriptionTripletComparator;
import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.DistanceFunction;
import weka.core.Instances;
import weka.core.neighboursearch.LinearNNSearch;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.RemoveByName;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Created by Rune on 29-03-2016.
 */
public class NearestNeighborGridSearch implements Callable<ArrayList<ClassifierEvalDescriptionTriplet>>{
    private Instances dataset;
    private DistanceFunction distanceFunction;
    private String regex;
    private Filter filter;
    private int numberOfFolds;

    public NearestNeighborGridSearch(Instances dataset, DistanceFunction distanceFunction, Filter filter, int numberOfFolds) {
        this.dataset = dataset;
        this.distanceFunction = distanceFunction;
        this.filter = filter;
        this.numberOfFolds = numberOfFolds;
    }

    public NearestNeighborGridSearch(Instances dataset, DistanceFunction distanceFunction, String regex, int numberOfFolds) {
        this.dataset = dataset;
        this.distanceFunction = distanceFunction;
        this.regex = regex;
        this.numberOfFolds = numberOfFolds;
    }

    @Override
    public ArrayList<ClassifierEvalDescriptionTriplet> call() throws Exception {
        ArrayList<ClassifierEvalDescriptionTriplet> triplets = new ArrayList<>();

        int k = 1;
        for (int i = 0; i < 5; i++) {
            IBk classifier = new IBk();
            classifier.setKNN(k);
            LinearNNSearch search = new LinearNNSearch();
            search.setDistanceFunction(distanceFunction);
            classifier.setNearestNeighbourSearchAlgorithm(search);

            FilteredClassifier filternn = new FilteredClassifier();
            //filter.setInputFormat(dataset);
            if(filter == null) {
                RemoveByName filter = new RemoveByName();
                filter.setExpression(regex);
            }
            filternn.setFilter(filter);
            filternn.setClassifier(classifier);

            filternn.buildClassifier(dataset);

            Evaluation eval = new Evaluation(dataset);
            eval.crossValidateModel(filternn, dataset, numberOfFolds, new Random());


            triplets.add(new ClassifierEvalDescriptionTriplet("NearestNeighbor(DF:"+distanceFunction.getClass().getSimpleName()+".K:"+k+").Filter:"+filter.getClass().getSimpleName(),
                    eval,filternn));
            k = k*2;
        }

        System.out.println("NearestNeighborTest with distance function: "+ distanceFunction.getClass().getSimpleName()+" and filter "+regex+" Done!");

        return triplets;
    }
}
