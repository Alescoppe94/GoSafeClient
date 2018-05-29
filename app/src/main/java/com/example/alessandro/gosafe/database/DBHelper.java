package com.example.alessandro.gosafe.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

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
            DAOTronco.FIELD_BEACONAID + " TEXT NOT NULL, " +
            DAOTronco.FIELD_BEACONBID + " TEXT NOT NULL, " +
            DAOTronco.FIELD_AGIBILE + " TEXT NOT NULL, " +
            DAOTronco.FIELD_AREA + " INTEGER NULL)";

    private static final String TABLE_BEACON = "CREATE TABLE " + DAOBeacon.TBL_NAME + " (" +
            DAOBeacon.FIELD_ID + " TEXT PRIMARY KEY NOT NULL, " +
            DAOBeacon.FIELD_ISPUNTODIRACCOLTA + " TEXT NOT NULL, " +
            DAOBeacon.FIELD_PIANOID + " INTEGER NOT NULL, " +
            DAOBeacon.FIELD_COORDX + " FLOAT NOT NULL, " +
            DAOBeacon.FIELD_COORDY + " FLOAT NOT NULL)";

    private static final String TABLE_PIANO = "CREATE TABLE " + DAOPiano.TBL_NAME + " (" +
            DAOPiano.FIELD_ID + " INTEGER PRIMARY KEY NOT NULL, " +
            DAOPiano.FIELD_IMMAGINE + " TEXT NOT NULL, " +
            DAOPiano.FIELD_PIANO + " INTEGER NOT NULL)";

    private static final String TABLE_PESO = "CREATE TABLE " + DAOPeso.TBL_NAME + " (" +
            DAOPeso.FIELD_ID + " INTEGER PRIMARY KEY NOT NULL, " +
            DAOPeso.FIELD_NOME + " TEXT NOT NULL, " +
            DAOPeso.FIELD_COEFFICIENTE + " REAL NOT NULL)";

    private static final String TABLE_PESITRONCO = "CREATE TABLE " + DAOPesiTronco.TBL_NAME + " (" +
            DAOPesiTronco.FIELD_ID + " INTEGER PRIMARY KEY NOT NULL, " +
            DAOPesiTronco.FIELD_TRONCOID + " INTEGER NOT NULL, " +
            DAOPesiTronco.FIELD_PESOID + " INTEGER NOT NULL, " +
            DAOPesiTronco.FIELD_VALORE + " REAL NOT NULL)";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(TABLE_UTENTE);
        db.execSQL(TABLE_TRONCO);
        db.execSQL(TABLE_BEACON);
        db.execSQL(TABLE_PIANO);
        db.execSQL(TABLE_PESO);
        db.execSQL(TABLE_PESITRONCO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //gestire upgrade, al momento perde i dati vecchi
        //TODO gestire prossimamente senza cancellare tutto
        db.execSQL("DROP TABLE IF EXISTS " + DAOUtente.TBL_NAME);
        db.execSQL(TABLE_UTENTE);
        db.execSQL("DROP TABLE IF EXISTS " + DAOTronco.TBL_NAME);
        db.execSQL(TABLE_TRONCO);
        db.execSQL("DROP TABLE IF EXISTS " + DAOBeacon.TBL_NAME);
        db.execSQL(TABLE_BEACON);
        db.execSQL("DROP TABLE IF EXISTS " + DAOPiano.TBL_NAME);
        db.execSQL(TABLE_PIANO);
        db.execSQL("DROP TABLE IF EXISTS " + DAOPeso.TBL_NAME);
        db.execSQL(TABLE_PESO);
        db.execSQL("DROP TABLE IF EXISTS " + DAOPesiTronco.TBL_NAME);
        db.execSQL(TABLE_PESITRONCO);

        SharedPreferences pref = context.getSharedPreferences("utenti_loggati", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }

    public long lastUpdate(){

        File dbpath = context.getDatabasePath(DB_NAME);
        return dbpath.lastModified();

    }

    public static String getTableTronco() {
        return TABLE_TRONCO;
    }

    public static String getTableBeacon() {
        return TABLE_BEACON;
    }

    public static String getTablePiano() {
        return TABLE_PIANO;
    }

    public static String getTablePeso() {
        return TABLE_PESO;
    }

    public static String getTablePesitronco() {
        return TABLE_PESITRONCO;
    }
}