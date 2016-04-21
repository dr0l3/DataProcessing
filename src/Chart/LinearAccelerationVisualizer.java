package Chart;

import Core.FeatureLine;
import Core.RawlineToWindowConverter;
import Core.Util;
import Core.Window;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Rune on 16-03-2016.
 */
public class LinearAccelerationVisualizer {

    public static void main(String[] args) {
        ArrayList<Window> windows = RawlineToWindowConverter.getAllWindowsFromURI("D:\\Dropbox\\Thesis\\Data\\Test");
        DecimalFormat df = new DecimalFormat("00.00000000");
        ArrayList<String> toPrint = new ArrayList<>();
        for (Window w : windows) {
            String seprator = "--------------" + w.getLabel() + "--------------";
            toPrint.add(seprator);
            /*for (FeatureLine featureLine : w.getListOfFeatureLines()) {
                String line = df.format(featureLine.getEffAccX()).replace(",", ".") + "\t \t" +
                        df.format(featureLine.getEffAccY()).replace(",", ".") + "\t \t" +
                        df.format(featureLine.getEffAccZ()).replace(",", ".");
                toPrint.add(line);
            }*/

            /*for (FeatureLine featureLine : w.getListOfFeatureLines()) {
                String line = df.format(featureLine.getAccUp()).replace(",", ".") + "\t \t" +
                        df.format(featureLine.getAccRest()).replace(",", ".");
                toPrint.add(line);
            }*/

            /*for (FeatureLine featureLine : w.getListOfFeatureLines()) {
                String line = df.format(featureLine.getAccX()).replace(",", ".") + "\t \t" +
                        df.format(featureLine.getAccY()).replace(",", ".") + "\t \t" +
                        df.format(featureLine.getAccZ()).replace(",", ".") + "\t \t" +
                        df.format(featureLine.getGraX()).replace(",", ".") + "\t \t" +
                        df.format(featureLine.getGraY()).replace(",", ".") + "\t \t" +
                        df.format(featureLine.getGraZ()).replace(",", ".");
                toPrint.add(line);
            }*/


            for (FeatureLine featureLine : w.getListOfFeatureLines()) {
                String line = df.format(featureLine.getRotX()).replace(",", ".") + "\t \t" +
                        df.format(featureLine.getRotY()).replace(",", ".") + "\t \t" +
                        df.format(featureLine.getRotZ()).replace(",", ".");
                toPrint.add(line);
            }

        }
        Util.saveAsFile(toPrint,"linearVisualizerRotation.arff");
    }
}
