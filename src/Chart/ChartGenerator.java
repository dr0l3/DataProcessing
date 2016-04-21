package Chart;

import Core.*;
import Core.Window;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.util.Pair;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.Chart_XY;
import org.knowm.xchart.Series_XY;
import org.knowm.xchart.internal.style.markers.SeriesMarkers;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rune on 22-03-2016.
 */
public class ChartGenerator {
    public static void main(String[] args) {

        ArrayList<Window> windows = RawlineToTapWindowConverterVarLength.getAllWindowsFromURI("D:\\Dropbox\\Thesis\\Data\\RawTapData", 3.0);

        printDerivatives(windows);
    }

    public static void printDerivatives(List<Window> windows) {
        long time = System.currentTimeMillis();
        String folderpath = "D:\\Dropbox\\Thesis\\Charts\\Charts"+time;
        File dir = new File(folderpath);
        dir.mkdir();

        for (int j = 0; j < windows.size(); j++) {
            Window w = windows.get(j);
            int size = w.getListOfFeatureLines().size();
            double[] acc_up_deriv = new double[size];
            double[] acc_x = new double[size];
            double[] acc_y = new double[size];
            double[] acc_z = new double[size];
            double[] acc_up = new double[size];
            double[] acc_rest = new double[size];
            double[] dummy_x = new double[size];

            for (int i = 0; i < w.getListOfFeatureLines().size(); i++) {
                acc_x[i] = w.getListOfFeatureLines().get(i).getEffAccX();
                acc_y[i] = w.getListOfFeatureLines().get(i).getEffAccY();
                acc_z[i] = w.getListOfFeatureLines().get(i).getEffAccZ();
                acc_up[i] = w.getListOfFeatureLines().get(i).getAccUp();
                acc_rest[i] = w.getListOfFeatureLines().get(i).getAccRest();
                dummy_x[i] = i;
            }

            SplineInterpolator interpolator = new SplineInterpolator();
            PolynomialSplineFunction timedVerticalDist = interpolator.interpolate(dummy_x,acc_up);
            UnivariateFunction direvative = timedVerticalDist.derivative();

            for (int i = 0; i < w.getListOfFeatureLines().size(); i++) {
                acc_up_deriv[i] = direvative.value(i);
            }

            Chart_XY chart = new Chart_XY(1600, 1200);

            chart.setTitle(w.getLabel());
            chart.setXAxisTitle("X");
            chart.setYAxisTitle("Y");

//            Series_XY series_rotX = chart.addSeries("acc_x", dummy_x, acc_x);
//            Series_XY series_rotY = chart.addSeries("acc_y", dummy_x, acc_y);
//            Series_XY series_rotZ = chart.addSeries("acc_z", dummy_x, acc_z);
//            Series_XY series_acc_up = chart.addSeries("acc_up", dummy_x, acc_up);
//            Series_XY series_acc_rest = chart.addSeries("acc_rest", dummy_x, acc_rest);
            Series_XY series_acc_deriv = chart.addSeries("deriv", dummy_x, acc_up_deriv);
//            series_rotX.setMarker(SeriesMarkers.CIRCLE);
//            series_rotY.setMarker(SeriesMarkers.DIAMOND);
//            series_rotZ.setMarker(SeriesMarkers.SQUARE);
//            series_acc_up.setMarker(SeriesMarkers.TRIANGLE_UP);
//            series_acc_rest.setMarker(SeriesMarkers.TRIANGLE_DOWN);
//            series_acc_deriv.setMarker(SeriesMarkers.DIAMOND);
//            series_acc_deriv.setLineColor(Color.MAGENTA);
            //chart.getStyler().setMarkerSize(0);

            chart.getStyler().setYAxisMax(40.0);
            chart.getStyler().setYAxisMin(-40.0);

            try {
                BitmapEncoder.saveBitmap(chart, folderpath + "\\chart" + j, BitmapEncoder.BitmapFormat.PNG);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void printChartWithTitle(List<Pair<Window,String>> windows){
        long time = System.currentTimeMillis();
        String folderpath = "D:\\Dropbox\\Thesis\\Charts\\Charts"+time;
        File dir = new File(folderpath);
        dir.mkdir();

        for (int j = 0; j < windows.size(); j++) {
            Window w = windows.get(j).getFirst();
            int size = w.getListOfLines().size();
            double[] acc_x = new double[size];
            double[] acc_y = new double[size];
            double[] acc_z = new double[size];
            double[] acc_up = new double[size];
            double[] acc_rest = new double[size];
            double[] dummy_x = new double[size];

            for (int i = 0; i < w.getListOfFeatureLines().size(); i++) {
                acc_x[i] = w.getListOfFeatureLines().get(i).getEffAccX();
                acc_y[i] = w.getListOfFeatureLines().get(i).getEffAccY();
                acc_z[i] = w.getListOfFeatureLines().get(i).getEffAccZ();
                acc_up[i] = w.getListOfFeatureLines().get(i).getAccUp();
                acc_rest[i] = w.getListOfFeatureLines().get(i).getAccRest();
                dummy_x[i] = i;
            }

            Chart_XY chart = new Chart_XY(1600, 1200);

            chart.setTitle(windows.get(j).getSecond());
            chart.setXAxisTitle("X");
            chart.setYAxisTitle("Y");

            Series_XY series_rotX = chart.addSeries("acc_x", dummy_x, acc_x);
            Series_XY series_rotY = chart.addSeries("acc_y", dummy_x, acc_y);
            Series_XY series_rotZ = chart.addSeries("acc_z", dummy_x, acc_z);
            Series_XY series_acc_up = chart.addSeries("acc_up", dummy_x, acc_up);
            Series_XY series_acc_rest = chart.addSeries("acc_rest", dummy_x, acc_rest);
            series_rotX.setMarker(SeriesMarkers.CIRCLE);
            series_rotY.setMarker(SeriesMarkers.DIAMOND);
            series_rotZ.setMarker(SeriesMarkers.SQUARE);
            series_acc_up.setMarker(SeriesMarkers.TRIANGLE_UP);
            series_acc_rest.setMarker(SeriesMarkers.TRIANGLE_DOWN);

            chart.getStyler().setYAxisMax(15.0);
            chart.getStyler().setYAxisMin(-15.0);

            try {
                BitmapEncoder.saveBitmap(chart, folderpath + "\\chart" + j, BitmapEncoder.BitmapFormat.PNG);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Util.updateProgress(j/windows.size());
        }
    }

    public static void printCharts(List<Window> windows){
        long time = System.currentTimeMillis();
        String folderpath = "D:\\Dropbox\\Thesis\\Charts\\Charts"+time;
        File dir = new File(folderpath);
        dir.mkdir();

        for (int j = 0; j < windows.size(); j++) {
            Window w = windows.get(j);
            int size = w.getListOfFeatureLines().size();
            double[] acc_x = new double[size];
            double[] acc_y = new double[size];
            double[] acc_z = new double[size];
            double[] acc_up = new double[size];
            double[] acc_rest = new double[size];
            double[] dummy_x = new double[size];

            for (int i = 0; i < w.getListOfFeatureLines().size(); i++) {
                acc_x[i] = w.getListOfFeatureLines().get(i).getEffAccX();
                acc_y[i] = w.getListOfFeatureLines().get(i).getEffAccY();
                acc_z[i] = w.getListOfFeatureLines().get(i).getEffAccZ();
                acc_up[i] = w.getListOfFeatureLines().get(i).getAccUp();
                acc_rest[i] = w.getListOfFeatureLines().get(i).getAccRest();
                dummy_x[i] = i;
            }

            Chart_XY chart = new Chart_XY(1600, 1200);

            chart.setTitle(w.getLabel());
            chart.setXAxisTitle("X");
            chart.setYAxisTitle("Y");

            //Series_XY series_rotX = chart.addSeries("acc_x", dummy_x, acc_x);
            //Series_XY series_rotY = chart.addSeries("acc_y", dummy_x, acc_y);
            Series_XY series_rotZ = chart.addSeries("acc_z", dummy_x, acc_z);
            //Series_XY series_acc_up = chart.addSeries("acc_up", dummy_x, acc_up);
            //Series_XY series_acc_rest = chart.addSeries("acc_rest", dummy_x, acc_rest);
            //series_rotX.setMarker(SeriesMarkers.CIRCLE);
            //series_rotY.setMarker(SeriesMarkers.DIAMOND);
            series_rotZ.setMarker(SeriesMarkers.SQUARE);
            //series_acc_up.setMarker(SeriesMarkers.TRIANGLE_UP);
            //series_acc_rest.setMarker(SeriesMarkers.TRIANGLE_DOWN);

            chart.getStyler().setYAxisMax(40.0);
            chart.getStyler().setYAxisMin(-40.0);

            try {
                BitmapEncoder.saveBitmap(chart, folderpath + "\\chart" + j, BitmapEncoder.BitmapFormat.PNG);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
