package com.example.erlend.quizzapp.util;

import android.app.IntentService;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

//Spiller en gitt musikkfil i bakgrunnen, på tvers av aktiviteter
public class BackgroundMusicPlayerService extends IntentService {

    public static final String KEY_MUSIC_ = "KEY_MUSIC_QUIZ_FINISHED";

    private MediaPlayer mediaPlayerMusic;
    private int currSongResId;

    private static BinderInterface binderInterfaceInstance;


    public BackgroundMusicPlayerService(){
        super("MusicPlayerService");
    }

    public BackgroundMusicPlayerService(String name) {
        super(name);
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //Blir ikke benyttet, BinderInterfacen blir brukt for kommunikasjon mellom aktivitetene i stedet
    }


    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        onHandleIntent(intent);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        mediaPlayerMusic.release();
        super.onDestroy();
    }



    //==KOMMUNIKASJON MED AKTIVITETENE==

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //Tillater kun én instanse av binder, for å unngå trøbbel knyttet til fler som er kjørende sammtidig og å gi bedre ytelse
        if(binderInterfaceInstance == null){
            binderInterfaceInstance = new BinderInterface();
        }
        return binderInterfaceInstance;
    }

    //For kommunisering med servicen utenfra
    public class BinderInterface extends Binder{

        public BackgroundMusicPlayerService getBoundService(){
            return BackgroundMusicPlayerService.this;
        }

        public void initMusicPlayer(int newResIdMusic){
            if(newResIdMusic != currSongResId){
                mediaPlayerMusic = MediaPlayer.create(getApplicationContext(), newResIdMusic);
                mediaPlayerMusic.setLooping(true);
                currSongResId = newResIdMusic;
            }

        }

        public void pauseMusic(){
            if(mediaPlayerMusic == null){
                throw new IllegalStateException("Music player is not initialized");
            }
            if(mediaPlayerMusic.isPlaying()){
                mediaPlayerMusic.pause();
            }
        }

        public void startMusic(){
            if(mediaPlayerMusic == null){
                throw new IllegalStateException("Music player is not initialized");
            }
            if(!mediaPlayerMusic.isPlaying()){
                mediaPlayerMusic.start();
            }
        }
    }
}
