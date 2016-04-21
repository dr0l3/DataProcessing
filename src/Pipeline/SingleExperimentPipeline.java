package Pipeline;

import Core.*;
import org.apache.commons.math3.util.Pair;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by Rune on 29-03-2016.
 */
public class SingleExperimentPipeline {
    public static void main(String[] args) {
        ArrayList<Window> windows = RawlineToTapWindowConverterVarLength.getAllWindowsFromURI("D:\\Dropbox\\Thesis\\Data\\RawTapData", 3);
        //ArrayList<Window> windows = RawlineToWindowConverter.getAllWindowsFromURI("D:\\Dropbox\\Thesis\\Data\\RawData");
        //windows.removeIf(window -> window.getLabel().contains("null"));
        //windows.stream().filter(w -> !w.getLabel().contains("null")).forEach(w -> w.setLabel("event"));
        for (Window w : windows) {
//            w.calculateECDFRepresentationRaw(30);
//            w.calculateECDFRepresentationDisc(30);
//            w.calculateECDFRepresentationUpDown(30);
//            w.calculateECDFRepresentationUpAndY(30);
//            w.calculateFeaturesForRelativeMovement();
//            w.calculateFeaturesForRawMovement();
//            w.calculateFeaturesForGravityDiscountedMovement();
//            w.calculateStartingOrientation();
//            w.calculateEndingOrientation();
//            w.calculateMeanVerticalAcceleration();
//            w.calculateVerticalSamplesBelowThreshold(1);
//            w.calculateVerticalSamplesBelowThreshold(0);
//            w.calculateVerticalSamplesBelowThreshold(-1);
//            w.calculateVerticalSamplesBelowThreshold(-2);
//            w.calculateVerticalSamplesBelowThreshold(-3);
//            w.calculateUpCorrelationWithDiscounted();
//            w.calculateSumOfUpwardsAcceleration();
//            w.calculateSumOfDownwardsAcceleration();
//            w.calculateOrientationJitter();
//            w.calculateVerticalTimedDistribution(30);
//            w.calculateFrequencyFeaturesDiscX();
//            w.calculateFrequencyFeaturesDiscY();
//            w.calculateFrequencyFeaturesDiscZ();
//            w.calculateFrequencyFeaturesRawX();
//            w.calculateFrequencyFeaturesRawY();
            w.calculateFrequencyFeaturesRawZ();
//            w.calculateFrequencyFeaturesHorizontal();
//            w.calculateFrequencyFeaturesVertical();

//            w.calculateZeroCrossings();
            w.calculateNumberOfTaps();
        }
        ArrayList<String> fileToBePrinted = new ArrayList<>();
        fileToBePrinted.add(Util.getHeader(windows));
        fileToBePrinted.addAll(windows.stream().map(Util::convertToLine).collect(Collectors.toList()));
        //print the file
        fileToBePrinted.removeIf(String::isEmpty);
        String fileName ="D:\\Dropbox\\Thesis\\Data\\Test\\"+"AllFeaturesCool3second.arff";
        Util.saveAsFile(fileToBePrinted,fileName);

        ConverterUtils.DataSource source;

        try {
            source = new ConverterUtils.DataSource(fileName);
            Instances data = source.getDataSet();

            if(data.classIndex() == -1)
                data.setClassIndex(data.numAttributes() - 1);


            String[] options = weka.core.Utils.splitOptions("-I 100 -K 0 -S 1");
            RandomForest rf = new RandomForest();
            rf.setOptions(options);
            rf.buildClassifier(data);

            Evaluation eval = new Evaluation(data);
            eval.crossValidateModel(rf,data, 10, new Random());

            System.out.println(eval.toClassDetailsString());
            System.out.println(eval.toMatrixString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
