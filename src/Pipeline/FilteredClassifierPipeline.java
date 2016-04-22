package Pipeline;

import ArffFile.CompleteFeatureFileGenerator;
import Core.*;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.Logistic;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.meta.Stacking;
import weka.classifiers.trees.RandomForest;
import weka.core.*;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.RemoveByName;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Rune on 28-03-2016.
 */
public class FilteredClassifierPipeline {
    private static final int CROSS_VALIDATION_NUMBER_OF_FOLDS = 10;
    private static final int PQ_CAPACITY_FOR_SVM = 3;

    public static void main(String[] args) throws Exception {

        //Declare the threadpool
        ExecutorService executorService = Executors.newFixedThreadPool(8);

        /**
         * PARAMETERS
         */
        ClassifierType type_of_classifer = ClassifierType.SIT_STAND_CLASSIFIER;
        System.out.println("Importing data");
        String fromLocation = "D:\\Dropbox\\Thesis\\Data\\RawDataProx";
        //String fromLocation = "D:\\Dropbox\\Thesis\\Data\\RawData";
        String toLocation = "D:\\Dropbox\\Thesis\\Data\\CompleteFeatureFiles\\";
        double window_size_seconds = 3;

        BiasConfiguration biasForIT119 = new BiasConfiguration(
                0.005f, -0.030f, -0.089f,
                -0.011f, 0.024f, 0.083f);
        BiasConfiguration megaBias = new BiasConfiguration(100, 100, 100, 100, 100, 100);
        BiasConfiguration nullBias = new BiasConfiguration(0, 0 , 0, 0, 0, 0);

        List<String> featureFileURIs = CompleteFeatureFileGenerator.createCompleteFeatureFileWithProximitySeparateFile(
                fromLocation,toLocation, window_size_seconds, type_of_classifer, biasForIT119);
        //load in all the different data sets and set weights appropriately
        List<Instances> all_data_inp = new ArrayList<>();
        for (String file : featureFileURIs) {
            Instances data = CompleteFeatureFileGenerator.getInstances(file);
            /*for (Instance instance : data) {
                if(file.contains("hand") && instance.classValue() == 0 && instance.value(instance.numAttributes()-2)> 2){
                    instance.setWeight(1.0);
                } else {
                    instance.setWeight(0.2);
                }
            }*/
            all_data_inp.add(data);
        }

        Instances alldata = new Instances(all_data_inp.get(0));
        for (int i = 1; i < all_data_inp.size(); i++) {
            for (Instance instance : all_data_inp.get(i)) {
                alldata.add(instance);
            }
        }


        //Create filters for desired subsets
        ArrayList<Filter> listOfFilters = new ArrayList<>();
        listOfFilters.add(createFilterInclusive(alldata, new String[]{"ECDF_RAW", "class"}));
        listOfFilters.add(createFilterInclusive(alldata, new String[]{"ECDF_RAW","START_ORIENTATION","END_ORIENTATION","PROXIMITY", "class"}));
        listOfFilters.add(createFilterInclusive(alldata, new String[]{"ECDF_DISC", "class"}));
        listOfFilters.add(createFilterInclusive(alldata, new String[]{"ECDF_DISC", "START_ORIENTATION","END_ORIENTATION","PROXIMITY", "class"}));
        listOfFilters.add(createFilterInclusive(alldata, new String[]{"ECDF_UP", "ECDF_REST", "class"}));
        listOfFilters.add(createFilterInclusive(alldata, new String[]{"ECDF_UP", "ECDF_REST","START_ORIENTATION","END_ORIENTATION","PROXIMITY", "class"}));
        listOfFilters.add(createFilterInclusive(alldata, new String[]{"ECDF_UP", "ECDF_RAW_Y", "class"}));
        listOfFilters.add(createFilterInclusive(alldata, new String[]{"ECDF_UP", "ECDF_RAW_Y", "START_ORIENTATION","END_ORIENTATION","PROXIMITY", "class"}));
//        listOfFilters.add(createFilterInclusive(alldata, new String[]{"TIMED_VERTICAL_BIN","UpAcc_Mean", "class"}));
        listOfFilters.add(createFilterInclusive(alldata, new String[]{"ORIENTATION", "PROXIMITY","class"}));
        listOfFilters.add(createFilterInclusive(alldata, new String[]{"ZERO_CROSSINGS_VERTICAL", "PURITY", "VERTICAL_POSITIVE_ACCELERATION", "VERTICAL_NEGATIVE_ACCELERATION","PROXIMITY", "class"}));
        listOfFilters.add(createFilterInclusive(alldata, new String[]{"ZERO_CROSSINGS_VERTICAL", "PURITY", "VERTICAL_POSITIVE_ACCELERATION", "VERTICAL_NEGATIVE_ACCELERATION", "START_ORIENTATION","END_ORIENTATION","PROXIMITY", "class"}));
//        listOfFilters.add(createFilterInclusive(alldata, new String[]{"NUMBER_OF_RAW_Z_TAPS", "class"}));
        listOfFilters.add(createFilterExclusive(alldata, new String[]{"ECDF","ORIENTATION","TIMED_VERTICAL", "ZERO_CROSSING", "PURITY", "VERTICAL_POSITIVE_ACCELERATION", "VERTICAL_NEGATIVE_ACCELERATION"}));


        //Train individual classifiers
        System.out.println("Training individual classifiers");
        ArrayList<ClassifierEvalDescriptionTriplet> individualClassifiers = trainIndividualClassifiers(executorService, alldata, listOfFilters,type_of_classifer);

        executorService.shutdown();

        Collections.sort(individualClassifiers, new ClassifierEvalDescriptionTripletComparator());


        //Create folder to hold all the new classifiers
        String folderPath = "D:\\Dropbox\\Thesis\\Data\\Output"+System.currentTimeMillis()+"\\";
        //noinspection ResultOfMethodCallIgnored
        new File(folderPath).mkdir();

        //export individual classifiers in case something goes wrong with the ensembles
        exportClassifiers(individualClassifiers.subList(0,20), folderPath, type_of_classifer);

        //get the 3 best classifiers
        Classifier[] the3BestClassifiers = new Classifier[3];
        for (int i = 0; i < 3; i++) {
            the3BestClassifiers[i] = individualClassifiers.get(i).getClassifier();
        }

        //get the 5 best classifiers
        Classifier[] the5BestClassifiers = new Classifier[5];
        for (int i = 0; i < 5; i++) {
            the5BestClassifiers[i] = individualClassifiers.get(i).getClassifier();
        }

        //get the 9 best classifiers
        Classifier[] the9BestClassifiers = new Classifier[9];
        for (int i = 0; i < 9; i++) {
            the9BestClassifiers[i] = individualClassifiers.get(i).getClassifier();
        }

        //Combine the individual classifiers
        System.out.println("Training combined classifiers");
        /*ArrayList<ClassifierEvalDescriptionTriplet> combinedClassifiers = trainCombinedClassifiers(alldata, the3BestClassifiers, the5BestClassifiers, the9BestClassifiers);
        Collections.sort(combinedClassifiers, new ClassifierEvalDescriptionTripletComparator());

        //export the combined classifiers
        exportClassifiers(combinedClassifiers, folderPath);*/

        System.out.println("All done");
    }

