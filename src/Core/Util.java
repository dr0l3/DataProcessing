package Core;

import Core.Window;
import org.apache.commons.math3.util.Pair;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Rune on 07-03-2016.
 */
public class Util {

    public static List<String> listOfFilesInDirectory(String directory){
        List<String> filenames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))){
            for (Path path : directoryStream){
                filenames.add(path.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filenames;
    }

    public static List<String> listOfFilesInDirectoryRecursive(String directory){
        List<String> filenames = new ArrayList<>();
        File root = new File(directory);
        File[] list = root.listFiles();
        if(list == null) return filenames;

        for(File f : list){
            if( f.isDirectory()){
                filenames.addAll(listOfFilesInDirectoryRecursive(f.getAbsolutePath()));
            } else {
                filenames.add(f.getAbsolutePath());
            }
        }
        return filenames;
    }

    public static ArrayList<String> importData(String uri)  {
        ArrayList<String> dataLines = new ArrayList<>();

        try {
            Files.lines(Paths.get(uri))
                    .filter(s -> !s.isEmpty())
                    .filter(s -> !s.startsWith("@"))
                    .forEach(dataLines::add);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataLines;
    }

    public static void saveAsFile(List<String> arrayToPrintToFile, String nameOfFile){
        //create empty file
        File file = new File(nameOfFile);
        try{
            PrintWriter pr = new PrintWriter(file);
            arrayToPrintToFile.forEach(pr::println);
            pr.close();
        } catch (Exception e){
            System.out.println(e.toString());
            System.out.println("Writing to file failed");
        }
    }

    public static int ordinalIndexOf(String s, char c, int i) {
        int pos = s.indexOf(c,0);
        while( i-- > 0 && pos != -1)
            pos = s.indexOf(c,pos+1);
        return pos;
    }

    public static boolean isEmptyString(String s){
        return s.equals("");
    }

    public static String getHeader(List<Window> windows){
        String newLine = "\n";
        String ret = "@RELATION action" + newLine;
        Window window = windows.get(0);
        for (Pair pair :window.getListOfFeatures()) {
            ret = ret.concat("@ATTRIBUTE "+pair.getKey()+ "\t \t \t NUMERIC" + newLine);
        }
        Set<String> possibleLabels = new TreeSet<>();
        for (Window w :
                windows) {
            possibleLabels.add(w.getLabel());
        }
        String listOfLabels = "";
        for (String s : possibleLabels) {
            listOfLabels = listOfLabels.concat(","+s);
        }
        listOfLabels = listOfLabels.substring(1);
        ret = ret.concat("@ATTRIBUTE  class   {"+listOfLabels+"}" + newLine + "@DATA" + newLine);
        return ret;
    }

    public static String convertToLine(Window w){
        DecimalFormat df = new DecimalFormat("00.00000000");

        String label = w.getLabel();
        String ret = "";
        for (Pair pair : w.getListOfFeatures()) {
            ret = ret.concat(df.format(pair.getValue()).replace(",", ".") + ",\t");
        }

        ret = ret.concat(label);
        return ret;
    }

    public static void updateProgress(double progressPercentage) {
        final int width = 100; // progress bar width in chars

        System.out.print("\r[");
        int i = 0;
        for (; i <= (int)(progressPercentage*width); i++) {
            System.out.print(".");
        }
        for (; i < width; i++) {
            System.out.print(" ");
        }
        System.out.print("]");
    }

    public static void updateProgressWithText(String message) {
        System.out.print("\r"+ message);
    }
}
