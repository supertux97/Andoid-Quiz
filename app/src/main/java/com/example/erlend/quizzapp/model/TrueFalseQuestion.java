package com.example.erlend.quizzapp.model;

import android.os.Parcel;
import android.os.Parcelable;

public class TrueFalseQuestion extends Question<Boolean>  {
    public final boolean correctAnswer;


    public TrueFalseQuestion(String key, String strQuestion, Category category, String urlImage, boolean isGeneral, boolean answer) {
        super(key, strQuestion, category, isGeneral, urlImage);
        this.correctAnswer = answer;
    }

    protected TrueFalseQuestion(Parcel parcel){
        super(parcel);
        this.correctAnswer = (boolean) parcel.readValue(Boolean.class.getClassLoader());
    }


    @Override
    public boolean checkAnswer(Boolean answer) {
        return answer == correctAnswer;
    }



    //============
    //===PACEL====
    //============
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeValue(correctAnswer);
    }

    //https://stackoverflow.com/questions/17725821/how-to-extend-android-class-which-implements-parcelable-interface
    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){

        @Override
        public Object createFromParcel(Parcel source) {
            return new TrueFalseQuestion(source);
        }

        @Override
        public TrueFalseQuestion[] newArray(int size) {
            return new TrueFalseQuestion[size];
        }
    };
}
