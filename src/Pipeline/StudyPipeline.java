package Pipeline;

import ArffFile.CompleteFeatureFileGenerator;
import Core.BiasConfiguration;
import Core.ClassifierEvalDescriptionTriplet;
import Core.ClassifierEvalDescriptionTripletComparator;
import Core.ClassifierType;
import weka.classifiers.functions.LibSVM;
import weka.core.*;
import weka.filters.Filter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Rune on 15-05-2016.
 */
public class StudyPipeline {

    private static final int CROSS_VALIDATION_NUMBER_OF_FOLDS = 10;
    private static final int PQ_CAPACITY_FOR_SVM = 3;


    public static void main(String[] args){
        if(args.length != 2){
            System.out.println("Wrong number of arguments, expected 2");
            return;
        }
        String path_to_new_events = args[0];
        String path_to_save_classifiers_in = args[1];

        //import original data
        ExecutorService executorService = Executors.newFixedThreadPool(8);

        try {
            createClassifiers(ClassifierType.EVENT_SNIFFER, path_to_new_events, path_to_save_classifiers_in, executorService);
            createClassifiers(ClassifierType.SIT_STAND_CLASSIFIER, path_to_new_events, path_to_save_classifiers_in, executorService);
        } catch (Exception e) {
            e.printStackTrace();
        }

        executorService.shutdown();

    }

