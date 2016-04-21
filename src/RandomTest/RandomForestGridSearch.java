package RandomTest;

import Core.RawlineToWindowConverter;
import Core.Util;
import Core.Window;
import org.apache.commons.math3.util.Pair;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by Rune on 17-03-2016.
 */
public class RandomForestGridSearch {
    public static void main(String[] args) {
        ArrayList<Window> windows = RawlineToWindowConverter.getAllWindowsFromURI("D:\\Dropbox\\Thesis\\Data\\Test");
        for (Window w : windows) {
            w.calculateECDFRepresentationUpAndY(30);
        }
        String featureString = "";
        ArrayList<String> fileToBePrinted = new ArrayList<>();
        fileToBePrinted.add(Util.getHeader(windows));
        fileToBePrinted.addAll(windows.stream().map(Util::convertToLine).collect(Collectors.toList()));
        //print the file
        fileToBePrinted.removeIf(String::isEmpty);
        Util.saveAsFile(fileToBePrinted,"D:\\Dropbox\\Thesis\\Data\\Test\\"+"ECDFUpAndYMovementRf.arff");

        ConverterUtils.DataSource source;

        try {
            source = new ConverterUtils.DataSource("D:\\Dropbox\\Thesis\\Data\\Test\\"+"ECDFUpAndYMovementRf.arff");
            Instances data = source.getDataSet();

            if(data.classIndex() == -1)
                data.setClassIndex(data.numAttributes() - 1);

            ArrayList<Pair<RandomForest, Evaluation>> randomForests = new ArrayList<>();

            for (int i = 0; i < 20; i++) {
                String[] options = weka.core.Utils.splitOptions("-I "+ ((i+1)*10) +" -K 0 -S 1");
                RandomForest rf = new RandomForest();
                rf.setOptions(options);
                rf.buildClassifier(data);

                Evaluation eval = new Evaluation(data);
                eval.crossValidateModel(rf,data, data.numInstances(), new Random(1));
                randomForests.add(new Pair<>(rf, eval));
            }

            for (int i = 0; i < 20; i++) {
                Pair rfAndEval = randomForests.get(i);
                Evaluation eval = (Evaluation) rfAndEval.getValue();
                System.out.println("#Trees = " +((i+1)*10));
                System.out.println(eval.toSummaryString());
                System.out.println(eval.toClassDetailsString());
                System.out.println("--------------------------------------------------------");
            }

/*            System.out.println(eval.toSummaryString());
            System.out.println(eval.toClassDetailsString());
            System.out.println(eval.toMatrixString());
            System.out.println(eval.toCumulativeMarginDistributionString());*/

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
