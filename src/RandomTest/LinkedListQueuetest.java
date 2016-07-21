package RandomTest;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Rune on 17-05-2016.
 */
public class LinkedListQueuetest {
    public static void main(String[] args) {
        Queue<String> queue = new LinkedBlockingQueue<>();
        queue.add("first");

        System.out.println(new LinkedList<>(queue).get(1));
    }
}
