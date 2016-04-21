package RandomTest;

/**
 * Created by Rune on 08-04-2016.
 */
public class Randomtensecprocess {
    public static void main(String[] args)  {

        Randomtensecprocess stuff = new Randomtensecprocess();

        System.out.println("Program Starting");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Program Ending");
    }

    public Randomtensecprocess() {
    }
}
