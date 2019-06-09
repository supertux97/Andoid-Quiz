package com.example.erlend.quizzapp;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.IBinder;

import com.example.erlend.quizz_fra_dekompilert.R;
import com.example.erlend.quizzapp.database.DatabaseContract;
import com.example.erlend.quizzapp.database.DatabaseContract.CurrentQuiz;
import com.example.erlend.quizzapp.database.DatabaseContract.FinishedQuiz;
import com.example.erlend.quizzapp.database.DatabaseHelper;
import com.example.erlend.quizzapp.model.Category;
import com.example.erlend.quizzapp.model.MultipleChoiceQuestion;
import com.example.erlend.quizzapp.model.PercentageQuestion;
import com.example.erlend.quizzapp.model.Question;
import com.example.erlend.quizzapp.model.Quiz;
import com.example.erlend.quizzapp.model.QuizStat;
import com.example.erlend.quizzapp.model.TrueFalseQuestion;
import com.example.erlend.quizzapp.model.Xp;
import com.example.erlend.quizzapp.util.BackgroundMusicPlayerService;
import com.example.erlend.quizzapp.util.DataRequest;
import com.example.erlend.quizzapp.util.Util;
import com.example.erlend.quizzapp.util.Callbackmethod;
import com.example.erlend.quizzapp.util.SnapshotToData;
import com.example.erlend.quizzapp.util.LoadingProgressable;
import com.example.erlend.quizzapp.activity.QuizzPickerActivity;

import com.example.erlend.quizzapp.activity.QuizzPickerActivity.OnErrorCallbackBundle;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.example.erlend.quizzapp.util.DataRequest.DatarequestBuilder;
import static com.example.erlend.quizzapp.util.Util.doesSqlLiteTableExist;

//Håndterer alle eksterne ressurser. Dette inkluderer lesing og skrifving fra datai SQLitedatabasen og lesing fra Firebase.

//DataManager er knyttet direkte til de ulike aktivitetene og har på denne måten også tillstand. Velger å benytte statiske metoder og felter
// frmfor en singelton fordi det ikke er aktuelt å benytte denne klassen i objetksform .
// De tillstandesløse metodene
//finnes i Util.
public class DataManager {
    //Holder styr på hvilke kategorier og quizzer som allerede har blitt lastet ned og laget objekter av.

    private static boolean categoriesRetrieved = false;
    private static DatabaseReference commonDatabaseReference;
    static {
        commonDatabaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private static DataManager.Type currentType = null;
    public static ArrayList<String> listQuizzesForCategoryDownloaded = new ArrayList();
    private static String currentCategoryKey = null;
    public static final String tagDataLoading = "dataLoading";

    public static final String KEY_SHARED_PREFERENCES = "shared_prefs";
    public static final String KEY_PREFS_MUSIC_MUTED = "music_muted";
    public static final String KEY_PREFS_EFFECTS_MUTED = "effects_muted";
    public static final String KEY_PREFS_TAKEN_TROPHIES = "KEY_PREFS_TAKEN_TROPHIES";


    public static final String STANDARD_DELIMITER = ";";

    private static final double TRESHOLD_DISPLAY_LOADING_INFO = 1.0;

    private static double networkSpeedMMPrSec;
    private static BackgroundMusicPlayerService.BinderInterface musicPlayerServiceCommunication;

    private DataManager() {
    }

    //Henter en instanse av musikkspilleren. Denne er er delt av alle aktivitetene for å forhindre at musikken stanser ved restart av aktiviteten(
    //feks på grunn av rotasjoner).
    public static BackgroundMusicPlayerService.BinderInterface getMusicPlayer() {
        return musicPlayerServiceCommunication;
    }

    public static void initMusicPlayerService(Context context){
        ServiceConnection musicPlayerConntection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                musicPlayerServiceCommunication = (BackgroundMusicPlayerService.BinderInterface) service;
                musicPlayerServiceCommunication.initMusicPlayer(R.raw.quizz_music);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };

        Intent intentMusicPlayer = new Intent(context, BackgroundMusicPlayerService.class);
        context.bindService(intentMusicPlayer, musicPlayerConntection, Context.BIND_AUTO_CREATE);
    }



    //=====================================
    //==HENTING AV QUIZZDATA FRA FIREBASE==
    //=====================================
    public static void requestCategories(DataRequest.DatarequestBuilder datarequestBuilder) {
        datarequestBuilder.type(Type.CATEGORY).databaseReference(commonDatabaseReference.child("category"));
        requestQuizDataFromRequest(datarequestBuilder.build());
    }


    public static void requestQuizzes(DatarequestBuilder datarequestBuilder, boolean currentQuiz) {
        datarequestBuilder.databaseReference(null);
        if(currentQuiz)
            datarequestBuilder.type(Type.QUIZ_CONTINUED);
        else
            datarequestBuilder.type(Type.QUIZZES);

        DataRequest dataRequest = datarequestBuilder.build();
        dataRequest.setDatabaseReference(commonDatabaseReference.child("quizz/" + dataRequest.getCategoryKey()));
        requestQuizDataFromRequest(dataRequest);
    }


