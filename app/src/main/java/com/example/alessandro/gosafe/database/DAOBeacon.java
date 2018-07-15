package com.example.alessandro.gosafe.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.example.alessandro.gosafe.entity.Beacon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * DAO che si occupa del recupeo informazioni dalla tabella beacon nel db
 */
public class DAOBeacon {

    //attributi utili per il dao
    private DBHelper dbhelper;
    private Context ctx;
    private SQLiteDatabase db;
    private ArrayList<Integer> coorddelpunto = new ArrayList<>() ;

    //nomi delle colonne della tabella e della tabella stessa
    public static final String TBL_NAME="Beacon";
    public static final String FIELD_ID="ID_beacon";
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

    /**
     * Costruttore
     * @param ctx Context
     */
    public DAOBeacon(Context ctx)
    {
        this.ctx=ctx;
    }

    /**
     * Apre la connesione al db
     * @return ritorna l'oggetto DAOBeacon
     * @throws SQLException
     */
    public DAOBeacon open() throws SQLException {
        dbhelper = new DBHelper(ctx);
        try {
            db=dbhelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Chiude la connessione al db
     */
    public void close()
    {
        dbhelper.close();
    }

    /**
     * Prepara le variabili da inserire nel db a partire dall'oggetto Beacon
     * @param beacon oggetto Beacon da inserire
     * @return ritorna l'oggetto in formato ContentValues pronto da inserire nel db
     */
    public ContentValues createContentValues(Beacon beacon)
    {
        ContentValues cv=new ContentValues();
        cv.put(FIELD_ID, beacon.getId());
        cv.put(FIELD_ISPUNTODIRACCOLTA, beacon.is_puntodiraccola());
        cv.put(FIELD_PIANOID, beacon.getPiano());
        cv.put(FIELD_COORDX, beacon.getCoordx());
        cv.put(FIELD_COORDY, beacon.getCoordy());
        return cv;
    }

    /**
     * Salva il beacon nel db
     * @param beacon l'oggetto Beacon da salvare
     * @return ritorna un booleano che indica il successo o meno dell'operazione
     */
    public boolean save(Beacon beacon)
    {
        boolean ins;
        String id_beacon = beacon.getId();
        Cursor crs;
        try
        {
            crs = db.query(TBL_NAME, FIELD_ALL, FIELD_ID+"='"+id_beacon+"'",null,null,null,null);
            if(crs.getCount()==0){
                ContentValues initialValues = createContentValues(beacon);
                ins = db.insert(TBL_NAME, null, initialValues)>=0;
            }
            else//se esiste già un beacon con questo id ins viene settato a False
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
     * Recupera il beacon a partire dall'id
     * @param id_beacon contiene il MAC address del beacon che è anche l'id nel db
     * @return ritorna l'oggetto Beacon appena richiesto
     */
    public Beacon getBeaconById(String id_beacon)
    {
        Cursor crs;
        Beacon beacon=null;
        try
        {
            crs=db.query(TBL_NAME, FIELD_ALL, FIELD_ID+"='"+id_beacon+"'",null,null,null,null);
            crs.moveToFirst();
            Boolean ispuntodiraccolta = crs.getInt(crs.getColumnIndex(FIELD_ISPUNTODIRACCOLTA)) == 1;
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

    /**
     * Serve per aggiornare un record nel db a partire da un beacon
     * @param beacon oggetto Beacon da aggiornare
     * @return ritorna booleano in base all'esito dell'operazione
     */
    public boolean update(Beacon beacon)
    {

        ContentValues updateValues = createContentValues(beacon);
        try
        {
            return db.update(TBL_NAME, updateValues, FIELD_ID + "='" + beacon.getId() + "'", null)>0;
        }
        catch (SQLiteException sqle)
        {
            sqle.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo che recupera tutti i beacon punti di raccolta
     * @return ritorna un insieme di oggetti Beacon
     */
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
                        crs.getInt(crs.getColumnIndex(FIELD_ISPUNTODIRACCOLTA)) == 1,
                        crs.getInt(crs.getColumnIndex(FIELD_PIANOID)),
                        crs.getInt(crs.getColumnIndex(FIELD_COORDX)),
                        crs.getInt(crs.getColumnIndex(FIELD_COORDY)));
                allPuntiDiRaccolta.add(beaconDiRaccolta);
            }
            crs.close();
        }
        catch(SQLiteException sqle)
        {
            sqle.printStackTrace();
            return null;
        }
        return allPuntiDiRaccolta;
    }

    /**
     * Metodo che recupera tutti i beacon su un piano
     * @param position indica il numero del piano
     * @return ritorna un Cursor contenente i risultati
     */
    public Cursor getAllBeaconInPiano(int position){
        Cursor crs;
        crs = db.query(TBL_NAME + " INNER JOIN Piano ON " + TBL_NAME + ".pianoId=Piano.ID_piano", FIELD_ALL, "Piano.piano="+position,null,null,null,null);
        return crs;
    }

    /**
     * Recupera le coordinate di un insieme di beacons
     * @param percorso riceve una lista di MAC address di beacon che rappresentano il percorso ordinato
     * @return vengono ritornate le coordinate dei beacon in una lista
     */
    public ArrayList<Integer> getCoords(ArrayList<String> percorso) {
        for(int i = 0; i<percorso.size(); i++){
            Cursor crs;
            crs = db.query(TBL_NAME, FIELD_ALL, FIELD_ID+ "='" +percorso.get(i)+ "'" ,null,null,null,null);
            if(crs!= null && crs.moveToFirst()){
                int xcoord = crs.getInt(crs.getColumnIndex(FIELD_COORDX));
                int ycoord = crs.getInt(crs.getColumnIndex(FIELD_COORDY));
                coorddelpunto.add(xcoord);
                coorddelpunto.add(ycoord);
                crs.close();
            }
        }
        return coorddelpunto; // Ritorna in output le coordinate x e y di tutti i beacon in input {B1.X = 1463,B1.Y = 222, B2.X= 2234, B2.Y= 177,...}
    }

    /**
     * Recupera le coordinate di un solo beacon a partire dal suo MAC address
     * @param idBeacon MAC address del beacon
     * @return ritorna una lista con le coordinate del beacon
     */
    public ArrayList<Integer> getCoordsByIdBeacon(String idBeacon){
        Cursor crs;
        crs = db.query(TBL_NAME, FIELD_ALL, FIELD_ID+ "='" +idBeacon+"'" ,null,null,null,null);
        ArrayList<Integer> xcoordandycoord = new ArrayList<>();
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