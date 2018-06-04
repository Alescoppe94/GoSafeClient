package com.example.alessandro.gosafe.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.example.alessandro.gosafe.entity.Beacon;
import com.example.alessandro.gosafe.entity.Tronco;

import java.util.*;

/**
 * Created by Alessandro on 13/04/2018.
 */

public class DAOTronco {

    private DBHelper dbhelper;
    private Context ctx;
    private SQLiteDatabase db;

    public static final String TBL_NAME="Tronco";
    public static final String FIELD_ID="ID_tronco";
    public static final String FIELD_BEACONAID="beaconAId";
    public static final String FIELD_BEACONBID="beaconBId";
    public static final String FIELD_AGIBILE="agibile";
    public static final String FIELD_AREA="area";
    private static final String[] FIELD_ALL = new String[]
            {
                    FIELD_ID,
                    FIELD_BEACONAID,
                    FIELD_BEACONBID,
                    FIELD_AGIBILE,
                    FIELD_AREA
            };

    public DAOTronco(Context ctx)
    {
        this.ctx=ctx;
    }

    public DAOTronco open() throws SQLException {
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

    public ContentValues createContentValues(Tronco tronco)
    {
        ContentValues cv=new ContentValues();
        cv.put(FIELD_ID, tronco.getId());
        cv.put(FIELD_BEACONAID, tronco.getBeaconEstremi().get(0).getId());
        cv.put(FIELD_BEACONBID, tronco.getBeaconEstremi().get(1).getId());
        cv.put(FIELD_AGIBILE, tronco.isAgibile());
        cv.put(FIELD_AREA, tronco.getArea());
        return cv;
    }

    public boolean save(Tronco tronco)
    {
        boolean ins;
        long id_tronco = tronco.getId();
        Cursor crs;
        try
        {
            crs = db.query(TBL_NAME, FIELD_ALL, FIELD_ID+"="+id_tronco,null,null,null,null);
            if(crs.getCount()==0){
                ContentValues initialValues = createContentValues(tronco);
                ins = db.insert(TBL_NAME, null, initialValues)>=0;
            }
            else//esiste già un tronco con questo id
                ins = false;
            crs.close();
            return ins;
        }
        catch (SQLiteException sqle)
        {
            sqle.printStackTrace();
        }
        return false;
    }

    public boolean delete(Tronco tronco)
    {
        try
        {
            boolean del = db.delete(TBL_NAME, FIELD_ID + "=" + tronco.getId(), null)>0;
            return del;
        }
        catch (SQLiteException sqle)
        {
            sqle.printStackTrace();
        }
        return false;
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

        }
        return false;
    }

    public Tronco getTroncoByID(long id_tronco)
    {
        Cursor crs;
        Tronco tronco=null;
        DAOBeacon daoBeacon = new DAOBeacon(ctx);
        ArrayList<Beacon> estremiTronco = new ArrayList<>();
        estremiTronco.add(daoBeacon.getBeaconById(FIELD_BEACONAID));
        estremiTronco.add(daoBeacon.getBeaconById(FIELD_BEACONBID));
        try
        {
            crs=db.query(TBL_NAME, FIELD_ALL, FIELD_ID+"="+id_tronco,null,null,null,null);
            Boolean agibile = (crs.getInt(crs.getColumnIndex(FIELD_AGIBILE)) == 1)? true : false;
            while(crs.moveToNext())
            {
                tronco = new Tronco(
                        crs.getInt(crs.getColumnIndex(FIELD_ID)),
                        agibile,
                        estremiTronco,
                        crs.getInt(crs.getColumnIndex(FIELD_AREA)));
            }
            crs.close();
        }
        catch(SQLiteException sqle)
        {
            sqle.printStackTrace();
        }

        return tronco;
    }

    public boolean update(Tronco tronco)
    {
        /*Cursor crs = db.rawQuery("select * from " +TBL_NAME,null);
        String id = crs.getString(crs.getColumnIndex(FIELD_ID));*/
        ContentValues updateValues = createContentValues(tronco);
        try
        {
            boolean upd = db.update(TBL_NAME, updateValues, FIELD_ID + "=" + tronco.getId(), null)>0;
            return upd;
        }
        catch (SQLiteException sqle)
        {
            sqle.printStackTrace();
        }
        return false;
    }

