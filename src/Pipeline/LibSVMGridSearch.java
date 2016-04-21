package Pipeline;

import Core.ClassifierEvalDescriptionTriplet;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.RemoveByName;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Created by Rune on 29-03-2016.
 */
public class LibSVMGridSearch implements Callable<ArrayList<ClassifierEvalDescriptionTriplet>> {
    private Instances dataset;
    private SelectedTag type;
    private int sizeOfReturnSet;
    private String regex;
    private Filter filter;
    private int numberOfCrossValidationFolds;

    public LibSVMGridSearch(Instances dataset, SelectedTag type, int sizeOfReturnSet, Filter filter, int numberOfCrossValidationFolds) {
        this.dataset = dataset;
        this.type = type;
        this.sizeOfReturnSet = sizeOfReturnSet;
        this.filter = filter;
        this.numberOfCrossValidationFolds = numberOfCrossValidationFolds;
    }

    public LibSVMGridSearch(Instances dataset, SelectedTag type, int sizeOfReturnSet, String regex, int numberOfCrossValidationFolds) {
        this.dataset = dataset;
        this.type = type;
        this.sizeOfReturnSet = sizeOfReturnSet;
        this.regex = regex;
        this.numberOfCrossValidationFolds = numberOfCrossValidationFolds;
    }

    @Override
    public ArrayList<ClassifierEvalDescriptionTriplet> call() throws Exception {
        ArrayList<ClassifierEvalDescriptionTriplet> triplets = new ArrayList<>();
        PriorityQueue<ClassifierEvalDescriptionTriplet> pq = new PriorityQueue<>(sizeOfReturnSet, new Comparator<ClassifierEvalDescriptionTriplet>() {
            @Override
            public int compare(ClassifierEvalDescriptionTriplet o1, ClassifierEvalDescriptionTriplet o2) {
                Double f1 = o1.getEvaluation().fMeasure(1);
                Double f2 = o2.getEvaluation().fMeasure(1);
                if(f1 > f2)
                    return 1;
                if(f2> f1)
                    return -1;
                return 0;
            }
        });

        double gamma = 10;
        int iterations = 10;
        for (int i = 0; i < iterations; i++) {
            double C = 0.01;

            for (int j = 0; j < iterations; j++) {
                String[] options = weka.core.Utils.splitOptions("-S 0 -K 2 -D 3 -G " + gamma + " -R 0.0 -N 0.5 -M 40.0 -C " + C + " -E 0.001 -P 0.1 -model D:\\Apps\\weka-3-7-3 -seed 1");

                LibSVM svm = new LibSVM();
                svm.setOptions(options);
                svm.setKernelType(type);
                FilteredClassifier filtersvm = new FilteredClassifier();
                //filter.setInputFormat(dataset);
                if (filter == null) {
                    RemoveByName filter = new RemoveByName();
                    filter.setExpression(regex);
                }
                filtersvm.setFilter(filter);
                filtersvm.setClassifier(svm);
                filtersvm.buildClassifier(dataset);

                Evaluation eval = new Evaluation(dataset);
                eval.crossValidateModel(filtersvm, dataset, numberOfCrossValidationFolds, new Random());
                if(pq.size() < sizeOfReturnSet || eval.fMeasure(1) > pq.peek().getEvaluation().fMeasure(1)){
                    pq.offer(new ClassifierEvalDescriptionTriplet("SVM(Type:"+type.getSelectedTag().getReadable()+"C:"+C+".Gamma:"+gamma+").Filter:"+filter.getClass().getSimpleName(),
                            eval,filtersvm));
                }
                C = C*5;
                System.out.println("SvmTest of type: "+type.getSelectedTag().getReadable()+" has done "+((i*10+(1+j)))+" iterations of "+iterations*iterations+"!");
            }

            gamma = gamma/5;
        }

        triplets.addAll(pq);

        System.out.println("SvmTest of type: "+type.getSelectedTag().getReadable()+" with filter "+regex+" Done!");

        return triplets;
    }
}
