package com.example.erlend.quizzapp.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.InputDeviceCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.erlend.quizz_fra_dekompilert.R;
import com.example.erlend.quizzapp.DataManager;
import com.example.erlend.quizzapp.model.QuizStat;
import com.example.erlend.quizzapp.util.DataRequest;
import com.example.erlend.quizzapp.util.LoadingProgressable;
import com.example.erlend.quizzapp.util.Util;
import com.example.erlend.quizzapp.util.Callbackmethod;
import com.example.erlend.quizzapp.adapter.CategoryQuizzAdapter;
import com.example.erlend.quizzapp.model.Category;
import com.example.erlend.quizzapp.model.Quiz;
import com.example.erlend.quizzapp.DataManager.QuizKategoryKeyBundle;
import com.example.erlend.quizzapp.DataManager.Type;
import com.example.erlend.quizzapp.customView.BottomNavigationFragment;
import com.example.erlend.quizzapp.customView.NoDataActivity;

import java.io.Serializable;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

//Aktivitet for å velge kategori og quiz
public class QuizzPickerActivity extends AppCompatActivity implements LoadingProgressable<Long> {
    
    public static final String KEY_BUNDLE_SAVED_DATA = "SavedQuizzData";
    public static final String KEY_INTET_CATEGORY_TO_LOAD = "Category";

    private volatile boolean isLoadingData = false;

    private CategoryQuizzAdapter adapterCategories;
    private CategoryQuizzAdapter adapterQuizzes;

    private DataRequest.DatarequestBuilder datarequestBuilder;

    private ProgressBar progressBarLoadingcategories;
    private TextView txtLoading;

    private TextView btnContinueQuiz;
    private LinearLayout contContinueQuizz;
    private MaterialProgressBar progressCurrentQuizz;
    private TextView txtCurrentQuizz;

    private RecyclerView recyclerviewCategoryQuizz;
    private TextView txtPickInformation;

