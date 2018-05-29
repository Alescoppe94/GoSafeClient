package com.example.alessandro.gosafe.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.example.alessandro.gosafe.entity.Beacon;
import com.example.alessandro.gosafe.entity.Piano;

import java.util.List;
import java.util.Map;

/**
 * Created by Alessandro on 04/05/2018.
 */

public class DAOPiano {

    private DBHelper dbhelper;
    private Context ctx;
    private SQLiteDatabase db;

    public static final String TBL_NAME="Piano";
    public static final String FIELD_ID="ID_piano";
    public static final String FIELD_IMMAGINE="immagine";
    public static final String FIELD_PIANO="piano";
    private static final String[] FIELD_ALL = new String[]
            {
                    FIELD_ID,
                    FIELD_IMMAGINE,
                    FIELD_PIANO
            };

    public DAOPiano(Context ctx)
    {
        this.ctx=ctx;
    }

    public DAOPiano open() throws SQLException {
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

    public ContentValues createContentValues(Piano piano)
    {
        ContentValues cv=new ContentValues();
        cv.put(FIELD_ID, piano.getId());
        cv.put(FIELD_IMMAGINE, piano.getImmagine());
        cv.put(FIELD_PIANO, String.valueOf(piano.getPiano())); // qua ovviamente è da cambiare. Nel beacon per ora il piano è una entità e non un id
        return cv;
    }

    public boolean save(Piano piano)
    {
        boolean ins;
        int id_piano = piano.getId();
        Cursor crs;
        try
        {
            crs = db.query(TBL_NAME, FIELD_ALL, FIELD_ID+"="+id_piano,null,null,null,null);
            if(crs.getCount()==0){
                ContentValues initialValues = createContentValues(piano);
                ins = db.insert(TBL_NAME, null, initialValues)>=0;
            }
            else//esiste già un beacon con questo id
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

    public boolean delete(Piano piano)
    {
        try
        {
            boolean del = db.delete(TBL_NAME, FIELD_ID + "=" + piano.getId(), null)>0;
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
            boolean del = db.delete(TBL_NAME,null,null)>0;
            System.out.println(TBL_NAME);
            return del;
        }
        catch (SQLiteException sqle)
        {
            // Gestione delle eccezioni
            return false;

        }
    }

    public boolean update(Piano piano)
    {
        /*Cursor crs = db.rawQuery("select * from " +TBL_NAME,null);
        String id = crs.getString(crs.getColumnIndex(FIELD_ID));*/
        ContentValues updateValues = createContentValues(piano);
        try
        {
            boolean upd = db.update(TBL_NAME, updateValues, FIELD_ID + "=" + piano.getId(), null)>0;
            return upd;
        }
        catch (SQLiteException sqle)
        {
            // Gestione delle eccezioni
            return false;
        }
    }

    public Piano getPianoById(int idPiano) {

        Cursor crs;
        Piano piano=null;
        try
        {
            crs=db.query(TBL_NAME, FIELD_ALL, FIELD_ID+"="+idPiano,null,null,null,null);
            while(crs.moveToNext())
            {
                piano = new Piano(
                        crs.getInt(crs.getColumnIndex(FIELD_ID)),
                        crs.getString(crs.getColumnIndex(FIELD_IMMAGINE)),
                        crs.getInt(crs.getColumnIndex(FIELD_PIANO)));
            }
            crs.close();
        }
        catch(SQLiteException sqle)
        {
            return null;
        }

        return piano;
    }
}
