package Core;

import Core.Util;
import Core.Window;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rune on 07-03-2016.
 */
public class RawlineToWindowConverter {
    private static String FROM_URI = "D:\\Dropbox\\Thesis\\Data\\Test";
    private static String TO_URI = "D:\\Dropbox\\Thesis\\Data\\WindowedData\\AllWindows.txt";

    public static void main(String[] args) {
        List<String> files = Util.listOfFilesInDirectory(FROM_URI);
        ArrayList<Window> allWindows = new ArrayList<>();
        for (String file : files) {
            allWindows.addAll(getWindows(file));
        }

        //save as a file
    }

    public static ArrayList<Window> getAllWindowsFromURI(String URI){
        List<String> files = Util.listOfFilesInDirectory(URI);
        ArrayList<Window> allWindows = new ArrayList<>();
        for (String file : files) {
            allWindows.addAll(getWindows(file));
        }
        return allWindows;
    }

    private static ArrayList<Window> getWindows(String file) {
        ArrayList<String> lines = Util.importData(file);
        return convertToWindows(lines);
    }

    private static ArrayList<Window> convertToWindows(ArrayList<String> lines) {
        ArrayList<Window> windows = new ArrayList<>();
        long ts_start = extractTimeStamp(lines.get(0));
        int indexOfTsStart = 0;
        String label = "null";
        for (int i = 1; i < lines.size(); i++) {
            //if line is an event start marker
            String line = lines.get(i);
            if(line.contains("----") && line.contains("start")){
                indexOfTsStart = i+1;
                ts_start = extractTimeStamp(lines.get(i+1));
                label = getEventType(lines.get(i));
            }

            //get timestamp
            if(!line.contains("---")) {
                long ts = extractTimeStamp(lines.get(i));

                //if complete window -> convert to window
                if (ts - ts_start > 2000000000) {
                    windows.add(new Window(lines.subList(indexOfTsStart, i - 1), label));
                    indexOfTsStart = i;
                    ts_start = extractTimeStamp(lines.get(i));
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
        int indexOfNinthPipe = Util.ordinalIndexOf(s,'|',8);
        //stringplsit
        return Long.parseLong(s.substring(indexOfNinthPipe+2));
    }
}
