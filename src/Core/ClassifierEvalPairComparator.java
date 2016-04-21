package Core;

import org.apache.commons.math3.util.Pair;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;

import java.util.Comparator;

/**
 * Created by Rune on 22-03-2016.
 */
public class ClassifierEvalPairComparator implements Comparator<Pair<Classifier,Evaluation>> {


    @Override
    public int compare(Pair<Classifier,Evaluation> p1, Pair<Classifier,Evaluation> p2) {
        return (int) (p1.getValue().fMeasure(0) - p2.getValue().fMeasure(0));
    }
}
