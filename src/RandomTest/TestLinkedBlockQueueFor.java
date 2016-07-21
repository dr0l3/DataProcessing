package RandomTest;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Rune on 13-05-2016.
 */
public class TestLinkedBlockQueueFor {

    public static void main(String[] args) {
        Queue<String> queue = new LinkedBlockingQueue<>();
        queue.add("first");
        queue.add("second");
        queue.add("third");
        queue.add("fourth");

        for (String s : queue) {
            System.out.println(s);
        }

        queue.poll();

        queue.add("fifth");

        for (String s : queue) {
            System.out.println(s);
        }
    }
}
