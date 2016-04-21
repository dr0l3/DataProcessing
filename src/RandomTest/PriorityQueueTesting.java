package RandomTest;

import weka.classifiers.functions.LibSVM;
import weka.core.SelectedTag;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created by Rune on 24-03-2016.
 */
public class PriorityQueueTesting {
    public static void main(String[] args) {

        SelectedTag tag  = new SelectedTag(LibSVM.KERNELTYPE_RBF, LibSVM.TAGS_KERNELTYPE);
        System.out.println(tag.toString());
        System.out.println(tag.getRevision());
        System.out.println(tag.getSelectedTag().getReadable());
        System.out.println(Arrays.toString(tag.getTags()));

        PriorityQueue<Double> pq = new PriorityQueue<>(1, new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                if(o1 > o2)
                    return 1;
                if(o2 > o1)
                    return -1;
                return 0;
            }
        });
        pq.add(2.0);
        pq.add(3.0);
        pq.add(1.0);

        System.out.println(pq.peek());

        System.out.println(pq.poll());

        if(pq.peek() < 2.1)
            pq.offer(2.1);

        System.out.println("printing");
        while (pq.peek() != null){
            System.out.println(pq.poll());
        }

    }
}
