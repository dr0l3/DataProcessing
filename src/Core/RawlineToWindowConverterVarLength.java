package Core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rune on 05-04-2016.
 */
public class RawlineToWindowConverterVarLength {

    public static ArrayList<Window> getAllWindowsFromURI(String URI, double window_size_seconds){
        List<String> files = Util.listOfFilesInDirectory(URI);
        ArrayList<Window> allWindows = new ArrayList<>();
        for (String file : files) {
            List<Window> windowsFromFile = getWindows(file, window_size_seconds);
            allWindows.addAll(windowsFromFile);
        }
        return allWindows;
    }

    private static List<Window> getWindows(String file, double window_size_seconds) {
        ArrayList<String> lines = Util.importData(file);
        List<Window> windows_from_file = convertToWindows_stand(lines, window_size_seconds);
        return windows_from_file;
    }

    private static ArrayList<Window> convertToWindows_stand(ArrayList<String> lines, double window_size_seconds) {
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
                    windows.add(new Window(lines.subList(indexOfTsStart, i - 1), label));
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
        int index_of_pipe_before_ts = Util.ordinalIndexOf(s,'|', 8);
        //stringplsit
        return Long.parseLong(s.substring(index_of_pipe_before_ts+2));
    }
}
