package Pipeline;

import Chart.ChartGenerator;
import Core.ClassifierType;
import Core.RawlineToWindowConverter;
import Core.RawlineToWindowConverterVarLength;
import Core.Window;
import org.apache.commons.math3.util.Pair;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

/**
 * Created by Rune on 29-03-2016.
 */
public class SingleExperimentWithPrint {
    public static void main(String[] args) {
        //Create dataset with some features
        System.out.println("Creating data set");
        ArrayList<Window> windows = RawlineToWindowConverterVarLength.getAllWindowsFromURI("D:\\Dropbox\\Thesis\\Data\\RawData", 3);
        windows.removeIf(window -> window.getLabel().contains("null"));
        //windows.stream().filter(w -> !w.getLabel().contains("null")).forEach(w -> w.setLabel("event"));
        for (Window w : windows) {
            //w.calculateECDFRepresentationRaw(30);
            //w.calculateECDFRepresentationDisc(30);
            //w.calculateECDFRepresentationUpDown(30);
            //w.calculateECDFRepresentationUpAndY(30);
            //w.calculateFeaturesForRelativeMovement();
            //w.calculateFeaturesForRawMovement();
            //w.calculateFeaturesForGravityDiscountedMovement();
            //w.calculateStartingOrientation();
            //w.calculateEndingOrientation();
            //w.calculateMeanVerticalAcceleration();
            //w.calculateVerticalSamplesBelowThreshold(1);
            //w.calculateVerticalSamplesBelowThreshold(0);
            //w.calculateVerticalSamplesBelowThreshold(-1);
            //w.calculateVerticalSamplesBelowThreshold(-2);
            //w.calculateVerticalSamplesBelowThreshold(-3);
            //w.calculateOrientationJitter();
            //w.calculateVerticalTimedDistribution(30);
//            w.calculateFrequencyFeaturesRawX();
//            w.calculateFrequencyFeaturesRawY();
//            w.calculateFrequencyFeaturesRawZ();
            w.calculateZeroCrossings();
        }

        //create map lookup
        //convert the window to instances
        BiMap<Instance,Window> bimap = new BiMap<>();
        for (Window window : windows) {
            Instances dataset = getHeaderFromWindow(window);
            dataset.setClassIndex(dataset.numAttributes()-1);
            Instance datapoint = convertWindowToInstance(window);
            dataset.add(datapoint);
            dataset.firstInstance().setClassValue(window.getLabel());
            //datapoint.setClassValue(window.getLabel());
            bimap.put(dataset.firstInstance(), window);
        }

        //split into train and test
        ArrayList<ArrayList<Window>> folds = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            folds.add(new ArrayList<>());
        }
        Random r = new Random();
        for (Window w : windows) {
            folds.get(r.nextInt(10)).add(w);
        }


        //Create or load some classifier
        System.out.println("Training classifiers");
        Classifier[] classifiers = new Classifier[10];
        for (int i = 0; i < 10; i++) {
            String[] options;
            try {
                options = weka.core.Utils.splitOptions("-I 100 -K 0 -S 1");
                RandomForest rf = new RandomForest();
                rf.setOptions(options);
                Instances dataset = getDataSet(i, folds);
                rf.buildClassifier(dataset);
                classifiers[i] = rf;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        HashSet<Pair<Window,String>> misclassifiedWindows = new HashSet<>();
        //Run a cross validated test
        //foreach classifier
        int rightlyClassified = 0;
        int wronglyClassified = 0;
        System.out.println("Performing cross validation");
        for (int i = 0; i < 10; i++) {
            ArrayList<Window> tobeTested = folds.get(i);
            for (Window w :tobeTested) {
                Instance inst = bimap.getKey(w);
                try {
                    double classification = classifiers[i].classifyInstance(inst);
                    Window window = bimap.get(inst);
                    if (classification != labelFromDouble(window.getLabel())){
                        misclassifiedWindows.add(new Pair<Window, String>(window,
                                window.getLabel()+" classified as "+getClassificationFromDouble(classification)));
                        wronglyClassified++;
                    } else {
                        rightlyClassified++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        //print the wrongly classified windows [handled]
        List<Pair<Window,String>> mis = new ArrayList<>(misclassifiedWindows);
        System.out.println("Right = " + rightlyClassified);
        System.out.println("Wrong = " + wronglyClassified);
        System.out.println("Printing wrongly classified windows");
        ChartGenerator.printChartWithTitle(mis);
    }

    private static String getClassificationFromDouble(double classification) {
        if (classification == 1)
            return "null";
        if (classification == 2)
            return "sit";
        return "stand";
    }

    private static double labelFromDouble(String label) {
        if(label.contains("null"))
            return 1;
        if(label.contains("stand"))
            return 0;
        return 2;
    }

    private static Instances getDataSet(int i, ArrayList<ArrayList<Window>> folds) {
        Instances dataset = getHeaderFromWindow(folds.get(0).get(0));
        for (int j = 0; j < folds.size(); j++) {
            if (j == i)
                continue;
            for (Window w :folds.get(j)) {
                dataset.add(convertWindowToInstance(w));
                dataset.lastInstance().setClassValue(w.getLabel());
            }
        }
        return dataset;
    }

    private static Instances getHeaderFromWindow(Window window){
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (Pair pair : window.getListOfFeatures()) {
            attributes.add(new Attribute((String) pair.getKey()));
        }

        ArrayList<String> labels = new ArrayList<>();
        labels.add("stand");
        labels.add("null");
        labels.add("sit");

        Attribute classLabel = new Attribute("class", labels);
        attributes.add(classLabel);

        Instances dataset = new Instances("dataset", attributes, 1000);
        dataset.setClassIndex(dataset.numAttributes()-1);
        return dataset;
    }

    private static Instance convertWindowToInstance(Window window){
        List<Pair<?,?>> features = window.getListOfFeatures();
        int size = features.size();
        double[] values = new double[(size+1)];
        for (int i = 0; i < size; i++) {
            values[i] = Double.parseDouble(features.get(i).getValue().toString());
        }
        values[(size)] = 1;
        DenseInstance inst = new DenseInstance(1,values);
        return inst;
    }
}
