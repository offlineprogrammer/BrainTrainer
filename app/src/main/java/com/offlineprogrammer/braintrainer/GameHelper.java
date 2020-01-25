package com.offlineprogrammer.braintrainer;

import java.util.Random;

public class GameHelper {

    public static int doMath(int a, int b, String sOperation){
        int result = 0;
        if (sOperation.equals("+")) {
            result = a+b;
        } else if (sOperation.equals("-")) {
            result = a-b;
        } else if (sOperation.equals("*")) {
            result = a*b;
        } else  if (sOperation.equals("/")) {
            result = a/b;
        }
        return result;
    }

    public static int getRandom(int a, int b, String sOperation){
        int result = 41;



        Random rand = new Random();
        if (sOperation.equals("+")) {
            result =rand.nextInt(41);
        } else if (sOperation.equals("-")) {
            result =rand.nextInt(41 + 20) - 20;;
        } else if (sOperation.equals("*")) {
            if (a == 0) {
                a = 1;
            }
            if (b == 0) {
                b =1;
            }
            result =rand.nextInt(2*a*b);
        } else  if (sOperation.equals("/")) {
            result =rand.nextInt(41);
        }
        return result;
    }
}
