package RandomTest;

import Chart.ChartGenerator;
import Core.Util;
import Core.Window;
import Core.WindowImporter;
import weka.core.pmml.Array;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rune on 27-04-2016.
 */
public class RestoreWindowFromServerDownload {

    public static void main(String[] args) {
        //input initial directory
        String directory = "D:\\Dropbox\\Thesis\\Data\\individual2016-05-29-22-30-36\\I7ugstBrSM2016-05-29-22-30-36\\";
        List<String> window_files = Util.listOfFilesInDirectory(directory);

        //get all files from directory that starts with window
        window_files.removeIf(filename -> (!filename.contains("window") && !filename.contains("corrections")));
        List<Window> windows = WindowImporter.getCorrectedWindows(directory);
        //print said charts
        ChartGenerator.printCharts(windows);
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
