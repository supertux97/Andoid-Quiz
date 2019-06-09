package com.example.erlend.quizzapp.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.example.erlend.quizzapp.DataManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


//Håndterer trofeer
//Tofeene er harkodet i Java fordi det ville vært utrordrende å lagre logikk i firebase. Eventuelt kunne man ha lastet opp og ned tofeene til Java.objekter,
//men dette ble det ikke tid til
public class TrophyManager {

    private TrophyManager(){};

    private static ArrayList<Trophy> listTrophies = new ArrayList<>();

    //==TROFEENE==
    public static void initTrophies(){

        //==Antall quizzer fullført==
        listTrophies.add(new Trophy("101", "Fullføre 3 quizer", "En start", 200, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).filter(QuizStat::isQuizzFinished).count() == 3;
            }
        }));
        listTrophies.add(new Trophy("102", "Fullfør 5 quizer", "Nybegynner", 200, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).filter(QuizStat::isQuizzFinished).count() == 5;
            }
        }));
        listTrophies.add(new Trophy("103", "Fullfør 10 quizer", "Vidrekommen", 250, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).filter(QuizStat::isQuizzFinished).count() == 10;
            }
        }));
        listTrophies.add(new Trophy("104", "Fullfør 25 quizer", "Quizzninja", 300, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).filter(QuizStat::isQuizzFinished).count() == 25;
            }
        }));
        listTrophies.add(new Trophy("105", "Fullfør 50 quizer", "Quizzguru", 500, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).filter(QuizStat::isQuizzFinished).count() == 50;
            }
        }));
        listTrophies.add(new Trophy("106", "Fullfør 100 quizer", "Quizzmaster", 2500, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).filter(QuizStat::isQuizzFinished).count() == 100;
            }
        }));

        //==Antall spørsmål==
        listTrophies.add(new Trophy("107", "Svar på 2 spørsmål", "Quiz Jr", 500, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).mapToInt(QuizStat::getTotalAnswers).sum() == 2;
            }
        }));
        listTrophies.add(new Trophy("108", "Svar på 50 spørsmål", "Quizzentusiast", 500, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).mapToInt(QuizStat::getTotalAnswers).sum() == 50;
            }
        }));
        listTrophies.add(new Trophy("109", "Besvar 100 spørsmål", "Quizzhekta", 2500, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).filter(QuizStat::isQuizzFinished).count() == 100;
            }
        }));



        //==Antall quizzer med alt riktig==
        listTrophies.add(new Trophy("110", "En quiz med alt riktig", "Perfekt start", 300, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).filter(QuizStat::isAllAnswersCorrect).count() == 1;
            }
        }));
        listTrophies.add(new Trophy("111", "3 quizer med alt riktig", "Perfekt fortsettelse", 500, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).filter(QuizStat::isAllAnswersCorrect).count() == 3;
            }
        }));
        listTrophies.add(new Trophy("112", "10 quizer med alt riktig", "Kunnskapsfreak", 250, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).filter(QuizStat::isAllAnswersCorrect).count() == 10;
            }
        }));
        listTrophies.add(new Trophy("113", "25 quizer med alt riktig", "Altviter", 300, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).filter(QuizStat::isAllAnswersCorrect).count() == 25;
            }
        }));
        listTrophies.add(new Trophy("114", "50 quizer med alt riktig", "Leksikon", 500, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).filter(QuizStat::isAllAnswersCorrect).count() == 50;
            }
        }));
        listTrophies.add(new Trophy("116", "Fulført quizer fra 8 ulike kategorier", "Eksprimenterer", 500, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                Map<String, Long> quizzesPrCategory = Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).collect(Collectors.groupingBy(QuizStat::getCategoryName, Collectors.counting()));
                return quizzesPrCategory.entrySet().size() == 8;
            }
        }));


            //==Quizzer av ulik kategori==
        listTrophies.add(new Trophy("115", "Fulført quizer fra 3 ulike kategorier", "Potet", 300, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                //https://www.mkyong.com/java8/java-8-collectors-groupingby-and-mapping-example/
                Map<String, Long> quizzesPrCategory = Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).collect(Collectors.groupingBy(QuizStat::getCategoryName, Collectors.counting()));
                return quizzesPrCategory.entrySet().size() == 3;
            }
        }));

        listTrophies.add(new Trophy("117", "Fulført quizer fra 10 ulike kategorier", "Jack of all trades", 1250, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                Map<String, Long> quizzesPrCategory = Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).collect(Collectors.groupingBy(QuizStat::getCategoryName, Collectors.counting()));
                return quizzesPrCategory.entrySet().size() == 10;
            }
        }));

        //==Quizzer med allmennspørsmål==
        listTrophies.add(new Trophy("118", "Fullført 3 quizer med allmennspørsmål", "Allmennkunnskap", 500, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                Map<String, Long> quizzesPrCategory = Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).collect(Collectors.groupingBy(QuizStat::getCategoryName, Collectors.counting()));
                return quizzesPrCategory.entrySet().size() == 8;
            }
        }));
        listTrophies.add(new Trophy("119", "Fullført 10 quizer med allmennspørsmål", "Litt av alt", 1250, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                Map<String, Long> quizzesPrCategory = Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).collect(Collectors.groupingBy(QuizStat::getCategoryName, Collectors.counting()));
                return quizzesPrCategory.entrySet().size() == 10;
            }
        }));

        //==Spesifikt for ulike kategorier
        //Data
        listTrophies.add(new Trophy("120", "Fullført 3 quizzer innenfor data", "Datanerd", 500, new TrophyLogic() {
                @Override
                public boolean tryUnlock() {
                    return Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).filter(quizzStat -> quizzStat.getCategoryName().equals("Data")).count() == 3;
                }
        }));
        listTrophies.add(new Trophy("121", "Fullført 10 quizzer innenfor data", "Dataavhengig", 500, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).filter(quizzStat -> quizzStat.getCategoryName().equals("Data")).count() == 10;
            }


        }));
        listTrophies.add(new Trophy("122", "30 riktige sprsmål i Data", "DataPro", 1250, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats())
                        .filter(quizzStat -> quizzStat.getCategoryName().equals("Data"))
                        .mapToInt(QuizStat::getNumCorrectAnswers)
                        .sum() == 30;
            }
        }));
        listTrophies.add(new Trophy("123", "100 riktige sprsmål i Data", "Hacker", 1250, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats())
                        .filter(quizzStat -> quizzStat.getCategoryName().equals("Data"))
                        .mapToInt(QuizStat::getNumCorrectAnswers)
                        .sum() == 50;
            }
        }));

        //Musikk
        listTrophies.add(new Trophy("124", "Fullført 3 quizzer innenfor musikk", "Musikkelsker", 500, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).filter(quizzStat -> quizzStat.getCategoryName().equals("Musikk")).count() == 3;
            }
        }));
        listTrophies.add(new Trophy("125", "Fullført 10 quizzer innenfor musikk", "Platespiller", 500, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).filter(quizzStat -> quizzStat.getCategoryName().equals("Musikk")).count() == 10;
            }


        }));
        listTrophies.add(new Trophy("126", "30 riktige sprsmål i Musikk", "Musikkleksikon", 1250, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats())
                        .filter(quizzStat -> quizzStat.getCategoryName().equals("Musikk"))
                        .mapToInt(QuizStat::getNumCorrectAnswers)
                        .sum() == 30;
            }
        }));
        listTrophies.add(new Trophy("127", "100 riktige sprsmål innenfor musikk", "Quizenes rockestjerne", 1250, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats())
                        .filter(quizzStat -> quizzStat.getCategoryName().equals("Musikk"))
                        .mapToInt(QuizStat::getNumCorrectAnswers)
                        .sum() == 50;
            }
        }));

        //Geografi
        listTrophies.add(new Trophy("128", "Fullført 3 quizzer innenfor geografi", "Kart og kompass", 500, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).filter(quizzStat -> quizzStat.getCategoryName().equals("Geografi")).count() == 3;
            }
        }));
        listTrophies.add(new Trophy("129", "Fullført 10 quizzer innenfor geografi", "Globus", 500, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats()).filter(quizzStat -> quizzStat.getCategoryName().equals("Geografi")).count() == 10;
            }


        }));
        listTrophies.add(new Trophy ("130", "30 riktige sprsmål i geografi", "Verdenskjent", 1250, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats())
                        .filter(quizzStat -> quizzStat.getCategoryName().equals("Geografi"))
                        .mapToInt(QuizStat::getNumCorrectAnswers)
                        .sum() == 30;
            }
        }));
        listTrophies.add(new Trophy("131", "100 riktige sprsmål i geografi", "Kjenner verden som sin egen bukselumme", 1250, new TrophyLogic() {
            @Override
            public boolean tryUnlock() {
                return Stream.of(QuizStat.getFinishedAndCurrQuizzstats())
                        .filter(quizzStat -> quizzStat.getCategoryName().equals("Geografi"))
                        .mapToInt(QuizStat::getNumCorrectAnswers)
                        .sum() == 50;
            }
        }));

    }


    public static boolean hasUnclockedTrophy(){
        return false;
    }

    public static List<Trophy> getUnlockedTrophies(){
        return Stream.of(listTrophies).filter(Trophy::isUnlocked).toList();
    }

    public static ArrayList<Trophy> getListTrophies(){
        return listTrophies;
    }

    public static void registerUnlockedTrophyById(String id){
        Stream.of(listTrophies).forEach(trophy -> {
            if(trophy.id.equals(id)){
                listTrophies.add(trophy);
            }
        });
    }

    public static Trophy getTrophyById(String targetId){
        return Stream.of(listTrophies).filter(trophy -> trophy.getId().equals(targetId)).findFirst().orElse(null);
    }

    //Har egen metode for dette fordi denne metoden bør bli kalt på så sjeldent som mulig siden den er ytelseskrevende
    //Kjøres i egen tråd for å ikke ta opp eventuelle andre tråder
    public static void checkForUnlocked(Context context){

        DataManager.requestFinishedQuizzes(context);
        new Thread(new Runnable() {
            @Override
            public void run() {
                long timeStart = System.currentTimeMillis();

                for(Trophy trophy: listTrophies){
                    if(!trophy.isUnlocked){
                        trophy.test();
                    }

                }
                long timeEnd = System.currentTimeMillis();

            }

        }).run();

    }

    public static void loadTakenTrophiesBySeperatedString(String str, String seperator){
        List<String> listIdTrophies = Arrays.asList(str.split(seperator));
        for(String id: listIdTrophies){
            if(!id.equals("")){
                Trophy trophy = getTrophyById(id);
                if(trophy != null){
                    trophy.setPresented();
                    trophy.setUnlocked(true);
                }
            }

        }
    }

    public static List<Trophy> getUnlockedUnpresentedTrophies(){
        return Stream.of(listTrophies).filter(trophy -> !trophy.isPresented && trophy.isUnlocked).toList();
    }

    //Jeg fant ingen metoder for å gjøre en interface parcable uten å måtte gjøre det i hver enkelt klase som implementerer det
    private interface TrophyLogic extends Serializable{

        /**
         *
         * @return om trofeet ble opplåst
         */
        boolean tryUnlock();

    }

    public static String generateStrTakenTrophiesId(){
        return TextUtils.join(
                        DataManager.STANDARD_DELIMITER,
                        Stream.of(getUnlockedTrophies()).map(Trophy::getId).toArray());
    }

    public static void saveTakenTrophiesPersistent(Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences(DataManager.KEY_SHARED_PREFERENCES, 0).edit();
        editor.putString(DataManager.KEY_PREFS_TAKEN_TROPHIES, generateStrTakenTrophiesId());
        editor.apply();
    }



    public static class Trophy implements Parcelable, Comparable<Trophy> {

        private String id; //Brukt ved persistent lagring av hvilke quizzer som er fullførte
        private String desctriotion;
        private String title;
        private int gainedXP;
        private TrophyLogic trophyLogic;
        private boolean isUnlocked = false;
        private boolean isPresented = false; //Om opplåsning av trofeet er vist grafisk for bruker

        public Trophy(String id, String description, String title, int gainedXP, TrophyLogic trophyLogic) {
            this.desctriotion = description;
            this.title = title;
            this.gainedXP = gainedXP;
            this.trophyLogic = trophyLogic;
            this.id = id;

        }

        public boolean test(){
            isUnlocked = trophyLogic.tryUnlock();
            return isUnlocked;
        }

        //==GETTERE OG SETTERE==
        public String getDescription() {
            return desctriotion;
        }

        public String getTitle() {
            return title;
        }

        public int getGainedXP() {
            return gainedXP;
        }


        public boolean isPresented(){
            return isPresented;
        }

        public boolean isUnlocked(){
            return isUnlocked;
        }

        public void setPresented(){
            isPresented = true;
        }

        public void setUnlocked(boolean unlocked){
            isUnlocked = unlocked;
        }

        public String getId(){
            return id;
        }



        //================
        //=======PARCEL===
        //================
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.id);
            dest.writeString(this.desctriotion);
            dest.writeString(this.title);
            dest.writeInt(this.gainedXP);
            dest.writeSerializable(this.trophyLogic);
            dest.writeByte(this.isUnlocked ? (byte) 1 : (byte) 0);
            dest.writeByte(this.isPresented ? (byte) 1 : (byte) 0);
        }

        protected Trophy(Parcel in) {
            this.id = in.readString();
            this.desctriotion = in.readString();
            this.title = in.readString();
            this.gainedXP = in.readInt();
            this.trophyLogic = (TrophyLogic) in.readSerializable();
            this.isUnlocked = in.readByte() != 0;
            this.isPresented = in.readByte() != 0;
        }

        public static final Parcelable.Creator<Trophy> CREATOR = new Parcelable.Creator<Trophy>() {
            @Override
            public Trophy createFromParcel(Parcel source) {
                return new Trophy(source);
            }

            @Override
            public Trophy[] newArray(int size) {
                return new Trophy[size];
            }
        };

        //Legger de ikke-fllførte trofeene bakerst i lista slik at de vil bli vist sist i trofevisningen
        @Override
        public int compareTo(@NonNull Trophy other) {
            if(other.isUnlocked && this.isUnlocked){
                return 0;
            }
            else if(other.isUnlocked && !this.isUnlocked){
                return 1;
            }
            else {
                return -1;
            }

        }
    }


}
