package com.offlineprogrammer.braintrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    Button goButton;
    ArrayList<Integer> answers = new ArrayList<Integer>();
    private int locationOfCorrectAnswer;
    TextView resultTextView;
    int score = 0;
    int numberOfQuestions = 0;
    TextView scoreTextView;
    Button button0 ;
    Button button1 ;
    Button button2 ;
    Button button3 ;
    private TextView sumTextView;
    TextView timerTextView;
    Button playAgain;
    ConstraintLayout gameLayout;
    ConstraintLayout configLayout;
    Boolean playIsActive = false;
    private String sOperation;
    CountDownTimer countDownTimer = null;

    public int doMath(int a, int b){

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

    public void newQuestion(){

        Random rand = new Random();

        int a = rand.nextInt(21);
        int b = rand.nextInt(21);

        locationOfCorrectAnswer = rand.nextInt(4);

        sumTextView.setText(Integer.toString(a) + " " + sOperation + " " + Integer.toString(b));

        answers.clear();

        for (int i=0; i<4;i++){
            if(i== locationOfCorrectAnswer){
                answers.add(doMath(a,b));

            } else {
                int wrongAnswer = rand.nextInt(41);
                while (wrongAnswer == doMath(a,b)){
                    wrongAnswer = rand.nextInt(41);
                }
                answers.add(wrongAnswer);

            }

        }

        button0.setText(Integer.toString(answers.get(0)));
        button1.setText(Integer.toString(answers.get(1)));
        button2.setText(Integer.toString(answers.get(2)));
        button3.setText(Integer.toString(answers.get(3)));

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        goButton = findViewById(R.id.goButton);

        goButton.setVisibility(View.VISIBLE);

        sumTextView = findViewById(R.id.sumTextView);
        resultTextView = findViewById(R.id.resultTextView);
        scoreTextView = findViewById(R.id.scoreTextView);
         button0 = findViewById(R.id.button0);
         button1 = findViewById(R.id.button1);
         button2 = findViewById(R.id.button2);
         button3 = findViewById(R.id.button3);
         timerTextView = findViewById(R.id.timerTextView);
         playAgain = findViewById(R.id.playAgainButton);
         gameLayout=findViewById(R.id.gameLayout);
        configLayout = findViewById(R.id.configLayout);


        sOperation = "+";

        configLayout.setVisibility(View.INVISIBLE);

         gameLayout.setVisibility(View.INVISIBLE);




    }

    public void start(View view){
        Log.i("goButton Clicked","Hide");
        goButton.setVisibility(View.INVISIBLE);
        gameLayout.setVisibility(View.VISIBLE);
        playIsActive = true;
        newQuestion();
        playAgain(playAgain);

    }

    public void chooseOperation(View view){

        goButton.setVisibility(View.VISIBLE);
        gameLayout.setVisibility(View.INVISIBLE);
        configLayout.setVisibility(View.INVISIBLE);
        sOperation = view.getTag().toString();
        Log.i("chooseOperation: ", " is" +sOperation);
        start(goButton);

    }


    public void configureGame(View view){
        Log.i("configureButton Clicked","Hide");

        goButton.setVisibility(View.INVISIBLE);
        gameLayout.setVisibility(View.INVISIBLE);
        configLayout.setVisibility(View.VISIBLE);


    }


    public void playAgain(View view){
        score=0;
        numberOfQuestions=0;
        timerTextView.setText("30s");
        scoreTextView.setText(Integer.toString(score) + "/" + Integer.toString(numberOfQuestions));
        newQuestion();
        playAgain.setVisibility(View.INVISIBLE);
        playIsActive = true;
        if(countDownTimer  != null){
            countDownTimer.cancel();
        }
        countDownTimer =  new CountDownTimer(30100,1000){

            @Override
            public void onTick(long millisUntilFinished) {

                timerTextView.setText(String.valueOf(millisUntilFinished/1000)+"s");

            }

            @Override
            public void onFinish() {
                resultTextView.setText("Done!");
                playAgain.setVisibility(View.VISIBLE);
                playIsActive=false;


            }
        }.start();
    }

    public void chooseAnswer(View view){
        if (playIsActive == false){
            return;
        }
        Log.i("Selected button","is " + view.getTag().toString());
        int selectedButton = Integer.parseInt(view.getTag().toString());

        if (selectedButton == locationOfCorrectAnswer){
            resultTextView.setText("Correct!");
            score++;
            Log.i("Winner", "button" + selectedButton);
        } else {
            resultTextView.setText("Wrong :(");
            Log.i("Wrong :( ", "button" + selectedButton);

        }
        numberOfQuestions++;
        scoreTextView.setText(Integer.toString(score) + "/" + Integer.toString(numberOfQuestions));
        newQuestion();

    }
}
