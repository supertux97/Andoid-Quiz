package com.example.erlend.quizzapp.customView;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.erlend.quizz_fra_dekompilert.R;
import com.example.erlend.quizzapp.DataManager.Type;

//Aktivitet som vises når det ikke finnes noe data av den forespurte typen.
// Den forespurte typen(CATEGORY|QUESTION|QUIZZES|STAT|NOT_SECIFIED) sendes med som en intent ved bruk av nøkkelen: KEY_NO_DATA_TYPE
public class NoDataActivity extends AppCompatActivity {
    public static final String KEY_DISPLAY_TO_PICKER_NAVIGATION = "KEY_DISPLAY_TO_PICKER_NAVIGATION";
    public static final String KEY_NO_DATA_TYPE = "NoDataType";
    private BottomNavigationView bottomNavigationView;
    private TextView txtNoDatamesage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_data);
        this.bottomNavigationView = findViewById(R.id.bottom_navigation_fragment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        txtNoDatamesage =  findViewById(R.id.txtNoDataFound);
        Type typeData = (Type) getIntent().getSerializableExtra(KEY_NO_DATA_TYPE);

        if (typeData == null) {
            typeData = Type.NOT_SECIFIED;
        }
        switch (typeData) {
            case CATEGORY:
                this.txtNoDatamesage.setText(R.string.noCategories);
                setCurrNavigationItem(R.id.navigation_quiz_picker);
                break;
            case QUESTION:
                this.txtNoDatamesage.setText(R.string.noQuestions);
                break;
            case QUIZZES:
                this.txtNoDatamesage.setText(R.string.noQuizzes);
                break;
            case STAT:
                this.txtNoDatamesage.setText(R.string.no_quizzes_taken);
                setCurrNavigationItem(R.id.navigation_stat);
                break;
            case TROPHY:
                setCurrNavigationItem(R.id.navigation_trophy);
                this.txtNoDatamesage.setText(R.string.noneTrophies);
                break;
            case GENERAL_QUESTIONS:
                this.txtNoDatamesage.setText(R.string.notEnoughQuestionsForGeneralQuizz);
                break;
            case NOT_SECIFIED:default:
                this.txtNoDatamesage.setText(R.string.noNotSpecifiedData);
                break;
        }

    }

    public void setCurrNavigationItem(int resIntMenuItem){
        ((BottomNavigationFragment) getSupportFragmentManager().findFragmentById(R.id.bottom_navigation_fragment)).setSelectedItem(resIntMenuItem);
    }



    @Override
    protected void onResume() {
        super.onResume();
        //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
