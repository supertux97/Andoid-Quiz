package com.example.erlend.quizzapp.activity;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.erlend.quizz_fra_dekompilert.R;
import com.example.erlend.quizzapp.DataManager;
import com.example.erlend.quizzapp.model.TrophyManager;
import com.example.erlend.quizzapp.model.Xp;
import com.plattysoft.leonids.ParticleSystem;

import java.io.Serializable;
import java.util.ArrayList;

import static com.example.erlend.quizzapp.activity.OnLevelUpTrophyActivity.Mode.*;

public class OnLevelUpTrophyActivity extends AppCompatActivity {

    public static final String KEY_MODE = "KEY_MODE";
    public static final String KEY_TROPHY = "KEY_TROPHY";

    private Mode mode;
    private TrophyManager.Trophy trophy;

    private boolean unlockedTrophyCaousedLevelUp = false;

    boolean musicEffectsMuted;
    private int soundIDLevelUp;
    private int soundIDUnlockedTrophy;
    private SoundPool soundPool;

    private int millisToShowTrophy = 4500;
    private int millisToShowLevelUp = 2500;

    private LinearLayout cont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mode = (Mode) getIntent().getSerializableExtra(KEY_MODE);
        musicEffectsMuted = getSharedPreferences(DataManager.KEY_SHARED_PREFERENCES,0).getBoolean(DataManager.KEY_PREFS_EFFECTS_MUTED, false);

        if(mode == LEVEL_UP){
            setContentView(R.layout.level_up);
        }
        else if(mode.isTrophyType){
            trophy = getIntent().getParcelableExtra(KEY_TROPHY);
            unlockedTrophyCaousedLevelUp = Xp.addXp(trophy.getGainedXP());
            setContentView(R.layout.unlocked_trophy);
        }

        cont = findViewById(R.id.contLevelUpTrophy);

        if(Build.VERSION.SDK_INT < 21 ){
            soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC,0);
        }
        else{
            soundPool = new SoundPool.Builder().setMaxStreams(2).build();
        }
        if(mode == LEVEL_UP)
            soundIDLevelUp = soundPool.load(getApplicationContext(), R.raw.level_up, 1);
        else if(mode.isTrophyType)
            soundIDUnlockedTrophy = soundPool.load(getApplicationContext(), R.raw.unlocked_trophy, 1);

        startAnimation();
    }

    private void startAnimation(){
        //Løser animasjonen på denne måten for å kunne utsette kjøring av koden til etter at animasjonen er ferdig
        Animation animRevealLevelUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        animRevealLevelUp.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation animation) {

                if(mode == LEVEL_UP)
                    displayLevelUpInformation();
                else if(mode.isTrophyType){
                    displayTrophyInformation();
                }

                if(!musicEffectsMuted){
                    if(mode == LEVEL_UP){
                        soundPool.play(soundIDLevelUp, 1, 1, 1, 0, 1);
                    }
                    else if(mode.isTrophyType){
                        soundPool.play(soundIDUnlockedTrophy, 1, 1, 1, 0, 1);
                    }
                }

            }

            @Override public void onAnimationStart(Animation animation) {}

            @Override public void onAnimationRepeat(Animation animation) {}
        });
        cont.startAnimation(animRevealLevelUp);
    }


    private void displayTrophyInformation() {
        TextView txtName = findViewById(R.id.txtTrophyName);
        TextView txtDescription = findViewById(R.id.txtTrophyDescription);

        txtName.setText(trophy.getTitle());
        txtDescription.setText(trophy.getDescription());

        displayParticles();
    }

    public void displayLevelUpInformation(){
        TextView txtLevel = findViewById(R.id.txtLevelUpLevel);
        txtLevel.setText(Xp.getLevel()+"");

        displayParticles();
    }

    //Velger å ikke laste inn aktiviteten på nytt ved rotasjon. På denne måten blir ikke animasjonen spilt på nytt.
    //En ny innlasting av aktiviteten er ikke viktig da en ny layoutfil eller nye viewvs ikke trenger å lastes inn
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    //Fortsetter aktiviteten
    @Override
    public void onBackPressed() {
        if(unlockedTrophyCaousedLevelUp){
            setResult(QuestionActivity.RESULTCODE_AWAITING_LEVEL_UP);
        }
        else{
            setResult(Activity.RESULT_OK);
        }
        finish(); //For at denne aktiviteten ikke skal kunne navgigeres tilbake til
    }

    //BRUKER PARTIKKELEFFEKTER(ParticleSystem) FRA: https://github.com/plattysoft/Leonids
    //Partikkeleffektene kan kun legges til etter at elementet er lastet inn og kan måles
    private void displayParticles(){

        //Henter ut bildene som skal brukes som partikler og setter opp partiklene
        //https://stackoverflow.com/questions/33325354/get-all-drawables-base-on-file-prefix-and-store-it-on-array-of-int
        TypedArray arrayDrawablesSparcles = getResources().obtainTypedArray(R.array.particles);

        for(int i = 0; i < arrayDrawablesSparcles.length(); i++){
            ParticleSystem particleSystem =
                    new ParticleSystem(this, 90, arrayDrawablesSparcles.getResourceId(i, 0), 900 )
                            .setFadeOut(300)
                            .setSpeedRange(0.1f, 0.4f)
                            .setScaleRange(0.1f, 0.3f);
            particleSystem.oneShot(cont, 400);
        }

        backToQuizAfterDelay();

    }

    private void backToQuizAfterDelay(){
        //Går tilbake til quizzen, men etter et delay slik at brukeren rekker å lese informasjonen knyttet til popupen
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Animation animExit = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.box_zoom_out);
                animExit.setAnimationListener(new Animation.AnimationListener() {

                    @Override public void onAnimationEnd(Animation animation) {

                        if(unlockedTrophyCaousedLevelUp){
                            setResult(QuestionActivity.RESULTCODE_AWAITING_LEVEL_UP);
                        }
                        else{
                            setResult(Activity.RESULT_OK);
                        }

                        finish();
                    }

                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                });
                cont.startAnimation(animExit);
            }
        }, mode == LEVEL_UP ? millisToShowLevelUp : millisToShowTrophy);
    }

    public enum Mode implements Serializable{
        LEVEL_UP(false), ACHIEVED_TROPHY(true), ACHIEVED_TROPHY_MULTIPLE(true), ACHIEVED_TROPHY_CAUSED_LEVEL_UP(true);


        private final boolean isTrophyType;
        Mode(boolean isTrophyType){
            this.isTrophyType = isTrophyType;
        }

        public boolean isTrophyType(){
            return isTrophyType;
        }
    }

}
