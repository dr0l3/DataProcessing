package Pipeline;

import Core.ClassifierEvaluationPairComparator;
import Core.RawlineToWindowConverter;
import Core.Util;
import Core.Window;
import org.apache.commons.math3.util.Pair;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.Logistic;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.Stacking;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.RandomForest;
import weka.core.*;
import weka.core.converters.ConverterUtils;
import weka.core.neighboursearch.LinearNNSearch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by Rune on 22-03-2016.
 */
public class ClassificationPipeline {

    private static int CROSS_VALIDATION_NUMBER = 10;

    public static void main(String[] args) throws Exception {

        ExecutorService executorService = Executors.newFixedThreadPool(8);

        String fileLocation = "D:\\Dropbox\\Thesis\\Data\\RawData";

        int starttime = (int) System.currentTimeMillis();
        ArrayList<String> featureFiles = new ArrayList<>();
        //create a lot of arff files
        Util.updateProgressWithText("Creating feature files");
        featureFiles.add(createECDFUpAndYFile(fileLocation));
        featureFiles.add(createECDFRawFile(fileLocation));
        featureFiles.add(createECDFDiscFile(fileLocation));
        featureFiles.add(createECDFUpDownFile(fileLocation));

        //create data sources
        Util.updateProgressWithText("Creating data sources");
        ArrayList<Instances> instances = getInstances(featureFiles);

        //Create all features dataset
        ArrayList<String> allDataFile = new ArrayList<>();
        allDataFile.add(createECDFAllFile(fileLocation));
        ArrayList<Instances> allDataInstances = getInstances(allDataFile);

        ArrayList<Pair<Classifier,Evaluation>> classifierPairs = new ArrayList<>();
        ArrayList<Callable<ArrayList<Pair<Classifier, Evaluation>>>> jobs = new ArrayList<>();

        Util.updateProgressWithText("Starting testing of individual models");

        //Schedule the test jobs
        for (Instances instance : instances) {
            jobs.add(new RandomForestTest(instance));
            jobs.add(new LibSvmLinearTest(instance, new SelectedTag(LibSVM.KERNELTYPE_LINEAR, LibSVM.TAGS_KERNELTYPE)));
            jobs.add(new LibSvmLinearTest(instance, new SelectedTag(LibSVM.KERNELTYPE_RBF, LibSVM.TAGS_KERNELTYPE)));
            jobs.add(new LibSvmLinearTest(instance, new SelectedTag(LibSVM.KERNELTYPE_POLYNOMIAL, LibSVM.TAGS_KERNELTYPE)));
            jobs.add(new LibSvmLinearTest(instance, new SelectedTag(LibSVM.KERNELTYPE_SIGMOID, LibSVM.TAGS_KERNELTYPE)));
            jobs.add(new NaiveBayesTest(instance));
            jobs.add(new LogisticTest(instance));
            jobs.add(new NearestNeighborTest(instance, new ManhattanDistance()));
            jobs.add(new NearestNeighborTest(instance, new EuclideanDistance()));
        }

        List<Future<ArrayList<Pair<Classifier,Evaluation>>>> testFutures = executorService.invokeAll(jobs);

        for (Future<ArrayList<Pair<Classifier, Evaluation>>> testFuture : testFutures) {
            classifierPairs.addAll(testFuture.get());
        }

        Util.updateProgressWithText("Testing of individual models complete");




        //sort the classifiers according to performance
        Collections.sort(classifierPairs, new ClassifierEvaluationPairComparator());

        Util.updateProgressWithText("Creating meta classifiers");

        //get the 3 best classifiers
        Classifier[] the3BestClassifiers = new Classifier[3];
        for (int i = 0; i < 3; i++) {
            the3BestClassifiers[i] = classifierPairs.get(i).getKey();
        }

        //get the 5 best classifiers
        Classifier[] the5BestClassifiers = new Classifier[5];
        for (int i = 0; i < 5; i++) {
            the5BestClassifiers[i] = classifierPairs.get(i).getKey();
        }

        //get the 10 best classifiers
        Classifier[] the10BestClassifiers = new Classifier[10];
        for (int i = 0; i < 10; i++) {
            the10BestClassifiers[i] = classifierPairs.get(i).getKey();
        }

        ArrayList<Pair<Classifier, Evaluation>> metaClassifiers = new ArrayList<>();

        /*ArrayList<Callable<ArrayList<Pair<Classifier, Evaluation>>>> metaJobs = new ArrayList<>();

        metaJobs.add(new StackingTester(allDataInstances.get(0), the3BestClassifiers.clone()));
        metaJobs.add(new StackingTester(allDataInstances.get(0), the5BestClassifiers.clone()));
        metaJobs.add(new StackingTester(allDataInstances.get(0), the10BestClassifiers.clone()));

        //metaJobs.add(new VotingTester(allDataInstances.get(0), the3BestClassifiers.clone()));
        //metaJobs.add(new VotingTester(allDataInstances.get(0), the5BestClassifiers.clone()));
        //metaJobs.add(new VotingTester(allDataInstances.get(0), the10BestClassifiers.clone()));

        List<Future<ArrayList<Pair<Classifier,Evaluation>>>> metaFutures = executorService.invokeAll(metaJobs);

        for (Future<ArrayList<Pair<Classifier, Evaluation>>> metaFuture : metaFutures) {
            metaClassifiers.addAll(metaFuture.get());
        }


        executorService.shutdown();*/

        String folderpath = "D:\\Dropbox\\Thesis\\Data\\Output"+System.currentTimeMillis()+"\\";
        File dir = new File(folderpath);
        dir.mkdir();

        exportClassifiers(classifierPairs.subList(0,50), folderpath);
        //exportClassifiers(metaClassifiers, folderpath);

        Util.updateProgressWithText("Done creating meta classifiers");

        System.out.println();
        System.out.println("----------------------------------------------------------------");
        System.out.println("Number of classifiers: " + classifierPairs.size());
        System.out.println("----------------------------------------------------------------");

        //Print the meta classifiers

        for (Pair<Classifier, Evaluation> pair : metaClassifiers) {
            System.out.println(pair.getKey().toString());
            System.out.println(pair.getKey().getCapabilities().toString());
            System.out.println(pair.getValue().toClassDetailsString());
            System.out.println(pair.getValue().toMatrixString());
            System.out.println("----------------------------------------------------------------");
        }

        for (int i = 0; i < 10; i++) {
            Evaluation eval = classifierPairs.get(i).getValue();
            System.out.println(classifierPairs.get(i).getKey().toString());
            System.out.println(eval.toClassDetailsString());
            System.out.println(eval.toMatrixString());
            System.out.println("----------------------------------------------------------------");
        }

        int endtime = (int) System.currentTimeMillis();

        System.out.println("Execution time in seconds: " + ((endtime-starttime)/ 1000));
    }