    private static void createClassifiers(ClassifierType type_of_classifer, String path_to_new_events, String path_to_save_classifiers_in ,ExecutorService executorService ) throws Exception {
        System.out.println("Importing data");
        String fromLocation = "D:\\Dropbox\\Thesis\\Data\\RawDataProx";
        String toLocation = "D:\\Dropbox\\Thesis\\Data\\CompleteFeatureFiles\\";
        double window_size_seconds = 3;

        BiasConfiguration biasForIT119 = new BiasConfiguration(
                0.005f, -0.030f, -0.089f,
                -0.011f, 0.024f, 0.083f);


        List<String> featureFileURIs = CompleteFeatureFileGenerator.createCompleteFeatureFileWithProximitySeparateFile(
                fromLocation,toLocation, window_size_seconds, type_of_classifer, biasForIT119);

        List<Instances> all_data_inp = new ArrayList<>();
        for (String file : featureFileURIs) {
            Instances data = CompleteFeatureFileGenerator.getInstances(file);
            for (Instance instance : data) {
                instance.setWeight(0.75); //TODO: It would be better if this was somehow a function of time
            }
            all_data_inp.add(data);
        }

        Instances alldata = new Instances(all_data_inp.get(0));
        for (int i = 1; i < all_data_inp.size(); i++) {
            for (Instance instance : all_data_inp.get(i)) {
                alldata.add(instance);
            }
        }

        //import new data
        String filename_for_new_feature_file =
                CompleteFeatureFileGenerator.createCompleteFeatureaFileWithProximityFromWindowFiles(
                        path_to_new_events,
                        path_to_new_events+"\\newfeatures.arff",
                        type_of_classifer);

        if (filename_for_new_feature_file == null){
            //no new data
        } else {
            //convert new data to instances
            Instances data_for_new_windows = CompleteFeatureFileGenerator.getInstances(filename_for_new_feature_file);
            for (Instance instance : data_for_new_windows) {
                instance.setWeight(1);
            }

            for (Instance data_for_new_window : data_for_new_windows) {
                alldata.add(data_for_new_window);
            }

        }



        //create filters
        ArrayList<Filter> listOfFilters = new ArrayList<>();
        //listOfFilters.add(FilteredClassifierPipeline.createFilterInclusive(alldata, new String[]{"ECDF_RAW", "class"}));
        //listOfFilters.add(FilteredClassifierPipeline.createFilterInclusive(alldata, new String[]{"ECDF_RAW","START_ORIENTATION","END_ORIENTATION","PROXIMITY", "class"}));
        listOfFilters.add(FilteredClassifierPipeline.createFilterInclusive(alldata, new String[]{"ECDF_DISC", "class"}));
        listOfFilters.add(FilteredClassifierPipeline.createFilterInclusive(alldata, new String[]{"ECDF_DISC", "START_ORIENTATION","END_ORIENTATION","PROXIMITY", "class"}));
        listOfFilters.add(FilteredClassifierPipeline.createFilterInclusive(alldata, new String[]{"ECDF_UP", "ECDF_REST", "class"}));
        listOfFilters.add(FilteredClassifierPipeline.createFilterInclusive(alldata, new String[]{"ECDF_UP", "ECDF_REST","START_ORIENTATION","END_ORIENTATION","PROXIMITY", "class"}));
        //listOfFilters.add(FilteredClassifierPipeline.createFilterInclusive(alldata, new String[]{"ECDF_UP", "ECDF_RAW_Y", "class"}));
        //listOfFilters.add(FilteredClassifierPipeline.createFilterInclusive(alldata, new String[]{"ECDF_UP", "ECDF_RAW_Y", "START_ORIENTATION","END_ORIENTATION","PROXIMITY", "class"}));
        //listOfFilters.add(FilteredClassifierPipeline.createFilterInclusive(alldata, new String[]{"ORIENTATION", "PROXIMITY","class"}));
        listOfFilters.add(FilteredClassifierPipeline.createFilterInclusive(alldata, new String[]{"ZERO_CROSSINGS_VERTICAL", "PURITY", "VERTICAL_POSITIVE_ACCELERATION", "VERTICAL_NEGATIVE_ACCELERATION","PROXIMITY", "class"}));
        listOfFilters.add(FilteredClassifierPipeline.createFilterInclusive(alldata, new String[]{"ZERO_CROSSINGS_VERTICAL", "PURITY", "VERTICAL_POSITIVE_ACCELERATION", "VERTICAL_NEGATIVE_ACCELERATION", "START_ORIENTATION","END_ORIENTATION","PROXIMITY", "class"}));
        //listOfFilters.add(FilteredClassifierPipeline.createFilterExclusive(alldata, new String[]{"ECDF","ORIENTATION","TIMED_VERTICAL", "ZERO_CROSSING", "PURITY", "VERTICAL_POSITIVE_ACCELERATION", "VERTICAL_NEGATIVE_ACCELERATION"}));
        //train the classifiers
        ArrayList<ClassifierEvalDescriptionTriplet> individualClassifiers = trainIndividualClassifiers(executorService, alldata, listOfFilters,type_of_classifer);

        Collections.sort(individualClassifiers, new ClassifierEvalDescriptionTripletComparator());
        //output the best one
        String folderPath = "D:\\Dropbox\\Thesis\\Data\\Output"+System.currentTimeMillis()+"\\";
        //noinspection ResultOfMethodCallIgnored
        new File(folderPath).mkdir();

        //export individual classifiers in case something goes wrong with the ensembles
        FilteredClassifierPipeline.exportClassifiers(individualClassifiers.subList(0,1), path_to_save_classifiers_in, type_of_classifer);
    }

    private static ArrayList<ClassifierEvalDescriptionTriplet> trainIndividualClassifiers(ExecutorService executorService, Instances alldata, List<Filter> listOfFilters, ClassifierType type_of_classifer) {
        ArrayList<ClassifierEvalDescriptionTriplet> individualClassifiers = new ArrayList<>();
        ArrayList<Callable<ArrayList<ClassifierEvalDescriptionTriplet>>> individualJobs = new ArrayList<>();
        for (Filter filter : listOfFilters) {
            individualJobs.add(new LibSVMGridSearch(alldata,type_of_classifer,new SelectedTag(LibSVM.KERNELTYPE_RBF, LibSVM.TAGS_KERNELTYPE),PQ_CAPACITY_FOR_SVM,filter,CROSS_VALIDATION_NUMBER_OF_FOLDS));
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
}
