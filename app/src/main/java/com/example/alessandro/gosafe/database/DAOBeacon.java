package com.example.alessandro.gosafe.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.example.alessandro.gosafe.entity.Beacon;
import com.example.alessandro.gosafe.entity.Piano;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Alessandro on 13/04/2018.
 */

public class DAOBeacon {

    private DBHelper dbhelper;
    private Context ctx;
    private SQLiteDatabase db;

    public static final String TBL_NAME="Beacon";
    public static final String FIELD_ID="id";
    public static final String FIELD_ISPUNTODIRACCOLTA="is_puntodiraccolta";
    public static final String FIELD_PIANOID="pianoId";
    public static final String FIELD_COORDX="coordx";
    public static final String FIELD_COORDY="coordy";
    private static final String[] FIELD_ALL = new String[]
            {
                    FIELD_ID,
                    FIELD_ISPUNTODIRACCOLTA,
                    FIELD_PIANOID,
                    FIELD_COORDX,
                    FIELD_COORDY
            };

    public DAOBeacon(Context ctx)
    {
        this.ctx=ctx;
    }

    public DAOBeacon open() throws SQLException {
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

    public ContentValues createContentValues(Beacon beacon)
    {
        ContentValues cv=new ContentValues();
        cv.put(FIELD_ID, beacon.getId());
        cv.put(FIELD_ISPUNTODIRACCOLTA, beacon.is_puntodiraccola());
        cv.put(FIELD_PIANOID, String.valueOf(beacon.getPiano().getId()));
        cv.put(FIELD_COORDX, beacon.getCoordx());
        cv.put(FIELD_COORDY, beacon.getCoordy());// qua ovviamente è da cambiare. Nel beacon per ora il piano è una entità e non un id
        return cv;
    }

    public boolean save(Beacon beacon)
    {
        boolean ins;
        String id_beacon = beacon.getId();
        Cursor crs;
        try
        {
            crs = db.query(TBL_NAME, FIELD_ALL, FIELD_ID+"="+id_beacon,null,null,null,null);
            if(crs.getCount()==0){
                ContentValues initialValues = createContentValues(beacon);
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

    public boolean delete(Beacon beacon)
    {
        try
        {
            boolean del = db.delete(TBL_NAME, FIELD_ID + "=" + beacon.getId(), null)>0;
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

    public Beacon getBeaconById(String id_beacon)
    {
        Cursor crs;
        Beacon beacon=null;
        try
        {
            crs=db.query(TBL_NAME, FIELD_ALL, FIELD_ID+"="+id_beacon,null,null,null,null);
            Boolean ispuntodiraccolta = (crs.getInt(crs.getColumnIndex(FIELD_ISPUNTODIRACCOLTA)) == 1)? true : false;
            DAOPiano pianoDAO = new DAOPiano(ctx);
            pianoDAO.open();
            Piano piano = pianoDAO.getPianoById(crs.getInt(crs.getColumnIndex(FIELD_PIANOID)));
            pianoDAO.close();
            while(crs.moveToNext())
            {
                beacon = new Beacon(
                        crs.getString(crs.getColumnIndex(FIELD_ID)),
                        ispuntodiraccolta,
                        piano,
                        crs.getFloat(crs.getColumnIndex(FIELD_COORDX)),
                        crs.getFloat(crs.getColumnIndex(FIELD_COORDY)));
            }
            crs.close();
        }
        catch(SQLiteException sqle)
        {
            return null;
        }

        return beacon;
    }

    public boolean update(Beacon beacon)
    {
        /*Cursor crs = db.rawQuery("select * from " +TBL_NAME,null);
        String id = crs.getString(crs.getColumnIndex(FIELD_ID));*/
        ContentValues updateValues = createContentValues(beacon);
        try
        {
            boolean upd = db.update(TBL_NAME, updateValues, FIELD_ID + "=" + beacon.getId(), null)>0;
            return upd;
        }
        catch (SQLiteException sqle)
        {
            // Gestione delle eccezioni
            return false;
        }
    }

    public Set<Beacon> getAllPuntiDiRaccolta() {
        Set<Beacon> allPuntiDiRaccolta = new HashSet<>();
        Cursor crs;
        try
        {
            crs = db.query(TBL_NAME, FIELD_ALL, FIELD_ISPUNTODIRACCOLTA + " = 1 ",null,null,null,null);
            while(crs.moveToNext())
            {
                DAOPiano pianoDAO = new DAOPiano(ctx);
                pianoDAO.open();
                Piano piano = pianoDAO.getPianoById(crs.getInt(crs.getColumnIndex(FIELD_PIANOID)));
                pianoDAO.close();
                Beacon beaconDiRaccolta = new Beacon(
                        crs.getString(crs.getColumnIndex(FIELD_ID)),
                        (crs.getInt(crs.getColumnIndex(FIELD_ISPUNTODIRACCOLTA)) == 1)? true : false,
                        piano,
                        crs.getFloat(crs.getColumnIndex(FIELD_COORDX)),
                        crs.getFloat(crs.getColumnIndex(FIELD_COORDY)));
                allPuntiDiRaccolta.add(beaconDiRaccolta);
            }
            crs.close();
        }
        catch(SQLiteException sqle)
        {
            return null;
        }
        return allPuntiDiRaccolta;
    }

    public Cursor getAllBeacon(int posizione)
    {
        Cursor crs;
        try
        {
           crs=db.query(TBL_NAME, FIELD_ALL, FIELD_PIANOID+"="+ posizione,null,null,null,null, null);
        }
        catch(SQLiteException sqle)
        {
            return null;
        }

        return crs;
        }

}
