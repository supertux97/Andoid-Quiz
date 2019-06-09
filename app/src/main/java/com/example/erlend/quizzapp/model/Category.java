package com.example.erlend.quizzapp.model;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import com.annimon.stream.Stream;
import com.example.erlend.quizzapp.util.Util;
import com.example.erlend.quizzapp.util.InformationTitle;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class Category implements Cloneable, InformationTitle, Parcelable {

    //En kategori er konstant og feltene kan derfor være final
    private static ArrayList<Category> listCategories = new ArrayList();
    public static final String CATEGORY_GENERAL = "RANDOM_QUESTIONS";
    public static final String KEY_GENERAL = "key_general";
    public static Category generalCategory;

    private final String key;
    private final String name;
    private final String strColor;
    private final String urlImage;
    private Bitmap image;

    int expecedNumberOfQuizzes;


    public Category(String name, String key, String urlImage, String color) {
        this.name = name;
        this.key = key;
        this.urlImage = urlImage;
        this.strColor = color;
        listCategories.add(this);
    }


    @Override
    public String toString() {
        return String.format("Name: %s Key: %s Color: %s Url: %s", this.name, this.key, this.strColor, this.urlImage);
    }

    @Override
    protected Category clone() throws CloneNotSupportedException {
        return (Category) super.clone();
    }

    @Override
    public String getTitle() {
        return this.name;
    }

    @Override
    public Bitmap getImage() {
        return this.image;
    }

    @Override
    public int getColorInt() {
        return Color.parseColor(this.strColor);
    }

    public boolean downloadImageFromUrl() throws MalformedURLException {
        this.image = Util.getBitmapFromURL(new URL(this.urlImage));
        return this.image != null;
    }




    public static Category getGeneralCategory(){
        if(generalCategory == null){
            generalCategory = new Category(Category.CATEGORY_GENERAL, KEY_GENERAL, null, "#ffffff");
            listCategories.remove(generalCategory);
        }
        return generalCategory;
    }

    public static ArrayList<Category> getListCategories() {
        ArrayList<Category> clonedListCategory = new ArrayList(listCategories.size());
        Iterator it = listCategories.iterator();
        while (it.hasNext()) {
            try {
                clonedListCategory.add(((Category) it.next()).clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return clonedListCategory;
    }

    public static ArrayList<InformationTitle> getListCategoriesInformationTitles() {
        //For å unngå at den retuenerte listen kan bli endret, vil hvert element i listen bli klonet
        ArrayList<InformationTitle> clonedListCategory = new ArrayList(listCategories.size());
        Iterator it = listCategories.iterator();
        while (it.hasNext()) {
            try {
                clonedListCategory.add(((Category) it.next()).clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return clonedListCategory;
    }


    public static Category getCategoryFromKey(String tagetKey) {
        Iterator it = listCategories.iterator();
        while (it.hasNext()) {
            Category category = (Category) it.next();
            if (category.key.equals(tagetKey)) {
                return category;
            }
        }

        return null;
    }




    //==GETTERE OG SETTERE==
    public static Category getCategoryByKey(String targetKey){
        return Stream.of(listCategories).filter(cat -> cat.key.equals(targetKey)).findFirst().orElse(null);
    }

    public String getName() {
        return this.name;
    }

    public String getKey() {
        return this.key;
    }

    public String getUrlImage() {
        return this.urlImage;
    }

    public String getStrColor() {
        return this.strColor;
    }

    public static boolean isCategoryDownloaded(String key){
        return listCategories.indexOf(getCategoryFromKey(key)) >= 0;
    }

    public int getExpecedNumberOfQuizzes() {
        return expecedNumberOfQuizzes;
    }

    public void setExpecedNumberOfQuizzes(int expecedNumberOfQuizzes) {
        this.expecedNumberOfQuizzes = expecedNumberOfQuizzes;
    }



    //=============
    //====PARCEL===
    ///============

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(image, flags);
        dest.writeString(key);
        dest.writeString(name);
        dest.writeString(strColor);
        dest.writeString(urlImage);
    }


    public Category(Parcel in){
        this.image = in.readParcelable(Bitmap.class.getClassLoader());
        this.key = in.readString();
        this.name = in.readString();
        this.strColor = in.readString();
        this.urlImage = in.readString();
        Category.listCategories.add(this);
    }


    public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){

        @Override
        public Object createFromParcel(Parcel source) {
            return new Category(source);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
}
