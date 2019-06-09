package com.example.erlend.quizzapp.util;

import android.content.Context;

import com.example.erlend.quizzapp.DataManager;
import com.example.erlend.quizzapp.activity.QuizzPickerActivity;
import com.google.firebase.database.DatabaseReference;

//En klasse som inneholder all informasjonen som trengs for å hente ulike data fra Firebase'. BuilderPattern benyttes for å gjøre settingen av en rekke felter enkel

  public class DataRequest {
    private Callbackmethod callbackGotData;
    private String categoryKey;
    private Context context;
    private DatabaseReference databaseReference;
    private QuizzPickerActivity.OnErrorCallbackBundle errorCallbackBundle;
    private LoadingProgressable loadingProgressableActivity;
    private String onlyQuizzToDownloadKey;
    private String quizKey;
    private QuizzPickerActivity quizzPickerActivity;
    private DataManager.Type type;

    //Brukes for å enkelt sette felter på en klasse. Siden mange av feltene er av samme datatype vil denne
    // builderklassen forhindre feil innsending av argumenter
    public static class DatarequestBuilder {
        private Callbackmethod callbackGotData;
        private String categoryKey;
        private Context context;
        private DatabaseReference databaseReference;
        private QuizzPickerActivity.OnErrorCallbackBundle errorCallbackBundle;
        private LoadingProgressable loadingProgressableActivity;
        private String onlyQuizzToDownloadKey;
        private String quizzKey;
        private QuizzPickerActivity quizzPickerActivity;
        private DataManager.Type type;

        public DatarequestBuilder loadingProgressActivity(LoadingProgressable loadingProgressableActivity) {
            this.loadingProgressableActivity = loadingProgressableActivity;
            return this;
        }

        public DatarequestBuilder quizzPickerActivity(QuizzPickerActivity quizzPickerActivity) {
            this.quizzPickerActivity = quizzPickerActivity;
            return this;
        }

        public DatarequestBuilder context(Context context) {
            this.context = context;
            return this;
        }

        public DatarequestBuilder callbackGotData(Callbackmethod callback) {
            this.callbackGotData = callback;
            return this;
        }

        public DatarequestBuilder errorCallbackBundle(QuizzPickerActivity.OnErrorCallbackBundle errorCallbackBundle) {
            this.errorCallbackBundle = errorCallbackBundle;
            return this;
        }

        public DatarequestBuilder databaseReference(DatabaseReference databaseReference) {
            this.databaseReference = databaseReference;
            return this;
        }

        public DatarequestBuilder categoryKey(String categoryKey) {
            this.categoryKey = categoryKey;
            return this;
        }

        public DatarequestBuilder quizKey(String quizzKey) {
            this.quizzKey = quizzKey;
            return this;
        }

        public DatarequestBuilder onlyQuizToDownloadKey(String onlyQuizzToDownloadKey) {
            this.onlyQuizzToDownloadKey = onlyQuizzToDownloadKey;
            return this;
        }

        public DatarequestBuilder type(DataManager.Type type) {
            this.type = type;
            return this;
        }

        public DataRequest build() {
            return new DataRequest(this.context,
                    this.loadingProgressableActivity,
                    this.callbackGotData,
                    this.errorCallbackBundle,
                    this.databaseReference,
                    this.categoryKey,
                    this.quizzKey,
                    this.onlyQuizzToDownloadKey,
                    this.type,
                    this.quizzPickerActivity);
        }
    }

    private DataRequest(
                        Context context,
                        LoadingProgressable loadingProgressableActivity,
                        Callbackmethod callbackGotData,
                        QuizzPickerActivity.OnErrorCallbackBundle errorCallbackBundle,
                        DatabaseReference databaseReference,
                        String categoryKey, String quizKey, String onlyQuizzToDownloadKey,
                        DataManager.Type type,
                        QuizzPickerActivity quizzPickerActivity) {

        this.context = context;
        this.loadingProgressableActivity = loadingProgressableActivity;
        this.callbackGotData = callbackGotData;
        this.errorCallbackBundle = errorCallbackBundle;
        this.databaseReference = databaseReference;
        this.categoryKey = categoryKey;
        this.quizKey = quizKey;
        this.onlyQuizzToDownloadKey = onlyQuizzToDownloadKey;
        this.type = type;
        this.quizzPickerActivity = quizzPickerActivity;
    }

    public void setDatabaseReference(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
    }

    public QuizzPickerActivity getQuizPickerActivity() {
        return this.quizzPickerActivity;
    }

    public Context getContext() {
        return this.context;
    }

    public LoadingProgressable getLoadingProgressableActivity() {
        return this.loadingProgressableActivity;
    }

    public Callbackmethod getCallbackGotData() {
        return this.callbackGotData;
    }

    public QuizzPickerActivity.OnErrorCallbackBundle getErrorCallbackBundle() {
        return this.errorCallbackBundle;
    }

    public DatabaseReference getDatabaseReference() {
        return this.databaseReference;
    }

    public String getCategoryKey() {
        return this.categoryKey;
    }

    public String getQuizKey() {
        return this.quizKey;
    }

    public String getOnlyQuizzToDownloadKey() {
        return this.onlyQuizzToDownloadKey;
    }

    public DataManager.Type getType() {
        return this.type;
    }

}
