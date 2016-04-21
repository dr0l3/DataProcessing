package RandomTest;

import Chart.ChartGenerator;
import Core.Window;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

/**
 * Created by Rune on 30-03-2016.
 */
public class PrintChartsFromFileTutorial {
    public static void main(String[] args) {
        ArrayList<Window> listOfStuff;
        try{
            FileInputStream filIn = new FileInputStream("D:\\Dropbox\\Thesis\\Data\\WindowsFromPhone\\WindowExport-938336090");
            ObjectInputStream in = new ObjectInputStream(filIn);
            listOfStuff = (ArrayList<Window>) in.readObject();
            in.close();
            filIn.close();
            ChartGenerator.printCharts(listOfStuff);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
