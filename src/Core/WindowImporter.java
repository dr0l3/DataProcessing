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

    public static List<Window> getCorrectedWindowsMutlipleUsersUnaplyBias(String fromLocation){
        List<String> files = Util.listOfFilesInDirectory(fromLocation);
        List<String> corrections_files = new ArrayList<>();
        List<String> bias_configurations = new ArrayList<>();
        for (String file : files) {
            if(file.contains("correctionsfile.csv")) {
                corrections_files.add(file);
            }
            if(file.contains("biasconfiguration.csv")){
                bias_configurations.add(file);
            }
        }

        HashMap<String, String> corrections_map = new HashMap<>();
        HashMap<String, BiasConfiguration> biasConfigurationHashMap = new HashMap<>();

        BufferedReader bufferedReader = null;
        BufferedReader bufferedReader1 = null;
        String line;
        String splitter = ",";

        for (String corrections_file : corrections_files) {
            String biasconfigfile = corrections_file.replaceFirst("correctionsfile", "biasconfiguration");
            try {
                bufferedReader = new BufferedReader(new FileReader(corrections_file));
                bufferedReader1 = new BufferedReader(new FileReader(biasconfigfile));
                String[] biasConfLine = bufferedReader1.readLine().split(splitter);
                BiasConfiguration biasConfiguration = new BiasConfiguration(Float.valueOf(biasConfLine[0]),Float.valueOf(biasConfLine[1]),Float.valueOf(biasConfLine[2]),Float.valueOf(biasConfLine[3]),Float.valueOf(biasConfLine[4]),Float.valueOf(biasConfLine[5]));
                while ((line = bufferedReader.readLine()) != null) {
                    String[] correction_line = line.split(splitter);
                    if (correction_line.length == 1) {
                        corrections_map.put(correction_line[0], "null");
                    } else {
                        corrections_map.put(correction_line[0], correction_line[1]);
                    }
                    biasConfigurationHashMap.put(correction_line[0], biasConfiguration);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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

                    BiasConfiguration biasConfiguration = biasConfigurationHashMap.get(filename);

                    for (FeatureLine fl : w.getListOfFeatureLines()){
                        fl.applyBias(biasConfiguration);
                    }


                    windows.add(w);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return windows;
    }

    public static List<Window> getCorrectedWindowsMultipleUsers(String fromLocation){
        List<String> files = Util.listOfFilesInDirectory(fromLocation);
        List<String> corrections_files = new ArrayList<>();
        for (String file : files) {
            if(file.contains("correctionsfile.csv")) {
                corrections_files.add(file);
            }
        }

        HashMap<String, String> corrections_map = new HashMap<>();

        BufferedReader bufferedReader = null;
        String line;
        String splitter = ",";

        for (String corrections_file : corrections_files) {
            try {
                bufferedReader = new BufferedReader(new FileReader(corrections_file));
                while ((line = bufferedReader.readLine()) != null) {
                    String[] correction_line = line.split(splitter);
                    if (correction_line.length == 1) {
                        corrections_map.put(correction_line[0], "null");
                    } else {
                        corrections_map.put(correction_line[0], correction_line[1]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
