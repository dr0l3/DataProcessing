package Core;

import org.apache.commons.math3.util.Pair;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;

import java.util.Comparator;

/**
 * Created by Rune on 24-03-2016.
 */
public class ClassifierEvaluationPairComparator implements Comparator<Pair<Classifier,Evaluation>> {
    @Override
    public int compare(Pair<Classifier, Evaluation> o1, Pair<Classifier, Evaluation> o2) {
        Double f1 = o1.getValue().fMeasure(1);
        Double f2 = o2.getValue().fMeasure(1);
        if(f1 > f2)
            return -1;
        if(f2> f1)
            return 1;
        return 0;
    }
}