    private static ArrayList<ClassifierEvalDescriptionTriplet> trainCombinedClassifiers(Instances alldata, Classifier[] the3BestClassifiers, Classifier[] the5BestClassifiers, Classifier[] the9BestClassifiers) throws Exception {
        ArrayList<ClassifierEvalDescriptionTriplet> metaClassifiers = new ArrayList<>();
        double ridge = 1;
        int cap = 10;
        for (int i = 0; i < cap; i++) {
            long start = System.currentTimeMillis();
            metaClassifiers.addAll(stackingLogisticExperiment(alldata,the3BestClassifiers.clone(), ridge));
            metaClassifiers.addAll(stackingLogisticExperiment(alldata,the5BestClassifiers.clone(), ridge));
//            metaClassifiers.addAll(stackingLogisticExperiment(alldata,the9BestClassifiers.clone(), ridge));
            ridge = ridge/10;
            long end = System.currentTimeMillis();
            System.out.println("Meta iteration + " +(i+1) + " of "+cap+" done. This iteration took "+((end-start)/1000)+ " seconds");
        }
        return metaClassifiers;
    }

    private static ArrayList<ClassifierEvalDescriptionTriplet> trainIndividualClassifiers(ExecutorService executorService, Instances alldata, List<Filter> listOfFilters, ClassifierType type_of_classifer) {
        ArrayList<ClassifierEvalDescriptionTriplet> individualClassifiers = new ArrayList<>();
        ArrayList<Callable<ArrayList<ClassifierEvalDescriptionTriplet>>> individualJobs = new ArrayList<>();
        for (Filter filter : listOfFilters) {
            individualJobs.add(new LogisticGridSearch(alldata,filter, CROSS_VALIDATION_NUMBER_OF_FOLDS));
            //individualJobs.add(new LibSVMGridSearch(alldata,new SelectedTag(LibSVM.KERNELTYPE_POLYNOMIAL, LibSVM.TAGS_KERNELTYPE),PQ_CAPACITY_FOR_SVM,filter,CROSS_VALIDATION_NUMBER_OF_FOLDS));
            individualJobs.add(new LibSVMGridSearch(alldata,type_of_classifer,new SelectedTag(LibSVM.KERNELTYPE_RBF, LibSVM.TAGS_KERNELTYPE),PQ_CAPACITY_FOR_SVM,filter,CROSS_VALIDATION_NUMBER_OF_FOLDS));
            //individualJobs.add(new LibSVMGridSearch(alldata,new SelectedTag(LibSVM.KERNELTYPE_LINEAR, LibSVM.TAGS_KERNELTYPE),PQ_CAPACITY_FOR_SVM,filter,CROSS_VALIDATION_NUMBER_OF_FOLDS));
            individualJobs.add(new NaiveBayesGridSearch(alldata,filter, CROSS_VALIDATION_NUMBER_OF_FOLDS));
            individualJobs.add(new RandomForestGridSearch(alldata,filter, CROSS_VALIDATION_NUMBER_OF_FOLDS));
            individualJobs.add(new NearestNeighborGridSearch(alldata,new ManhattanDistance(),filter, CROSS_VALIDATION_NUMBER_OF_FOLDS));
            individualJobs.add(new NearestNeighborGridSearch(alldata,new EuclideanDistance(),filter, CROSS_VALIDATION_NUMBER_OF_FOLDS));
        }


        List<Future<ArrayList<ClassifierEvalDescriptionTriplet>>> individualClassifierFutures = null;
        try {
            individualClassifierFutures = executorService.invokeAll(individualJobs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        if (individualClassifierFutures != null) {
            for (Future<ArrayList<ClassifierEvalDescriptionTriplet>> future: individualClassifierFutures) {
                try {
                    individualClassifiers.addAll(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        return individualClassifiers;
    }

    /**
     *
     * @param alldata The set of instances to create the filter for
     * @param listOfIncludeAttributes The list of attribute names to be included (attribute name needs only contain the string)
     * @return A Filter filtering out all attributes that does not contain a string in listOfIncludeAttributes
     */
    private static Filter createFilterInclusive(Instances alldata, String[] listOfIncludeAttributes) {
        String indexListAsString = getIndexListStringInvertedSelected(alldata, Arrays.asList(listOfIncludeAttributes));
        return getRemoveFilterFromString(indexListAsString);
    }


    /**
     *
     * @param alldata The set of instances to create the filter for
     * @param listOfExcludeAttributes The list of attribute names to be excuded (attribute name needs only contain the string)
     * @return A Filter filtering out all attributes that does contain a string in listOfIncludeAttributes
     */
    private static Filter createFilterExclusive(Instances alldata, String[] listOfExcludeAttributes){
        String indexListAsString = getIndexListString(alldata, Arrays.asList(listOfExcludeAttributes));
        return getRemoveFilterFromString(indexListAsString);
    }

    private static Remove getRemoveFilterFromString(String listOfIndexes) {
        Remove filter = new Remove();
        String[] options = new String[2];
        options[0] = "-R";
        options[1] = listOfIndexes;
        try {
            filter.setOptions(options);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return filter;
    }

    private static String getIndexListString(Instances alldata, List<String> queryStrings) {
        String listOfIndexes = "";
        for (int i = 0; i < alldata.numAttributes(); i++) {
            if (containsAnyFromList(queryStrings, alldata.attribute(i).name())) {
                listOfIndexes = listOfIndexes.concat(","+(1+i));
            }
        }
        //Strip preceding , before return
        return listOfIndexes.substring(1);
    }

    private static String getIndexListStringInvertedSelected(Instances alldata, List<String> queryStrings) {
        String listOfIndexes = "";
        for (int i = 0; i < alldata.numAttributes(); i++) {
            if (!containsAnyFromList(queryStrings, alldata.attribute(i).name())) {
                listOfIndexes = listOfIndexes.concat(","+(1+i));
            }
        }
        //Strip preceding , before return
        return listOfIndexes.substring(1);
    }

    private static boolean containsAnyFromList(List<String> queryList, String inputString){
        for (String aQueryList : queryList) {
            if (inputString.contains(aQueryList))
                return true;
        }
        return false;
    }

    private static ArrayList<ClassifierEvalDescriptionTriplet> stackingLogisticExperiment(Instances dataset, Classifier[] classifiers, double ridge) throws Exception {
        ArrayList<ClassifierEvalDescriptionTriplet> triplets = new ArrayList<>();
        Stacking stacking = new Stacking();
        stacking.setClassifiers(classifiers);
        Logistic metaclassifier = new Logistic();
        metaclassifier.setRidge(ridge);
        stacking.setMetaClassifier(metaclassifier);
        stacking.buildClassifier(dataset);
        Evaluation evalStack = new Evaluation(dataset);
        evalStack.crossValidateModel(stacking, dataset, CROSS_VALIDATION_NUMBER_OF_FOLDS, new Random());

        String description = "Stack off: \n";
        for (Classifier individualClassifier :
                classifiers) {
            description = description.concat(individualClassifier.toString());
        }

        triplets.add(new ClassifierEvalDescriptionTriplet(description, evalStack, stacking));
        return triplets;
    }

    private static void exportClassifiers(List<ClassifierEvalDescriptionTriplet> pairs, String folderpath, ClassifierType type) throws Exception {

        ArrayList<String> stringOutput = new ArrayList<>();

        Collections.sort(pairs, new ClassifierEvalDescriptionTripletComparator());


        for (ClassifierEvalDescriptionTriplet triplet: pairs){
            Classifier classifier = triplet.getClassifier();
            Evaluation eval = triplet.getEvaluation();
            String desc = triplet.getDescription();

            String filename = System.currentTimeMillis()+ "_"+ classifier.getClass().getSimpleName()+type.toString();

            ObjectOutputStream oss = new ObjectOutputStream(new FileOutputStream(folderpath+filename.toLowerCase()+".model"));
            oss.writeObject(classifier);
            oss.flush();
            oss.close();
            stringOutput.clear();
            stringOutput.add(classifier.toString());
            stringOutput.add(filename);
            stringOutput.add(eval.toClassDetailsString());
            stringOutput.add(eval.toMatrixString());
            Util.saveAsFile(stringOutput,(folderpath+filename+".txt"));

        }
    }
}
