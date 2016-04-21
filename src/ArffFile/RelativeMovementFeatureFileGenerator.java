package ArffFile;

import Core.RawlineToWindowConverter;
import Core.Util;
import Core.Window;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Created by Rune on 08-03-2016.
 */
public class RelativeMovementFeatureFileGenerator {

    public static void main(String[] args) {
        ArrayList<Window> windows = RawlineToWindowConverter.getAllWindowsFromURI("D:\\Dropbox\\Thesis\\Data\\Test");
        String featureString = "";
        for (Window w : windows) {
            w.calculateFeaturesForRelativeMovement();
        }
        ArrayList<String> fileToBePrinted = new ArrayList<>();
        fileToBePrinted.add(Util.getHeader(windows));
        fileToBePrinted.addAll(windows.stream().map(Util::convertToLine).collect(Collectors.toList()));
        //print the file
        Util.saveAsFile(fileToBePrinted,"D:\\Dropbox\\Thesis\\Data\\Test\\"+"randomstuff.arff");
    }
}
