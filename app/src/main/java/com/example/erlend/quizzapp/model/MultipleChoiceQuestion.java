package com.example.erlend.quizzapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Iterator;

public class MultipleChoiceQuestion extends Question<String> {
    private ChoiceContainer choiceContainer;


    public MultipleChoiceQuestion(String key, String strQuestion, Category category, String image, boolean isgeneral, ChoiceContainer choiceContainer) {
        super(key, strQuestion, category, isgeneral, image);
        this.choiceContainer = choiceContainer;
    }


    @Override
    public boolean checkAnswer(String strAnswer) {
        return strAnswer.equals(getCorrectChoice());
    }

    //==GETTERE OG SETTERE==
    private int getCorrectAnswerIndex() {
        return this.choiceContainer.getChoices().indexOf(getCorrectChoice());
    }

    public ArrayList<String> getAlternatives() {
        return this.choiceContainer.getChoices();
    }

    public String getCorrectChoice() {
        if (this.choiceContainer.correctChoice != null) {
            return this.choiceContainer.correctChoice;
        }
        throw new IllegalStateException("No correct Choice set for question: " + this.strQuestion);
    }




    //================
    //====PARCEL======
    //================
    public MultipleChoiceQuestion(Parcel parcel){
        super(parcel);
        this.choiceContainer = parcel.readParcelable(ChoiceContainer.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeParcelable(choiceContainer, flags);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){

        @Override
        public MultipleChoiceQuestion createFromParcel(Parcel source) {
            return new MultipleChoiceQuestion(source);
        }

        @Override
        public MultipleChoiceQuestion[] newArray(int size) {
            return new MultipleChoiceQuestion[size];
        }
    };



    //En hjlpeklasse for å lagre alternativene
    public static class ChoiceContainer implements Parcelable{
        private  ArrayList<String> arrayChoices = new ArrayList();
        private String correctChoice;

        public ChoiceContainer(ArrayList<String> choices) {
            Iterator it = choices.iterator();
            while (it.hasNext()) {
                this.arrayChoices.add((String) it.next());
            }
        }

        //==GETTERE OG SETTERE==
        public ArrayList<String> getChoices() {
            return (ArrayList) this.arrayChoices.clone();
        }

        //Denne egenskapen blir satt i ChoiceContainer og ikke i choice for å unngå at flere choice in en choicekontainer er satt til det riktige
        // og også for å kunne gjenbruke alternativene
        public void setCorrectChoice(String correctChoice) {
            this.correctChoice = correctChoice;
        }



        //================
        //====PARCEL======
        //================
        public ChoiceContainer(Parcel parcel){
            this.arrayChoices = parcel.readArrayList(ArrayList.class.getClassLoader());
            this.correctChoice = parcel.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeStringList(arrayChoices);
            dest.writeString(correctChoice);
        }


        public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){

            @Override
            public Object createFromParcel(Parcel source) {
                return new ChoiceContainer(source);
            }

            @Override
            public ChoiceContainer[] newArray(int size) {
                return new ChoiceContainer[size];
            }
        };


    }
}
