package ArffFile;

import Core.RawlineToWindowConverter;
import Core.Util;
import Core.Window;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Created by Rune on 28-03-2016.
 */
public class AllFeatureFileGenerator {
    public static void main(String[] args) {
        ArrayList<Window> windows = RawlineToWindowConverter.getAllWindowsFromURI("D:\\Dropbox\\Thesis\\Data\\RawData");
        for (Window w : windows) {
            //w.calculateECDFRepresentationRaw(30);
            //w.calculateECDFRepresentationDisc(30);
            //w.calculateECDFRepresentationUpDown(30);
            //w.calculateECDFRepresentationUpAndY(30);
            w.calculateFeaturesForRelativeMovement();
            //w.calculateFeaturesForRawMovement();
            //w.calculateFeaturesForGravityDiscountedMovement();
            w.calculateStartingOrientation();
            w.calculateEndingOrientation();
            w.calculateOrientationJitter();
            w.calculateVerticalTimedDistribution(100);
        }
        String featureString = "";
        ArrayList<String> fileToBePrinted = new ArrayList<>();
        fileToBePrinted.add(Util.getHeader(windows));
        fileToBePrinted.addAll(windows.stream().map(Util::convertToLine).collect(Collectors.toList()));
        //print the file
        fileToBePrinted.removeIf(String::isEmpty);
        Util.saveAsFile(fileToBePrinted,"D:\\Dropbox\\Thesis\\Data\\Test\\"+"AllFeaturesCool.arff");
    }
}
