package ArffFile;

import Core.*;
import org.apache.commons.math3.util.Pair;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Rune on 29-03-2016.
 */
public class CompleteFeatureFileGenerator {

    public static void main(String[] args) {
        BiasConfiguration nullBias = new BiasConfiguration(0, 0, 0, 0, 0, 0);
        createCompleteFeatureFileWithProximity("D:\\Dropbox\\Thesis\\Data\\RawDataProx", "D:\\Dropbox\\Thesis\\Data\\Test\\", 3., ClassifierType.EVENT_SNIFFER, nullBias);
    }

    public static String createCompleteFeatureFile(String fromLocation, String toLocation, double windowsize_seconds, ClassifierType type) {
        ArrayList<Window> windows = new ArrayList<>();
        String filenameSuffix = "";
        if (type == ClassifierType.SIT_STAND_CLASSIFIER) {
            windows = RawlineToWindowConverterVarLength.getAllWindowsFromURI(fromLocation, windowsize_seconds);
            windows.removeIf(window -> window.getLabel().contains("null"));
            filenameSuffix = "CompleteFeatureFileSitStand.arff";
        }
        if (type == ClassifierType.EVENT_SNIFFER) {
            windows = RawlineToWindowConverterVarLength.getAllWindowsFromURI(fromLocation, windowsize_seconds);
            windows.stream().filter(w -> !w.getLabel().contains("null")).forEach(w -> w.setLabel("event"));
            filenameSuffix = "CompleteFeatureFileEventSniffer.arff";
        }

        if (type == ClassifierType.TAP_SNIFFER) {
            windows = RawlineToTapWindowConverterVarLength.getAllWindowsFromURI(fromLocation, windowsize_seconds);
            filenameSuffix = "CompleteFeatureFileTapSniffer.arff";
        }


        for (Window w : windows) {
            w.calculateECDFRepresentationDisc(30);
            w.calculateECDFRepresentationRaw(30);
            w.calculateECDFRepresentationUpDown(30);
            w.calculateFeaturesForRawMovement();
            w.calculateFeaturesForGravityDiscountedMovement();
            w.calculateFeaturesForRelativeMovement();
            w.calculateStartingOrientation();
            w.calculateEndingOrientation();
            w.calculateOrientationJitter();
            w.calculateVerticalTimedDistribution(30);
            w.calculateSumOfUpwardsAcceleration();
            w.calculateSumOfDownwardsAcceleration();
            w.calculateZeroCrossings();
            w.calculateNumberOfTaps();
        }
        ArrayList<String> fileToBePrinted = new ArrayList<>();
        fileToBePrinted.add(Util.getHeader(windows));
        fileToBePrinted.addAll(windows.stream()
                .map(Util::convertToLine)
                .collect(Collectors.toList()));
        //print the file
        fileToBePrinted.removeIf(String::isEmpty);
        String filename = toLocation + filenameSuffix;
        Util.saveAsFile(fileToBePrinted, filename);
        return filename;
    }

    public static String createCompleteFeatureaFileWithProximityFromWindowFiles(String fromLocation, String complete_filename, ClassifierType type) {
        //import the windows
        List<Window> windows = WindowImporter.getWindows(fromLocation);
        //filter the windows
        if (type == ClassifierType.EVENT_SNIFFER) {
            //turn all sit/stand events into events
            windows.stream()
                    .filter(window1 -> !window1.getLabel().contains("null"))
                    .forEach(window2 -> window2.setLabel("event"));
        }
        if (type == ClassifierType.SIT_STAND_CLASSIFIER) {
            //remove all null windows
            windows.removeIf(window -> window.getLabel().contains("null"));
        }
        if(windows.isEmpty()){
            return null;
        }
        //convert to arff format
        //retarded copying of windows needed to initialize listOfFeatures
        List<Window> windows2 = new ArrayList<>();
        windows.forEach(window ->windows2.add(new Window(window)));
        windows2.forEach(Window::calculateAllFeatures);
        ArrayList<String> fileToBePrinted = new ArrayList<>();
        fileToBePrinted.add(Util.getHeader(windows2));
        fileToBePrinted.addAll(windows2.stream()
                .map(Util::convertToLine)
                .collect(Collectors.toList()));
        fileToBePrinted.removeIf(String::isEmpty);
        //save the file
        Util.saveAsFile(fileToBePrinted, complete_filename);

        //return the filenames
        return complete_filename;
    }

