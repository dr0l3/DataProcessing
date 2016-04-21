package RandomTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Rune on 23-03-2016.
 */
public class ComparatorTest {
    public static void main(String[] args) {
        ArrayList<Double> stuff = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            stuff.add(Math.random());
        }

        for (int i = 0; i < 10; i++) {
            System.out.println(stuff.get(i));
        }

        Collections.sort(stuff, new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                if (o1 > o2)
                    return -1;
                if (o2 > o1)
                    return 1;
                return 0;
            }
        });

        System.out.println("----");

        for (int i = 0; i < 10; i++) {
            System.out.println(stuff.get(i));
        }
    }
}