    private static void exportClassifiers(List<Pair<Classifier, Evaluation>> pairs, String folderpath) throws Exception {

        ArrayList<String> stringOutput = new ArrayList<>();

        Collections.sort(pairs, new ClassifierEvaluationPairComparator());


        for (Pair<Classifier, Evaluation> pair: pairs){
            Classifier classifier = pair.getKey();
            Evaluation eval = pair.getValue();
            String filename = System.currentTimeMillis()+ "_"+ classifier.getClass().getSimpleName();

            ObjectOutputStream oss = new ObjectOutputStream(new FileOutputStream(folderpath+filename+".model"));
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


    private static class VotingTester implements Callable<ArrayList<Pair<Classifier, Evaluation>>>{

        private Instances dataset;
        private Classifier[] classifiers;

        public VotingTester(Instances dataset, Classifier[] classifiers) {
            this.dataset = dataset;
            this.classifiers = classifiers;
        }

        @Override
        public ArrayList<Pair<Classifier, Evaluation>> call() throws Exception {
            ArrayList<Pair<Classifier, Evaluation>> pairs = new ArrayList<>();
            Vote combinedClassifier = new Vote();
            combinedClassifier.setClassifiers(classifiers);
//            combinedClassifier.buildClassifier(dataset);
            Evaluation eval = new Evaluation(dataset);
            eval.crossValidateModel(combinedClassifier, dataset, CROSS_VALIDATION_NUMBER, new Random());
            pairs.add(new Pair<Classifier, Evaluation>(combinedClassifier, eval));
            return pairs;
        }
    }

    private static class StackingTester implements Callable<ArrayList<Pair<Classifier, Evaluation>>>{

        private Instances dataset;
        private Classifier[] classifiers;

        public StackingTester(Instances dataset, Classifier[] classifiers) {
            this.dataset = dataset;
            this.classifiers = classifiers;
        }

        @Override
        public ArrayList<Pair<Classifier, Evaluation>> call() throws Exception {

            ArrayList<Pair<Classifier, Evaluation>> stacks = new ArrayList<>();
            double ridge = 1;
            for (int i = 0; i < 5; i++) {
                Stacking stacking = new Stacking();
                stacking.setClassifiers(classifiers);
                Logistic metaclassifier = new Logistic();
                metaclassifier.setRidge(ridge);
                stacking.setMetaClassifier(metaclassifier);
                stacking.buildClassifier(dataset);
                Evaluation evalStack = new Evaluation(dataset);
                evalStack.crossValidateModel(stacking, dataset, CROSS_VALIDATION_NUMBER, new Random());

                stacks.add(new Pair<Classifier, Evaluation>(stacking,evalStack));
                ridge = ridge/10;
            }

            return stacks;
        }
    }

    private static class NearestNeighborTest implements Callable<ArrayList<Pair<Classifier, Evaluation>>>{

        private Instances dataset;
        private DistanceFunction distanceFunction;

        public NearestNeighborTest(Instances dataset, DistanceFunction distanceFunction) {
            this.dataset = dataset;
            this.distanceFunction = distanceFunction;
        }

        @Override
        public ArrayList<Pair<Classifier, Evaluation>> call() throws Exception {

            ArrayList<Pair<Classifier, Evaluation>> pairs = new ArrayList<>();

            int k = 1;
            for (int i = 0; i < 5; i++) {
                IBk classifier = new IBk();
                classifier.setKNN(k);
                LinearNNSearch search = new LinearNNSearch();
                search.setDistanceFunction(distanceFunction);
                classifier.setNearestNeighbourSearchAlgorithm(search);
                classifier.buildClassifier(dataset);

                Evaluation eval = new Evaluation(dataset);
                eval.crossValidateModel(classifier, dataset, CROSS_VALIDATION_NUMBER, new Random());


                pairs.add(new Pair<Classifier, Evaluation>(classifier,eval));
                k = k*2;
            }

            System.out.println("NearestNeighborTest with distance function: "+ distanceFunction.getClass().getSimpleName()+" Done!");

            return pairs;
        }
    }

    private static class LogisticTest implements Callable<ArrayList<Pair<Classifier, Evaluation>>>{

        private Instances dataset;

        public LogisticTest(Instances dataset) {
            this.dataset = dataset;
        }

        @Override
        public ArrayList<Pair<Classifier, Evaluation>> call() throws Exception {
            ArrayList<Pair<Classifier,Evaluation>> pairs = new ArrayList<>();

            double ridge = 1;

            for (int i = 0; i < 10; i++) {
                String[] options = weka.core.Utils.splitOptions("-R "+ridge+ " -M -1");
                Logistic logistic = new Logistic();
                logistic.setOptions(options);

                logistic.buildClassifier(dataset);

                Evaluation eval = new Evaluation(dataset);
                eval.crossValidateModel(logistic,dataset, CROSS_VALIDATION_NUMBER, new Random());
                pairs.add(new Pair<Classifier, Evaluation>(logistic,eval));
                ridge = ridge/10;
            }

            System.out.println("LogisticTest Done!");

            return pairs;
        }
    }

    private static class NaiveBayesTest implements Callable<ArrayList<Pair<Classifier, Evaluation>>>{

        private Instances dataset;

        public NaiveBayesTest(Instances dataset) {
            this.dataset = dataset;
        }

        @Override
        public ArrayList<Pair<Classifier, Evaluation>> call() throws Exception {
            ArrayList<Pair<Classifier,Evaluation>> pairs = new ArrayList<>();

            String[] options = weka.core.Utils.splitOptions("-K");

            NaiveBayes naiveBayes = new NaiveBayes();
            naiveBayes.setOptions(options);
            naiveBayes.buildClassifier(dataset);

            Evaluation eval = new Evaluation(dataset);
            eval.crossValidateModel(naiveBayes,dataset, CROSS_VALIDATION_NUMBER, new Random());

            pairs.add(new Pair<Classifier, Evaluation>(naiveBayes,eval));

            naiveBayes = new NaiveBayes();
            naiveBayes.setOptions(weka.core.Utils.splitOptions(""));
            naiveBayes.buildClassifier(dataset);

            eval = new Evaluation(dataset);
            eval.crossValidateModel(naiveBayes,dataset, CROSS_VALIDATION_NUMBER, new Random());

            pairs.add(new Pair<Classifier, Evaluation>(naiveBayes,eval));

            System.out.println("NaiveBayesTest Done!");

            return pairs;
        }
    }

    private static class LibSvmLinearTest implements Callable<ArrayList<Pair<Classifier,Evaluation>>> {

        private Instances dataset;
        private SelectedTag type;
        private int PQ_CAPACITY = 10;

        public LibSvmLinearTest(Instances dataset, SelectedTag type) {
            this.dataset = dataset;
            this.type = type;
        }

        @Override
        public ArrayList<Pair<Classifier, Evaluation>> call() throws Exception {

            ArrayList<Pair<Classifier,Evaluation>> pairs = new ArrayList<>();
            PriorityQueue<Pair<Classifier, Evaluation>> pq = new PriorityQueue<>(PQ_CAPACITY, new Comparator<Pair<Classifier, Evaluation>>() {
                @Override
                public int compare(Pair<Classifier, Evaluation> o1, Pair<Classifier, Evaluation> o2) {
                    Double f1 = o1.getValue().fMeasure(1);
                    Double f2 = o2.getValue().fMeasure(1);
                    if(f1 > f2)
                        return 1;
                    if(f2> f1)
                        return -1;
                    return 0;
                }
            });

            double C = 0.01;
            double gamma = 10;
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    String[] options = weka.core.Utils.splitOptions("-S 0 -K 2 -D 3 -G "+gamma+" -R 0.0 -N 0.5 -M 40.0 -C " +C+ " -E 0.001 -P 0.1 -model D:\\Apps\\weka-3-7-3 -seed 1");

                    LibSVM svm = new LibSVM();
                    svm.setKernelType(type);
                    svm.setOptions(options);
                    svm.buildClassifier(dataset);

                    Evaluation eval = new Evaluation(dataset);
                    eval.crossValidateModel(svm, dataset, CROSS_VALIDATION_NUMBER, new Random());
                    if(pq.size() < PQ_CAPACITY || eval.fMeasure(1) > pq.peek().getValue().fMeasure(1)){
                        pq.offer(new Pair<Classifier,Evaluation>(svm,eval));
                    }
                }
                C = C*5;
                gamma = gamma/5;
                System.out.println("SvmTest of type: "+type.getSelectedTag().getReadable()+" has done "+((i+1)*20)+" iterations!");
            }

            pairs.addAll(pq);

            System.out.println("SvmTest of type: "+type.getSelectedTag().getReadable()+" Done!");

            return pairs;
        }
    }

