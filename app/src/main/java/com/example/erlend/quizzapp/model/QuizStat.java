package com.example.erlend.quizzapp.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.annimon.stream.Stream;
import com.example.erlend.quizzapp.DataManager;

import java.util.ArrayList;
import java.util.Iterator;


//Inneholder informasjon om den aktive quizzen. Blir brukt av aktiviteten for spørsmål. Denne klassen lar også aktivitetene kommunisere med
//den tilhørende quizen og kategorien til quizen
public class QuizStat implements Cloneable, Parcelable {
    private static ArrayList<QuizStat> listQuizzesTaken = new ArrayList();
    private static ArrayList<QuizStat> listQuizStat = new ArrayList<>();
    private static QuizStat savedQuizz; //Quiz som blir lagret ved lukking av appen

    // for å unngå at et svar kan markeres som ritkig eller feil flere ganger
    private boolean answerSet = false;

    private int finalTimeUsedSeconds = -1;
    private boolean firstQuestion = true;

    private boolean isContinuedQuizz = false;

    //For å gjøre parcabefiseringen enklere, blir kun indeksen til quizzen i Quiz sin statiske quizzListe lagret. På denne måten trenger ikke quizz og alt i
    //quizz og quizz sine felter å parcabelifiseres.
    private int indexQuizz = -1;

    private int numCorrectAnswers;
    private int numQuestionsInQuizz;
    private int numWrongAnswers;
    private int questionNoIndex = 0;
    private boolean quizzFinished;
    private boolean hasStarted = false;

    private int remainingTimeCurrQuestion = -1;

    private long id; //For å unikt identifisere en enkelt quizzStat
    private long timestampStartedQuizzSession;

    private long totalTimeBeforeCurrentSession; //Hvis dette er en quizz som har blitt lagret
    private long timeUsedForQuizzSession;

    private int percentageSelected = -1; //For å lagre valgt prosent, hvis den finnes
    private byte[] questionKeysString;


    public QuizStat() {
        listQuizStat.add(this);
        this.id = System.currentTimeMillis();
    }

    public QuizStat(long id){
        listQuizStat.add(this);
        this.id = id;
    }


    @Override
    protected QuizStat clone() throws CloneNotSupportedException {
        return (QuizStat) super.clone();
    }

    //==GETTERE OG SETTERE==

    public void notifyDataSetChanged() {
        this.numQuestionsInQuizz = getQuizz().getListQuestions().size();
    }

    public void setQuizz(Quiz quiz) {
        this.indexQuizz = Quiz.getIndexOfQuizz(quiz);
        this.numQuestionsInQuizz = quiz.getListQuestions().size();
        notifyDataSetChanged();
    }

    public void correctAnswer() {
        if (!this.answerSet) {
            this.numCorrectAnswers++;
        }
        this.answerSet = true;
    }

    public void incorrectAnswer() {
        if (!this.answerSet) {
            this.numWrongAnswers++;
        }
        this.answerSet = true;
    }

    public Quiz getQuizz() {
        if(indexQuizz == -1)
            return null;
        return Quiz.getListQuizs().get(this.indexQuizz);
    }

    public String getCategoryName(){
        return getQuizz().getCategory().getName();
    }

    public void isNotFirstQuestion() {
        this.firstQuestion = false;
    }

    public boolean isQuizzFinished() {
        return this.quizzFinished;
    }

    public void setFinished(){
        this.quizzFinished = true;
    }

    public Question getQuestionByNumber(int number) {
        return getQuizz().getQuestionByIndex(number - 1);
    }

    public String getQuizzName() {
        return getQuizz().getName();
    }

    public int getNumberOfQuestions() {
        return numQuestionsInQuizz;
    }

    public int getTotalAnswers() {
        return this.numCorrectAnswers + this.numWrongAnswers;
    }

    public boolean hasNextQuestion() {
        return (this.questionNoIndex + 1) + 1 <= this.numQuestionsInQuizz;
    }

    public boolean isAllAnswersCorrect() {
        return this.numCorrectAnswers == this.numQuestionsInQuizz;
    }

