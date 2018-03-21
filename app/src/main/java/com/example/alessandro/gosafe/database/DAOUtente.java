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
    public static final String FIELD_EMAIL="email";
    public static final String FIELD_NOME="nome";
    public static final String FIELD_COGNOME="cognome";
    private static final String[] FIELD_ALL = new String[]
            {
                    FIELD_ID,
                    FIELD_USER,
                    FIELD_PASS,
                    FIELD_EMAIL,
                    FIELD_NOME,
                    FIELD_COGNOME
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
        cv.put(FIELD_EMAIL, utente.getEmail());
        cv.put(FIELD_NOME, utente.getNome());
        cv.put(FIELD_COGNOME, utente.getCognome());
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
            // Gestione delle eccezioni
            return false;
        }
    }

    public boolean update(Utente utente)
    {
        ContentValues updateValues = createContentValues(utente);
        try
        {
            boolean upd = db.update(TBL_NAME, updateValues, FIELD_ID + "=" + utente.getId_utente(), null)>0;
            return upd;
        }
        catch (SQLiteException sqle)
        {
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
            // Gestione delle eccezioni
            return false;
        }

    }

    public boolean deleteAll()
    {
        try
        {
            boolean del = db.delete(TBL_NAME, null, null)>0;
            return del;
        }
        catch (SQLiteException sqle)
        {
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
                        crs.getString(crs.getColumnIndex(FIELD_EMAIL)),
                        crs.getString(crs.getColumnIndex(FIELD_NOME)),
                        crs.getString(crs.getColumnIndex(FIELD_COGNOME)),
                        false);
            }
            crs.close();
        }
        catch(SQLiteException sqle)
        {
            return null;
        }

        return utente;
    }

    public Utente findByUsername(String username)
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
                        crs.getString(crs.getColumnIndex(FIELD_EMAIL)),
                        crs.getString(crs.getColumnIndex(FIELD_NOME)),
                        crs.getString(crs.getColumnIndex(FIELD_COGNOME)),
                        false);
            }
            crs.close();
        }
        catch(SQLiteException sqle)
        {
            return null;
        }

        return utente;
    }

    public Cursor findAll()
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
}