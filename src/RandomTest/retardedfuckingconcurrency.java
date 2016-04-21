package RandomTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by Rune on 23-03-2016.
 */
public class retardedfuckingconcurrency {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService es = Executors.newFixedThreadPool(10);
        Set<Callable<Double>> arrayList = new HashSet<>();

        for (int i = 0; i < 10; i++) {
            arrayList.add(new stuff(i));
        }
        List<Future<Double>> blah = es.invokeAll(arrayList);

    }

    private static class stuff implements Callable<Double>{

        private int seed;

        public stuff(int seed) {
            this.seed = seed;
        }

        @Override
        public Double call() throws Exception {
            return Math.random()+ seed;
        }
    }

    private static class stuff2 implements Callable<String>{
        private int seed;

        public stuff2(int seed) {
            this.seed = seed;
        }

        @Override
        public String call() throws Exception {
            return String.valueOf(seed);
        }
    }
}
