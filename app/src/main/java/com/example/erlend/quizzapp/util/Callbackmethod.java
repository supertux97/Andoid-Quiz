package com.example.erlend.quizzapp.util;

import java.io.Serializable;
//Et interface som inneholder en generisk metode som kan bli kjørt
//Benytter har serializable fordi det hadde blitt fryktelig mye overhead å ha måtte implementere Parcable og metodene og feltene det medfører
//på alle objektene som implementerer denne interfacen(som i dette prosjektet er en god del)
public interface Callbackmethod<T> extends Serializable {
    void callback(T t);
}
