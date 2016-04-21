package Core;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;

/**
 * Created by Rune on 28-03-2016.
 */
public class ClassifierEvalDescriptionTriplet {
    private String description;
    private Evaluation evaluation;
    private Classifier classifier;

    public ClassifierEvalDescriptionTriplet(String description, Evaluation evaluation, Classifier classifier) {
        this.description = description;
        this.evaluation = evaluation;
        this.classifier = classifier;
    }

    public String getDescription() {
        return description;
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public Classifier getClassifier() {
        return classifier;
    }
}
