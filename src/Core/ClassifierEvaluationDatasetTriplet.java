package Core;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

/**
 * Created by Rune on 22-03-2016.
 */
public class ClassifierEvaluationDatasetTriplet {
    Classifier classifier;
    Evaluation evaluation;
    Instances dataset;

    public ClassifierEvaluationDatasetTriplet(Classifier classifier, Evaluation evaluation, Instances dataset) {
        this.classifier = classifier;
        this.evaluation = evaluation;
        this.dataset = dataset;
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public Instances getDataset() {
        return dataset;
    }
}
