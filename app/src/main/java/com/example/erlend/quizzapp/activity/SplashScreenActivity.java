package com.example.erlend.quizzapp.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.erlend.quizz_fra_dekompilert.R;
import com.example.erlend.quizzapp.DataManager;
import com.example.erlend.quizzapp.model.TrophyManager;
import com.example.erlend.quizzapp.util.LoadingProgressable;
import com.example.erlend.quizzapp.util.Util;

public class SplashScreenActivity extends AppCompatActivity implements LoadingProgressable {

    private int millisDelayAfterInit = 2500;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Her gjennomføres potensielt UI-blokkerede operasjoner, men siden aktiviteten bare består av et statisk bilde
        //og ingen interaktive elementer vil ikke dette ha noe stort å si. Dessuten vil jeg forsikre meg om at alle operasjonene er ferdige før
        //QuickpickerActivity åpnes
        setContentView(R.layout.activity_splash_screen);

        TrophyManager.initTrophies();
        String takenTrophies = getSharedPreferences(DataManager.KEY_SHARED_PREFERENCES, 0).getString(DataManager.KEY_PREFS_TAKEN_TROPHIES, "");
        TrophyManager.loadTakenTrophiesBySeperatedString(takenTrophies, DataManager.STANDARD_DELIMITER);
        DataManager.loadXp(getApplicationContext());
        DataManager.initMusicPlayerService(getApplicationContext());

        //Slik at splashscreenen skal viss
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!Util.hasNetworkConnection(getApplicationContext())) {
                    afterLoading(DataManager.Type.NETWORK_TEST, false);
                } else {
                    DataManager.registerNetworkSpeed(SplashScreenActivity.this);
                }
            }
        }, millisDelayAfterInit);
    }


    //Velger å kun laste inn layouten på nytt siden aktiviteten hverken bør eller trenger å lastes inn på nytt.
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_splash_screen);
    }

    @Override
    public void afterLoading(DataManager.Type type, boolean z) {
        if(type == DataManager.Type.NETWORK_TEST){
            startActivity(new Intent(getApplicationContext(), QuizzPickerActivity.class));
            overridePendingTransition(R.anim.fade_in_slowly, R.anim.fade_out_slowly);
            finish();
        }
    }

    @Override
    public void beforeLoading(DataManager.Type type, boolean s) {}

    @Override
    public void clearprogress() {}

    @Override
    public void setprogress(int i) {}

}
