package com.example.alessandro.gosafe.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Classe di gestione del DB
 * Crea il DB (se non esistente) e lo aggiorna (in base alla versione) creando e cancellando a
 * dovere le tabelle
 */
class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "gosafe.db";
    private static final int DB_VERSION = 14;
    private Context context;
    private SQLiteDatabase db;
    private static final String TABLE_UTENTE = "CREATE TABLE " + DAOUtente.TBL_NAME + " (" +
            DAOUtente.FIELD_ID + " INTEGER PRIMARY KEY NOT NULL, " +
            DAOUtente.FIELD_NOME + " TEXT NOT NULL, " +
            DAOUtente.FIELD_COGNOME + " TEXT NOT NULL, " +
            DAOUtente.FIELD_USER + " TEXT NOT NULL, " +
            DAOUtente.FIELD_PASS + " TEXT NOT NULL, " +
            DAOUtente.FIELD_BEACONID + " TEXT NULL, " +
            DAOUtente.FIELD_PERCORSOID + " TEXT NULL, " +
            DAOUtente.FIELD_ISAUTENTICATO + " TEXT NOT NULL, " +
            DAOUtente.FIELD_TOKEN + " TEXT NULL)";

    private static final String TABLE_TRONCO = "CREATE TABLE " + DAOTronco.TBL_NAME + " (" +
            DAOTronco.FIELD_ID + " INTEGER PRIMARY KEY NOT NULL, " +
            DAOTronco.FIELD_BEACONAID + " INTEGER NOT NULL, " +
            DAOTronco.FIELD_BEACONBID + " INTEGER NOT NULL, " +
            DAOTronco.FIELD_AGIBILE + " TEXT NOT NULL, " +
            DAOTronco.FIELD_COSTO + " INTEGER NOT NULL, " +
            DAOTronco.FIELD_AREA + " INTEGER NULL)";

    private static final String TABLE_BEACON = "CREATE TABLE " + DAOBeacon.TBL_NAME + " (" +
            DAOBeacon.FIELD_ID + " STRING PRIMARY KEY NOT NULL, " +
            DAOBeacon.FIELD_ISPUNTODIRACCOLTA + " TEXT NOT NULL, " +
            DAOBeacon.FIELD_PIANOID + " INTEGER NOT NULL)";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(TABLE_UTENTE);
        db.execSQL(TABLE_TRONCO);
        db.execSQL(TABLE_BEACON);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //gestire upgrade, al momento perde i dati vecchi
        //TODO gestire prossimamente senza cancellare tutto
        db.execSQL("DROP TABLE IF EXISTS " + DAOUtente.TBL_NAME);
        db.execSQL(TABLE_UTENTE);

        SharedPreferences pref = context.getSharedPreferences("utenti_loggati", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }
}