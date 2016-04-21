package RandomTest;

import Core.RawlineToWindowConverter;
import Core.Util;
import Core.Window;
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
public class GridSearchTutorial {

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
        Util.saveAsFile(fileToBePrinted,"D:\\Dropbox\\Thesis\\Data\\Test\\"+"ECDFUpAndYMovementTutorial.arff");

        ConverterUtils.DataSource source;

        try {
            source = new ConverterUtils.DataSource("D:\\Dropbox\\Thesis\\Data\\Test\\"+"ECDFUpAndYMovementTutorial.arff");
            Instances data = source.getDataSet();

            if(data.classIndex() == -1)
                data.setClassIndex(data.numAttributes() - 1);

            String[] options = weka.core.Utils.splitOptions("-I 10 -K 0 -S 1");
            RandomForest rf = new RandomForest();
            rf.setOptions(options);
            rf.buildClassifier(data);

            Evaluation eval = new Evaluation(data);
            eval.crossValidateModel(rf,data, data.numInstances(), new Random(1));
            System.out.println(eval.toSummaryString());
            System.out.println(eval.toClassDetailsString());
            System.out.println(eval.toMatrixString());
            System.out.println(eval.toCumulativeMarginDistributionString());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