    public boolean hasStarted(){
        return hasStarted;
    }
    public void setHasStarted(boolean val){
        this.hasStarted = val;
    }

    public int getNumCorrectAnswers() {
        return this.numCorrectAnswers;
    }

    public int getNumWrongAnswers() {
        return this.numWrongAnswers;
    }

    public long getTimestampStartedQuizzSession() {
        return this.timestampStartedQuizzSession;
    }

    public int getIndexQuizz() {
        return this.indexQuizz;
    }

    public void setCurrentQuestionNumber(int number) {
        this.questionNoIndex = number - 1;
    }


    public void setFirstQuestion(boolean firstQuestion) {
        this.firstQuestion = firstQuestion;
    }

    public void setNumCorrectAnswers(int numCorrectAnswers) {
        this.numCorrectAnswers = numCorrectAnswers;
    }

    public boolean isContinuedQuizz() {
        return isContinuedQuizz;
    }

    public void setContinuedQuizz(boolean continuedQuizz) {
        isContinuedQuizz = continuedQuizz;
    }

    public long getId() {
        return id;
    }

    public int getRemainingTimeCurrQuestion() {
        return remainingTimeCurrQuestion;
    }

    public void setRemainingTimeCurrQuestion(int remainingTimeCurrQuestion) {
        this.remainingTimeCurrQuestion = remainingTimeCurrQuestion;
    }

    public void setNumWrongAnswers(int numWrongAnswers) {
        this.numWrongAnswers = numWrongAnswers;
    }

    public int getPercentageSelected() {
        return percentageSelected;
    }

    public int getCurrentQuestionNo() {
        return this.questionNoIndex + 1;
    }

    public void setPercentageSelected(int percentageSelected) {
        this.percentageSelected = percentageSelected;
    }

    public Question nextQuestion() {
        this.answerSet = false;
        if (this.firstQuestion) {
            this.firstQuestion = false;
            return getQuizz().getQuestionByIndex(this.questionNoIndex);
        }
        Quiz quiz = getQuizz();
        int i = this.questionNoIndex + 1;
        this.questionNoIndex = i;
        return quiz.getQuestionByIndex(i);
    }

    public void startQuizzSession() {
        hasStarted = true;
        this.timestampStartedQuizzSession = System.currentTimeMillis();
    }

    public void pauseQuizzSession(){
        if(hasStarted){
            long timestampPaused = System.currentTimeMillis();
            timeUsedForQuizzSession += (timestampPaused - timestampStartedQuizzSession );
        }

    }

    public long getTimeUsedMillis(){
        long timeUsed = timeUsedForQuizzSession + totalTimeBeforeCurrentSession;
        return timeUsedForQuizzSession + totalTimeBeforeCurrentSession;
    }

    public int getTimeUsedSeconds(){
        return (int) (getTimeUsedMillis() / 1000);
    }

    public void setTimeUsed(long millis){
        this.totalTimeBeforeCurrentSession = millis;
    }

    public void setTotalTimeBeforeCurrentSession(long totalTimeBeforeCurrentSession) {
        this.totalTimeBeforeCurrentSession = totalTimeBeforeCurrentSession;
    }

    public Question getCurrentQuestion() {
        return getQuizz().getQuestionByIndex(this.questionNoIndex);
    }





