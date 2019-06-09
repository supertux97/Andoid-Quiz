package com.example.erlend.quizzapp.util;


import com.example.erlend.quizzapp.util.Callbackmethod;

//Beskriver et element som tilbyr funksjonalitet knyttet til aktivering/deaktivering av elementet og ulike markeringene
public interface InputControl {
     void disableInputs();
     void enableInputs();
     void onCreatedView(Callbackmethod callback);
     void markQuestionSelection(); //Markerer det valgte alternativet
     void markQuestionCorrectness(); //Markerer det valgte alternativet slik at det kommer frem om riktig ble valgt.
     void markCorrectQuestion();
}
