package RandomTest;

import Pipeline.ClassificationPipeline;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.Logistic;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.meta.Stacking;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.core.converters.LibSVMLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.RemoveByName;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by Rune on 28-03-2016.
 */
public class FilteredClassifierTest {
    public static void main(String[] args) throws Exception {
        //Import all data
        String fileLocation = "D:\\Dropbox\\Thesis\\Data\\RawData";
        ArrayList<String> allDataFile = new ArrayList<>();
        allDataFile.add(ClassificationPipeline.createECDFAllFile(fileLocation));
        Instances alldata = ClassificationPipeline.getInstances(allDataFile).get(0);


        //TODO: Split into desired substs
        //Create filters for desired subsets
        RemoveByName selectOnlyRaw = new RemoveByName();
        selectOnlyRaw.setExpression("^ECDF_(DISC_.|UP|REST)_BIN_(..|.)_OF_..*");
        selectOnlyRaw.setInputFormat(alldata);

        RemoveByName selectOnlyDisc = new RemoveByName();
        selectOnlyDisc.setExpression("^ECDF_(RAW_.|UP|REST)_BIN_(..|.)_OF_..*");
        selectOnlyDisc.setInputFormat(alldata);

        RemoveByName selectOnlyUpDown = new RemoveByName();
        selectOnlyUpDown.setExpression("^ECDF_(RAW|DISC)_(Z|Y|X)_BIN_(..|.)_OF_..*");
        selectOnlyUpDown.setInputFormat(alldata);

        RemoveByName selectOnlyUpRawY = new RemoveByName();
        selectOnlyUpRawY.setExpression("^ECDF_(DISC_.|RAW_X|RAW_Z|REST)_BIN_(..|.)_OF_..*");
        selectOnlyUpRawY.setInputFormat(alldata);

        RandomForest rf = new RandomForest();
        rf.setNumTrees(50);
        FilteredClassifier filteredrf = new FilteredClassifier();
        filteredrf.setFilter(selectOnlyRaw);
        filteredrf.setClassifier(rf);
        filteredrf.buildClassifier(alldata);

        Logistic log = new Logistic();
        log.setRidge(0.1);
        FilteredClassifier filteredLog = new FilteredClassifier();
        filteredLog.setFilter(selectOnlyDisc);
        filteredLog.setClassifier(log);
        filteredLog.buildClassifier(alldata);

        Classifier[] classifiers = new Classifier[2];
        classifiers[0] = filteredLog;
        classifiers[1] = filteredrf;

        Logistic logMeta = new Logistic();
        logMeta.setRidge(1);

        Stacking stack = new Stacking();
        stack.setClassifiers(classifiers);
        stack.setMetaClassifier(logMeta);
        stack.buildClassifier(alldata);

        Evaluation eval = new Evaluation(alldata);
        eval.crossValidateModel(stack,alldata,10, new Random());
        System.out.println(eval.toClassDetailsString());
        System.out.println(eval.toMatrixString());
    }
}
