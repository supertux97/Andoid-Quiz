package com.example.erlend.quizzapp.util;

import com.example.erlend.quizzapp.DataManager.Type;

//Beksriver en type som har funksjonalitet knyttet til innlasting av en ressurs/gjennomføring av en oppgave
public interface LoadingProgressable<T> {
    void afterLoading(Type type, boolean displayAfterLoading);

    void beforeLoading(Type type, boolean displayLoadingVisually);

    void clearprogress();

    void setprogress(int i);
}
