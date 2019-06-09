package com.example.erlend.quizzapp.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.annimon.stream.Stream;
import com.example.erlend.quizzapp.util.Util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public abstract class Question<T> implements Parcelable, Cloneable {

    public static final String KEY_RANDOM = "random";
    protected static ArrayList<Question> listQuestions = new ArrayList<>();

    protected final Category category;
    protected final boolean isGeneral;
    protected final String strQuestion;
    protected final String urlImage;
    protected String key;
    protected Bitmap image;


    protected Question(String key, String strQuestion, Category category, boolean isGeneral, String image) {
        listQuestions.add(this);
        this.key = key;
        this.strQuestion = strQuestion;
        this.category = category;
        this.urlImage = image;
        this.isGeneral = isGeneral;
    }


    public abstract boolean checkAnswer(T t);


    public boolean downloadImageFromUrl() throws MalformedURLException {
        this.image = Util.getBitmapFromURL(new URL(urlImage));
        return this.urlImage != null;
    }

    @Override
    protected Question clone() throws CloneNotSupportedException {
        return (Question) super.clone();
    }


    //==GETTERE OG SETTERE==
    public static Question getQuestionByKey(String targetKey){
        return Stream.of(listQuestions).filter(quizz -> quizz.key.equals(targetKey)).findFirst().orElse(null);
    }

    public String getStrQuestion() {
        return this.strQuestion;
    }

    public Category getCategory() {
        return this.category;
    }

    public String getUrlImage() {
        return this.urlImage;
    }

    public boolean isGeneral() {
        return this.isGeneral;
    }

    public String getkey(){
        return key;
    }

    public static boolean isQuestionDownloaded(String key){
        return false;
       // return Stream.of(listQuestions).anyMatch(question -> question.key.equals(key));
    }

    public Bitmap getImage() {
        return this.image;
    }

    public boolean hasImage() {
        return this.image != null;
    }


    //================
    //====PARCEL======
    //================
    protected Question(Parcel in){
        this.category = in.readParcelable(Category.class.getClassLoader());
        this.key = in.readString();
        this.image = in.readParcelable(Bitmap.class.getClassLoader());
        this.isGeneral = (boolean) in.readValue(Boolean.class.getClassLoader());
        this.strQuestion = in.readString();
        this.urlImage = in.readString();
    }


    @Override
    public void writeToParcel(Parcel parcel, int flags){
        parcel.writeParcelable(category, flags);
        parcel.writeString(key);
        parcel.writeParcelable(image, flags);
        parcel.writeValue(isGeneral);
        parcel.writeString(strQuestion);
        parcel.writeString(urlImage);
    }

}
