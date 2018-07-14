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
    private static final String DB_NAME = "gosafe.db";
    private static final int DB_VERSION = 14;
    private Context context;

    //di seguito sono contenute le strutture delle tabelle del db
    private static final String TABLE_UTENTE = "CREATE TABLE " + DAOUtente.TBL_NAME + " (" +
            DAOUtente.FIELD_ID + " INTEGER PRIMARY KEY NOT NULL, " +
            DAOUtente.FIELD_NOME + " TEXT NOT NULL, " +
            DAOUtente.FIELD_COGNOME + " TEXT NOT NULL, " +
            DAOUtente.FIELD_USER + " TEXT NOT NULL, " +
            DAOUtente.FIELD_PASS + " TEXT NOT NULL, " +
            DAOUtente.FIELD_BEACONID + " TEXT, " +
            DAOUtente.FIELD_PERCORSOID + " TEXT, " +
            DAOUtente.FIELD_ISAUTENTICATO + " TEXT NOT NULL, " +
            DAOUtente.FIELD_TOKEN + " TEXT, " +
            DAOUtente.FIELD_IDSESSIONE + " TEXT)";

    private static final String TABLE_TRONCO = "CREATE TABLE " + DAOTronco.TBL_NAME + " (" +
            DAOTronco.FIELD_ID + " INTEGER PRIMARY KEY NOT NULL, " +
            DAOTronco.FIELD_BEACONAID + " TEXT NOT NULL, " +
            DAOTronco.FIELD_BEACONBID + " TEXT NOT NULL, " +
            DAOTronco.FIELD_AGIBILE + " TEXT NOT NULL, " +
            DAOTronco.FIELD_AREA + " INTEGER)";

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

    /**
     * costruttore
     * @param context prende il Context dell'applicazione
     */
    DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    /**
     * metodo eseguito all'avvio che crea tutte le tabelle
     * @param db prende in input il db su cui creare le tabelle
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(TABLE_UTENTE);
        db.execSQL(TABLE_TRONCO);
        db.execSQL(TABLE_BEACON);
        db.execSQL(TABLE_PIANO);
        db.execSQL(TABLE_PESO);
        db.execSQL(TABLE_PESITRONCO);
    }

    /**
     * metodo eseguito qualora ci sia un cambiamento della struttura del db.
     * @param db database su cui avviene la modifica della struttura
     * @param oldVersion numero di versione del vecchio db
     * @param newVersion numero di versione del nuovo db
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

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

    /**
     * metodo per recuperare il nome della tabella Tronco
     * @return ritorna il nome della tabella Tronco
     */
    public static String getTableTronco() {
        return TABLE_TRONCO;
    }

    /**
     * metodo per recuperare il nome della tabella Beacon
     * @return ritorna il nome della tabella Beacon
     */
    public static String getTableBeacon() {
        return TABLE_BEACON;
    }

    /**
     * metodo per recuperare il nome della tabella Piano
     * @return ritorna il nome della tabella Piano
     */
    public static String getTablePiano() {
        return TABLE_PIANO;
    }

    /**
     * metodo per recuperare il nome della tabella Peso
     * @return ritorna il nome della tabella Peso
     */
    public static String getTablePeso() {
        return TABLE_PESO;
    }

    /**
     * metodo per recuperare il nome della tabella PesiTronco
     * @return ritorna il nome della tabella PesiTronco
     */
    public static String getTablePesitronco() {
        return TABLE_PESITRONCO;
    }
}