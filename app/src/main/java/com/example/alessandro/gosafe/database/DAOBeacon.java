package com.example.alessandro.gosafe.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.PointF;
import android.util.Log;

import com.example.alessandro.gosafe.entity.Beacon;
import com.example.alessandro.gosafe.entity.Piano;

import java.util.ArrayList;
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
    public static final String FIELD_ID="ID_beacon";
    public static final String FIELD_ISPUNTODIRACCOLTA="is_puntodiraccolta";
    public static final String FIELD_PIANOID="pianoId";
    public static final String FIELD_COORDX="coordx";
    public static final String FIELD_COORDY="coordy";
    public ArrayList<Integer> coorddelpunto = new ArrayList<Integer>() ;
    public ArrayList<Integer> xcoordandycoord = new ArrayList<>();

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
        cv.put(FIELD_PIANOID, beacon.getPiano());
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
            crs=db.query(TBL_NAME, FIELD_ALL, FIELD_ID+"='"+id_beacon+"'",null,null,null,null);
            Log.v("Cursor Object DAOBEACON", DatabaseUtils.dumpCursorToString(crs));
            crs.moveToFirst();
            Boolean ispuntodiraccolta = (crs.getInt(crs.getColumnIndex(FIELD_ISPUNTODIRACCOLTA)) == 1)? true : false;
            beacon = new Beacon(
                    crs.getString(crs.getColumnIndex(FIELD_ID)),
                    ispuntodiraccolta,
                    crs.getInt(crs.getColumnIndex(FIELD_PIANOID)),
                    crs.getInt(crs.getColumnIndex(FIELD_COORDX)),
                    crs.getInt(crs.getColumnIndex(FIELD_COORDY)));
            crs.close();
        }
        catch(SQLiteException sqle)
        {
            sqle.printStackTrace();
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
            boolean upd = db.update(TBL_NAME, updateValues, FIELD_ID + "='" + beacon.getId() + "'", null)>0;
            return upd;
        }
        catch (SQLiteException sqle)
        {
            sqle.printStackTrace();
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
                Beacon beaconDiRaccolta = new Beacon(
                        crs.getString(crs.getColumnIndex(FIELD_ID)),
                        (crs.getInt(crs.getColumnIndex(FIELD_ISPUNTODIRACCOLTA)) == 1)? true : false,
                        crs.getInt(crs.getColumnIndex(FIELD_PIANOID)),
                        crs.getInt(crs.getColumnIndex(FIELD_COORDX)),
                        crs.getInt(crs.getColumnIndex(FIELD_COORDY)));
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

    public Cursor getAllBeaconInPiano(int position){
        Cursor crs;
        crs = db.query(TBL_NAME, FIELD_ALL, FIELD_PIANOID+"="+position,null,null,null,null);
        return crs;
    }

    public ArrayList<Integer> getCoords(ArrayList<Integer> percorso) { // Prende un insieme di id_beacon in input {1,8,4,5}
        for(int i = 0; i<percorso.size(); i++){
            Cursor crs;
            crs = db.query(TBL_NAME, FIELD_ALL, FIELD_ID+ "=" +percorso.get(i) ,null,null,null,null);
            System.out.println("GETI: " +percorso.get(i));
            if(crs!= null && crs.moveToFirst()){
                int xcoord = crs.getInt(crs.getColumnIndex(FIELD_COORDX));
                int ycoord = crs.getInt(crs.getColumnIndex(FIELD_COORDY));
                coorddelpunto.add(xcoord); // Fa una append delle coordinate x e y
                coorddelpunto.add(ycoord);
                crs.close();
            }
        }
        return coorddelpunto; // Ritorna in output le coordinate x e y di tutti i beacon in input {B1.X = 1463,B1.Y = 222, B2.X= 2234, B2.Y= 177,...}
    }

    public ArrayList<Integer> getCoordsByIdBeacon(String idBeacon){
        Cursor crs;
        crs = db.query(TBL_NAME, FIELD_ALL, FIELD_ID+ "='" +idBeacon+"'" ,null,null,null,null);
        if(crs!= null && crs.moveToFirst()){
            int xcoord = crs.getInt(crs.getColumnIndex(FIELD_COORDX));
            int ycoord = crs.getInt(crs.getColumnIndex(FIELD_COORDY));
            xcoordandycoord.add(xcoord);
            xcoordandycoord.add(ycoord);
            crs.close();
        }
        return xcoordandycoord;
    }
}