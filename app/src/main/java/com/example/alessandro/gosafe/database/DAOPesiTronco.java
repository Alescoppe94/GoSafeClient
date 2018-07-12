package com.example.alessandro.gosafe.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.HashMap;

/**
 * dao che gestisce la tabella pesitronco
 */
public class DAOPesiTronco {

    private DBHelper dbhelper;
    private Context ctx;
    private SQLiteDatabase db;

    //contiene le informazioni sulle colonne della tabella
    public static final String TBL_NAME="PesiTronco";
    public static final String FIELD_ID="ID_pesitronco";
    public static final String FIELD_TRONCOID="troncoId";
    public static final String FIELD_PESOID="pesoId";
    public static final String FIELD_VALORE="valore";
    private static final String[] FIELD_ALL = new String[]
            {
                    FIELD_ID,
                    FIELD_TRONCOID,
                    FIELD_PESOID,
                    FIELD_VALORE
            };

    /**
     * costruttore
     * @param ctx prende in input il Context
     */
    public DAOPesiTronco(Context ctx)
    {
        this.ctx=ctx;
    }

    /**
     * apre la connessione al db
     * @return ritorna l'oggetto
     * @throws SQLException
     */
    public DAOPesiTronco open() throws SQLException {
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
     * prepara le informazioni da inserire nel db
     * @param id id del peso
     * @param troncoId id del tronco
     * @param pesoId id del peso
     * @param valore valore del peso
     * @return ritorna l'oggetto ContentValues da inserire nel db
     */
    public ContentValues createContentValues(int id, int troncoId, int pesoId, float valore)
    {
        ContentValues cv=new ContentValues();
        cv.put(FIELD_ID, id);
        cv.put(FIELD_TRONCOID, troncoId);
        cv.put(FIELD_PESOID, pesoId);
        cv.put(FIELD_VALORE, valore);
        return cv;
    }

    /**
     * serve per salvare le informazioni nel database
     * @param id id del pesotronco
     * @param troncoId id del tronco
     * @param pesoId id del peso
     * @param valore valore ddel pesotronco
     * @return ritorna un booleano con l'esito
     */
    public boolean save(int id, int troncoId, int pesoId, float valore)
    {
        boolean ins;
        Cursor crs;
        try
        {
            crs = db.query(TBL_NAME, FIELD_ALL, FIELD_ID+"="+ id,null,null,null,null);
            if(crs.getCount()==0){
                ContentValues initialValues = createContentValues(id, troncoId, pesoId, valore);
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
     * metodo che fa l'update di un pesotronco nel db
     * @param id id del pesotronco
     * @param troncoId id del tronco
     * @param pesoId id del peso
     * @param valore valore del pesotronco
     * @return ritorna un booleano contenente l'esito
     */
    public boolean update(int id, int troncoId, int pesoId, float valore)
    {
        ContentValues updateValues = createContentValues(id, troncoId, pesoId, valore);
        try
        {
            return db.update(TBL_NAME, updateValues, FIELD_ID + "=" + id, null)>0;
        }
        catch (SQLiteException sqle)
        {
            sqle.printStackTrace();
            return false;
        }
    }

    /**
     * metodo che recupera tutti i pesitronco per un tronco specifico
     * @param troncoId id del tronco di cui prendere i pesitronco
     * @return ritorna un hashmap con tutti i pesi tronco
     */
    public HashMap<Float,Float> getPesiTronco(int troncoId) {

        HashMap<Float, Float> coeffVal = new HashMap<>();
        Cursor crs;
        try
        {
            crs=db.rawQuery("select valore,coefficiente from " +TBL_NAME + " INNER JOIN Peso on Peso.ID_peso = pesoId WHERE troncoId ="+ troncoId,null);
            while(crs.moveToNext())
            {
                coeffVal.put(crs.getFloat(crs.getColumnIndex("coefficiente")), crs.getFloat(crs.getColumnIndex(FIELD_VALORE)));
            }
            crs.close();
        }
        catch(SQLiteException sqle)
        {
            sqle.printStackTrace();
            return null;
        }
        return coeffVal;
    }

    /**
     * metodo che prende il valore del pesotronco corrispondente a un peso
     * @param troncoId id del tronco di cui prendere il pesotronco
     * @param peso nome del peso
     * @return ritorna il pesotronco associato
     */
    public Float geValoreByPesoId(int troncoId, String peso) {
        Float valore = null;
        Cursor crs;
        try
        {
            crs=db.rawQuery("select valore from " +TBL_NAME + " INNER JOIN Peso on Peso.ID_peso = pesoId WHERE troncoId ="+ troncoId + " AND Peso.nome = '" + peso + "'",null);
            while(crs.moveToNext())
            {
                valore = crs.getFloat(crs.getColumnIndex(FIELD_VALORE));
            }
            crs.close();
        }
        catch(SQLiteException sqle)
        {
            sqle.printStackTrace();
            return null;
        }
        return valore;
    }
}
