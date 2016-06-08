package Core;

import org.apache.commons.math3.util.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Rune on 15-05-2016.
 */
public class WindowImporter {
    public static List<Window> getWindows(String fromLocation){
        List<String> files = Util.listOfFilesInDirectory(fromLocation);
        ArrayList<Window> windows = new ArrayList<>();
        for (String file : files) {
            if(file.contains("window")){
                try {
                    windows.add(getWindowFromFile(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return windows;
    }

    public static List<Window> getCorrectedWindows(String fromLocation){
        List<String> files = Util.listOfFilesInDirectory(fromLocation);
        String corrections_file = "";
        for (String file : files) {
            if(file.contains("correctionsfile.csv")) {
                corrections_file = file;
                break;
            }
        }

        HashMap<String, String> corrections_map = new HashMap<>();

        BufferedReader bufferedReader = null;
        String line;
        String splitter = ",";
        try {
            bufferedReader = new BufferedReader(new FileReader(corrections_file));
            while((line = bufferedReader.readLine()) != null){
                String[] correction_line = line.split(splitter);
                if(correction_line.length == 1){
                    corrections_map.put(correction_line[0], "null");
                } else {
                    corrections_map.put(correction_line[0], correction_line[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<Window> windows = new ArrayList<>();
        for (String file : files) {
            if(file.contains("window")){
                try {
                    Window w = getWindowFromFile(file);
                    File window_file = new File(file);
                    if(window_file.length()< 10 )
                        continue;
                    String filename = file.substring(file.lastIndexOf("\\")+7);
                    String correction = corrections_map.get(filename);
                    if(correction.equals("sit")){
                        w.setLabel("sit");
                    } else if(correction.equals("stand")){
                        w.setLabel("stand");
                    } else {
                        w.setLabel("null");
                    }
                    windows.add(w);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return windows;
    }

    private static Window getWindowFromFile(String path) throws IOException {
        Object result = new ArrayList<>();
        FileInputStream fis;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(path);

            ois = new ObjectInputStream(fis);
            result = ois.readObject();

        } catch (OptionalDataException e) {
            if (!e.eof) throw e;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            assert ois != null;
            ois.close();
        }

        return (Window) result;
    }
}
