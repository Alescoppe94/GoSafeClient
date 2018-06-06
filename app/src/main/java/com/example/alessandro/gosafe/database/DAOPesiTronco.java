package com.example.alessandro.gosafe.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Alessandro on 04/05/2018.
 */

public class DAOPesiTronco {

    private DBHelper dbhelper;
    private Context ctx;
    private SQLiteDatabase db;

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

    public DAOPesiTronco(Context ctx)
    {
        this.ctx=ctx;
    }

    public DAOPesiTronco open() throws SQLException {
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

    public ContentValues createContentValues(int id, int troncoId, int pesoId, float valore)
    {
        ContentValues cv=new ContentValues();
        cv.put(FIELD_ID, id);
        cv.put(FIELD_TRONCOID, troncoId);
        cv.put(FIELD_PESOID, pesoId);
        cv.put(FIELD_VALORE, valore);
        return cv;
    }

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
            // Gestione delle eccezioni
            return false;
        }
    }

    public boolean delete(int id)
    {
        try
        {
            boolean del = db.delete(TBL_NAME, FIELD_ID + "=" + id, null)>0;
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

    public boolean update(int id, int troncoId, int pesoId, float valore)
    {
        /*Cursor crs = db.rawQuery("select * from " +TBL_NAME,null);
        String id = crs.getString(crs.getColumnIndex(FIELD_ID));*/
        ContentValues updateValues = createContentValues(id, troncoId, pesoId, valore);
        try
        {
            boolean upd = db.update(TBL_NAME, updateValues, FIELD_ID + "=" + id, null)>0;
            return upd;
        }
        catch (SQLiteException sqle)
        {
            // Gestione delle eccezioni
            return false;
        }
    }

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
