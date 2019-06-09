package com.example.erlend.quizzapp.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.erlend.quizz_fra_dekompilert.R;
import com.example.erlend.quizzapp.DataManager;
import com.example.erlend.quizzapp.model.Quiz;
import com.example.erlend.quizzapp.model.QuizStat;
import com.example.erlend.quizzapp.model.TrophyManager;
import com.example.erlend.quizzapp.model.Xp;
import com.example.erlend.quizzapp.util.InputControl;
import com.example.erlend.quizzapp.util.Util;
import com.example.erlend.quizzapp.util.Callbackmethod;
import com.example.erlend.quizzapp.model.MultipleChoiceQuestion;
import com.example.erlend.quizzapp.model.PercentageQuestion;
import com.example.erlend.quizzapp.model.Question;
import com.example.erlend.quizzapp.model.TrueFalseQuestion;
import com.example.erlend.quizzapp.DataManager.QUIZ_TYPE;
import com.example.erlend.quizzapp.util.Cancelable;
import com.example.erlend.quizzapp.customView.BottomNavigationFragment;
import com.example.erlend.quizzapp.customView.MultipleChoiceQuestionFragment;
import com.example.erlend.quizzapp.customView.PercentageFragment;
import com.example.erlend.quizzapp.customView.TrueFalseFragment;

import java.util.ArrayList;
import java.util.List;

public class QuestionActivity extends AppCompatActivity {

    //===Til bruk ved rotering===
    public static final String KEY_BUNDLE_CONFIGURATION_CHANGE_CAUSED_RESTART = "KEY_BUNDLE_CONFIGURATION_CHANGE_CAUSED_RESTART"; //Om feks en rtasjonsendring medførte at aktiviteten ble startet på nytt
    public static final String KEY_BUNDLE_IS_QUIZZ_FINISHED = "KEY_BUNDLE_IS_QUIZZ_FINISHED"; //For at ikke spørsmået skal lastes på nytt ved fullført quiz
    private static final String KEY_BUNDLE_MUSIC_POSITION = "KEY_BUNDLE_MUSIC_POSITION";
    public static final String KEY_BUNDLE_COUNTDOWN_DISPLAYED = "KEY_BUNDLE_COUNTDOWN_DISPLAYED"; //For at ikke spørsmået skal lastes på nytt ved fullført quiz


    //===Til bruk for fragments===
    public static final String KEY_BUNDLE_CALLBACK_ANSWER_CHOOSEN = "KEY_BUNDLE_CALLBACK_ANSWER_CHOOSEN";
    public static final String KEY_BUNDLE_QUESTION = "KEY_BUNDLE_QUESTION";
    public static final String KEY_BUNDLE_QUIZZSTAT = "KEY_BUNDLE_QUIZZSTAT";

    public static final int RESULTCODE_AWAITING_LEVEL_UP = 1;
    public static final int REQUEST_CODE_LEVEL_UP_TROPHY = 2;
    public static final int REQUEST_CODE_COUNTDOWN_FINISHED = 1;
    public static final int REQUEST_CODE_QUIZZ_FINISHED = 3;


    private boolean isConfigurationChangePerformed = false;
    private volatile boolean isCountdownFinished = false;
    private boolean shouldStateBeSaved = true; //Om data om den gjeldende quizen skal bli lagret slik at den kan bli tatt opp når onCreate blir kalt på nytt

    private boolean isQuizzFinished = false;
    private boolean shouldDisplayCountdownActivity = true;
    private boolean isActivityRestartedByConfigChange = false;
    private boolean shouldQuizzBeSaved = true;
    private boolean isContinuedQuizz = false;

    //==Timing==
    private final long millisCountdownUpdate = 10;
    private final long millisTimePerQuestion = 10000;
    private final int  millisDelayShowCorrectAnswer = 1500;
    private final int  millisDelayBeforeNextQuestion = 400;
    private final int  millisTimeToShowCorrectQuestion = 1500;
    private int millisDelayNextTrophyLevelup = 300;


    private boolean isMusicMuted = false;
    private boolean isEffectsMuted = false;

    private int millisRemainingTimeCountdown = -1;

