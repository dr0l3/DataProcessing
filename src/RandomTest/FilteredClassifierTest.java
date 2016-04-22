package RandomTest;

import Core.ClassifierEvalDescriptionTriplet;
import Core.ClassifierEvalDescriptionTripletComparator;
import Core.Util;
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

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        /*RemoveByName selectOnlyRaw = new RemoveByName();
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
        System.out.println(eval.toMatrixString());*/
        LibSVM firstClassifier = new LibSVM();
        FilteredClassifier filt = new FilteredClassifier();
        filt.setClassifier(firstClassifier);
        filt.setFilter(new RemoveByName());

        LibSVM secondClassifier = new LibSVM();
        Classifier classifier = new LibSVM();
        if(classifier instanceof LibSVM){
            ((LibSVM) classifier).setProbabilityEstimates(true);
        }
        if(filt.getClassifier() instanceof LibSVM){
            ((LibSVM) filt.getClassifier()).setProbabilityEstimates(true);
        }
        classifier.buildClassifier(alldata);
        secondClassifier.buildClassifier(alldata);
        filt.buildClassifier(alldata);

        exportClassifiers(classifier, "classifier");
        exportClassifiers(secondClassifier, "secondClassifier");
        exportClassifiers(filt, "filt");

        classifier = importClassifier("classifier");
        secondClassifier = (LibSVM) importClassifier("secondClassifier");
        filt = (FilteredClassifier) importClassifier("filt");

        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            int testInstance = random.nextInt(alldata.numInstances()-1);
            System.out.print(Arrays.toString(classifier.distributionForInstance(alldata.get(testInstance)))+ " = ");
            System.out.println(classifier.classifyInstance(alldata.get(testInstance)));
            System.out.print(Arrays.toString(secondClassifier.distributionForInstance(alldata.get(testInstance)))+ " = ");
            System.out.println(secondClassifier.classifyInstance(alldata.get(testInstance)));
            System.out.print(Arrays.toString(filt.distributionForInstance(alldata.get(testInstance)))+ " = ");
            System.out.println(filt.classifyInstance(alldata.get(testInstance)));
        }
    }

    private static Classifier importClassifier(String classifier) {
        String folderpath = "D:\\Dropbox\\Thesis\\Data\\TestRandomTest\\";
        try {
            InputStream is = new FileInputStream(folderpath+classifier+".model");
            Classifier classifier1 = (Classifier) weka.core.SerializationHelper.read(is);
            return classifier1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void exportClassifiers(Classifier classifier, String name) {

        String folderpath = "D:\\Dropbox\\Thesis\\Data\\TestRandomTest\\";
        String filename = System.currentTimeMillis()+ "_"+ classifier.getClass().getSimpleName();

        try {
            ObjectOutputStream oss = new ObjectOutputStream(new FileOutputStream(folderpath+name+".model"));
            oss.writeObject(classifier);
            oss.flush();
            oss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