    public static List<String> createCompleteFeatureFileWithProximitySeparateFile(String fromLocation, String toLocation, double window_size_seconds, ClassifierType type, BiasConfiguration biasForSample) {
        List<Pair<String, List<Window>>> uri_window_list = new ArrayList<>();
        uri_window_list = RawlineToWindowConverterProximityVarLength.getAllWindowFromURIInSeparateLists(fromLocation, window_size_seconds, biasForSample);
        String filenamePrefix = "CompleteFeatureFile";
        if (type == ClassifierType.EVENT_SNIFFER) {
            //turn all sit/stand events into events
            uri_window_list
                    .forEach(pair -> pair.getValue()
                            .stream()
                            .filter(window -> !window.getLabel().contains("null"))
                            .forEach(window1 -> window1.setLabel("event")));
        }
        if (type == ClassifierType.SIT_STAND_CLASSIFIER) {
            //remove all null windows
            uri_window_list
                    .forEach(pair -> pair.getValue()
                            .removeIf(window -> window.getLabel().contains("null")));
        }

        List<String> fileNames = new ArrayList<>();

        for (Pair<String, List<Window>> pair : uri_window_list) {
            List<Window> windows = pair.getValue();
            for (Window w : windows) {
                w.calculateAllFeatures();
            }
            ArrayList<String> fileToBePrinted = new ArrayList<>();
            fileToBePrinted.add(Util.getHeader(windows));
            fileToBePrinted.addAll(windows.stream()
                    .map(Util::convertToLine)
                    .collect(Collectors.toList()));
            //print the file
            fileToBePrinted.removeIf(String::isEmpty);
            String filename = toLocation + filenamePrefix + pair.getKey().substring(pair.getKey().lastIndexOf("\\") + 1);
            Util.saveAsFile(fileToBePrinted, filename);
            fileNames.add(filename);
        }
        return fileNames;
    }

    public static String createCompleteFeatureFileWithProximity(String fromLocation, String toLocation, double windowsize_seconds, ClassifierType type, BiasConfiguration bias) {
        ArrayList<Window> windows = new ArrayList<>();
        String filenameSuffix = "";
        if (type == ClassifierType.SIT_STAND_CLASSIFIER) {
            windows = RawlineToWindowConverterProximityVarLength.getAllWindowsFromURI(fromLocation, windowsize_seconds, bias);
            windows.removeIf(window -> window.getLabel().contains("null"));
            filenameSuffix = "CompleteFeatureFileSitStand.arff";
        }
        if (type == ClassifierType.EVENT_SNIFFER) {
            windows = RawlineToWindowConverterProximityVarLength.getAllWindowsFromURI(fromLocation, windowsize_seconds, bias);
            windows.stream().filter(w -> !w.getLabel().contains("null")).forEach(w -> w.setLabel("event"));
            filenameSuffix = "CompleteFeatureFileEventSniffer.arff";
        }

        if (type == ClassifierType.TAP_SNIFFER) {
            windows = RawlineToWindowConverterProximityVarLength.getAllWindowsFromURI(fromLocation, windowsize_seconds, bias);
            filenameSuffix = "CompleteFeatureFileTapSniffer.arff";
        }


        for (Window w : windows) {
            w.calculateAllFeatures();
        }
        ArrayList<String> fileToBePrinted = new ArrayList<>();
        fileToBePrinted.add(Util.getHeader(windows));
        fileToBePrinted.addAll(windows.stream()
                .map(Util::convertToLine)
                .collect(Collectors.toList()));
        //print the file
        fileToBePrinted.removeIf(String::isEmpty);
        String filename = toLocation + filenameSuffix;
        Util.saveAsFile(fileToBePrinted, filename);
        return filename;
    }

    public static Instances getInstances(String featureFile) throws Exception {
        Instances dataFile = new ConverterUtils.DataSource(featureFile).getDataSet();
        if (dataFile.classIndex() == -1)
            dataFile.setClassIndex(dataFile.numAttributes() - 1);

        return dataFile;
    }
}