    public Tronco getTroncoByBeacons(Beacon beaconA, Beacon beaconB) {

        Cursor crs;
        Tronco tronco=null;
        DAOBeacon daoBeacon = new DAOBeacon(ctx);
        ArrayList<Beacon> estremiTronco = new ArrayList<>();
        estremiTronco.add(beaconA);
        estremiTronco.add(beaconB);
        try
        {
            crs=db.query(TBL_NAME, FIELD_ALL, FIELD_BEACONAID+"='" + beaconA.getId() + "' AND " + FIELD_BEACONBID+"='" + beaconB.getId() + "' OR " + FIELD_BEACONAID+"='" + beaconB.getId() + "' AND " + FIELD_BEACONBID+"='" + beaconA.getId() +"'",null,null,null,null);
            crs.moveToFirst();
            Boolean agibile = (crs.getInt(crs.getColumnIndex(FIELD_AGIBILE)) == 1)? true : false;
            tronco = new Tronco(
                    crs.getInt(crs.getColumnIndex(FIELD_ID)),
                    agibile,
                    estremiTronco,
                    crs.getInt(crs.getColumnIndex(FIELD_AREA)));
            crs.close();
        }
        catch(SQLiteException sqle)
        {
            sqle.printStackTrace();
        }

        return tronco;
    }

    public boolean checkDirezioneTronco(Tronco troncoOttimo) {

        Cursor crs;
        boolean success = false;
        try
        {
            crs = db.query(TBL_NAME, FIELD_ALL, FIELD_ID+"=" + troncoOttimo.getId() + " AND " + FIELD_BEACONAID+"='" + troncoOttimo.getBeaconEstremi().get(0).getId() + "' AND " + FIELD_BEACONBID+"='" + troncoOttimo.getBeaconEstremi().get(1).getId()+"'",null,null,null,null);
            if(crs.getCount()==1)
                success = true;
            crs.close();
        }
        catch (SQLiteException sqle)
        {
            sqle.printStackTrace();
        }
        return success;
    }

    public Set<Tronco> getAllTronchi() {

        Set<Tronco> allTronchiEdificio = new HashSet<>();
        Cursor crs;
        try
        {
            crs = db.query(TBL_NAME, FIELD_ALL, null,null,null,null,null);
            while(crs.moveToNext())
            {
                ArrayList<Beacon> estremiOrdinati = new ArrayList<>();
                ArrayList<Beacon> estremiInvertiti = new ArrayList<>();
                DAOBeacon beaconDAO = new DAOBeacon(ctx);
                beaconDAO.open();
                estremiOrdinati.add(beaconDAO.getBeaconById(crs.getString(crs.getColumnIndex(FIELD_BEACONAID))));
                estremiOrdinati.add(beaconDAO.getBeaconById(crs.getString(crs.getColumnIndex(FIELD_BEACONBID))));
                estremiInvertiti.add(beaconDAO.getBeaconById(crs.getString(crs.getColumnIndex(FIELD_BEACONBID))));
                estremiInvertiti.add(beaconDAO.getBeaconById(crs.getString(crs.getColumnIndex(FIELD_BEACONAID))));
                beaconDAO.close();
                Tronco troncoOrd = new Tronco(
                        crs.getInt(crs.getColumnIndex(FIELD_ID)),
                        (crs.getInt(crs.getColumnIndex(FIELD_AGIBILE)) == 1)? true : false,
                        estremiOrdinati,
                        crs.getInt(crs.getColumnIndex(FIELD_AREA)));
                Tronco troncoInv = new Tronco(
                        crs.getInt(crs.getColumnIndex(FIELD_ID)),
                        (crs.getInt(crs.getColumnIndex(FIELD_AGIBILE)) == 1)? true : false,
                        estremiInvertiti,
                        crs.getInt(crs.getColumnIndex(FIELD_AREA)));
                allTronchiEdificio.add(troncoOrd);
                allTronchiEdificio.add(troncoInv);
            }
            crs.close();
        }
        catch(SQLiteException sqle)
        {
            return null;
        }
        return allTronchiEdificio;
    }
}