package RandomTest;

import weka.classifiers.Classifier;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.Vote;

/**
 * Created by Rune on 24-03-2016.
 */
public class ClassifierNameTester {

    public static void main(String[] args) {
        Classifier svm = new LibSVM();
        System.out.println(svm.getClass().getSimpleName());
        Classifier vote = new Vote();
        System.out.println(vote.getClass().getSimpleName());
    }
}
