package com.example.erlend.quizzapp.util;

import com.annimon.stream.Stream;
import com.example.erlend.quizzapp.model.QuizStat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//En klasse som genererer statistikk fra listen med gjennomførte quizer. Her benyttes stremas hyppig, for å gi
//kompakt og lett leselig kode
public class StatTools {

    private static ArrayList<QuizStat> listQuizzData;


    private StatTools() {}


    /*Blir kalt på hver gang man mistenker at ny data kan ha blitt lagt til. Quizdataen blir kunn hentet ut en enkelt gang og ikke for hver enkelt linje med statistikk
    for å unngå unødvendig bruk av ressursene.*/
    public static void retrieveQuizzData(){
        listQuizzData = QuizStat.getQuizzesTaken();
    }


    //==SPØRSMÅL==
    public static int getNumberOfQuestions(){
        return Stream.of(listQuizzData).mapToInt(QuizStat::getTotalAnswers).sum();
    }

    public static int getNumberOfCorrectAnswers(){

        return Stream.of(listQuizzData).mapToInt(QuizStat::getNumCorrectAnswers).sum();
    }

    public static int getNumberofWrongAnswers(){
        return Stream.of(listQuizzData).mapToInt(QuizStat::getNumWrongAnswers).sum();
    }


    //==QUIZER==
    public static boolean doesAnyQuizesHasAllCorrect(){
        return Stream.of(listQuizzData).anyMatch(QuizStat::isAllAnswersCorrect);
    }

    public static int getnumberOfQuizzesTaken(){
        return listQuizzData.size();
    }

    public static int getNumberOfQuizzesWithAllCorrect(){
        return (int) Stream.of(listQuizzData).filter(QuizStat::isAllAnswersCorrect).count();
    }

    public static TimeContainer getTotalTimeUsage(){
        return  secondsToTime( Stream.of(listQuizzData).mapToInt(QuizStat::getTimeUsedSeconds).sum() );
    }


    //==KATEGORIER==
    public static Map<String, CorrectWrongCont> getCategoryStats(){

        //https://www.javacodegeeks.com/2015/11/java-8-streams-api-grouping-partitioning-stream.html
        Map<String, CorrectWrongCont> categoryStats = new HashMap<>();

        for(QuizStat quizStat : listQuizzData){
            CorrectWrongCont correctWrongCont = categoryStats.get(quizStat.getCategoryName());
            if(correctWrongCont == null)
                correctWrongCont = new CorrectWrongCont();
            categoryStats.put(quizStat.getCategoryName(), correctWrongCont);
            correctWrongCont.correct+= quizStat.getNumCorrectAnswers();
            correctWrongCont.wrong += quizStat.getNumWrongAnswers();
        }

        return categoryStats;
    }

    //Finner den kategoriern med flest eller færrest riktige
    public static KeyValCont getCategoryMostOrLeastCorrect(boolean mostCorrect){

        ArrayList<KeyValCont> listCategoryCont = new ArrayList<>();

        //https://stackoverflow.com/questions/46898/how-to-efficiently-iterate-over-each-entry-in-a-map

        //Går gjennom hver oppføring fra resultatet fra getCategorystat og regner ut prosentandelen for riktige/feil spørsmål
        // EKS -> key: musikk, value: {correct: 1, wrong: 5}
        for(Map.Entry<String, CorrectWrongCont> entry: getCategoryStats().entrySet()){
            int correctnessPercentage = (int) ( ((float) entry.getValue().correct) / (((float) entry.getValue().correct) + ((float) entry.getValue().wrong)) * 100 );
            listCategoryCont.add(new KeyValCont(entry.getKey(), correctnessPercentage));
        }

        //Sorterer i stigende eller synkende rekkefølge ahneiggig av om det er kategorien med flest eller færrest riktige
        return Stream.of(listCategoryCont).max( (categoryCont1, categoryCont2) -> {
            if(mostCorrect)
                return categoryCont1.value - categoryCont2.value;
            else
                return categoryCont2.value - categoryCont1.value;
        }).get();
    }

    public static KeyValCont getCategoryWithMostQuizzesTaken(){
        //Grupperer listen over gjennomførte quizer på kategorinavnet og henter ut den med flest oppføringer (vil havne som element 0).
        Stream stream = Stream.of(listQuizzData).chunkBy(QuizStat::getCategoryName).max( (array1, array2) -> array1.size() - array2.size()).stream();
        ArrayList arrayList = (ArrayList) stream.toList().get(0); //Den kategorien som er tatt flest ganger

        return new KeyValCont(( (QuizStat) arrayList.get(0)).getCategoryName(), arrayList.size());
    }


    public static class KeyValCont{
        public String key;
        public int value;

        public KeyValCont(){}

        public KeyValCont(String key, int value){
            this.key = key;
            this.value = value;
        }
    }



    public static class CorrectWrongCont{
        public  int correct;
        public  int wrong;
        public String name;

        public CorrectWrongCont(){}

        public CorrectWrongCont(int correct, int wrong, String name){
            this.correct = correct;
            this.wrong = wrong;
            this.name = name;
        }
    }


    public static TimeContainer secondsToTime(int seconds){
        final int secondsInHour = 3600;
        final int minutesInHour = 60;

        int h = seconds / secondsInHour;
        int m = (seconds % secondsInHour) / 60;
        int s = seconds - (secondsInHour * h) - (m*minutesInHour);
        return new TimeContainer(h, m, s);
    }

    //Hjelpeklase for å lagre tid
    public static class TimeContainer{
        public  final int hours;
        public  final int minutes;
        public  final int seconds;

        public TimeContainer(int hours, int minutes, int seconds){
            this.hours = hours;
            this.minutes = minutes;
            this.seconds = seconds;
        }


    }
}
