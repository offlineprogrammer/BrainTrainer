package com.offlineprogrammer.braintrainer;

import java.util.ArrayList;
import java.util.Random;

public class TheGame {

    private String mOperation;
    private boolean isActive = false;
    public int locationOfCorrectAnswer;
    public  int a;
    public  int b;
    private int score;
    private int timer;
    private int numberOfQuestions;
    ArrayList<Integer> answers = new ArrayList<Integer>();

    public TheGame(String sOperation, int gameTimer) {
        mOperation=sOperation;
        timer=gameTimer;
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


    public ArrayList<Integer> setupGame(){
        Random rand = new Random();
        a = rand.nextInt(21);
        b = rand.nextInt(21);
        locationOfCorrectAnswer = rand.nextInt(4);
       // sumTextView.setText(String.format("%s %s %s", Integer.toString(a), myGame.getOperation() , Integer.toString(b)));
        answers.clear();
        for (int i=0; i<4;i++){
            if(i== locationOfCorrectAnswer){
                answers.add(doMath(a,b));
            } else {
                int wrongAnswer = getRandom(a,b);
                while (wrongAnswer == doMath(a,b)){
                    wrongAnswer = getRandom(a,b);;
                }
                answers.add(wrongAnswer);
            }
        }
        return answers ;
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

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(int numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public void incrementScore() {
        this.score++;
    }

    public void incrementNumberOfQuestions() {
        this.numberOfQuestions++;
    }
}