    private enum Orientation {LANDSCAPE, PORTRAIT}
    private Orientation orientationBeforeLoading = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_quizz_picker);

        progressBarLoadingcategories =  findViewById(R.id.progressLoadingCategories);
        txtPickInformation =  findViewById(R.id.txtPickCategoryQuizz);
        txtLoading =  findViewById(R.id.txtLoading);
        recyclerviewCategoryQuizz =  findViewById(R.id.recyclerViewCategories);
        contContinueQuizz =  findViewById(R.id.contContinueQuizz);
        btnContinueQuiz =  findViewById(R.id.btnContinueQuizz);
        txtCurrentQuizz =  findViewById(R.id.txtCurrentQuizz);
        progressCurrentQuizz =  findViewById(R.id.progressCurrentQuizz);

        recyclerviewCategoryQuizz.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2, GridLayoutManager.HORIZONTAL, false));
        progressBarLoadingcategories.setProgress(0);
        OnErrorCallbackBundle errorCallbackBundle = new OnErrorCallbackBundle();
        datarequestBuilder = new DataRequest.DatarequestBuilder().
                errorCallbackBundle(errorCallbackBundle).
                context(getApplicationContext()).
                loadingProgressActivity(this).
                quizzPickerActivity(this);


        //SavedInstanceState benyttes ved rotasjonsendringer
        String currentCategoryKey;
        if(savedInstanceState != null) {
            SavedBundleData savedBundleData = (SavedBundleData) savedInstanceState.getSerializable(KEY_BUNDLE_SAVED_DATA);
            if(savedBundleData != null){
                Type currentType = savedBundleData.getType();
                DataManager.setCurrentType(currentType);
                switch (currentType) {
                    case CATEGORY:
                        requestCategories();
                        break;
                    case QUIZZES:
                        setActiveQuizzcontainerInvisible();
                        requestQuizzes(savedBundleData.getCurrentCategoryKey());
                        break;
                    default:
                        break;
                }
            }

        }
        else if(DataManager.getCategoryKeyCurrent() != null && DataManager.getCurrentType() == Type.QUIZZES){
            requestQuizzes(DataManager.getCategoryKeyCurrent());
        }
        else if(DataManager.getCurrentType() == Type.CATEGORY){
            requestCategories();
        }
        else if(getIntent() != null){
            currentCategoryKey = getIntent().getStringExtra(KEY_INTET_CATEGORY_TO_LOAD);
            if(currentCategoryKey != null){
                requestQuizzes(currentCategoryKey);

            }
            else
                requestCategories();
        }
        else
            requestCategories();

        //Velger riktig element i navgasjonen i bånn
        ((BottomNavigationFragment) getSupportFragmentManager().findFragmentById(R.id.bottom_navigation_fragment)).setSelectedItem(R.id.navigation_quiz_picker);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {

        DataManager.Type currentType = DataManager.getCurrentType();
        SavedBundleData savedBundleData = new SavedBundleData(currentType);
        if(currentType != null){
            switch (currentType) {
                case CATEGORY:
                    break;
                case QUIZZES:
                    savedBundleData.setCurrentCategoryKey(DataManager.getCategoryKeyCurrent());
                    break;
                default:
                    break;
            }
        }

        outState.putSerializable(KEY_BUNDLE_SAVED_DATA, savedBundleData);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!isLoadingData) {
            recreate();
        }
    }

    //For å håndtere tilbakeknappen
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(DataManager.getCurrentType() == Type.QUIZZES){
                navigateBackToCategorySelection();
                return true;
            }
            else{
                //For at aktiviteten forstatt skal ligge der etter at tilbakeknappen er presset
                moveTaskToBack(true);
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    public void onFoundActiveQuizz() {

        contContinueQuizz.setVisibility(View.VISIBLE);

        QuizStat savedQuizStat = QuizStat.getSavedQuizz();

        Quiz activeQuiz = savedQuizStat.getQuizz();

        DataManager.setCurrentType(Type.CATEGORY);
        progressCurrentQuizz.setProgress((int) ((((float) (savedQuizStat.getNumCorrectAnswers() + savedQuizStat.getNumWrongAnswers() )) / ((float) activeQuiz.getExpectedNumberOfQuestions())) * 100));
        txtCurrentQuizz.setText(activeQuiz.isGeneralQuizz() ? getResources().getString(R.string.quizz_general) : activeQuiz.getName());
        btnContinueQuiz.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                requestQuestions(activeQuiz.getKey(), activeQuiz.getCategoryKey());
            }
        });
    }

    private void requestCategories() {
        if (DataManager.isCategoriesRetrieved()) {
            if (recyclerviewCategoryQuizz.getAdapter() != getCategoryAdapter()) {
                recyclerviewCategoryQuizz.setAdapter(getCategoryAdapter());
                getCategoryAdapter().notifyDataSetChanged();
            }
            afterLoading(Type.CATEGORY, true);
            return;
        }
        datarequestBuilder.onlyQuizToDownloadKey(null).callbackGotData(new Callbackmethod() {
            @Override
            public void callback(Object o) {
                DataManager.setCurrentType(Type.CATEGORY);
                recyclerviewCategoryQuizz.setAdapter(getCategoryAdapter());
                getCategoryAdapter().notifyDataSetChanged();
            }
        });
        DataManager.requestCategories(datarequestBuilder);
    }

    private CategoryQuizzAdapter getCategoryAdapter(){
        if(adapterCategories == null){
            adapterCategories = CategoryQuizzAdapter.getAdapterForCategories(getApplicationContext(), new Callbackmethod<String>() {
                //ON CLICK CATEGORY
                @Override
                public void callback(String categoryKey) {
                    requestQuizzes(categoryKey);
                    DataManager.setCurrentCategoryKey(categoryKey);
                }
            }, new Callbackmethod() {
                //ON CLICK RANDOM CATEGORY
                @Override
                public void callback(Object o) {
                    datarequestBuilder.callbackGotData(new Callbackmethod<QuizStat>() {
                        @Override
                        public void callback(QuizStat quizStat) {
                            startQuestionActivity(quizStat);
                        }
                    });

                    DataManager.generateQuizGeneralQuestions(datarequestBuilder);

                }
            }, recyclerviewCategoryQuizz, Category.getListCategories());
        }
        return adapterCategories;
    }

    private void requestQuizzes(final String categoryKey) {
        datarequestBuilder.callbackGotData(new Callbackmethod() {

            @Override
            public void callback(Object noData) {
                //Sentrerer containeren for valg av quiz hvis man er i landskapsmodus
                DataManager.setCurrentType(Type.QUIZZES);
                DataManager.setCurrentCategoryKey(categoryKey);
                adapterQuizzes = CategoryQuizzAdapter.getAdapterForQuizzes(getApplicationContext(),new Callbackmethod<QuizKategoryKeyBundle>() {

                    @Override
                    public void callback(QuizKategoryKeyBundle quizzBundle) {
                        requestQuestions(quizzBundle.quiz_key, quizzBundle.category_key);
                    }
                }, recyclerviewCategoryQuizz, Quiz.getListQuizzes(categoryKey));
                adapterQuizzes.notifyDataSetChanged();


                recyclerviewCategoryQuizz.setAdapter(adapterQuizzes);
            }
        });
        datarequestBuilder.categoryKey(categoryKey).onlyQuizToDownloadKey(null);
        DataManager.requestQuizzes(datarequestBuilder, false);
    }

    private void setActiveQuizzcontainerInvisible(){
        if(Util.isPortrait(getResources())){
            contContinueQuizz.setVisibility(View.INVISIBLE);
        }
        else{
            contContinueQuizz.setVisibility(View.GONE);
        }
    }

    private void requestQuestions(String quizzKey, String categoryKey) {
        this.datarequestBuilder.quizKey(quizzKey).categoryKey(categoryKey).onlyQuizToDownloadKey(null).callbackGotData(new Callbackmethod<Quiz>() {
            @Override
            public void callback(Quiz downloadedQuiz) {
                DataManager.setCurrentType(Type.QUESTION);

                QuizStat quizzStatForDownloadedQuiz;
                if(QuizStat.hasSavedQuizz()){
                    QuizStat savedQuizz = QuizStat.getSavedQuizz();
                    boolean savedQuizzSameAsChoosen = savedQuizz.getQuizz().getKey().equals(downloadedQuiz.getKey());
                    if(savedQuizzSameAsChoosen){
                        quizzStatForDownloadedQuiz = savedQuizz;
                        quizzStatForDownloadedQuiz.setQuizz(downloadedQuiz);
                    }
                    else{
                        quizzStatForDownloadedQuiz = new QuizStat();
                        quizzStatForDownloadedQuiz.setQuizz(downloadedQuiz);
                    }
                }
                else{
                    quizzStatForDownloadedQuiz = new QuizStat();
                    quizzStatForDownloadedQuiz.setQuizz(downloadedQuiz);
                }

                startQuestionActivity(quizzStatForDownloadedQuiz);
            }
        });

        if(quizzKey.equals(Quiz.KEY_GENERAL)){
            DataManager.requestQuestionsForGeneral(datarequestBuilder);
        }
        else{
            DataManager.requestQuestions(datarequestBuilder);
        }
    }

    public void startQuestionActivity(QuizStat quizStat) {

        Intent intentQuestionActivity = new Intent(getApplicationContext(), QuestionActivity.class);
        intentQuestionActivity.putExtra(QuestionActivity.KEY_BUNDLE_QUIZZSTAT, quizStat);

        startActivity(intentQuestionActivity);
        //overridePendingTransition(R.anim.slide_out_long, R.anim.fade_out_custom_length);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    //TILBAKEKNAPP TRYKKET PÅ
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigateBackToCategorySelection();
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateBackToCategorySelection(){
        DataManager.setCurrentType(Type.CATEGORY);
        beforeLoading(Type.CATEGORY, false);
        requestCategories();
        getCategoryAdapter().notifyDataSetChanged();

    }
    private void displaySnackbarNoInternet(final Type type) {
        displayErrorSnackbar(R.string.no_internet_connection, R.string.retry, new Callbackmethod<Object> () {
            @Override
            public void callback(Object retrievedType) {
                reloadData(type);
            }
        });
    }

    private void reloadData(Type type){
        switch (type) {
            case CATEGORY:
                requestCategories();
                break;
            case QUIZZES:
                requestQuizzes(DataManager.getCategoryKeyCurrent());
                break;
            default:
        }
    }

    public void displayErrorSnackbar(String errorMessage, int btnTitleResId, final Callbackmethod<Object> onBtnPress ){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.rootQuickPicker), errorMessage, -2);
        snackbar.setAction(btnTitleResId, new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnPress.callback(null);
            }
        });
        snackbar.setActionTextColor(InputDeviceCompat.SOURCE_ANY);
        snackbar.getView().setBackgroundColor(Color.RED);
        snackbar.show();
    }

    public void displayErrorSnackbar(int errorTitleResId, int btnTitleResId, final Callbackmethod<Object> onBtnPress) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.rootQuickPicker), errorTitleResId, -2);
        snackbar.setAction(btnTitleResId, new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnPress.callback(null);
            }
        });
        snackbar.setActionTextColor(InputDeviceCompat.SOURCE_ANY);
        snackbar.getView().setBackgroundColor(Color.RED);
        snackbar.show();
    }



    @Override
    public void setprogress(int progressPercent) {
        Util.updateProgressBar(progressPercent, progressBarLoadingcategories);
    }

    @Override
    public void clearprogress() {
        progressBarLoadingcategories.setProgress(0);
    }

    @Override
    public void beforeLoading(Type type, boolean displayLoadingVisually) {
        orientationBeforeLoading = Util.isPortrait(getResources()) ? Orientation.PORTRAIT: Orientation.LANDSCAPE;

        isLoadingData = true;
        clearprogress();
        int resIdActionbarHeader = 0;
        int resIdLoading = 0;
            switch (type) {
                case CATEGORY:
                    resIdActionbarHeader = R.string.category_actionbar_header;
                    resIdLoading = R.string.category_loading;
                    break;
                case QUIZZES:
                    resIdActionbarHeader = R.string.quizz_actionbar_header;
                    resIdLoading = R.string.quizz_loading;
                    break;
                case QUESTION: case GENERAL_QUESTIONS:
                    resIdActionbarHeader = R.string.question_loading;
                    resIdLoading = R.string.question_loading;
                    break;
                default:
                    break;
            }

        //todo skjekk api-versjon og bruyk riktig metode for getCOlor

        if(displayLoadingVisually){
            progressBarLoadingcategories.setVisibility(View.VISIBLE);
            setActiveQuizzcontainerInvisible();
            findViewById(R.id.contQuizzPicker).setVisibility(View.INVISIBLE);
            txtLoading.setBackgroundColor(getResources().getColor(R.color.lightGray));

            if(resIdActionbarHeader != 0){
                if(getSupportActionBar() != null)
                    getSupportActionBar().setTitle(resIdActionbarHeader);
                txtLoading.setText(resIdLoading);
            }

        }

    }

    @Override
    public void afterLoading(Type typeLoaded, boolean displayAfterLoading) {
        isLoadingData = false;
        Orientation orientationAfterLoading = Util.isPortrait(getResources()) ? Orientation.PORTRAIT : Orientation.LANDSCAPE;
        if(orientationBeforeLoading != null && orientationAfterLoading != orientationBeforeLoading){
            recreate();
        }
        txtPickInformation.setText( (typeLoaded == Type.CATEGORY || typeLoaded ==  Type.QUIZ_CONTINUED) ? R.string.category_pick : R.string.quizz_pick);
        txtLoading.setBackgroundColor(0);
        txtLoading.setText("");
        txtLoading.setVisibility(View.GONE);
        if (!Util.isPortrait(getResources())) {
            progressBarLoadingcategories.setVisibility(View.GONE);
        }
        else {
            progressBarLoadingcategories.setVisibility(View.INVISIBLE);
        }

        if ( typeLoaded != Type.QUESTION && typeLoaded != Type.GENERAL_QUESTIONS && displayAfterLoading) {
            findViewById(R.id.contQuizzPicker).setVisibility(View.VISIBLE);
        }

        if (typeLoaded == Type.CATEGORY) {
            if(getSupportActionBar() != null)
                getSupportActionBar().setTitle(getResources().getString(R.string.category_actionbar_header));
            DataManager.requestCurrentQuiz(datarequestBuilder, getApplicationContext());
        }

        if(typeLoaded == Type.QUIZZES){
            getSupportActionBar().setTitle(getResources().getString(R.string.quizz_actionbar_header));
            setActiveQuizzcontainerInvisible();
        }
    }


    public class OnErrorCallbackBundle {
        public void onNoNetwork() {
            displaySnackbarNoInternet(Type.CATEGORY);
        }


         public void onDtabaseError(String str){

            displayErrorSnackbar(str, R.string.retry, new Callbackmethod<Object> () {
                @Override
                public void callback(Object retrievedType) {
                    reloadData(Type.CATEGORY);
                }
            });
        }

        public void onDtabaseError(){

            displayErrorSnackbar(R.string.databaseError, R.string.retry, new Callbackmethod<Object> () {
                @Override
                public void callback(Object retrievedType) {
                    reloadData(Type.CATEGORY);
                }
            });
        }

        public void onNoData(final Type type) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(type != Type.QUIZ_CONTINUED){
                        Intent intentNoDataActivity = new Intent(getApplicationContext(), NoDataActivity.class);
                        intentNoDataActivity.putExtra(NoDataActivity.KEY_NO_DATA_TYPE, type);
                        startActivity(intentNoDataActivity);
                    }
                    else {
                        contContinueQuizz.setVisibility(View.GONE);
                    }

                }
            });
        }
    }

    public static class SavedBundleData implements Serializable {
        private String currentCategoryKey;
        private final Type type;

        private SavedBundleData(Type type) {
            this.type = type;
        }

        public Type getType() {
            return this.type;
        }

        private String getCurrentCategoryKey() {
            return this.currentCategoryKey;
        }

        private void setCurrentCategoryKey(String currentCategoryKey) {
            this.currentCategoryKey = currentCategoryKey;
        }
    }
}