    private QuizStat quizStat;
    private Countdown countDown;
    private String categorykey;
    private OnGotAnswerCorrectness onGotAnswerCorrectness;
    private ArrayList<Cancelable> listRunningOperations = new ArrayList<>();

    private Fragment fragmentCurrQuestion;

    private ImageView imgQuestion;
    private ProgressBar progressCountdown;
    private ViewGroup rootQuestion;
    private Toast toast;
    private TextView txtQuestionImage;
    private TextView txtQuestionNoImage;
    private TextView txtQuestionNumber;
    private InputControl fragmentInputControl = null;

    private ImageView imgMuteMusic;
    private ImageView imgMuteEffects;

    private boolean isActivityInitFinished = false;
    private boolean shouldMusicShouldBePausedOnPause = true;

    //==Lydeffekter og musikk==
    private SoundPool soundPoolEffetcs;
    private int millisCurrPosMediaPlayerMusic = 0;
    private int correctAnswerMusicID, wrongAnswerMusicID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            shouldDisplayCountdownActivity = savedInstanceState.getBoolean(KEY_BUNDLE_COUNTDOWN_DISPLAYED);
            isActivityRestartedByConfigChange = savedInstanceState.getBoolean(KEY_BUNDLE_CONFIGURATION_CHANGE_CAUSED_RESTART);
            isQuizzFinished = savedInstanceState.getBoolean(KEY_BUNDLE_IS_QUIZZ_FINISHED, isQuizzFinished);
            millisCurrPosMediaPlayerMusic = savedInstanceState.getInt(KEY_BUNDLE_MUSIC_POSITION);
        }

        quizStat = getIntent().getParcelableExtra(KEY_BUNDLE_QUIZZSTAT);
        isContinuedQuizz = quizStat.isContinuedQuizz();
        millisRemainingTimeCountdown = quizStat.getRemainingTimeCurrQuestion();
        categorykey = quizStat.getQuizz().getCategoryKey();

        isMusicMuted = getSharedPreferences(DataManager.KEY_SHARED_PREFERENCES, 0).getBoolean(DataManager.KEY_PREFS_MUSIC_MUTED, false);
        isEffectsMuted = getSharedPreferences(DataManager.KEY_SHARED_PREFERENCES, 0).getBoolean(DataManager.KEY_PREFS_MUSIC_MUTED, false);

        //Starter aktivitet for nedtelling, som i sin tur gir beskejd når nedtellingen er ferdig i onActivityResult
        if(shouldDisplayCountdownActivity){
            Intent intentCoutndownActivity = new Intent(getApplicationContext(), CountdownActivity.class);
            startActivityForResult(intentCoutndownActivity, REQUEST_CODE_COUNTDOWN_FINISHED);
        }
        else {
            setUpAndConfigureActivity();
        }

    }

    private void setUpAndConfigureActivity(){
        setContentView(R.layout.activity_question);


        if(VERSION.SDK_INT < 21 ){
            soundPoolEffetcs = new SoundPool(2, AudioManager.STREAM_MUSIC,0);
        }
        else{
            soundPoolEffetcs = new SoundPool.Builder().setMaxStreams(2).build();
        }
        correctAnswerMusicID = soundPoolEffetcs.load(getApplicationContext(), R.raw.correct, 1);
        wrongAnswerMusicID = soundPoolEffetcs.load(getApplicationContext(), R.raw.wrong, 1);


        txtQuestionImage = findViewById(R.id.txtQuestionImage);
        txtQuestionNoImage = findViewById(R.id.txtQuestionNoImage);
        imgQuestion = findViewById(R.id.imgQuestion);
        progressCountdown = findViewById(R.id.progressCountdownQuestion);
        Button btnQuit = findViewById(R.id.btnQuitQuizz);

        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);

        if (Util.isPortrait(getResources()))
            rootQuestion = (RelativeLayout) findViewById(R.id.rootQuestion);
        else
            rootQuestion = (LinearLayout) findViewById(R.id.rootQuestion);


        //==ACTIONBAR==
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout layoutActionbar = (LinearLayout) inflater.inflate(R.layout.actionbar_question,null);

        TextView txtQuizzName = layoutActionbar.findViewById(R.id.txtActionbarQuizzname);
        imgMuteMusic = layoutActionbar.findViewById(R.id.imgMuteMusic);
        imgMuteEffects = layoutActionbar.findViewById(R.id.imgMuteEffects);
        txtQuestionNumber = layoutActionbar.findViewById(R.id.txtActionbarQuestionNumber);

        imgMuteEffects.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleEffects();
            }
        });

        imgMuteMusic.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMusic();
            }
        });

        //https://stackoverflow.com/a/30120089
        actionBar.setCustomView(layoutActionbar);
        actionBar.setDisplayShowCustomEnabled(true);

        txtQuizzName.setSelected(true);
        txtQuizzName.setHorizontallyScrolling(true);
        String quizzTitle = quizStat.getQuizz().getTitle();
        quizzTitle = quizzTitle.equals(Quiz.NAME_GENERAL) ? getResources().getString(R.string.quizz_general) : quizzTitle;
        txtQuizzName.setText(quizzTitle);
        btnQuit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showQuitDialog(view);
            }
        });


        //==BUNNNAVIGASJON==
        FragmentManager fragmentManager = getSupportFragmentManager();
        BottomNavigationFragment bottomNavigationFragment = (BottomNavigationFragment) fragmentManager.findFragmentById(R.id.bottom_navigation_fragment);
        bottomNavigationFragment.setSelectedItem(R.id.navigation_quiz_picker);


        onGotAnswerCorrectness = new OnGotAnswerCorrectness();

        if(!isQuizzFinished)
            quizStat.startQuizzSession();
        if( isContinuedQuizz || isQuizzFinished || isActivityRestartedByConfigChange)
            loadQuestion(false); //Laster ikke inn neste spørsmål
        else
            loadQuestion(true);

        isMusicMuted = getSharedPreferences(DataManager.KEY_SHARED_PREFERENCES, 0).getBoolean(DataManager.KEY_PREFS_MUSIC_MUTED, false);
        isEffectsMuted = getSharedPreferences(DataManager.KEY_SHARED_PREFERENCES, 0).getBoolean(DataManager.KEY_PREFS_EFFECTS_MUTED, false);

        DataManager.getMusicPlayer().initMusicPlayer(R.raw.quizz_music);
        isActivityInitFinished = true;
    }


    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = getSharedPreferences(DataManager.KEY_SHARED_PREFERENCES, 0).edit();
        editor.putBoolean(DataManager.KEY_PREFS_EFFECTS_MUTED, isEffectsMuted);
        editor.putBoolean(DataManager.KEY_PREFS_MUSIC_MUTED, isMusicMuted);
        editor.apply();

        if(shouldMusicShouldBePausedOnPause && isActivityInitFinished)
            DataManager.getMusicPlayer().pauseMusic();

        quizStat.pauseQuizzSession();
        cancelOperations();
        if(countDown != null)
            countDown.shouldStartOnResume = true;

        if(quizStat != null && !isQuizzFinished && shouldQuizzBeSaved)
            saveCurrentQuizz();

        DataManager.saveXP(getApplicationContext());
    }


    @Override
    protected void onResume() {
       super.onResume();

        shouldMusicShouldBePausedOnPause = true;
        if(isActivityInitFinished){ //Hvis den ikke er ferdig vil ikke musikk og lydsplleren ha blitt satt opp riktig enda
           isMusicMuted = !isMusicMuted;
           isEffectsMuted = !isEffectsMuted;
           toggleMusic();
           toggleEffects();
       }

       if (countDown != null && countDown.shouldStartOnResume) {
           startCountDown(millisRemainingTimeCountdown);
           quizStat.startQuizzSession();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //FRA NEDTELLING
        if(requestCode == REQUEST_CODE_COUNTDOWN_FINISHED){

            if(resultCode == Activity.RESULT_OK){
                shouldDisplayCountdownActivity = false;
                setUpAndConfigureActivity();
            }

            else if(resultCode == Activity.RESULT_CANCELED){
                backToQuizzSelection(shouldQuizzBeSaved);
            }
        }


        //FRA TROFE/LEVEL UP
        else if (requestCode == REQUEST_CODE_LEVEL_UP_TROPHY){

            List<TrophyManager.Trophy> listUnlockedAndUnpresentedTrophies = TrophyManager.getUnlockedUnpresentedTrophies();

            resetCountDown();
            quizStat.isNotFirstQuestion();

            boolean shouldShowAdditionalLevelUpTrophy = false;
            boolean isAwaitingLevelUp = false;
            boolean isAwaitingTrophy = false;

            if(resultCode == RESULTCODE_AWAITING_LEVEL_UP){
                shouldShowAdditionalLevelUpTrophy = true;
                isAwaitingLevelUp = true;
            }
            else{
                isAwaitingTrophy = listUnlockedAndUnpresentedTrophies.size() > 0;
            }

            final boolean finalIsAwaitingTrophy = isAwaitingTrophy;
            final boolean finalIsAwaitingLevelUp = isAwaitingLevelUp;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(finalIsAwaitingTrophy){
                        startTrophyActivity(listUnlockedAndUnpresentedTrophies.get(0), NextQuestionMode.TROPHY_BEFORE_NEXT);
                    }
                    else if (finalIsAwaitingLevelUp){
                        afterAnswerGiven(NextQuestionMode.MODE_LEVEL_UP_BEFORE_NEXT, false, false);
                        countDown.shouldStartOnResume = false; //Nedtellingen skal ikke startes fordi den økte xp-en skal vises
                    }
                    else{
                        quizStat.startQuizzSession();
                        nextQuestionOrFinished();
                    }
                }
            }, shouldShowAdditionalLevelUpTrophy ? millisDelayNextTrophyLevelup : millisDelayBeforeNextQuestion);

            countDown.shouldStartOnResume = false; //Nedtellingen blir startet i nextQuestionOrFinished
        }


        //FRA FERDIG QUIZ
        else if(requestCode == REQUEST_CODE_QUIZZ_FINISHED && resultCode == FinishedQuizzActivity.RESULT_CODE_BACK_TO_QUIZZPICKER){
            backToQuizzSelection(false);
        }

    }


    /*
       Brukt for å registrere eventuelle konfigurasjonsendringer. Mest sansynlig vil dette være en rotasjonsendring.
        Siden rotasjonsendringen innenbæer at et nytt layout vil bli lastet inn, vil hele aktiviteten restartes slik at dette kan skje,
         men siden denne hendelsen blir registrert og lagret i bundelen som sendes til den nystartede aktiviteten,
         vil man nå vite om aktiviteten ble startet på bakgrunn av en konfigurasjonsendring eller om det var en vanlig start. Feks vil man ikke
         laste inn et nytt spørsmål ved start på grunn av rotasjonsendring, mens man vil dette hvis en helt ny quizz starter.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        isConfigurationChangePerformed = true;
        shouldMusicShouldBePausedOnPause = false;
        shouldDisplayCountdownActivity = false;
        cancelOperations();
        super.onConfigurationChanged(newConfig);
        recreate();
    }

    //Lagrer data om quizen som foregår nå. Denne er utelukkende til bruk ved rotasjonsendringer, for å ta vare på informasjon om feks gjenstående
    //tid på nedtellingen. Annen informasjon som skal bli lagret mer persietent(ved avsluttning av quizzen), blir lagret i en SQLlite-databse.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        cancelOperations();
        Bundle outStateNew = new Bundle();
        if (shouldStateBeSaved) {
            outStateNew.putBoolean(KEY_BUNDLE_COUNTDOWN_DISPLAYED, shouldDisplayCountdownActivity);
            outStateNew.putBoolean(KEY_BUNDLE_CONFIGURATION_CHANGE_CAUSED_RESTART, isConfigurationChangePerformed);
            outStateNew.putBoolean(KEY_BUNDLE_IS_QUIZZ_FINISHED, isQuizzFinished);
            outState.putInt(KEY_BUNDLE_MUSIC_POSITION, millisCurrPosMediaPlayerMusic);
            super.onSaveInstanceState(outStateNew);
        }
        else{
            super.onSaveInstanceState(new Bundle());
        }
    }

    @Override
    public void onBackPressed() {
        backToQuizzSelection(!isQuizzFinished);
    }

    private void startTrophyActivity(TrophyManager.Trophy trophy, NextQuestionMode mode){
        Intent intentSHowTrophy = new Intent(getApplicationContext(), OnLevelUpTrophyActivity.class);
        intentSHowTrophy.putExtra(OnLevelUpTrophyActivity.KEY_TROPHY, trophy);

        if(mode == NextQuestionMode.TROPHY_BEFORE_NEXT)
            intentSHowTrophy.putExtra(OnLevelUpTrophyActivity.KEY_MODE, OnLevelUpTrophyActivity.Mode.ACHIEVED_TROPHY);

        startActivityForResult(intentSHowTrophy, REQUEST_CODE_LEVEL_UP_TROPHY);

        trophy.setPresented();
    }

    private void toggleEffects(){
        isEffectsMuted = !isEffectsMuted;
        imgMuteEffects.setImageDrawable(
                isEffectsMuted ? getResources().getDrawable(R.drawable.sound_effects_muted) : getResources().getDrawable(R.drawable.sound_effects));
    }

    private void toggleMusic(){
        if(isActivityInitFinished){
            isMusicMuted = !isMusicMuted;
            imgMuteMusic.setImageDrawable(
                    isMusicMuted ? getResources().getDrawable(R.drawable.music_muted) : getResources().getDrawable(R.drawable.music));
            if(isMusicMuted){
                DataManager.getMusicPlayer().pauseMusic();
            }
            else{
                DataManager.getMusicPlayer().startMusic();
            }
        }

    }

    //Laster inn spørsmålet og setter opp UI-elementer og fragments i tilegg til å start nedtellingen
    public void loadQuestion(boolean loadNext) {
        if(!isQuizzFinished){
            startCountDown(millisRemainingTimeCountdown);
        }

        Question question = getQuestion(loadNext);

        if (question.hasImage()) {
            txtQuestionNoImage.setVisibility(View.INVISIBLE);
            txtQuestionImage.setVisibility(View.VISIBLE);
            imgQuestion.setVisibility(View.VISIBLE);
            txtQuestionImage.setText(question.getStrQuestion());
            imgQuestion.setImageBitmap(question.getImage());

            if (VERSION.SDK_INT >= 23) {
                rootQuestion.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark, null));
            }
            else {
                rootQuestion.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            }
        }
        else {
            txtQuestionNoImage.setVisibility(View.VISIBLE);
            txtQuestionImage.setVisibility(View.INVISIBLE);
            imgQuestion.setVisibility(View.INVISIBLE);
            txtQuestionNoImage.setText(question.getStrQuestion());
            rootQuestion.setBackgroundColor(0);
        }

        txtQuestionNumber.setText(String.format("%s/%s", quizStat.getCurrentQuestionNo(), quizStat.getNumberOfQuestions()));

        startFragment(question, onGotAnswerCorrectness);
    }

    private Question getQuestion(boolean shouldLoadNext) {
        if (shouldLoadNext) {
            return quizStat.nextQuestion();
        }
        return quizStat.getCurrentQuestion();
    }

    public void startFragment(Question question, Callbackmethod callbackAnswerInFragmentSelected) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentCurrQuestion = null;
        Bundle arguments = new Bundle();

        if(question instanceof TrueFalseQuestion) {
            fragmentCurrQuestion = new TrueFalseFragment();
        }
        else if (question instanceof MultipleChoiceQuestion) {
            fragmentCurrQuestion = new MultipleChoiceQuestionFragment();
        }
        else if(question instanceof PercentageQuestion){
            arguments.putInt(PercentageFragment.KEY_BUNDLE_SELECTED_PERCENTAGE, quizStat.getPercentageSelected());
        }
        fragmentInputControl = (InputControl) fragmentCurrQuestion;

        //For å deaktivere inputene når en quiz er ferdig og brukren roterer enheten. Dette var kanskje mere
        // nyttig da appen ikke hadde en skjerm for ferdig quiz og den bare stoppet på siste spørsmål. Rotering ved et s0rørsmål
        // før neste spørsmål er lastet inn eller aktiviteten for ferdig quiz er lastet inn er forøverig en skummel ting å gjøre siden rotering på feil tidspunkt
        //kan resultere i app-krasj. Dette er et bug jeg ikke har hatt tid til å fikse
        if(isQuizzFinished){
            fragmentInputControl.onCreatedView(new Callbackmethod() {
                @Override
                public void callback(Object o) {
                    fragmentInputControl.disableInputs();
                }
            });
        }


        arguments.putSerializable(KEY_BUNDLE_CALLBACK_ANSWER_CHOOSEN, callbackAnswerInFragmentSelected);
        arguments.putParcelable(KEY_BUNDLE_QUESTION, question);
        fragmentCurrQuestion.setArguments(arguments);

        if (fragmentManager.getFragments().size() == 0) {
            fragmentTransaction.add(R.id.rootAnswer, fragmentCurrQuestion);
        }
        else {
            fragmentTransaction.replace(R.id.rootAnswer, fragmentCurrQuestion);
        }
        fragmentTransaction.commit();
    }

    //Ligger i en egen klasse fordi en instanse av denne blir sendt med til fragmentet for å bli kjørt når et svar blir angitt
    private class OnGotAnswerCorrectness implements Callbackmethod<Boolean>{

        //Kalles på når fragmentet har funnet ut om opgitt svar var riktig eller ikke
        @Override
        public void callback(Boolean isAnswerCorrect) {
            boolean showLevelUp = false;
            if (isAnswerCorrect) {
                quizStat.correctAnswer();
                if(Xp.correctAnswer()){
                    showLevelUp = true;
                }
            }
            else {
                Xp.wrongAnswer();
                quizStat.incorrectAnswer();
            }

            //For at en eventuell trophy knyttet til gjennomførte quizzer skal ha sjans til å vises. Denne if-testen vil gjelde like etter at
            //svaret på siste spørsmål i quizen er angitt
            if(quizStat.getNumberOfQuestions() == quizStat.getNumCorrectAnswers() + quizStat.getNumWrongAnswers()){
                quizStat.setFinished();
            }

            TrophyManager.checkForUnlocked(getApplicationContext());
            int numTrophiesReadyToBepresented = TrophyManager.getUnlockedUnpresentedTrophies().size();
            if(numTrophiesReadyToBepresented > 0)
                TrophyManager.saveTakenTrophiesPersistent(getApplicationContext());
            countDown.cancel();

            //Hvis både level up og trofe er sann, vil dette bli håndtert etter at levelUp er ferdig
            if(showLevelUp)
                afterAnswerGiven(NextQuestionMode.MODE_LEVEL_UP_BEFORE_NEXT, isAnswerCorrect, true);
            else if(numTrophiesReadyToBepresented > 0)
                afterAnswerGiven(NextQuestionMode.TROPHY_BEFORE_NEXT, isAnswerCorrect, true);
             else
                afterAnswerGiven(NextQuestionMode.MODE_NEXT, isAnswerCorrect, true);
        }

    }

    //Kjøres når man ønsker å gå vidre til neste spørsmål, men ikke er sikker på om det finnes flere spørsmål i quizzen.
    //Dette skjer når tiden for spørsmålet har gått ut eller brukeren har angitt et svar, og det har blitt opplyst om svaret er riktig
    public void nextQuestionOrFinished() {

        if (quizStat.hasNextQuestion()) {
            resetCountDown();
            loadQuestion(true);
        }
        else{
            finishQuizz();
        }


    }

    public void showQuitDialog(View view) {
        cancelOperations();
        final Dialog dialog = new Dialog(QuestionActivity.this);
        dialog.setContentView(R.layout.dialog_confirm_exit_quizz);

        //https://stackoverflow.com/questions/32029532/alternative-to-the-deprecated-flag-blur-behind-flag-for-dimming-background-of-di
        getWindow().setBackgroundDrawable(new ColorDrawable());

        dialog.show();
        Button btnExitFalse =  dialog.findViewById(R.id.btnCancelExit);
        Button btnExitTrue = dialog.findViewById(R.id.btnPerformExit);
        btnExitTrue.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                backToQuizzSelection(false);
            }
        });
        btnExitFalse.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
                countDown = (Countdown) new Countdown(millisRemainingTimeCountdown).doInBackground();
                listRunningOperations.add(countDown);
            }
        });

        //https://stackoverflow.com/questions/10346011/how-to-handle-back-button-with-in-the-dialog
        dialog.setOnKeyListener(new Dialog.OnKeyListener(){

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK){
                    dialog.dismiss();
                     countDown = (Countdown) new Countdown(millisRemainingTimeCountdown).doInBackground();
                     listRunningOperations.add(countDown);
                }
                return true;
            }
        });
    }

    public void saveCurrentQuizz() {
        if (fragmentCurrQuestion instanceof PercentageFragment) {
            quizStat.setPercentageSelected(((PercentageFragment) fragmentCurrQuestion).getProgress());
        }
        QuizStat.setSavedQuizz(quizStat);
        quizStat.setRemainingTimeCurrQuestion(millisRemainingTimeCountdown);
        DataManager.saveQuiz(getApplicationContext(), quizStat, QUIZ_TYPE.ONGOING);
    }
    public void removeCurrentQuizz() {
        QuizStat.removeSavedQuizz();
        DataManager.removeCurrentQuiz(getApplicationContext());
        Quiz.getGeneralQuiz().getListQuestions().clear();
    }


    public void backToQuizzSelection(boolean saveQuizz) {
        shouldStateBeSaved = false;
        shouldQuizzBeSaved = saveQuizz;
        cancelOperations();
        Intent intentPickQuizz = new Intent(getApplicationContext(), QuizzPickerActivity.class);

        if(quizStat.getQuizz().isGeneralQuizz()){
            DataManager.setCurrentType(DataManager.Type.CATEGORY);
        }
        else {
            intentPickQuizz.putExtra(QuizzPickerActivity.KEY_INTET_CATEGORY_TO_LOAD, categorykey);
        }

        if (!saveQuizz) {
            removeCurrentQuizz();
        }

        startActivity(intentPickQuizz);
        finish();
    }



    public void displayIncorrectAnswerToast() {
        toast.cancel();
        toast = Toast.makeText(getApplicationContext(), "FEIL", Toast.LENGTH_SHORT);
        toast.show();
    }

    public void finishQuizz() {
        cancelOperations();
        isQuizzFinished = true;
        shouldStateBeSaved = false;

        if(quizStat != null)
            quizStat.pauseQuizzSession();
        if(fragmentInputControl != null)
            fragmentInputControl.disableInputs();
        DataManager.saveQuiz(getApplicationContext(), quizStat, QUIZ_TYPE.FINISHED);

        removeCurrentQuizz();
        Intent intentFinishActivity = new Intent(getApplicationContext(), FinishedQuizzActivity.class);
        intentFinishActivity.putExtra(FinishedQuizzActivity.KEY_QUIZSTAT_FINISHED, quizStat);
        startActivityForResult(intentFinishActivity, REQUEST_CODE_QUIZZ_FINISHED);
        overridePendingTransition(R.anim.slide_up, R.anim.fade_out_slowly);
    }

    public void startCountDown(int remainingMillis) {
        if (remainingMillis == -1 || isCountdownFinished) {
            countDown = new Countdown(millisTimePerQuestion);
            listRunningOperations.add(countDown);
            countDown.doInBackground();
        }
        else {
            countDown = new Countdown((long) remainingMillis);
            listRunningOperations.add(countDown);
            countDown.doInBackground();
        }

        countDown.shouldStartOnResume = false;

    }

    public void resetCountDown() {
        millisRemainingTimeCountdown = -1;
        countDown.countdownProgress = 100;
        isCountdownFinished = false;
        progressCountdown.setProgress(100);
        countDown.cancel(true);
    }



    private enum NextQuestionMode {
        MODE_NEXT, TROPHY_BEFORE_NEXT, MODE_LEVEL_UP_BEFORE_NEXT;
    }

    //Etter at riktig/feil svar er markert
    private void afterAnswerGiven(NextQuestionMode mode, boolean isAnswerCorrect, boolean inputsSHouldBeUpdated) {
        if(inputsSHouldBeUpdated){
            fragmentInputControl.disableInputs();
            fragmentInputControl.markQuestionSelection();
        }

        Runnable actionDisplayCorrectAnswer = new Runnable() {
            @Override
            public void run() {
                fragmentInputControl.markQuestionCorrectness();
                fragmentInputControl.markCorrectQuestion();

                if(!isEffectsMuted){
                    if(isAnswerCorrect){
                        soundPoolEffetcs.play(correctAnswerMusicID, 1, 1, 1,0,1);
                    }
                    else{
                        soundPoolEffetcs.play(wrongAnswerMusicID, 1, 1, 1,0,1);
                    }
            }
         }
        };

        Runnable actionDisplayLevelUp = new Runnable() {
            @Override
            public void run() {
                shouldMusicShouldBePausedOnPause = false;
                Intent intentDisplayLevelUp = new Intent(getApplicationContext(), OnLevelUpTrophyActivity.class);
                intentDisplayLevelUp.putExtra(OnLevelUpTrophyActivity.KEY_MODE, OnLevelUpTrophyActivity.Mode.LEVEL_UP);
                startActivityForResult(intentDisplayLevelUp, REQUEST_CODE_LEVEL_UP_TROPHY);

            }
        };

        Runnable actionDisplayTrophy = new Runnable() {
            @Override
            public void run() {
                shouldMusicShouldBePausedOnPause = false;
                List<TrophyManager.Trophy> listUnlockedAndUnpresentedTrophies = TrophyManager.getUnlockedUnpresentedTrophies();
                startTrophyActivity(listUnlockedAndUnpresentedTrophies.get(0), mode);

            }
        };

        Runnable actionNextQuestion = new Runnable() {
            @Override
            public void run() {
                fragmentInputControl.enableInputs();
                nextQuestionOrFinished();

            }
        };

        ExtendedHandler handler = new ExtendedHandler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(inputsSHouldBeUpdated)
                    actionDisplayCorrectAnswer.run();

                if (mode == NextQuestionMode.MODE_NEXT) {
                    new Handler().postDelayed(actionNextQuestion, millisTimeToShowCorrectQuestion);
                }
                else if(mode == NextQuestionMode.TROPHY_BEFORE_NEXT)
                    new Handler().postDelayed(actionDisplayTrophy, millisTimeToShowCorrectQuestion);
                else if (mode == NextQuestionMode.MODE_LEVEL_UP_BEFORE_NEXT){
                    new Handler().postDelayed(actionDisplayLevelUp, millisDelayNextTrophyLevelup);
                }
            }
        }, millisDelayShowCorrectAnswer);
        listRunningOperations.add(handler);

    }

    //Avbryter pågående operasjoner. Denne metoden er trygg å kalle selv om operasjonene allerede er avsluttet
    private void cancelOperations(){
        if(toast != null)
            toast.cancel();

        for(Cancelable cancelable: listRunningOperations){
            if(cancelable != null){
                cancelable.cancel();
            }
        }
    }

    private class Countdown extends AsyncTask<Object, Integer, Object> implements Cancelable{
        private long timeForQuestion = 0;
        private CountDownTimer countDownTimer;

        private int countdownProgress;


        private boolean shouldStartOnResume = false;

        private Countdown(long remainingTimeForQuestion) {
            this.timeForQuestion = remainingTimeForQuestion;
        }

        @Override
        protected Object doInBackground(Object... objects) {

             countDownTimer = new CountDownTimer(timeForQuestion, millisCountdownUpdate) {

                 @Override
                public void onTick(long millisUniutillFinished) {
                    millisRemainingTimeCountdown = (int) millisUniutillFinished;
                    countdownProgress = (int) ((((float) millisUniutillFinished) / millisTimePerQuestion) * 100);
                    publishProgress(countdownProgress);

                    if(isCancelled()){
                        countDownTimer.cancel();
                        cancel();
                        onCancelled();
                    }
                }

                //Tiden har gått ut
                @Override
                public void onFinish() {
                    isCountdownFinished = true;
                    Xp.wrongAnswer();
                    afterAnswerGiven(NextQuestionMode.MODE_NEXT, false, true);

                    displayIncorrectAnswerToast();
                    if(quizStat != null)
                        quizStat.incorrectAnswer();

                    if(isCancelled()){
                        countDownTimer.cancel();
                        cancel();
                        onCancelled();
                    }
                }
            }.start();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values[0]);
            Util.updateProgressBar(values[0], progressCountdown);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
        }

        @Override
        public void cancel() {
            countDownTimer.cancel();
        }
    }


    private static class ExtendedHandler extends Handler implements Cancelable{

        @Override
        public void cancel() {
            removeCallbacks(null); //Fjerner alle callbacks
        }
    }

}
