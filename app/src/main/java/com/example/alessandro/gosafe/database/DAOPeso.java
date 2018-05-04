package com.example.alessandro.gosafe.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.example.alessandro.gosafe.entity.Piano;

/**
 * Created by Alessandro on 04/05/2018.
 */

public class DAOPeso {

    private DBHelper dbhelper;
    private Context ctx;
    private SQLiteDatabase db;

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

    public DAOPeso(Context ctx)
    {
        this.ctx=ctx;
    }

    public DAOPeso open() throws SQLException {
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

    private ContentValues createContentValues(int idPeso, String nome, float coeff)
    {
        ContentValues cv=new ContentValues();
        cv.put(FIELD_ID, idPeso);
        cv.put(FIELD_NOME, nome);
        cv.put(FIELD_COEFFICIENTE, coeff); // qua ovviamente è da cambiare. Nel beacon per ora il piano è una entità e non un id
        return cv;
    }

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
            // Gestione delle eccezioni
            return false;
        }
    }

    public boolean delete(int idPeso)
    {
        try
        {
            boolean del = db.delete(TBL_NAME, FIELD_ID + "=" + idPeso, null)>0;
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

    public boolean update(int idPeso, String nome, float coeff)
    {
        /*Cursor crs = db.rawQuery("select * from " +TBL_NAME,null);
        String id = crs.getString(crs.getColumnIndex(FIELD_ID));*/
        ContentValues updateValues = createContentValues(idPeso, nome, coeff);
        try
        {
            boolean upd = db.update(TBL_NAME, updateValues, null, null)>0;
            return upd;
        }
        catch (SQLiteException sqle)
        {
            // Gestione delle eccezioni
            return false;
        }
    }

}
