package com.example.erlend.quizzapp.model;

public class Xp {

    private static int xp = 0;
    private static int level = 1;
    private static int xpPrLevel = 1000;
    public static final int XP_CORRECT_QUESTION = 20;
    public static final int XP_WRONG_QUESTION = 20;


    private Xp(){};


    public static void setData(int xp, int level){
        Xp.xp = xp;
        Xp.level = level;
    }

    /**
    @return: whether the increment resulted in an level, up**/
    public static boolean addXp(int xpToAdd){
        if(xp + xpToAdd > xpPrLevel){
            level += xpToAdd / xpPrLevel;
            xp = xpToAdd % xpPrLevel;
            return true;
        }
        else{
            xp += xpToAdd;
            return false;
        }
    }

    public static boolean correctAnswer(){
        return addXp(XP_CORRECT_QUESTION);
    }

    public static void wrongAnswer(){
        removeXp(XP_WRONG_QUESTION);
    }

    //Fjerning av xp vil aldri resultere i at nivået går ned. I vørste fall kan xp-nivået bli 0.
    public static void removeXp(int amount){
        xp -= amount;
        if(xp < 0 ) {
            xp = 0;
        }
    }

    public static int getXpPrLevel() {
        return xpPrLevel;
    }

    public static int getXp() {
        return xp;
    }

    public static int getLevel() {
        return level;
    }

    public static void reset(){
        xp = 0;
        level = 0;
    }
}