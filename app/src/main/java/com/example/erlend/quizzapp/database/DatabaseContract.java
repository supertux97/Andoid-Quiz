package com.example.erlend.quizzapp.database;

import android.provider.BaseColumns;

import com.example.erlend.quizzapp.util.Util;

import static com.example.erlend.quizzapp.util.Util.Column;

//Holder styr på de ulike tabellene og kolonnene i SQLiteDatabasen. Ideen for dene strukturen med en indre klasse for hver tabelle ble ehntet fra Google sin
//tutorial for SqlLitedatabaser //https://developer.android.com/training/data-storage/sqlite.html
public class DatabaseContract {
    public static final String DATABASE_NAME = "APP_DATA.db";
    public static final int DATABASE_VERSION = 1;

    //Inneholder felles felter for Current og FInished-quiz
    private static abstract class QuizzBase implements BaseColumns {
        public static final String COLUMN_NAME_CATEGORY_KEY = "category_key";
        public static final String COLUMN_NAME_NUM_CORRECT_ANSWWERS = "correct_answers";
        public static final String COLUMN_NAME_NUM_WRONG_ANSWWERS = "wrong_answers";
        public static final String COLUMN_NAME_TIMESTAMP_QUIZZ_START = "timestamp_quizz_start";
    }

    public static class CurrentQuiz extends QuizzBase implements BaseColumns {
        public static final String TABLE_NAME = "CurrentQuiz";
        public static final String COLUMN_NAME_CURRENT_TIME_USED = "time_used";
        public static final String COLUMN_NAME_QUIZZ_KEY = "quiz_key";
        public static final String COLUMN_NAME_REMAINING_TIME_CURR_QUESTION = "remaining_time";
        public static final String COLUMN_NAME_HAS_STARTED = "has_started";
        public static final String COLUMN_NAME_QUESTION_KEYS_FOR_GENERAL = "question_keys_for_general";
        public static final String SQL_CREATE_TABLE =
                Util.generateSQLCreateTable(TABLE_NAME,
                        new Column(COLUMN_NAME_NUM_CORRECT_ANSWWERS, TYPE.INT),
                        new Column(COLUMN_NAME_NUM_WRONG_ANSWWERS, TYPE.INT),
                        new Column(COLUMN_NAME_CURRENT_TIME_USED, TYPE.INT),
                        new Column(COLUMN_NAME_REMAINING_TIME_CURR_QUESTION, TYPE.INT),
                        new Column(COLUMN_NAME_TIMESTAMP_QUIZZ_START, TYPE.INT),
                        new Column(COLUMN_NAME_CATEGORY_KEY, TYPE.TEXT),
                        new Column(COLUMN_NAME_QUIZZ_KEY, TYPE.TEXT),
                        new Column(COLUMN_NAME_QUESTION_KEYS_FOR_GENERAL, TYPE.TEXT),
                        new Column(COLUMN_NAME_HAS_STARTED, TYPE.BOOLEAN));
        public static final String SQL_DROP_TABLE = Util.generateSQLDropTable(TABLE_NAME);
    }

    public static class FinishedQuiz extends QuizzBase implements BaseColumns {
        public static final String TABLE_NAME = "FinishedQuiz";
        public static final String COLUMN_NAME_QUIZZ_NAME = "quizz_name";
        public static final String COLUMN_NAME_TOTAL_TIME_USED = "used_time";
        public static final String COLUMN_NAME_QUIZZ_KEY = "quiz_key";

        public static final String SQL_CREATE_TABLE =
                Util.generateSQLCreateTable(TABLE_NAME,
                        new Column(COLUMN_NAME_NUM_CORRECT_ANSWWERS, TYPE.INT),
                        new Column(COLUMN_NAME_NUM_WRONG_ANSWWERS, TYPE.INT),
                        new Column(COLUMN_NAME_TOTAL_TIME_USED, TYPE.INT),
                        new Column(COLUMN_NAME_TIMESTAMP_QUIZZ_START, TYPE.INT),
                        new Column(COLUMN_NAME_CATEGORY_KEY, TYPE.TEXT),
                        new Column(COLUMN_NAME_QUIZZ_NAME, TYPE.TEXT),
                        new Column(COLUMN_NAME_QUIZZ_KEY, TYPE.TEXT));
        public static final String SQL_DROP_TABLE = Util.generateSQLDropTable(TABLE_NAME);
    }

    public static class XP implements BaseColumns {
        public static final String TABLE_NAME = "Xp";
        public static final String COLUMN_NAME_XP = "xp";
        public static final String COLUMN_NAME_LEVEL = "level";

        //  public static final String SQL_CREATE_TABLE = "CREATE TABLE "
        public static final String SQL_CREATE_TABLE =
                Util.generateSQLCreateTable(TABLE_NAME,
                        new Column(COLUMN_NAME_XP, TYPE.INT),
                        new Column(COLUMN_NAME_LEVEL, TYPE.INT));
        public static final String SQL_DROP_TABLE = Util.generateSQLDropTable(TABLE_NAME);
    }


    //Representerer en SQL-lite datatype.
    public enum TYPE {
        TEXT("TEXT"), INT("INT"), BLOB("BLOB"), BOOLEAN("BYTE");
        private final String str;

        TYPE(String str){
            this.str = str;
        }

        public String str() {
            return str;
        }
    }
}
