package RandomTest;

import javafx.scene.chart.Chart;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.Chart_XY;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.Series_XY;
import org.knowm.xchart.internal.style.markers.SeriesMarkers;

import java.io.IOException;

/**
 * Created by Rune on 22-03-2016.
 */
public class xchartTutorial {

    public static void main(String[] args) {
        double [] xData = new double[] { 0.0, 1.0, 2.0};
        double [] zData = new double[] { 1.0, 1.0, 1.0};
        double [] yData = new double[] { 0.0, 1.0, 2.0};

        Chart_XY chart = new Chart_XY(500, 400);
        chart.setTitle("Sample chart");
        chart.setXAxisTitle("X");
        chart.setYAxisTitle("Y");

        Series_XY series_xy = chart.addSeries("y(x)", xData, yData);
        Series_XY series_xz = chart.addSeries("z(x)", xData, zData);
        series_xy.setMarker(SeriesMarkers.CIRCLE);
        series_xz.setMarker(SeriesMarkers.SQUARE);

        try {
            BitmapEncoder.saveBitmap(chart, "D:\\Dropbox\\Thesis\\Charts\\chart" , BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
