package RandomTest;

import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * Created by Rune on 09-03-2016.
 */
public class randomstuff {
    public static void main(String[] args) {

        String stuff = "@ATTRIBUTE  upAcc_MinVal               NUMERIC" + "\r" +
                "@ATTRIBUTE  upAcc_MaxVal               NUMERIC" + "\r" +
                "@ATTRIBUTE  upAcc_MinDif               NUMERIC" + "\r" +
                "@ATTRIBUTE  upAcc_MaxDif               NUMERIC" + "\r" +
                "@ATTRIBUTE  upAcc_Mean                 NUMERIC" + "\r" +
                "@ATTRIBUTE  upAcc_RootMeanSquare       NUMERIC" + "\r" +
                "@ATTRIBUTE  upAcc_AverageDif           NUMERIC" + "\r" +
                "@ATTRIBUTE  restAcc_MinVal             NUMERIC" + "\r" +
                "@ATTRIBUTE  restAcc_MaxVal             NUMERIC" + "\r" +
                "@ATTRIBUTE  restAcc_MinDif             NUMERIC" + "\r" +
                "@ATTRIBUTE  restAcc_MaxDif             NUMERIC" + "\r" +
                "@ATTRIBUTE  restAcc_Mean               NUMERIC" + "\r" +
                "@ATTRIBUTE  restAcc_Mean_Absolute      NUMERIC" + "\r" +
                "@ATTRIBUTE  restAcc_RootMeanSquare     NUMERIC" + "\r" +
                "@ATTRIBUTE  restAcc_AverageDif         NUMERIC" + "\r" +
                "@ATTRIBUTE  xAccRaw_MinVal             NUMERIC" + "\r" +
                "@ATTRIBUTE  xAccRaw_MaxVal             NUMERIC" + "\r" +
                "@ATTRIBUTE  xAccRaw_MinDif             NUMERIC" + "\r" +
                "@ATTRIBUTE  xAccRaw_MaxDif             NUMERIC" + "\r" +
                "@ATTRIBUTE  xAccRaw_Mean               NUMERIC" + "\r" +
                "@ATTRIBUTE  xAccRaw_RootMeanSquare     NUMERIC" + "\r" +
                "@ATTRIBUTE  xAccRaw_AverageDif         NUMERIC" + "\r" +
                "@ATTRIBUTE  yAccRaw_MinVal             NUMERIC" + "\r" +
                "@ATTRIBUTE  yAccRaw_MaxVal             NUMERIC" + "\r" +
                "@ATTRIBUTE  yAccRaw_MinDif             NUMERIC" + "\r" +
                "@ATTRIBUTE  yAccRaw_MaxDif             NUMERIC" + "\r" +
                "@ATTRIBUTE  yAccRaw_Mean               NUMERIC" + "\r" +
                "@ATTRIBUTE  yAccRaw_RootMeanSquare     NUMERIC" + "\r" +
                "@ATTRIBUTE  yAccRaw_AverageDif         NUMERIC" + "\r" +
                "@ATTRIBUTE  zAccRaw_MinVal             NUMERIC" + "\r" +
                "@ATTRIBUTE  zAccRaw_MaxVal             NUMERIC" + "\r" +
                "@ATTRIBUTE  zAccRaw_MinDif             NUMERIC" + "\r" +
                "@ATTRIBUTE  zAccRaw_MaxDif             NUMERIC" + "\r" +
                "@ATTRIBUTE  zAccRaw_Mean               NUMERIC" + "\r" +
                "@ATTRIBUTE  zAccRaw_RootMeanSquare     NUMERIC" + "\r" +
                "@ATTRIBUTE  zAccRaw_AverageDif         NUMERIC" + "\r";

        //createAttributelistFromString(stuff);
        //createValuesListFromString(stuff);
        System.out.println((4/30)*403.0);
    }

    public static void createAttributelistFromString(String s){
        String[] listOfAttributes = s.split("\r");
        int i = 1;
        for (String attribute : listOfAttributes) {
            String out = attribute.replace("@ATTRIBUTE", "");
            out = out.replace("NUMERIC", "");
            out = out.trim();
            System.out.println("attributes.add(new Attribute(\""+out+"\"));");
            i++;
        }
    }

    public static void createValuesListFromString(String s){
        String[] listOfAttributes = s.split("\r");
        int i = 1;
        for (String attribute : listOfAttributes) {
            String out = attribute.replace("@ATTRIBUTE", "");
            out = out.replace("NUMERIC", "");
            out = out.trim();
            System.out.println("window.get"+out+"(),\r");
            i++;
        }
    }
}
