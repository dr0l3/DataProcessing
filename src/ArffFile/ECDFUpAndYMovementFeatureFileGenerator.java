package ArffFile;

import Core.RawlineToWindowConverter;
import Core.Util;
import Core.Window;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Created by Rune on 15-03-2016.
 */
public class ECDFUpAndYMovementFeatureFileGenerator {
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
        Util.saveAsFile(fileToBePrinted,"D:\\Dropbox\\Thesis\\Data\\Test\\"+"ECDFUpAndYMovement.arff");
    }
}
