package com.example.erlend.quizzapp.util;

import android.graphics.Bitmap;

//Representerer en flis med informasjon. Blir brukt for kategorier og quizer i adapterern i QuickPickerActivity
public interface InformationTitle {
    int getColorInt();

    Bitmap getImage();

    String getTitle();
}
