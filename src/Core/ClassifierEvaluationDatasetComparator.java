package Core;

import java.util.Comparator;

/**
 * Created by Rune on 22-03-2016.
 */
public class ClassifierEvaluationDatasetComparator implements Comparator<ClassifierEvaluationDatasetTriplet> {
    @Override
    public int compare(ClassifierEvaluationDatasetTriplet o1, ClassifierEvaluationDatasetTriplet o2) {
        return (int) (o2.getEvaluation().fMeasure(0) - o1.getEvaluation().fMeasure(0));
    }
}