    public static void requestQuestions(DatarequestBuilder datarequestBuilder) {
        datarequestBuilder.type(Type.QUESTION);
        datarequestBuilder.databaseReference(null);
        DataRequest dataRequest = datarequestBuilder.build();
        dataRequest.setDatabaseReference(commonDatabaseReference.child(String.format("quizz/%s/%s/questions", new Object[]{dataRequest.getCategoryKey(), dataRequest.getQuizKey()})));
        requestQuizDataFromRequest(dataRequest);
    }




    private static void requestQuizDataFromRequest(final DataRequest dataRequest) {
        if (commonDatabaseReference == null) {
            dataRequest.getErrorCallbackBundle().onDtabaseError();
        }
        if (!Util.hasNetworkConnection(dataRequest.getContext())) {
            dataRequest.getErrorCallbackBundle().onNoNetwork();
        }
        dataRequest.getDatabaseReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                QuizDataDownloader quizDataDownloader = new QuizDataDownloader(dataRequest);

                quizDataDownloader.determineIfLoadingbarShuldBeDisplayed(dataSnapshot);
                quizDataDownloader.execute(dataSnapshot);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                dataRequest.getErrorCallbackBundle().onDtabaseError(databaseError.getMessage());
            }
        });
    }





    //=====================================
    //==LESING AV QUIZZDATA FRA SQLLITE==
    //=====================================
    public static boolean requestFinishedQuizzes(Context context) {
        DatabaseHelper databasehelper = new DatabaseHelper(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
        SQLiteDatabase database = databasehelper.getReadableDatabase();

        if(!doesSqlLiteTableExist(DatabaseContract.FinishedQuiz.TABLE_NAME, database)){
            return false;
        }

        Cursor cursor = database.query(DatabaseContract.FinishedQuiz.TABLE_NAME, null, null, null, null, null, null);

        if (!cursor.moveToFirst()) {
            return false;
        }
        do {
            String quizKey = cursor.getString(cursor.getColumnIndex(DatabaseContract.FinishedQuiz.COLUMN_NAME_QUIZZ_KEY));

            int numCorrectAnswers = cursor.getInt(cursor.getColumnIndex(DatabaseContract.FinishedQuiz.COLUMN_NAME_NUM_CORRECT_ANSWWERS));
            int numWrongAnswers = cursor.getInt(cursor.getColumnIndex(DatabaseContract.FinishedQuiz.COLUMN_NAME_NUM_WRONG_ANSWWERS));
            long millisUsed = cursor.getLong(cursor.getColumnIndex(DatabaseContract.FinishedQuiz.COLUMN_NAME_TOTAL_TIME_USED));
            int quizStatId = cursor.getInt(cursor.getColumnIndex(DatabaseContract.FinishedQuiz.COLUMN_NAME_TIMESTAMP_QUIZZ_START));
            String categoryKey = cursor.getString(cursor.getColumnIndex(FinishedQuiz.COLUMN_NAME_CATEGORY_KEY));
            String quizzName = cursor.getString(cursor.getColumnIndex(DatabaseContract.FinishedQuiz.COLUMN_NAME_QUIZZ_NAME));

            if(QuizStat.doesQUizzStatExist(quizStatId)){
                continue;
            }

            Quiz quiz = null;
            QuizStat quizStat = QuizStat.getQuizzStatById(quizStatId, QUIZ_TYPE.FINISHED);
            if(quizStat == null){
                quizStat = new QuizStat(quizStatId);

                quizStat.setNumCorrectAnswers(numCorrectAnswers);
                quizStat.setNumWrongAnswers(numWrongAnswers);
                quizStat.setTimeUsed(millisUsed);
                quizStat.setFinished();

                if(quizKey.equals(Quiz.KEY_GENERAL)){
                    quiz = Quiz.getGeneralQuiz();
                    quiz.setCategory(Category.getGeneralCategory());
                }
                else if(Quiz.isQuizzDownloaded(quizKey)){
                    quiz = Quiz.getQuizzByKey(quizKey);
                }
                else{
                    quiz = new Quiz(quizKey, quizzName, false, categoryKey);
                    quiz.setCategory(Category.getCategoryByKey(categoryKey));
                }

                quizStat.setQuizz(quiz);
            }

            //QuizStat.removeFinishedQuizzes();
            QuizStat.addFinishedQuizzIfNotExists(quizStat);
        } while (cursor.moveToNext());
        return true;
    }


    public static void requestCurrentQuiz(DatarequestBuilder datarequestBuilder, Context context) {

        if(QuizStat.hasSavedQuizz()){
            datarequestBuilder.build().getQuizPickerActivity().onFoundActiveQuizz();
            return;
        }

        DatabaseReference quizReference = null;

        datarequestBuilder.
                databaseReference(quizReference);

        DatabaseHelper databasehelper = new DatabaseHelper(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
        SQLiteDatabase database = databasehelper.getReadableDatabase();

        //https://stackoverflow.com/questions/2810615/how-to-retrieve-data-from-cursor-class
        Cursor cursor = database.query(CurrentQuiz.TABLE_NAME, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            String quizzKey = cursor.getString(cursor.getColumnIndex(CurrentQuiz.COLUMN_NAME_QUIZZ_KEY));
            String categoryKey = cursor.getString(cursor.getColumnIndex(CurrentQuiz.COLUMN_NAME_CATEGORY_KEY));
            final long quizStatID = cursor.getLong(cursor.getColumnIndex(DatabaseContract.FinishedQuiz.COLUMN_NAME_TIMESTAMP_QUIZZ_START));
            final int numCorrectAnswers = cursor.getInt(cursor.getColumnIndex(CurrentQuiz.COLUMN_NAME_NUM_CORRECT_ANSWWERS));
            final int numWrongAnswers = cursor.getInt(cursor.getColumnIndex(CurrentQuiz.COLUMN_NAME_NUM_WRONG_ANSWWERS));
            final int remainingTime = cursor.getInt(cursor.getColumnIndex(CurrentQuiz.COLUMN_NAME_REMAINING_TIME_CURR_QUESTION));
            final boolean hasStarted = cursor.getInt(cursor.getColumnIndex(CurrentQuiz.COLUMN_NAME_HAS_STARTED)) == 1;
            final long totalTimeUsedOnQuizz = cursor.getLong(cursor.getColumnIndex(CurrentQuiz.COLUMN_NAME_CURRENT_TIME_USED));
            final String strQuestionKeysForGeneralQuiz = cursor.getString(cursor.getColumnIndex(CurrentQuiz.COLUMN_NAME_QUESTION_KEYS_FOR_GENERAL));
            final QuizzPickerActivity quizzPickerActivity = datarequestBuilder.build().getQuizPickerActivity();

            //Henter inn den tilhørende quizen
            datarequestBuilder.callbackGotData(new Callbackmethod<Quiz>() {
                //Når quizzen er hentet
                @Override
                public void callback(Quiz downloadedQuiz) {
                    QuizStat quizStat = new QuizStat(quizStatID);
                    quizStat.setQuizz(downloadedQuiz);
                    quizStat.setCurrentQuestionNumber((numCorrectAnswers + numWrongAnswers) + 1);
                    quizStat.setNumCorrectAnswers(numCorrectAnswers);
                    quizStat.isNotFirstQuestion();
                    quizStat.setHasStarted(hasStarted);
                    quizStat.setTotalTimeBeforeCurrentSession(totalTimeUsedOnQuizz);
                    quizStat.setRemainingTimeCurrQuestion(remainingTime);
                    quizStat.setContinuedQuizz(true);
                    QuizStat.setSavedQuizz(quizStat);

                    quizzPickerActivity.onFoundActiveQuizz();
                }
            });
            datarequestBuilder.
                    categoryKey(categoryKey).
                    quizKey(quizzKey).
                    onlyQuizToDownloadKey(quizzKey);


            DataRequest dataRequest = datarequestBuilder.build();
            if(quizzKey.equals(Quiz.KEY_GENERAL)){
                Quiz.setQuestionKeysStringForGeneralQuizz(strQuestionKeysForGeneralQuiz);
                dataRequest.getCallbackGotData().callback(Quiz.getGeneralQuiz());
            }
            else{
                requestQuizzes(datarequestBuilder, true);
            }

        }
    }



    /*
     Henter et vist antall tilfeldige spørsmål som er av typen general. Dette blir hentet fra en egen gren av trestrukturen som inneholder slike spørsmål, nemlig
     general. Dette betyr at samme spørsmål kan ligge lagret både inne i en enkeltquizz og i general. Dette er en ulempe på den ene siden, fordi man da får en
     dobbeltlagring og oppdatering av spørsmål blir krevende, men på den andre siden er ikke dette noe stort problem i praksis da spørsmålene sjeldent trenger å oppdateres.
     En egen gren med slike spørsmål gjør også at det å hente ut spørsmålene blir en veldig mye enklere og mindre ressurskrevende oppgavene enn å ha måtte først valgt en
     tilfeldgi kategori, en tilfeldig quizz, et tilfeldig spørsmål inne i denne quizzen av typen general og ha måtte gjentatt dette feks 20 ganger.
     Jeg vurderer fordelen med tanke på enkelthet og ytelse som større enn ulempene med tanke på dobbeltlagring.

     Her er strukturen, som kan illustrere vanskeligheten med å hente ut tilfeldgie spørsmål


     root
     *category
        *kat1
        *kat2
        * osv
    *quizz
        *kat1
            *quizz1
                *questions
                    *question1
                        *answer
                        * category
                        * image
                        * question
                        * quizz
                        * type
                        * isGeneral
                    * question2
                    * osv
            * quizz2
            * osv
        *kat2
        * osv
    * general
        *question1
            *answer
            * category
            * image
            * question
            * quizz
            * type
            * isGeneral
        * question2

     */
    public static void generateQuizGeneralQuestions(DatarequestBuilder dataRequestBuilder){
        //Får NetworkOnMainThreadExeption selv om netlastingen foregår i en AsyncTask. Metoden som skaper problemet, nedlasting av et bilde, blir
        //også kjørt i en annen AsyncTask uten problemer overhodet. Har derfor fjernet bildene fra de almenne spørsmålene som een midlertidig løsning

        dataRequestBuilder.databaseReference(commonDatabaseReference.child("general/"));
        DataRequest dataRequest = dataRequestBuilder.build();

        dataRequest.getDatabaseReference().addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {

                  FindAndDownloadRandomQuestions findAndDownloadRandomQuestions = new FindAndDownloadRandomQuestions(dataRequestBuilder);
                  findAndDownloadRandomQuestions.execute(dataSnapshot);
              }

              @Override
              public void onCancelled(DatabaseError databaseError) {

              }
          });

    }

    /**@return om det ligger lagret noe Xp eller ikke**/
    public static boolean loadXp(Context context){
        SQLiteDatabase database = new DatabaseHelper(context, DatabaseContract.DATABASE_NAME, null, 1).getReadableDatabase();
        if(!doesSqlLiteTableExist(DatabaseContract.XP.TABLE_NAME, database)){
            return false;
        }

        Cursor cursor = database.query(DatabaseContract.XP.TABLE_NAME, null, null,null,null,null,null);
        if(!cursor.moveToFirst()){
            return false;
        }

        int xp = cursor.getInt(cursor.getColumnIndex(DatabaseContract.XP.COLUMN_NAME_XP));
        int level = cursor.getInt(cursor.getColumnIndex(DatabaseContract.XP.COLUMN_NAME_LEVEL));
        Xp.setData(xp, level);
        return true;
    }


    //=====================================
    //==LAGRING AV QUIZZDATA TIL SQLLITE==
    //=====================================
    public static void saveXP(Context context){
        SQLiteDatabase database = new DatabaseHelper(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION).getWritableDatabase();
        if(!doesSqlLiteTableExist(DatabaseContract.XP.TABLE_NAME, database )){
            return;
        }
        ContentValues contentToSave = new ContentValues();
        contentToSave.put(DatabaseContract.XP.COLUMN_NAME_XP, Xp.getXp());
        contentToSave.put(DatabaseContract.XP.COLUMN_NAME_LEVEL, Xp.getLevel());
        database.delete(DatabaseContract.XP.TABLE_NAME, null, null);
        database.insert(DatabaseContract.XP.TABLE_NAME, null, contentToSave);
    }

    public static void saveQuiz(Context context, QuizStat quizStatToSave, QUIZ_TYPE QUIZ_type) {
        boolean hasOngoingQuiz = true;
        SQLiteDatabase database = new DatabaseHelper(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION).getWritableDatabase();
        if (quizStatToSave == null || quizStatToSave.isQuizzFinished()) {
            hasOngoingQuiz = false;
        }

        String tableName = QUIZ_type == QUIZ_TYPE.ONGOING ? CurrentQuiz.TABLE_NAME : DatabaseContract.FinishedQuiz.TABLE_NAME;

        if (QUIZ_type == QUIZ_TYPE.FINISHED || hasOngoingQuiz) {
            ContentValues contentToInsert = new ContentValues();
            contentToInsert.put(FinishedQuiz.COLUMN_NAME_NUM_CORRECT_ANSWWERS, quizStatToSave.getNumCorrectAnswers());
            contentToInsert.put(DatabaseContract.FinishedQuiz.COLUMN_NAME_NUM_WRONG_ANSWWERS, quizStatToSave.getNumWrongAnswers());
            contentToInsert.put(DatabaseContract.FinishedQuiz.COLUMN_NAME_CATEGORY_KEY, quizStatToSave.getQuizz().getCategoryKey());
            contentToInsert.put(DatabaseContract.FinishedQuiz.COLUMN_NAME_TIMESTAMP_QUIZZ_START, quizStatToSave.getId());


            if (QUIZ_type == QUIZ_TYPE.ONGOING) {
                contentToInsert.put(CurrentQuiz.COLUMN_NAME_CURRENT_TIME_USED, quizStatToSave.getTimeUsedMillis());
                contentToInsert.put(CurrentQuiz.COLUMN_NAME_QUIZZ_KEY, quizStatToSave.getQuizz().getKey());
                int remaining_time = quizStatToSave.getRemainingTimeCurrQuestion();
                contentToInsert.put(CurrentQuiz.COLUMN_NAME_REMAINING_TIME_CURR_QUESTION, remaining_time);
                contentToInsert.put(CurrentQuiz.COLUMN_NAME_HAS_STARTED, quizStatToSave.hasStarted() ? 1:0);
                if(quizStatToSave.getQuizz().isGeneralQuizz()){
                    contentToInsert.put(CurrentQuiz.COLUMN_NAME_QUESTION_KEYS_FOR_GENERAL, Quiz.getQuestionStrForGeneralQuizz());
                }

                //Fordi denne tabellen bare skal ha en enkelt rad, den aktive quizen, blir tabellen tømt før ny data blir satt inn
                database.delete(CurrentQuiz.TABLE_NAME, null, null);
            }
            else if (QUIZ_type == QUIZ_TYPE.FINISHED) {
                contentToInsert.put(DatabaseContract.FinishedQuiz.COLUMN_NAME_TOTAL_TIME_USED, quizStatToSave.getTimeUsedMillis());
                contentToInsert.put(DatabaseContract.FinishedQuiz.COLUMN_NAME_QUIZZ_NAME, quizStatToSave.getQuizz().getName());
                contentToInsert.put(DatabaseContract.FinishedQuiz.COLUMN_NAME_QUIZZ_KEY, quizStatToSave.getQuizz().getKey());
            }
            database.beginTransaction();
            database.insert(tableName, null, contentToInsert);
            database.setTransactionSuccessful();
            database.endTransaction();
            return;
        }

        database.delete(CurrentQuiz.TABLE_NAME, null, null);
    }


    public static void removeCurrentQuiz(Context context) {
        new DatabaseHelper(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION)
                .getWritableDatabase().delete(CurrentQuiz.TABLE_NAME, null, null);
    }

    private static Question getQuestion(SnapshotToData snapshotToData, String key){
        Question question = null;

        String strQuestion = snapshotToData.getStringFromKey("question");
        String strCategoryKey = snapshotToData.getStringFromKey("category");
        String strImage = snapshotToData.getStringFromKey("image");
        String strType = snapshotToData.getStringFromKey("type");
        Category category = Category.getCategoryFromKey(strCategoryKey);

        if (strType.equals("trueFalse")) {
            boolean answer = snapshotToData.getBooleanFromKey("answer");
            question = new TrueFalseQuestion(Question.KEY_RANDOM, strQuestion, category, strImage, false, answer);
        }
        else if (strType.equals("percentage")) {
            int answer = snapshotToData.getIntFromKey("answer");
            question = new PercentageQuestion(key, strQuestion,category,  answer, strImage, false);
        }
        else if (strType.equals("multiple")){
            int correctAnswerIndex = snapshotToData.getIntFromKey("answer");

            ArrayList<String> choices = snapshotToData.getArrayOfValuesFromKey("alternatives");
            String correctChoice = choices.get(correctAnswerIndex);
            MultipleChoiceQuestion.ChoiceContainer choiceContainer = new MultipleChoiceQuestion.ChoiceContainer(choices);
            choiceContainer.setCorrectChoice(correctChoice);

            question = new MultipleChoiceQuestion(key, strQuestion, category, strImage, false, choiceContainer);}

        if (strImage != null) {
            try {
                question.downloadImageFromUrl();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        return question;
    }

    public static void requestQuestionsForGeneral(DatarequestBuilder datarequestBuilder) {
        List<String> lisKeyQuestionsToGet = Arrays.asList(Quiz.getQuestionStrForGeneralQuizz().split(";"));
        DataRequest dataRequest = datarequestBuilder.build();

        Quiz.getGeneralQuiz().getListQuestions().clear();

            commonDatabaseReference.child("general/").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()){
                        DataSnapshot snapshotCurr = iterator.next();
                        //Hvis alle spørsmålene som trengs har blitt hentet
                        if(Quiz.getGeneralQuiz().getListQuestions().size() == lisKeyQuestionsToGet.size()){
                            break;
                        }
                        //Hvis spørsmålet iteratoren er på nå er et som er etterspurt
                        if( lisKeyQuestionsToGet.indexOf(snapshotCurr.getKey() ) >= 0 ){
                            //Hvis spørsmålet allerede er latet ned trengs ikke dette å gjøres en gang til
                            Question question = Question.getQuestionByKey(snapshotCurr.getKey());
                            if(question != null){
                                Quiz.getGeneralQuiz().addQuestionToQuizz(question);
                            }
                            else{
                                SnapshotToData snapshotToData = new SnapshotToData(snapshotCurr);
                                Quiz.getGeneralQuiz().addQuestionToQuizz(getQuestion(snapshotToData,  Question.KEY_RANDOM));
                            }

                        }
                    }

                    QuizStat.getSavedQuizz().setQuizz(Quiz.getGeneralQuiz());
                    if(lisKeyQuestionsToGet.size() != Quiz.getGeneralQuiz().getListQuestions().size()){
                        dataRequest.getErrorCallbackBundle().onNoData(Type.GENERAL_QUESTIONS);
                    }
                    dataRequest.getCallbackGotData().callback(Quiz.getGeneralQuiz());

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}

        });

    }



    public static class FindAndDownloadRandomQuestions extends AsyncTask<DataSnapshot, Integer, Integer>{


        private DatarequestBuilder dataRequestBuilder;
        private DataRequest dataRequest;

        private int numOfQuestionsRequestedForQuiz;

        private QuizStat quizStat;
        private Quiz quiz;

        public FindAndDownloadRandomQuestions(DatarequestBuilder dataRequestBuilder) {
            this.dataRequestBuilder = dataRequestBuilder;
        }

        @Override
        protected Integer doInBackground(DataSnapshot ... dataSnapshot ) {

            boolean isDownloading = true;

            int numGeneralQuestions = (int) dataSnapshot[0].getChildrenCount();
            if(numGeneralQuestions < numOfQuestionsRequestedForQuiz){
                dataRequest.getErrorCallbackBundle().onNoData(Type.GENERAL_QUESTIONS);
                return 0;
            }

            StringBuilder strBuilderQuestionKeys = new StringBuilder();

            //Genererer en array med indexen til de tilfeldige spørsmålene som skal hentes ut
            //Henter tilfeldig spørsmål
            List<Integer> listChoosenIndexes = Util.listWithUniqueRandomNums(0, numGeneralQuestions -1, numOfQuestionsRequestedForQuiz);

            Iterator<DataSnapshot> iterator = dataSnapshot[0].getChildren().iterator();

            for(int currIndex = 0; iterator.hasNext(); currIndex++){
                DataSnapshot currDataSnapshot = iterator.next();
                if(listChoosenIndexes.contains(currIndex)){
                    SnapshotToData snapshotToData = new SnapshotToData(currDataSnapshot);
                    quiz.addQuestionToQuizz(getQuestion(snapshotToData, Question.KEY_RANDOM));
                    //Lagrer en semikolonseparert streng bestående av de ulike spøsmålene som ble plukket ut
                    //Dette er for å kunne hente ut akkuratt disse spørsmålene på nytt hvis brukeren setter quizzen på pause og deretter lukker appen, åpner den
                    //og ønsker å fortsette quizzen
                    strBuilderQuestionKeys.append(currDataSnapshot.getKey());
                    strBuilderQuestionKeys.append(";");
                }
            }

            quizStat.setQuizz(quiz);
            //Fjerner siste semilokon
            strBuilderQuestionKeys.delete(strBuilderQuestionKeys.length() -1, strBuilderQuestionKeys.length() ); //Fjerner siste selikolon

            Quiz.setQuestionKeysStringForGeneralQuizz(strBuilderQuestionKeys.toString());


            return 1;
        }


        @Override
        protected void onPreExecute() {
            dataRequestBuilder.type(Type.GENERAL_QUESTIONS);
            dataRequestBuilder.databaseReference(commonDatabaseReference.child("general/"));
            dataRequest = dataRequestBuilder.build();

            numOfQuestionsRequestedForQuiz = Quiz.NUM_QUESTIONS_FOR_GENERAL_QUIZZ;

            quizStat = new QuizStat();
            quiz = Quiz.getGeneralQuiz();
            quiz.getListQuestions().clear();
            dataRequest.getLoadingProgressableActivity().beforeLoading(Type.GENERAL_QUESTIONS, true);

        }

        @Override
        protected void onPostExecute(Integer integer) {
            dataRequest.getLoadingProgressableActivity().afterLoading(Type.GENERAL_QUESTIONS, true);

            if(quiz.getListQuestions().size() < Quiz.NUM_QUESTIONS_FOR_GENERAL_QUIZZ){
                dataRequest.getErrorCallbackBundle().onNoData(Type.GENERAL_QUESTIONS);
            }
            else {
                dataRequest.getCallbackGotData().callback(quizStat);
            }
        }
    }


    //=====================================
    //==NEDLASTING AV SPØRSMÅL==
    //=====================================


    //Tar seg av nedlastingen av quizz-data: kategorier, quizzer og spørsmål
    public static class QuizDataDownloader extends AsyncTask<DataSnapshot, Integer, Long> {
        private final OnErrorCallbackBundle bundleErrorEvent;
        private final Callbackmethod callbackSuccess;
        private DataRequest dataRequest;
        private String keyOnlyQuizToGet = null;
        private final LoadingProgressable loadingProgressableActivity;
        private Quiz quizDownloaded;
        private final String quizzKey;
        private Quiz singleQuizDownloaded; //Brukt for å hente ut informasjon om den aktive quizen
        private final Type type;
        private String rootKey;

        //For små nedlastinger og kjappe nettverk trengs ikke nedlastingsbaren å vises da dette kun vil oppfattes som blinkende skrift man ikke rekker å lese
        private static boolean shouldDownloadbarbeDisplayed;


        public QuizDataDownloader(DataRequest dataRequest) {
            this.loadingProgressableActivity = dataRequest.getLoadingProgressableActivity();
            this.callbackSuccess = dataRequest.getCallbackGotData();
            this.bundleErrorEvent = dataRequest.getErrorCallbackBundle();
            this.type = dataRequest.getType();
            this.quizzKey = dataRequest.getQuizKey();
            this.keyOnlyQuizToGet = dataRequest.getOnlyQuizzToDownloadKey();
            this.dataRequest = dataRequest;
        }


        @Override
        protected Long doInBackground(DataSnapshot... dataSnapshots) {
            boolean foundQuiz = false;

            Iterator<DataSnapshot> iterQuizData = dataSnapshots[0].getChildren().iterator();
            rootKey = dataSnapshots[0].getKey();
            long numQuizData = dataSnapshots[0].getChildrenCount();

            if (numQuizData == 0) {
                this.bundleErrorEvent.onNoData(this.type);
                cancel(true);
                return Long.valueOf(0);
            }

            float downloaded = 0.0f;
            boolean quizzesInCategoryDownloaded = DataManager.isQuizForCategoryDownloaded(rootKey);

            if (keyOnlyQuizToGet != null) {
                singleQuizDownloaded = Quiz.getQuizzByKey(keyOnlyQuizToGet);
            }

            //==SKjekker om dataen som trengs allerede er lastet ned. I såfall avbrytes nedlastingen
            if (type == Type.QUESTION && Quiz.isQuestionsForQuizzDownloaded(Quiz.getQuizzByKey(quizzKey), (int) numQuizData)) {
                this.quizDownloaded = Quiz.getQuizzByKey(quizzKey);
                return 0l;
            }

            if(quizzesInCategoryDownloaded){
                return 0l;
            }


            if(type == Type.QUIZZES){
                Category category = Category.getCategoryFromKey(dataRequest.getCategoryKey());
                if(category != null)
                    category.setExpecedNumberOfQuizzes((int) numQuizData);
            }


            while (iterQuizData.hasNext() && !foundQuiz ) {
                DataSnapshot snapshotQuizData =  iterQuizData.next();
                SnapshotToData snapshotToData = new SnapshotToData(snapshotQuizData);
                String key = snapshotQuizData.getKey();
                String strName;


                if (type == Type.QUIZZES || type == Type.QUIZ_CONTINUED) {

                    if(Quiz.isQuizzDownloaded(key)){
                        continue;
                    }

                    strName = snapshotToData.getStringFromKey("name");
                    String strCatagoryKey = snapshotToData.getStringFromKey("category");
                    boolean forKids = snapshotToData.getBooleanFromKey("forKids");
                    Quiz quiz = new Quiz(key, strName, forKids, strCatagoryKey);

                    DataManager.currentCategoryKey = strCatagoryKey;
                    if (key.equals(this.keyOnlyQuizToGet)) {
                        foundQuiz = true;
                        quiz.setExpectedNumberOfQuestions((int) snapshotQuizData.child("questions").getChildrenCount());
                        singleQuizDownloaded = quiz;
                    }
                }


                else if (type == Type.CATEGORY) {

                    if(Category.isCategoryDownloaded(key))
                        continue;

                    strName = snapshotToData.getStringFromKey("name");
                    String strColor = snapshotToData.getStringFromKey("color");
                    String urlImage = snapshotToData.getStringFromKey("image");

                    Category category = new Category(strName, key, urlImage, strColor);
                    if (urlImage != null) {
                        try {
                            category.downloadImageFromUrl();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                }


                else if (type == Type.QUESTION) {

                    if(Question.isQuestionDownloaded(key))
                        continue;

                    quizDownloaded = Quiz.getQuizzByKey(quizzKey);
                    quizDownloaded.setExpectedNumberOfQuestions((int) numQuizData);

                    quizDownloaded.addQuestionToQuizz(getQuestion(snapshotToData, key));
                }


                downloaded += 1.0f;
                int progress = (int) (downloaded / numQuizData * 100);
                publishProgress(progress);

                if (isCancelled()) {
                    break;
                }
            }

            return numQuizData;
        }

        @Override
        protected void onPreExecute() {

            if (this.loadingProgressableActivity != null || type != Type.QUIZ_CONTINUED) {
                this.loadingProgressableActivity.beforeLoading(type, shouldDownloadbarbeDisplayed);
            }
        }

        @Override
        protected void onPostExecute(Long aLong) {
            if (this.loadingProgressableActivity != null || type != Type.QUIZ_CONTINUED) {
                this.loadingProgressableActivity.afterLoading(type, true);
            }

            switch (this.type) {
                case QUESTION:
                    callbackSuccess.callback(quizDownloaded);
                   break;
                case QUIZ_CONTINUED:
                    DataManager.registerQuizzesForCategoryDownloaded(rootKey);
                    callbackSuccess.callback(this.singleQuizDownloaded);
                    break;
                case QUIZZES:
                    callbackSuccess.callback(null);
                    break;
                case CATEGORY:
                    callbackSuccess.callback(null);
                    DataManager.categoriesRetrieved = true;
                    break;
                default:
                    this.callbackSuccess.callback(null);
                    break;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (this.loadingProgressableActivity != null) {
                this.loadingProgressableActivity.setprogress(values[0].intValue());
            }
        }


        //Skjekker om den estimerte nedlastingstiden overstiger grensen for hvor lenge brukeren kan vente på dataen uten å få en fremdriftsviser.
        //Dette gjøres for å slippe at fremdriftsviseren kun er synlig i et øyeblikk på raske tilkoplinger og dermed kan gi en irriterende blinkende effekt.
        private void determineIfLoadingbarShuldBeDisplayed(DataSnapshot... dataSnapshots){

            //Siden en quiz benytter kategorien sitt bilde(som allerede er lastet ned), vil det utelukkende være veldig små mengder med tekstelige data som lates ned
            //dette er såpass små mengder at det vil gå på et øyeblikk, med mindre nettverket er EKSTREMT tregt.
            if(type == Type.QUIZZES){
                shouldDownloadbarbeDisplayed = false;
                return;
            }

            long numQuizData = dataSnapshots[0].getChildrenCount();
            double calculatedDownloadSizeMb = 0;

            //Størrelsen er justert for at firebase bruker litt tid på å kople til, og at det også er tekstelige data å laste ned.
            double mbPrCategory = 4.4; //ikonet til kategorien
            double mbPrQuestion = 4.4; //bildet som kan være på hvert spørsmål

            switch (type){
                case CATEGORY:
                    calculatedDownloadSizeMb = mbPrCategory * numQuizData;
                    break;
                case QUESTION:
                    calculatedDownloadSizeMb =  mbPrQuestion * numQuizData;
                    break;
                default:
                    break;
            }

            double calculatedTimeForDownload = calculatedDownloadSizeMb / networkSpeedMMPrSec;
            boolean timeForDownloadLargeEnughToDisplayLoading = calculatedTimeForDownload > TRESHOLD_DISPLAY_LOADING_INFO;

            boolean dataAlreadyDownloaded = false;

            if( dataRequest.getType() == Type.CATEGORY && DataManager.categoriesRetrieved )
                dataAlreadyDownloaded = true;

            if(dataRequest.getType() == Type.QUESTION){
                Quiz quizToCheckForDownloaded = Quiz.getQuizzByKey(dataRequest.getQuizKey());
                if(quizToCheckForDownloaded != null && Quiz.isQuestionsForQuizzDownloaded(quizToCheckForDownloaded)){
                    dataAlreadyDownloaded = true;
                }
            }

            if(dataRequest.getType() == Type.QUIZZES){
                Category categoryForQuizzesToDownload = Category.getCategoryFromKey(dataRequest.getCategoryKey());
                if(Quiz.isQuizzesForCategoryDownloaded(categoryForQuizzesToDownload))
                    dataAlreadyDownloaded = true;
            }

            shouldDownloadbarbeDisplayed =  !dataAlreadyDownloaded && timeForDownloadLargeEnughToDisplayLoading;

        }

    }

    //=====================================
    //==DIVERSE==
    //=====================================


    public static void registerNetworkSpeed(LoadingProgressable loadingProgressableActivity){

        class TestNetworkSpeed extends AsyncTask{

            @Override
            protected Object doInBackground(Object[] objects) {
                networkSpeedMMPrSec = Util.getNetworkSpeedMBPrSec(2);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                loadingProgressableActivity.afterLoading(Type.NETWORK_TEST, false);
            }
        }

        new TestNetworkSpeed().execute();
    }


    public static boolean isCategoriesRetrieved() {
        return categoriesRetrieved;
    }

    public static String getCategoryKeyCurrent() {
        return currentCategoryKey;
    }

    public static void setCurrentCategoryKey(String currentCategoryKey) {
        DataManager.currentCategoryKey = currentCategoryKey;
    }

    public static void setCurrentType(DataManager.Type currentType) {
        DataManager.currentType = currentType;
    }

    public static DataManager.Type getCurrentType() {
        return currentType;
    }

    public static boolean isQuizForCategoryDownloaded(String categoryKey) {
        return listQuizzesForCategoryDownloaded.contains(categoryKey);
    }

    public static void registerQuizzesForCategoryDownloaded(String categorykey) {
        listQuizzesForCategoryDownloaded.add(categorykey);
    }

    public static class QuizKategoryKeyBundle {
        public final String category_key;
        public final String quiz_key;

        public QuizKategoryKeyBundle(String quiz_key, String category_key) {
            this.category_key = category_key;
            this.quiz_key = quiz_key;
        }
    }

    public static double getNetworkSpeedMMPrSec() {
        return networkSpeedMMPrSec;
    }

    public enum QUIZ_TYPE {
        FINISHED,
        ONGOING
    }

    //Til bruk i diverse kontekster, feks som argument til metoder eller konsruktører.
    //Hensikten er kode som er enklere å lese og skrive
    //Strengverdiene er til bruk i debugging
    public enum Type implements Serializable {
        CATEGORY(QuizzPickerActivity.KEY_INTET_CATEGORY_TO_LOAD),
        QUIZZES("Quiz"),
        QUESTION("Question"),
        GENERAL_QUESTIONS("Random questions"),
        STAT("stat"),
        QUIZ_CONTINUED("questions for current quizz"),
        NETWORK_TEST("network test"),
        DATABASE_ERROR("database error"),
        NOT_SECIFIED("Not specified"),
        TROPHY("Trophy");

        public final String name;

        private Type(String name) {
            this.name = name;
        }
    }
}
