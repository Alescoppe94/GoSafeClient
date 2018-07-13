package com.example.alessandro.gosafe.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

/**
 *classe dao che gestisce la tabella Peso
 */
public class DAOPeso {

    private DBHelper dbhelper;
    private Context ctx;
    private SQLiteDatabase db;

    //variabili contenenti i nomi delle colonne della tabella Piano
    public static final String TBL_NAME="Peso";
    public static final String FIELD_ID="ID_peso";
    public static final String FIELD_NOME="nome";
    public static final String FIELD_COEFFICIENTE="coefficiente";
    private static final String[] FIELD_ALL = new String[]
            {
                    FIELD_ID,
                    FIELD_NOME,
                    FIELD_COEFFICIENTE
            };

    /**
     * costruttore
     * @param ctx prende il Context
     */
    public DAOPeso(Context ctx)
    {
        this.ctx=ctx;
    }

    /**
     * apre la connessione al db
     * @return ritorna l'oggetto contenente la connessione al db
     * @throws SQLException
     */
    public DAOPeso open() throws SQLException {
        dbhelper = new DBHelper(ctx);
        try {
            db=dbhelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * chiude la connessione al db
     */
    public void close()
    {
        dbhelper.close();
    }

    /**
     * prepara il peso da inserire nel db
     * @param idPeso id del peso da aggiungere
     * @param nome nome del peso da aggiungere
     * @param coeff valore del peso da aggiungere
     * @return ContentValues contenente gli lementi pronti da inserire nel db
     */
    public ContentValues createContentValues(int idPeso, String nome, float coeff)
    {
        ContentValues cv=new ContentValues();
        cv.put(FIELD_ID, idPeso);
        cv.put(FIELD_NOME, nome);
        cv.put(FIELD_COEFFICIENTE, coeff);
        return cv;
    }

    /**
     * salva un nuovo peso nel db
     * @param idPeso id del nuovo peso
     * @param nome nome del nuovo peso
     * @param coeff valore del nuovo peso
     * @return ritorna un booleano con l'esito dell'operazione
     */
    public boolean save(int idPeso, String nome, float coeff)
    {
        boolean ins;
        Cursor crs;
        try
        {
            crs = db.query(TBL_NAME, FIELD_ALL, FIELD_ID+"="+ idPeso,null,null,null,null);
            if(crs.getCount()==0){
                ContentValues initialValues = createContentValues(idPeso, nome, coeff);
                ins = db.insert(TBL_NAME, null, initialValues)>=0;
            }
            else//esiste già un beacon con questo id
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
     * metodo che aggiorna il coeffieciente di un peso
     * @param idPeso id del peso da aggiornare
     * @param nome eventuale nuovo nome del peso da aggiornare
     * @param coeff nuovo valore del coefficiente
     * @return ritorna un booleano con l'esito dell'operazione
     */
    public boolean update(int idPeso, String nome, float coeff)
    {
        ContentValues updateValues = createContentValues(idPeso, nome, coeff);
        try
        {
            return db.update(TBL_NAME, updateValues, FIELD_ID + "=" + idPeso, null)>0;
        }
        catch (SQLiteException sqle)
        {
            sqle.printStackTrace();
            return false;
        }
    }

}
