package com.example.erlend.quizzapp.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.erlend.quizz_fra_dekompilert.R;

import java.util.Timer;
import java.util.TimerTask;

//Viser en nettelling. Til bruk før en ny quiz stater eller gjenopptas. Nedtellingen blir ikke kjørt hvis appen kun blir minimert og deretter åpnet uten
//at brukeren har navigert seg tilbake til hovedmenyen.
public class CountdownActivity extends AppCompatActivity {

    private Timer timer;

    private int millisAnimateTextviewPrNum = 600;
    private int millisDelayBeforeNext = 400;
    private int textSizeTargetCountdown;
    private int from = 3;

    private int[] currNum = {from};
    private boolean runTimer = true;

    private TextView txtCountdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_countdown);

        txtCountdown = findViewById(R.id.txtCountdown);
        textSizeTargetCountdown = (int) getResources().getDimension(R.dimen.txtSizeTargetCountdown);
    }

    private void countdown(final int[] num){

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                if(!runTimer){
                    timer.cancel();
                    timer.purge();
                    return;
                }

                final String[] text = new String[1];
                AnimatorSet animatorSet = new AnimatorSet();

                ObjectAnimator animTxtSize = ObjectAnimator.ofFloat(txtCountdown, "textSize", 0, textSizeTargetCountdown);
                ObjectAnimator animAlpha = ObjectAnimator.ofFloat(txtCountdown, "alpha", 0, 1);

                animatorSet.play(animTxtSize).with(animAlpha);
                animatorSet.setInterpolator(new AccelerateInterpolator());
                animatorSet.setDuration(millisAnimateTextviewPrNum);
                text[0] = num[0] == 0?getResources().getString(R.string.go).toUpperCase() +"!": num[0] +"";

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtCountdown.setText(text[0]);
                        animatorSet.start();

                    }
                });

                num[0]--;


                if(num[0] < 0 ){
                    timer.cancel();
                    timer.purge();
                    onFinishedCountdown();
                    return;
                }


            }
        },0, millisAnimateTextviewPrNum + millisDelayBeforeNext);
    }

    //Navigerer tilbake til valg av quizz
    @Override
    public void onBackPressed() {
        finish();
        setResult(Activity.RESULT_CANCELED);
    }

    //Feks ved minimering av appen eller rotering
    @Override
    protected void onPause() {
        super.onPause();
        runTimer = false;

    }

    @Override
    protected void onResume(){
        super.onResume();
        runTimer = true;
        countdown(currNum);
    }


    private void onFinishedCountdown(){
        Looper looper = Looper.myLooper();
        looper.prepare();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setResult(Activity.RESULT_OK);
                finish();
            }
        }, millisDelayBeforeNext * 2);

        looper.loop();
        looper.quit();

    }

    //Håndtererer roteringen ved å ikke gjøre noe slik at netdtellingen ikke begynner på nytt.
    //Samme layout benyttes da også for begge orientasjonene
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}
