package RandomTest;

import Core.BiasConfiguration;
import Core.FeatureLine;
import Core.SensorEventRecord;
import Core.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rune on 18-04-2016.
 */
public class CrossCorrelation {
    public static void main(String[] args) {
        List<String> filnames = Util.listOfFilesInDirectory("D:\\Dropbox\\Thesis\\Data\\Crossvalidationtest\\");
        ArrayList<String> lines_file1 = new ArrayList<>();
        ArrayList<String> lines_file2 = new ArrayList<>();
        lines_file1 = Util.importData(filnames.get(0));
        lines_file2 = Util.importData(filnames.get(1));
        List<SensorEventRecord> file_1_events = rawlineToSensorEventRecord(lines_file1);
        List<SensorEventRecord> file_2_events = rawlineToSensorEventRecord(lines_file2);

        float cross_correlation_raw = calculateCrossCorrelation(file_1_events, file_2_events);
        float cross_correlation_it = calculateCrossCorrelation(file_1_events, file_1_events);
        float cross_correlation_mine = calculateCrossCorrelation(file_2_events, file_2_events);
        float min_differnce = differenceBetweenSamples(file_1_events, file_2_events);


        BiasConfiguration bias_it119 = new BiasConfiguration(
                0.005f,-0.03f,-0.090f,
                -0.012f,0.024f,0.083f);
        BiasConfiguration bias_myphone = new BiasConfiguration(
                -0.01f,0.002f,0.018f,
                0.02f,0.007f,-0.0006f);
        List<SensorEventRecord> file_1_corrected = biasCorrectSensorEvents(new ArrayList<>(file_1_events), bias_it119);
        List<SensorEventRecord> file_2_corrected = biasCorrectSensorEvents(new ArrayList<>(file_2_events), bias_myphone);
        float cross_correlation_corrected = calculateCrossCorrelation(file_1_corrected, file_2_corrected);
        float cross_correlation_it_cor = calculateCrossCorrelation(file_1_corrected, file_1_corrected);
        float cross_correlation_mine_cor = calculateCrossCorrelation(file_2_corrected, file_2_corrected);
        float min_diffence_cor = differenceBetweenSamples(file_1_corrected, file_2_corrected);
        float sanity = differenceBetweenSamples(file_1_corrected, file_1_corrected);
        System.out.println("Raw CC =            " + cross_correlation_raw);
        System.out.println("Corrected CC =      " + cross_correlation_corrected);
        System.out.println("Raw CC it =         " + cross_correlation_it);
        System.out.println("Corrected CC it =   " + cross_correlation_it_cor);
        System.out.println("Raw CC mine =       " + cross_correlation_mine);
        System.out.println("Corrected CC mine = " + cross_correlation_mine_cor);
        System.out.println("difference raw =    " + min_differnce);
        System.out.println("difference cor =    " + min_diffence_cor);
        System.out.println("sanity = " + sanity);

    }

    private static float differenceBetweenSamples(List<SensorEventRecord> file_1, List<SensorEventRecord> file_2){
        List<SensorEventRecord> smallest;
        List<SensorEventRecord> biggest;
        int n;
        if(file_1.size() > file_2.size()){
            smallest = file_2;
            biggest = file_2;
            n = biggest.size();
        } else {
            smallest = file_1;
            biggest = file_2;
            n = biggest.size();
        }
        float highest_diffence = Float.MAX_VALUE;
        float[] null_acc = {0,0,0};
        for (int d = 0; d < (n-1); d++) {
            float difference_x = 0;
            float difference_y = 0;
            float differnece_z = 0;
            for (int i = 0; i < n; i++) {
                float[] acc_1 = (i > 0 && i < smallest.size()-1) ? smallest.get(i).getAcceleration() : null_acc;
                float[] acc_2 = ((i+d) > 0 && (i+d) < biggest.size()-1) ? biggest.get(i+d).getAcceleration() : null_acc;
                difference_x += Math.abs(acc_1[0] - acc_2[0]);
                difference_y += Math.abs(acc_1[1] - acc_2[1]);
                differnece_z += Math.abs(acc_1[2] - acc_2[2]);
            }
            float total_difference = difference_x + difference_y + differnece_z;
            if(total_difference < highest_diffence)
                highest_diffence = total_difference;
        }
        return highest_diffence;
    }

    private static List<SensorEventRecord> biasCorrectSensorEvents(List<SensorEventRecord> records, BiasConfiguration bias) {
        List<SensorEventRecord> records_corrected = new ArrayList<>();
        for (SensorEventRecord ser : records) {
            ser.applyBias(bias);
        }
        return records;
    }

    private static float calculateBiasCorrectedAcceleration(float acc_old, float gra, float correction_coefficient){
        return (acc_old - (gra * correction_coefficient));
    }

    private static float calculateCrossCorrelation(List<SensorEventRecord> file_1_events, List<SensorEventRecord> file_2_events) {
        int n;
        List<SensorEventRecord> smallest;
        List<SensorEventRecord> biggest;
        if(file_1_events.size() > file_2_events.size()){
            smallest = file_2_events;
            biggest = file_1_events;
            n = biggest.size();
        } else {
            smallest = file_1_events;
            biggest = file_2_events;
            n = biggest.size();
        }
        float highest_correlation = 0;
        float[] null_acc = {0,0,0};
        for (int d = 0; d < (n-1); d++) {
            float correlation_x = 0;
            float correlation_y = 0;
            float correlation_z = 0;
            for (int i = 0; i < n; i++) {
                float[] acc_1 = (i > 0 && i < smallest.size()-1) ? smallest.get(i).getAcceleration() : null_acc;
                float[] acc_2 = ((i+d) > 0 && (i+d) < biggest.size()-1) ? biggest.get(i+d).getAcceleration() : null_acc;
                correlation_x += acc_1[0] * acc_2[0];
                correlation_y += acc_1[1] * acc_2[1];
                correlation_z += acc_1[2] * acc_2[2];
            }
            float total_correlation = correlation_x + correlation_y + correlation_z;
            if(total_correlation > highest_correlation)
                highest_correlation = total_correlation;
        }
        return highest_correlation;
    }

    private static List<SensorEventRecord> rawlineToSensorEventRecord(ArrayList<String> lines) {
        ArrayList<SensorEventRecord> records = new ArrayList<>();
        for (String str : lines) {
            String[] split = str.split(" | ");
            float[] acc = {Float.valueOf(split[0]), Float.valueOf(split[2]), Float.valueOf(split[4])};
            float[] grav = {Float.valueOf(split[6]), Float.valueOf(split[8]), Float.valueOf(split[10])};
            long ts = Long.parseLong(split[12]);
            records.add(new SensorEventRecord(acc,grav,ts));
        }

        return records;
    }


}
