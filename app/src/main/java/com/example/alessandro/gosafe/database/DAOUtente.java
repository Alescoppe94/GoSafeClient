package com.example.alessandro.gosafe.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.example.alessandro.gosafe.entity.Utente;


public class DAOUtente {
    private DBHelper dbhelper;
    private Context ctx;
    private SQLiteDatabase db;

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
                    FIELD_TOKEN
            };

    public DAOUtente(Context ctx)
    {
        this.ctx=ctx;
    }

    public DAOUtente open() throws SQLException {
        dbhelper = new DBHelper(ctx);
        try {
            db=dbhelper.getWritableDatabase();
        } catch (Exception e) {
            //gestire eccezioni
            e.printStackTrace();
        }
        return this;
    }

    public void close()
    {
        dbhelper.close();
    }

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
        return cv;
    }

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
            // Gestione delle eccezioni
            return false;
        }
    }


    //controllare bene
    public boolean delete(Utente utente)
    {
        try
        {
            boolean del = db.delete(TBL_NAME, FIELD_ID + "=" + utente.getId_utente(), null)>0;
            return del;
        }
        catch (SQLiteException sqle)
        {
            sqle.printStackTrace();
            // Gestione delle eccezioni
            return false;
        }

    }

    public boolean deleteAll()
    {
        try
        {
            boolean del = db.delete(TBL_NAME,null,null)>0;
            System.out.println(TBL_NAME);
            return del;
        }
        catch (SQLiteException sqle)
        {
            sqle.printStackTrace();
            // Gestione delle eccezioni
            return false;

        }
    }

    public Utente findByID(long id_utente)
    {
        Cursor crs;
        Utente utente=null;
        try
        {
            crs=db.query(TBL_NAME, FIELD_ALL, FIELD_ID+"="+id_utente,null,null,null,null);
            while(crs.moveToNext())
            {
                utente = new Utente(
                        crs.getString(crs.getColumnIndex(FIELD_USER)),
                        crs.getString(crs.getColumnIndex(FIELD_PASS)),
                        crs.getString(crs.getColumnIndex(FIELD_NOME)),
                        crs.getString(crs.getColumnIndex(FIELD_COGNOME)),
                        crs.getString(crs.getColumnIndex(FIELD_BEACONID)),
                        crs.getInt(crs.getColumnIndex(FIELD_PERCORSOID)),
                        false,
                        crs.getString(crs.getColumnIndex(FIELD_TOKEN)));
            }
            crs.close();
        }
        catch(SQLiteException sqle)
        {
            sqle.printStackTrace();
            return null;
        }

        return utente;
    }

    public Utente findUtente(){ //TODO: ??
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
                    cursor.getString(cursor.getColumnIndex(FIELD_TOKEN)));
        }
        cursor.close();
        return utente;
    }

    public boolean update(Utente utente)
    {
        /*Cursor crs = db.rawQuery("select * from " +TBL_NAME,null);
        String id = crs.getString(crs.getColumnIndex(FIELD_ID));*/
        ContentValues updateValues = createContentValues(utente);
        try
        {
            boolean upd = db.update(TBL_NAME, updateValues, FIELD_ID + "=" + utente.getId_utente(), null)>0;
            return upd;
        }
        catch (SQLiteException sqle)
        {
            sqle.printStackTrace();
            // Gestione delle eccezioni
            return false;
        }
    }

    public Utente getUserByUsername(String username) {
        Utente utente = null;
        Cursor  cursor = db.query(TBL_NAME, FIELD_ALL, FIELD_USER+" = '" + username + "'",null,null,null,null);
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
                    cursor.getString(cursor.getColumnIndex(FIELD_TOKEN)));
        }
        cursor.close();
        return utente;
    }

   /* public Utente findByUsername(String username)
    {
        Cursor crs;
        Utente utente=null;
        try
        {
            crs=db.query(true, TBL_NAME, FIELD_ALL, FIELD_USER+" like '"+username+"'",null,null,null,null,null);
            while(crs.moveToNext())
            {
                utente = new Utente(
                        crs.getString(crs.getColumnIndex(FIELD_USER)),
                        crs.getString(crs.getColumnIndex(FIELD_PASS)),
                        crs.getString(crs.getColumnIndex(FIELD_NOME)),
                        crs.getString(crs.getColumnIndex(FIELD_COGNOME)),
                        crs.getInt(crs.getColumnIndex(FIELD_BEACONID)),
                        crs.getInt(crs.getColumnIndex(FIELD_PERCORSOID)),
                        false,
                        crs.getString(crs.getColumnIndex(FIELD_TOKEN)));
            }
            crs.close();
        }
        catch(SQLiteException sqle)
        {
            return null;
        }

        return utente;
    }
    */

    /*public Cursor findAll()
    {
        Cursor crs;
        try
        {
            crs=db.query(TBL_NAME, FIELD_ALL, null, null, null, null, null);
        }
        catch(SQLiteException sqle)
        {
            return null;
        }
        return crs;
    }
*/

}