    public static ArrayList<QuizStat> getQuizzesTaken() {
        //For å unngå at den retuenerte listen kan bli endret, vil hvert element i listen bli klonet
        ArrayList<QuizStat> clonedListQuizzesTaken = new ArrayList();
        Iterator it = listQuizzesTaken.iterator();
        while (it.hasNext()) {
            try {
                clonedListQuizzesTaken.add(((QuizStat) it.next()).clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return clonedListQuizzesTaken;
    }

    public static ArrayList<QuizStat> getFinishedAndCurrQuizzstats() {
        ArrayList<QuizStat> quizzesTaken = getQuizzesTaken();
        quizzesTaken.add(QuizStat.getSavedQuizz()); //En aktiv quizz vil bli lagret i starten av aktiviteten
        return quizzesTaken;
    }

    public static void addFinishedQuizzIfNotExists(QuizStat finishedQuizz) {
        //En quizz-stat med denne id-en finnes allerede

        if(listQuizzesTaken.indexOf(finishedQuizz) == - 1)
            listQuizzesTaken.add(finishedQuizz);
    }

    public static boolean doesQUizzStatExist(long id){
        return Stream.of(listQuizzesTaken).anyMatch(quizzStat -> quizzStat.getId() == id );
    }

    public static void removeFinishedQuizzes(){
        listQuizzesTaken.clear();
    }

    public static void addQuizzStatToListIfNotExists(QuizStat quizStat){
        if(listQuizStat.indexOf(quizStat) == -1 )
            listQuizStat.add(quizStat);
    }

    public static QuizStat getSavedQuizz() {
        return savedQuizz;
    }

    public static void setSavedQuizz(QuizStat savedQuizz) {
        savedQuizz.isContinuedQuizz = true;
        QuizStat.savedQuizz = savedQuizz;
    }

    public static void removeSavedQuizz() {
        savedQuizz = null;
    }

    public static boolean hasSavedQuizz() {
        return savedQuizz != null;
    }

    public static QuizStat getQuizzStatById(long id, DataManager.QUIZ_TYPE QUIZ_type){
        if(listQuizStat.size() == 0)
            return null;

        Stream<QuizStat> stream = Stream.of(listQuizStat).filter(quizzStat -> {
            boolean correctId =  quizzStat.id == id;
            boolean ofCorrectType = QUIZ_type == DataManager.QUIZ_TYPE.FINISHED ? quizzStat.isQuizzFinished():!quizzStat.isQuizzFinished();
            return correctId && ofCorrectType;
        }
        );

        return stream.findFirst().orElse(null);
    }



    //================
    //====PARCEL(generert av Android Parcable Code Generator)======
    //================

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.answerSet ? (byte) 1 : (byte) 0);
        dest.writeInt(this.finalTimeUsedSeconds);
        dest.writeByte(this.firstQuestion ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isContinuedQuizz ? (byte) 1 : (byte) 0);
        dest.writeInt(this.indexQuizz);
        dest.writeInt(this.numCorrectAnswers);
        dest.writeInt(this.numQuestionsInQuizz);
        dest.writeInt(this.numWrongAnswers);
        dest.writeInt(this.questionNoIndex);
        dest.writeByte(this.quizzFinished ? (byte) 1 : (byte) 0);
        dest.writeInt(this.remainingTimeCurrQuestion);
        dest.writeLong(this.id);
        dest.writeLong(this.timestampStartedQuizzSession);
        dest.writeLong(this.totalTimeBeforeCurrentSession);
        dest.writeLong(this.timeUsedForQuizzSession);
        dest.writeInt(this.percentageSelected);


    }

    protected QuizStat(Parcel in) {
        this.answerSet = in.readByte() != 0;
        this.finalTimeUsedSeconds = in.readInt();
        this.firstQuestion = in.readByte() != 0;
        this.isContinuedQuizz = in.readByte() != 0;
        this.indexQuizz = in.readInt();
        this.numCorrectAnswers = in.readInt();
        this.numQuestionsInQuizz = in.readInt();
        this.numWrongAnswers = in.readInt();
        this.questionNoIndex = in.readInt();
        this.quizzFinished = in.readByte() != 0;
        this.remainingTimeCurrQuestion = in.readInt();
        this.id = in.readLong();
        this.timestampStartedQuizzSession = in.readLong();
        this.totalTimeBeforeCurrentSession = in.readLong();
        this.timeUsedForQuizzSession = in.readLong();
        this.percentageSelected = in.readInt();
        listQuizStat.add(this);
    }

    public static final Creator<QuizStat> CREATOR = new Creator<QuizStat>() {
        @Override
        public QuizStat createFromParcel(Parcel source) {
            return new QuizStat(source);
        }

        @Override
        public QuizStat[] newArray(int size) {
            return new QuizStat[size];
        }
    };

}