    private static class RandomForestTest implements Callable<ArrayList<Pair<Classifier, Evaluation>>> {
        private Instances dataset;

        public RandomForestTest(Instances dataset){
            this.dataset = dataset;
        }

        @Override
        public ArrayList<Pair<Classifier, Evaluation>> call() throws Exception {
            ArrayList<Pair<Classifier,Evaluation>> pairs = new ArrayList<>();

            double numberOfTrees = 10;
            for (int i = 0; i < 10; i++) {
                String[] options = weka.core.Utils.splitOptions("-I " + (int) numberOfTrees +" -K 0 -S 1");
                RandomForest rf = new RandomForest();
                rf.setOptions(options);
                rf.buildClassifier(dataset);

                Evaluation eval = new Evaluation(dataset);
                eval.crossValidateModel(rf, dataset, CROSS_VALIDATION_NUMBER, new Random());
                pairs.add(new Pair<Classifier,Evaluation>(rf,eval));

                numberOfTrees = numberOfTrees * 1.5;
            }

            System.out.println("RandomForestTest Done!");

            return pairs;
        }
    }

    public static ArrayList<Instances> getInstances(ArrayList<String> featureFiles) throws Exception {
        ArrayList<Instances> dataFiles = new ArrayList<>();

        for (String file :featureFiles) {
            dataFiles.add(new ConverterUtils.DataSource(file).getDataSet());
        }

        for (Instances datafile : dataFiles) {
            if(datafile.classIndex() == -1)
                datafile.setClassIndex(datafile.numAttributes() - 1);
        }
        return dataFiles;
    }

