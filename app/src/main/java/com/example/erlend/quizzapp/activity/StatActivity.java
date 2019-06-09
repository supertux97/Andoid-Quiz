package com.example.erlend.quizzapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.erlend.quizz_fra_dekompilert.R;
import com.example.erlend.quizzapp.model.Category;
import com.example.erlend.quizzapp.model.Xp;
import com.example.erlend.quizzapp.util.Util;
import com.example.erlend.quizzapp.DataManager;
import com.example.erlend.quizzapp.DataManager.Type;
import com.example.erlend.quizzapp.util.StatTools;
import com.example.erlend.quizzapp.customView.BottomNavigationFragment;
import com.example.erlend.quizzapp.customView.NoDataActivity;
import com.example.erlend.quizzapp.customView.StatRow;

import java.util.Map;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class StatActivity extends AppCompatActivity {

    private StatRow statRowQuestions;
    private StatRow statRowCorrectQuestions;
    private StatRow statRowWrongQuestions;
    private StatRow statRowTimeUsage;
    private StatRow statRowQuizzes;
    private StatRow statRowQuizzesAllCorrect;

    private StatRow statRowCategoryMostCorrect;
    private StatRow statRowCategoryMostWrong;
    private StatRow statRowCategoryMostQuizzes;

    private LinearLayout rootInforPrCategory;

    private TextView txtXp;
    private TextView txtLevel;
    private MaterialProgressBar progressXp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(null); //Ønsker å laste inn hele aktivitetn på nytt, inkludert fragments
        if (DataManager.requestFinishedQuizzes(getApplicationContext())) {
            setContentView( R.layout.activity_stat);
          //  this.bottomNavigationView.setSelectedItemId(R.id.navigation_stat);
        }
        else{
            Intent intentNoData = new Intent(getApplicationContext(), NoDataActivity.class);
            intentNoData.putExtra(NoDataActivity.KEY_NO_DATA_TYPE, Type.STAT);
            startActivity(intentNoData);
            finish(); //Brukeren skal ikke bli sendt tilbake til denne aktiviteten ved trykk på tilbakeknappen
            return;
        }

        //Velger riktig element i navgasjonen i bånn
        ((BottomNavigationFragment) getSupportFragmentManager().findFragmentById(R.id.bottom_navigation_fragment)).setSelectedItem(R.id.navigation_stat);

        getRefUiElem();
        StatTools.retrieveQuizzData(); //Henter den nyeste statistikken
        setStats();
        setCategoryStats();
    }

    private void getRefUiElem(){

        statRowQuestions = findViewById(R.id.statRowQuestions);
        statRowCorrectQuestions = findViewById(R.id.statRowCorrectQuestions);
        statRowWrongQuestions = findViewById(R.id.statRowWrongQuestions);
        statRowTimeUsage = findViewById(R.id.statRowTimeUsage);
        statRowQuizzes= findViewById(R.id.statRowQuizzes);
        statRowQuizzesAllCorrect = findViewById(R.id.statRowQuizzesAllCorrect);

        rootInforPrCategory = findViewById(R.id.rootStatCategory);

        statRowCategoryMostCorrect = findViewById(R.id.statRowCategoryMostCorrect);
        statRowCategoryMostWrong = findViewById(R.id.statRowCategoryMostWrong);
        statRowCategoryMostQuizzes = findViewById(R.id.statRowCategoryMostQuizzes);

        txtXp = findViewById(R.id.statXp);
        txtLevel = findViewById(R.id.statLevel);
        progressXp = findViewById(R.id.progressStatXp);
    }

    public void setStats(){
        statRowQuestions.setRowValue(StatTools.getNumberOfQuestions());
        statRowCorrectQuestions.setRowValue(StatTools.getNumberOfCorrectAnswers());
        statRowWrongQuestions.setRowValue(StatTools.getNumberofWrongAnswers());

        StatTools.TimeContainer timeUsed = StatTools.getTotalTimeUsage();

        statRowTimeUsage.setRowValue(String.format("%s:%s:%s",
                Util.padWithZeroIfOneDigit(timeUsed.hours), Util.padWithZeroIfOneDigit(timeUsed.minutes), Util.padWithZeroIfOneDigit(timeUsed.seconds)));

        statRowQuizzes.setRowValue(StatTools.getnumberOfQuizzesTaken());
        statRowQuizzesAllCorrect.setRowValue(StatTools.getNumberOfQuizzesWithAllCorrect());

        txtXp.setText(String.format("%s/%s", Xp.getXp(), Xp.getXpPrLevel()));
        txtLevel.setText(String.format("%s: %s", getResources().getString(R.string.level), Xp.getLevel()));

        int progress = (int) (((float) Xp.getXp()) / ((float) Xp.getXpPrLevel()) * 100 );
        progressXp.setProgress(progress);
    }

    public void setCategoryStats(){
        Map<String, StatTools.CorrectWrongCont> categoryStats = StatTools.getCategoryStats();
        String strCorrect = getResources().getString(R.string.correct_plural);


        for(String categoryName: categoryStats.keySet()){
            StatTools.CorrectWrongCont statCont = categoryStats.get(categoryName);

            StatRow statRow = new StatRow(getApplicationContext());
            statRow.setRowKey(categoryName.equals(Category.CATEGORY_GENERAL) ? getResources().getString(R.string.quizz_general) : categoryName);
            statRow.setRowValue(String.format("%s/%s %s", statCont.correct, statCont.correct + statCont.wrong, strCorrect));
            rootInforPrCategory.addView(statRow);

        }


        StatTools.KeyValCont categoryMostCorrect = StatTools.getCategoryMostOrLeastCorrect(true);
        StatTools.KeyValCont categoryMostWrong = StatTools.getCategoryMostOrLeastCorrect(false);

        StatTools.KeyValCont mostQuizzes = StatTools.getCategoryWithMostQuizzesTaken();

        categoryMostCorrect.key = categoryMostCorrect.key.equals(Category.CATEGORY_GENERAL) ? getResources().getString(R.string.quizz_general) : categoryMostCorrect.key;
        categoryMostWrong.key = categoryMostWrong.key.equals(Category.CATEGORY_GENERAL) ? getResources().getString(R.string.quizz_general) : categoryMostWrong.key;
        mostQuizzes.key = mostQuizzes.key.equals(Category.CATEGORY_GENERAL) ? getResources().getString(R.string.quizz_general) : mostQuizzes.key;

        statRowCategoryMostCorrect.setRowValue(String.format("%s: %s%% %s", categoryMostCorrect.key, categoryMostCorrect.value,  strCorrect ));
        statRowCategoryMostWrong.setRowValue(String.format("%s: %s%% %s", categoryMostWrong.key, categoryMostWrong.value, strCorrect ));
        statRowCategoryMostQuizzes.setRowValue(String.format("%s: %s", mostQuizzes.key, mostQuizzes.value));
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), QuizzPickerActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        //overridePendingTransition(R.anim.slide_down, R.anim.no_anim_hack);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
