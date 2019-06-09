package com.example.erlend.quizzapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

//Håndterer oprettelse, opp og nedgradering av av databasen og tabellene
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String SQL_CREATE_DATABASE = String.format("sqlite3 %s", new Object[]{DatabaseContract.DATABASE_NAME});
    private static final String SQL_DROP_DATABASE = String.format("DROP DATABASE %s", new Object[]{DatabaseContract.DATABASE_NAME});

    public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.CurrentQuiz.SQL_CREATE_TABLE);
        db.execSQL(DatabaseContract.FinishedQuiz.SQL_CREATE_TABLE);
        db.execSQL(DatabaseContract.XP.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DatabaseContract.CurrentQuiz.SQL_DROP_TABLE);
        db.execSQL(DatabaseContract.FinishedQuiz.SQL_DROP_TABLE);
        db.execSQL(DatabaseContract.XP.SQL_DROP_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }
}
