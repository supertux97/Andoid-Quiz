package com.example.erlend.quizzapp.model;

import android.os.Parcel;
import android.os.Parcelable;

public class PercentageQuestion extends Question<Integer> {
    private final int correctAnswer;


    public PercentageQuestion(String key, String strQuestion, Category category, int correctAnswer, String image, boolean isGeneral) {
        super(key, strQuestion, category, isGeneral, image);
        if (correctAnswer < 0 || correctAnswer > 100) {
            throw new IllegalArgumentException("The correct percentage have to bee from 0-100 got: " + correctAnswer);
        }
        this.correctAnswer = correctAnswer;
    }


    //==GETTERE OG SETTERE==
    @Override
    public boolean checkAnswer(Integer givenAnswer) {
        return givenAnswer == correctAnswer;
    }
    public int getCorrectAnswer(){
        return correctAnswer;
    }


    //================
    //====PARCEL======
    //================

    public PercentageQuestion(Parcel parcel){
        super(parcel);
        correctAnswer = parcel.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeInt(correctAnswer);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){

        @Override
        public PercentageQuestion createFromParcel(Parcel source) {
            return new PercentageQuestion(source);
        }

        @Override
        public PercentageQuestion[] newArray(int size) {
            return new PercentageQuestion[size];
        }
    };



}
