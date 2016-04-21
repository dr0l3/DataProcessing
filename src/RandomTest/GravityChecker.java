package RandomTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import Core.Util;

/**
 * Created by Rune on 08-03-2016.
 */
public class GravityChecker {

    private static String FROM_URI = "D:\\Dropbox\\Thesis\\Data\\Test";

    public static void main(String[] args) {
        List<String> files = Util.listOfFilesInDirectory(FROM_URI);
        ArrayList<String> lines = new ArrayList<>();
        files.forEach(filename -> {
            lines.addAll(Util.importData(filename));
        });

        ArrayList<Double> gravities = new ArrayList<>();

        for (String line : lines) {
            //print gravity
            if(line.contains("---"))
                continue;
            double gravity = computeGravity(line);
            gravities.add(gravity);
            //System.out.println(gravity);
        }

        Collections.sort(gravities);
        Double max = Double.MIN_VALUE;
        Double min = Double.MAX_VALUE;
        for (Double gravity : gravities) {
            if(gravity > max)
                max = gravity;
            if(gravity < min)
                min = gravity;
        }

        System.out.println(max - min);
    }

    private static double computeGravity(String line) {
        //extract g1
        int pipeThree = Util.ordinalIndexOf(line,'|', 2);
        int pipeFour = Util.ordinalIndexOf(line,'|', 3);
        int pipeFive = Util.ordinalIndexOf(line,'|', 4);
        int pipeSix = Util.ordinalIndexOf(line,'|', 5);
        String g1 = line.substring(pipeThree+1, pipeFour).trim();
        Double gr1 = Double.parseDouble(g1);
        //between 3 and 4
        //extract g2
        String g2 = line.substring(pipeFour+1, pipeFive).trim();
        Double gr2 = Double.parseDouble(g2);
        //between 4 and 5
        //extract g3
        String g3 = line.substring(pipeFive+1, pipeSix).trim();
        Double gr3 = Double.parseDouble(g3);
        //between 5 and 6
        //compute length
        return Math.sqrt(Math.pow(gr1,2)+Math.pow(gr2,2)+Math.pow(gr3,2));
    }
}
