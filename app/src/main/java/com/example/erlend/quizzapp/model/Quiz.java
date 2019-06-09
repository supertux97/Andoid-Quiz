package com.example.erlend.quizzapp.model;

import android.graphics.Bitmap;

import com.annimon.stream.Stream;
import com.example.erlend.quizzapp.util.InformationTitle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class Quiz implements Cloneable, InformationTitle, Serializable {
    public static final String KEY_GENERAL = "KEY_GENERAL";
    public static final String NAME_GENERAL = "NAME_GENERAL";
    public static final int NUM_QUESTIONS_FOR_GENERAL_QUIZZ = 3;

    private static Quiz generalQuiz;
    private static ArrayList<Quiz> listQuizs = new ArrayList();

    private final String key;
    private final String categoryKey;
    private final String name;
    private Category category;
    private int expectedNumberOfQuestions;
    private boolean forKids;
    private ArrayList<Question> listQuestions = new ArrayList();

    private String questionKeysString;


    public Quiz(String id, String name, boolean forKids, String categoryKey) {
        this.key = id;
        this.name = name;
        this.forKids = forKids;
        this.categoryKey = categoryKey;
        listQuizs.add(this);
        if(!categoryKey.equals(Category.KEY_GENERAL)){
            retrieveCategory();
        }
    }


    @Override
    protected Quiz clone() throws CloneNotSupportedException {
        return (Quiz) super.clone();
    }

    @Override
    public String getTitle() {
        return this.name;
    }

    @Override
    public Bitmap getImage() {
        return this.category.getImage();
    }

    @Override
    public int getColorInt() {
        return 0;
        //return getCategory().getColorInt();
    }

    private void retrieveCategory() {
        Iterator it = Category.getListCategories().iterator();
        while (it.hasNext()) {
            Category category = (Category) it.next();
            if (category.getKey().equals(this.categoryKey)) {
                this.category = category;
            }
        }
    }

    public Question getQuestionByIndex(int no) {
        if (no > this.listQuestions.size() || no < 0) {
            return null;
        }
        return (Question) this.listQuestions.get(no);
    }



    public static Quiz getGeneralQuiz(){
        if(generalQuiz == null){
            generalQuiz = new Quiz(Quiz.KEY_GENERAL, Quiz.NAME_GENERAL, true, Category.getGeneralCategory().getKey());
            generalQuiz.setCategory(Category.getGeneralCategory());
        }
        return generalQuiz;
    }

    public ArrayList<Question> getListQuestions() {
        return listQuestions;
    }

    public static ArrayList<Quiz> getListQuizs() {
        return getListQuizzes(null);
    }

    public static ArrayList<Quiz> getListQuizzes(String categoryKey) {
        ArrayList<Quiz> clonedListQuizs = new ArrayList(listQuizs.size());
        Iterator it = listQuizs.iterator();
        while (it.hasNext()) {
            Quiz quiz = (Quiz) it.next();
            if (categoryKey == null || (categoryKey != null && quiz.categoryKey.equals(categoryKey))) {
                try {
                    clonedListQuizs.add(quiz.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }
        return clonedListQuizs;
    }

    public static int getIndexOfQuizz(Quiz quiz) {
        return listQuizs.indexOf(quiz);
    }

    public static Quiz getQuizzByKey(String tagetKey) {
        Iterator it = listQuizs.iterator();
        while (it.hasNext()) {
            Quiz quiz = (Quiz) it.next();
            if (quiz.key.equals(tagetKey)) {
                return quiz;
            }
        }
        return null;
    }


    //==GETTERE OG SETTERE==
    public boolean isGeneralQuizz(){
        return name.equals(Quiz.NAME_GENERAL);
    }

    public void addQuestionToQuizz(Question question) {
        this.listQuestions.add(question);
    }

    public void setCategory(Category category){
        this.category = category;
    }
    public Category getCategory() {
        return this.category;
    }
    public String getKey() {
        return this.key;
    }

    public String getCategoryKey() {
        return this.categoryKey;
    }

    public String getName() {
        return this.name;
    }

    public void setExpectedNumberOfQuestions(int number) {
        this.expectedNumberOfQuestions = number;
    }

    public int getExpectedNumberOfQuestions() {
        return this.expectedNumberOfQuestions;
    }




    public static boolean isQuizzDownloaded(String key){
        return Stream.of(listQuizs).anyMatch(quizz -> quizz.key.equals(key));
    }

    //For å lagre hvilke spørsmål en generall quizz inneholder til bruk for å lagre og fortsette en quizz med almenne spørsmål på tvers av restarter.
    //Dette må gjøres fordi en allmenn quizz ellers vil hente ut tilfeldige spørsmål.
    public static void setQuestionKeysStringForGeneralQuizz(String str){
        Quiz.getGeneralQuiz().questionKeysString = str;
    }

    public static String getQuestionStrForGeneralQuizz(){
        return Quiz.getGeneralQuiz().questionKeysString;
    }


    public static boolean isQuestionsForQuizzDownloaded(Quiz quiz, int numOfQuestions){
        return quiz.listQuestions.size() == numOfQuestions;
    }

    public static boolean isQuestionsForQuizzDownloaded(Quiz quiz){
        return quiz.listQuestions.size() == quiz.expectedNumberOfQuestions;
    }

    public static boolean isQuizzesForCategoryDownloaded(Category category){
        return category.getExpecedNumberOfQuizzes() == Quiz.getQuizzesByCategoryKey(category.getKey()).size();
    }

    public static ArrayList<Quiz> getQuizzesByCategoryKey(String categoryKey){
        return (ArrayList<Quiz>) Stream.of(listQuizs).filter(quizz -> quizz.getCategory().getKey().equals(categoryKey)).toList();
    }




}