    public static String createECDFAllFile(String fileLocation) {
        ArrayList<Window> windows = RawlineToWindowConverter.getAllWindowsFromURI(fileLocation);
        for (Window w : windows) {
            w.calculateECDFRepresentationDisc(30);
            w.calculateECDFRepresentationRaw(30);
            w.calculateECDFRepresentationUpDown(30);
        }
        String featureString = "";
        ArrayList<String> fileToBePrinted = new ArrayList<>();
        fileToBePrinted.add(Util.getHeader(windows));
        fileToBePrinted.addAll(windows.stream().map(Util::convertToLine).collect(Collectors.toList()));
        //print the file
        fileToBePrinted.removeIf(String::isEmpty);
        String filename = "D:\\Dropbox\\Thesis\\Data\\Test\\"+"ECDFAllPipeline.arff";
        Util.saveAsFile(fileToBePrinted,filename);
        return filename;
    }

    private static String createECDFUpDownFile(String fileLocation) {
        ArrayList<Window> windows = RawlineToWindowConverter.getAllWindowsFromURI(fileLocation);
        for (Window w : windows) {
            w.calculateECDFRepresentationUpDown(30);
        }
        String featureString = "";
        ArrayList<String> fileToBePrinted = new ArrayList<>();
        fileToBePrinted.add(Util.getHeader(windows));
        fileToBePrinted.addAll(windows.stream().map(Util::convertToLine).collect(Collectors.toList()));
        //print the file
        fileToBePrinted.removeIf(String::isEmpty);
        String filename = "D:\\Dropbox\\Thesis\\Data\\Test\\"+"ECDFUpDownPipeline.arff";
        Util.saveAsFile(fileToBePrinted,filename);
        return filename;
    }

