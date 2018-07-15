package com.example.alessandro.gosafe.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.example.alessandro.gosafe.entity.Utente;

/**
 * Classe dao che si occupa di interfacciarsi con la tabella Utente
 */
public class DAOUtente {
    private DBHelper dbhelper;
    private Context ctx;
    private SQLiteDatabase db;

    //costanti contenenti i nomi delle colonne della tabella
    public static final String TBL_NAME="Utente";
    public static final String FIELD_ID="ID_utente";
    public static final String FIELD_USER="username";
    public static final String FIELD_PASS="password";
    public static final String FIELD_NOME="nome";
    public static final String FIELD_COGNOME="cognome";
    public static final String FIELD_BEACONID="beaconid";
    public static final String FIELD_PERCORSOID="percorsoid";
    public static final String FIELD_ISAUTENTICATO="isautenticato";
    public static final String FIELD_TOKEN="token";
    public static final String FIELD_IDSESSIONE="idsessione";
    private static final String[] FIELD_ALL = new String[]
            {
                    FIELD_ID,
                    FIELD_USER,
                    FIELD_PASS,
                    FIELD_NOME,
                    FIELD_COGNOME,
                    FIELD_BEACONID,
                    FIELD_PERCORSOID,
                    FIELD_ISAUTENTICATO,
                    FIELD_TOKEN,
                    FIELD_IDSESSIONE
            };

    /**
     * Costruttore
     * @param ctx prende come parametro il Context dell'applicazione
     */
    public DAOUtente(Context ctx)
    {
        this.ctx=ctx;
    }

    /**
     * Apre la connessione alla tabella utente nel db
     * @return ritorna l'oggetto contenente la connessione
     * @throws SQLException
     */
    public DAOUtente open() throws SQLException {
        dbhelper = new DBHelper(ctx);
        try {
            db=dbhelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Chiude la connessione al db
     */
    public void close()
    {
        dbhelper.close();
    }

    /**
     * Prepara l'oggetto Utente per essere inserito nel db
     * @param utente utente da inserire nel db
     * @return ritorna ContentValues contenente l'utente pronto da inserire nel db
     */
    private ContentValues createContentValues(Utente utente)
    {
        ContentValues cv=new ContentValues();
        cv.put(FIELD_ID, utente.getId_utente());
        cv.put(FIELD_USER, utente.getUsername());
        cv.put(FIELD_PASS, utente.getPassword());
        cv.put(FIELD_NOME, utente.getNome());
        cv.put(FIELD_COGNOME, utente.getCognome());
        cv.put(FIELD_BEACONID, utente.getBeaconid());
        cv.put(FIELD_PERCORSOID, utente.getPercorsoid());
        cv.put(FIELD_ISAUTENTICATO, utente.getIs_autenticato());
        cv.put(FIELD_TOKEN, utente.getToken());
        cv.put(FIELD_IDSESSIONE,utente.getIdsessione());
        return cv;
    }

    /**
     * Metodo per salvare un utente nel db
     * @param utente utente da inserire nel db
     * @return ritorna un booleano con l'esito dell'operazione
     */
    public boolean save(Utente utente)
    {
        boolean ins;
        long id_utente = utente.getId_utente();
        Cursor crs;
        try
        {
            crs = db.query(TBL_NAME, FIELD_ALL, FIELD_ID+"="+id_utente,null,null,null,null);
            if(crs.getCount()==0){
                ContentValues initialValues = createContentValues(utente);
                ins = db.insert(TBL_NAME, null, initialValues)>=0;
            }
            else//esiste già un utente con questo id
                ins = false;
            crs.close();
            return ins;
        }
        catch (SQLiteException sqle)
        {
            sqle.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina tutti gli utenti nel db(ce n'è al massimo uno alla volta)
     * @return ritorna un booleano con l'esito dell'operazione
     */
    public boolean deleteAll()
    {
        try
        {
            return db.delete(TBL_NAME,null,null)>0;
        }
        catch (SQLiteException sqle)
        {
            sqle.printStackTrace();
            return false;

        }
    }

    /**
     * Metodo che serve per recuperare l'Utente dal db locale
     * @return ritorna l'oggetto Utente contenuto nel db
     */
    public Utente findUtente(){
        Utente utente = null;
        Cursor  cursor = db.rawQuery("select * from " +TBL_NAME,null);
        if (cursor.moveToFirst()) {

            utente = new Utente(
                    cursor.getLong(cursor.getColumnIndex(FIELD_ID)),
                    cursor.getString(cursor.getColumnIndex(FIELD_USER)),
                    cursor.getString(cursor.getColumnIndex(FIELD_PASS)),
                    cursor.getString(cursor.getColumnIndex(FIELD_NOME)),
                    cursor.getString(cursor.getColumnIndex(FIELD_COGNOME)),
                    cursor.getString(cursor.getColumnIndex(FIELD_BEACONID)),
                    cursor.getInt(cursor.getColumnIndex(FIELD_PERCORSOID)),
                    true,
                    cursor.getString(cursor.getColumnIndex(FIELD_TOKEN)),
                    cursor.getString(cursor.getColumnIndex(FIELD_IDSESSIONE)));
        }
        cursor.close();
        return utente;
    }

    /**
     * Metodo che consente l'aggiornamento di un utente
     * @param utente utente che deve essere aggiornato
     * @return ritorna un booleano con l'esito dell'operazione
     */
    public boolean update(Utente utente)
    {
        ContentValues updateValues = createContentValues(utente);
        try
        {
            return db.update(TBL_NAME, updateValues, FIELD_ID + "=" + utente.getId_utente(), null)>0;
        }
        catch (SQLiteException sqle)
        {
            sqle.printStackTrace();
            return false;
        }
    }

}
