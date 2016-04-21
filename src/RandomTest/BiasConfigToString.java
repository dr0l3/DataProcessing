package RandomTest;

import Core.BiasConfiguration;

/**
 * Created by Rune on 19-04-2016.
 */
public class BiasConfigToString {

    public static void main(String[] args) {
        BiasConfiguration biasConfiguration = new BiasConfiguration(-0.005f, -0.03f , -0.093f, -0.0099f, 0.023f, 0.092f);
        System.out.println(biasConfiguration.toString());
    }
}
