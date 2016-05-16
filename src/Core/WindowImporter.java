package Core;

import org.apache.commons.math3.util.Pair;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.util.ArrayList;
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
