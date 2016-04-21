package Core;

import java.util.Comparator;

/**
 * Created by Rune on 28-03-2016.
 */
public class ClassifierEvalDescriptionTripletComparator implements Comparator<ClassifierEvalDescriptionTriplet> {
    @Override
    public int compare(ClassifierEvalDescriptionTriplet o1, ClassifierEvalDescriptionTriplet o2) {
        Double f1 = o1.getEvaluation().fMeasure(0);
        Double f2 = o2.getEvaluation().fMeasure(0);
        if(f1 > f2)
            return -1;
        if(f2> f1)
            return 1;
        return 0;
    }
}