    private static String createECDFDiscFile(String fileLocation) {
        ArrayList<Window> windows = RawlineToWindowConverter.getAllWindowsFromURI(fileLocation);
        for (Window w : windows) {
            w.calculateECDFRepresentationDisc(30);
        }
        String featureString = "";
        ArrayList<String> fileToBePrinted = new ArrayList<>();
        fileToBePrinted.add(Util.getHeader(windows));
        fileToBePrinted.addAll(windows.stream().map(Util::convertToLine).collect(Collectors.toList()));
        //print the file
        fileToBePrinted.removeIf(String::isEmpty);
        String filename = "D:\\Dropbox\\Thesis\\Data\\Test\\"+"ECDFDiscPipeline.arff";
        Util.saveAsFile(fileToBePrinted,filename);
        return filename;
    }

    private static String createECDFRawFile(String fileLocation) {
        ArrayList<Window> windows = RawlineToWindowConverter.getAllWindowsFromURI(fileLocation);
        for (Window w : windows) {
            w.calculateECDFRepresentationRaw(30);
        }
        String featureString = "";
        ArrayList<String> fileToBePrinted = new ArrayList<>();
        fileToBePrinted.add(Util.getHeader(windows));
        fileToBePrinted.addAll(windows.stream().map(Util::convertToLine).collect(Collectors.toList()));
        //print the file
        fileToBePrinted.removeIf(String::isEmpty);
        String filename = "D:\\Dropbox\\Thesis\\Data\\Test\\"+"ECDFRawPipeline.arff";
        Util.saveAsFile(fileToBePrinted,filename);
        return filename;
    }

    private static String createECDFUpAndYFile(String fileLocation) {
        ArrayList<Window> windows = RawlineToWindowConverter.getAllWindowsFromURI(fileLocation);
        for (Window w : windows) {
            w.calculateECDFRepresentationUpAndY(30);
        }
        String featureString = "";
        ArrayList<String> fileToBePrinted = new ArrayList<>();
        fileToBePrinted.add(Util.getHeader(windows));
        fileToBePrinted.addAll(windows.stream().map(Util::convertToLine).collect(Collectors.toList()));
        //print the file
        fileToBePrinted.removeIf(String::isEmpty);
        String filename = "D:\\Dropbox\\Thesis\\Data\\Test\\"+"ECDFUpAndYPipeline.arff";
        Util.saveAsFile(fileToBePrinted,filename);
        return filename;
    }
}
