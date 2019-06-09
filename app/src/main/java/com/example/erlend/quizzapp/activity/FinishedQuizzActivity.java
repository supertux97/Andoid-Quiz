package com.example.erlend.quizzapp.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.annimon.stream.IntStream;
import com.annimon.stream.Stream;
import com.example.erlend.quizz_fra_dekompilert.R;
import com.example.erlend.quizzapp.DataManager;
import com.example.erlend.quizzapp.model.QuizStat;
import com.example.erlend.quizzapp.util.Util;
import com.example.erlend.quizzapp.util.StatTools;
import com.example.erlend.quizzapp.customView.StatRow;

import java.util.ArrayList;
import java.util.LinkedHashMap;


public class FinishedQuizzActivity extends AppCompatActivity {

    public static final int RESULT_CODE_BACK_TO_QUIZZPICKER = 1;
    public static final String KEY_QUIZSTAT_FINISHED = "KEY_QUIZSTAT_FINISHED";

    private QuizStat quizStatFinished;

    private boolean configChangeHasOccured = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished_quizz);
        configChangeHasOccured = false;

        ArrayList<ImageView> listStars = new ArrayList<>();

        quizStatFinished = getIntent().getParcelableExtra(KEY_QUIZSTAT_FINISHED);

        StatTools.TimeContainer timeUsed = StatTools.secondsToTime((int) (quizStatFinished.getTimeUsedMillis() / 1000));

        StatRow statRowNumQuestions = findViewById(R.id.finishRowNumQuestions);
        StatRow statRowCorrectAnswers = findViewById(R.id.finishRowCorrect);
        StatRow statRowWrongAnswers = findViewById(R.id.finishRowWrong);
        StatRow statRowWTimeUsed = findViewById(R.id.finishRowTimeUsed);
        Button btnNewQuizz = findViewById(R.id.btnNewQuizz);

        FrameLayout viewStar1 = findViewById(R.id.fiinishStar1);
        FrameLayout viewStar2 = findViewById(R.id.fiinishStar2);
        FrameLayout viewStar3 = findViewById(R.id.fiinishStar3);

        listStars.add(viewStar1.findViewById(R.id.imgStar));
        listStars.add( viewStar2.findViewById(R.id.imgStar));
        listStars.add( viewStar3.findViewById(R.id.imgStar));

        btnNewQuizz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CODE_BACK_TO_QUIZZPICKER);
                finish();
            }
        });

        statRowNumQuestions.setRowValue(quizStatFinished.getNumberOfQuestions());
        statRowCorrectAnswers.setRowValue(quizStatFinished.getNumCorrectAnswers());
        statRowWrongAnswers.setRowValue(quizStatFinished.getNumWrongAnswers());
        statRowWTimeUsed.setRowValue(String.format("%sm %ss", timeUsed.minutes, timeUsed.seconds));

        DataManager.getMusicPlayer().initMusicPlayer(R.raw.finished);

        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        configChangeHasOccured = false;

        //Endrer bakgrunn på det antallet stjerner brukeren har opnådd
        IntStream.range(0, calculateStars()).forEach(starNr ->
            listStars.get(starNr).setImageDrawable(getResources().getDrawable(R.drawable.star) ));

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!configChangeHasOccured){
            DataManager.getMusicPlayer().pauseMusic();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        boolean musicMuted = getSharedPreferences(DataManager.KEY_SHARED_PREFERENCES, 0).getBoolean(DataManager.KEY_PREFS_MUSIC_MUTED, false);
        if(!musicMuted)
            DataManager.getMusicPlayer().startMusic();
        configChangeHasOccured = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CODE_BACK_TO_QUIZZPICKER);
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        configChangeHasOccured = true;
        recreate();
    }

    //Stjernene blir kalkulert på bakgrunn av andel riktige spørsmål og i tilegg tidbruk pr spørsmål
    private int calculateStars(){

        //Bruker denne implementasjonen for at rekkefølgen alltid skal være den samme som den ble satt inn i
        LinkedHashMap<Integer, Integer> listRequirements = new LinkedHashMap<>();
        //Key: Stjerner, Value: minimumskrav
        listRequirements.put(3, 100);
        listRequirements.put(2, 70);
        listRequirements.put(1, 50);
        listRequirements.put(0, 0);

        int score = 0;
        float timePrQuestion =  quizStatFinished.getTimeUsedSeconds() / ((float) quizStatFinished.getTotalAnswers());

        score += Util.percentageInt(quizStatFinished.getNumCorrectAnswers(), quizStatFinished.getTotalAnswers());
        score += 22 - timePrQuestion;

        final int finalScore = score; //Streams krever at score er final
        //Finner første match på value i listRecuremenets og returnerer den tilhørende keyen
        return Stream.of(listRequirements.entrySet()).filter(entry -> finalScore >= entry.getValue()).findFirst().get().getKey();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }
}
