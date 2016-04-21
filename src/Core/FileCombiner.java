package Core;

import Core.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rune on 07-03-2016.
 */
public class FileCombiner {
    private static String FROM_URI = "D:\\Dropbox\\Thesis\\Data\\RawData";
    private static String TO_URI = "D:\\Dropbox\\Thesis\\Data\\CombinedData\\Alldata.txt";

    public static void main(String[] args) {
        List<String> files = Util.listOfFilesInDirectory(FROM_URI);
        ArrayList<String> lines = new ArrayList<>();
        files.forEach(filename -> {
            lines.addAll(Util.importData(filename));
        });

        Util.saveAsFile(lines, TO_URI);
    }
}
