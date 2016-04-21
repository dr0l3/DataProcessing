package RandomTest;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Rune on 15-03-2016.
 */
public class ApacheCommonsTest {

    public static void main(String[] args) {
        ArrayList<Double> xs = new ArrayList<>();
        ArrayList<Double> ys = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            xs.add((double) i);
            ys.add(Math.random() * 100);
        }

        Collections.sort(ys);
        double[] x = xs.stream().mapToDouble(Double::doubleValue).toArray(); //via method reference
        double[] y = ys.stream().mapToDouble(Double::doubleValue).toArray(); //via method reference
        System.out.println(Arrays.toString(x));
        System.out.println(Arrays.toString(y));
        SplineInterpolator interpolator = new SplineInterpolator();
        PolynomialSplineFunction ecdf = interpolator.interpolate(x,y);
        System.out.println(ecdf.toString());
        System.out.println(ecdf.value(0.2*100));
        System.out.println(ecdf.value(0.4*100));
        System.out.println(ecdf.value(0.6*100));
        System.out.println(ecdf.value(0.8*100));
    }
}
