package Core;

import org.apache.commons.math3.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rune on 13-04-2016.
 */
public class RawlineToWindowConverterProximityVarLength {
    public static List<Pair<String, List<Window>>> getAllWindowFromURIInSeparateLists(String URI, double window_size_seconds, BiasConfiguration biasForSample){
        List<String> files = Util.listOfFilesInDirectoryRecursive(URI);
        List<Pair<String, List<Window>>> uri_windowlist_pairs = new ArrayList<>();
        for (String file : files) {
            List<Window> windowsFromFile = null;
            try {
                windowsFromFile = getWindows(file, window_size_seconds, biasForSample);
            } catch (IOException e) {
                e.printStackTrace();
            }
            uri_windowlist_pairs.add(new Pair<>(file, windowsFromFile));
        }
        return uri_windowlist_pairs;
    }

    public static ArrayList<Window> getAllWindowsFromURI(String URI, double window_size_seconds, BiasConfiguration biasForSample){
        List<String> files = Util.listOfFilesInDirectoryRecursive(URI);
        ArrayList<Window> allWindows = new ArrayList<>();
        for (String file : files) {
            List<Window> windowsFromFile = null;
            try {
                windowsFromFile = getWindows(file, window_size_seconds, biasForSample);
            } catch (IOException e) {
                e.printStackTrace();
            }
            allWindows.addAll(windowsFromFile);
        }
        return allWindows;
    }

    private static List<Window> getWindows(String file, double window_size_seconds, BiasConfiguration biasForSample) throws IOException {
        ArrayList<String> lines = Util.importData(file);
        List<Window> windows_from_file = convertToWindowsWithProximity(lines, window_size_seconds, biasForSample);
        return windows_from_file;
    }

    private static ArrayList<Window> convertToWindowsWithProximity(ArrayList<String> lines, double window_size_seconds, BiasConfiguration biasForSample) {
        int window_size_nanoseconds = (int) (window_size_seconds* 1000000000);
        ArrayList<Window> windows = new ArrayList<>();
        long ts_start = extractTimeStamp(lines.get(0));
        int indexOfTsStart = 0;
        String label = "null";


        for (int i = 1; i < lines.size(); i++) {
            //if line is an event start marker
            String line = lines.get(i);
            if(line.contains("----") && line.contains("start")){
                indexOfTsStart = i+1;
                ts_start = extractTimeStamp(lines.get(indexOfTsStart));
                label = getEventType(lines.get(i));
            }

            //get timestamp
            if(!line.contains("---")) {
                long ts = extractTimeStamp(lines.get(i));

                //if complete window -> convert to window
                if (ts - ts_start > window_size_nanoseconds) {
                    List<String> windowlines = lines.subList(indexOfTsStart, i-1);
                    Window w = Window.createStandSitWindowWithProximityFromString(windowlines, label, biasForSample);
                    windows.add(w);
                    indexOfTsStart = i;
                    ts_start = extractTimeStamp(lines.get(indexOfTsStart));
                    label = "null";
                }
            }
        }

        return windows;
    }

    private static String getEventType(String s) {
        if(s.contains("sit"))
            return "sit";
        if(s.contains("stand"))
            return "stand";
        return "null";
    }

    private static long extractTimeStamp(String s) {
        //if string does not contain | return -1
        if(!s.contains("|")){
            return -1;
        }
        //find index of ninth | (zero indexed)
        int indexOfTenthPipe = Util.ordinalIndexOf(s,'|',9);
        //stringplsit
        return Long.parseLong(s.substring(indexOfTenthPipe+2));
    }
}
