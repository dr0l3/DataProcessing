package RandomTest;

import Chart.ChartGenerator;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.knowm.xchart.Chart_XY;
import org.knowm.xchart.XChartPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by Rune on 01-04-2016.
 */
public class FFTTest {
    public static void main(String[] args) {
        double[] stuff = new double[128];
        Random random = new Random();
        for (int i = 0; i < stuff.length; i++) {
            int next = (int) (random.nextInt(2) + Math.sin(i));
            if(next > 80 && next < 90)
                next = next/2;
            stuff[i] = next;
            //stuff[i] = Math.cos(i);
        }

        double[] xData = new double[128];
        for (int i = 0; i < xData.length; i++) {
            xData[i] = i;
        }

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        FastFourierTransformer fft2 = new FastFourierTransformer(DftNormalization.UNITARY);

        Complex[] res = fft.transform(stuff, TransformType.FORWARD);

        double[] xDataComplex = new double[stuff.length];
        double[] yDataComplex = new double[stuff.length];
        for (int i = 0; i < stuff.length; i++) {
            xDataComplex[i] = res[i].getReal();
            yDataComplex[i] = res[i].getImaginary();
        }

        double[] magnitudes = new double[stuff.length];
        for (int i = 0; i < stuff.length; i++) {
            if(i < stuff.length/2)
                magnitudes[i] = Math.sqrt(Math.pow(res[i].getReal(),2) + Math.pow(res[i].getImaginary(),2)) * 2 / stuff.length;
            else
                magnitudes[i] = 0;
        }

        System.out.println(Arrays.toString(res));

        //DC = res[0].getReal();
        //Spectral Energy = sum ((real^2 + imag^2) / number of samples)
        //entropy = sum(c_j * log(c_j)) where c_j = (sqrt(real^2+imag^2))/(sum(sqrt(real^2+imag^2)))
        //correlation between different axes
        //correlation = sum(a_j * c_j) for all j

        //System.out.println(Arrays.toString(magnitudes));

        Chart_XY timeChart = new Chart_XY(800,400);
        timeChart.addSeries("Signal",xData,stuff);

        Chart_XY frequencyChart = new Chart_XY(800, 400);
        frequencyChart.addSeries("frequency",xData,magnitudes);

        Chart_XY complexChart = new Chart_XY(800, 400);
        complexChart.addSeries("real/imag", xDataComplex, yDataComplex);

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
                                                   @Override
                                                   public void run() {
                                                       JFrame chartFrame = new JFrame("stuff");
                                                       chartFrame.setLayout(new BoxLayout(chartFrame.getContentPane(), BoxLayout.X_AXIS));
                                                       chartFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                                                       XChartPanel<Chart_XY> signalPanel = new XChartPanel<Chart_XY>(timeChart);
                                                       XChartPanel<Chart_XY> frequencyPanel = new XChartPanel<Chart_XY>(frequencyChart);
                                                       XChartPanel<Chart_XY> compralPanel = new XChartPanel<Chart_XY>(complexChart);
                                                       chartFrame.add(signalPanel);
                                                       chartFrame.add(frequencyPanel);
                                                       chartFrame.add(compralPanel);
                                                       chartFrame.pack();
                                                       chartFrame.setVisible(true);
                                                   }
                                               });

        /*double[] abses = new double[128];
        for (int i = 0; i < 128; i++) {
            abses[i] = Math.pow( res[i].abs(),2);
        }
        double PSD = 0;
        for (int i = 0; i < abses.length; i++) {
            PSD += abses[i];
        }
        PSD = PSD/stuff.length;
        System.out.println(Arrays.toString(abses));
        System.out.println(PSD);*/
    }
}
