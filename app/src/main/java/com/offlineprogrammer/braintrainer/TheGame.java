package com.offlineprogrammer.braintrainer;

import java.util.Random;

public class TheGame {

    private String mOperation;
    private boolean isActive = false;

    public TheGame(String sOperation) {
        mOperation=sOperation;
    }

    public  int doMath(int a, int b){
        int result = 0;
        if (mOperation.equals("+")) {
            result = a+b;
        } else if (mOperation.equals("-")) {
            result = a-b;
        } else if (mOperation.equals("*")) {
            result = a*b;
        } else  if (mOperation.equals("/")) {
            result = a/b;
        }
        return result;
    }








    public  int getRandom(int a, int b){
        int result = 41;



        Random rand = new Random();
        if (mOperation.equals("+")) {
            result =rand.nextInt(41);
        } else if (mOperation.equals("-")) {
            result =rand.nextInt(41 + 20) - 20;;
        } else if (mOperation.equals("*")) {
            if (a == 0) {
                a = 1;
            }
            if (b == 0) {
                b =1;
            }
            result =rand.nextInt(2*a*b);
        } else  if (mOperation.equals("/")) {
            result =rand.nextInt(41);
        }
        return result;
    }


    public String getOperation() {
        return mOperation;
    }

    public void setOperation(String mOperation) {
        this.mOperation = mOperation;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
