package com.example.alessandro.gosafe.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.example.alessandro.gosafe.entity.Piano;

import java.util.ArrayList;

/**
 * classe dao che si interfaccia con la tabella Piano
 */
public class DAOPiano {

    private DBHelper dbhelper;
    private Context ctx;
    private SQLiteDatabase db;

    //costanti contenenti i nomi delle colonne della tabella Piano
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

    /**
     * costruttire
     * @param ctx prende il contesto come parametro
     */
    public DAOPiano(Context ctx)
    {
        this.ctx=ctx;
    }

    /**
     * metodo che apre la connessione alla tabella Piano
     * @return ritorna l'oggetto contenente la connessione
     * @throws SQLException
     */
    public DAOPiano open() throws SQLException {
        dbhelper = new DBHelper(ctx);
        try {
            db=dbhelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * chiude la connessione alla db
     */
    public void close()
    {
        dbhelper.close();
    }

    /**
     * prepara l'oggetto piano affinchè possa essere inserito nel db
     * @param piano piano da inserire nel db
     * @return ritorna ContentValues contenente le informazioni del Piano
     */
    public ContentValues createContentValues(Piano piano)
    {
        ContentValues cv=new ContentValues();
        cv.put(FIELD_ID, piano.getId());
        cv.put(FIELD_IMMAGINE, piano.getImmagine());
        cv.put(FIELD_PIANO, String.valueOf(piano.getPiano())); // qua ovviamente è da cambiare. Nel beacon per ora il piano è una entità e non un id
        return cv;
    }

    /**
     * metodo che si occupa di salvare un Piano nel db
     * @param piano contiene il piano da inserire
     * @return ritorna un booleano con l'esito dell'operazione
     */
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
            sqle.printStackTrace();
            return false;
        }
    }

    /**
     * metodo che esegue l'update di un Piano nel db
     * @param piano Piano da aggiornare
     * @return ritorna l'esito dell'operazione in un booleano
     */
    public boolean update(Piano piano)
    {
        ContentValues updateValues = createContentValues(piano);
        try
        {
            return db.update(TBL_NAME, updateValues, FIELD_ID + "=" + piano.getId(), null)>0;
        }
        catch (SQLiteException sqle)
        {
            sqle.printStackTrace();
            return false;
        }
    }

    /**
     * ritorna il numero del piano a partire dal suo id
     * @param idPiano id del piano di cui recuperare il numero
     * @return ritorna un intero contenente il numero del piano
     */
    public int getNumeroPianoById(int idPiano) {

        Cursor crs;
        int numeropiano=0;
        try
        {
            crs=db.query(TBL_NAME, FIELD_ALL, FIELD_ID+"="+idPiano,null,null,null,null);

            crs.moveToNext();
            numeropiano = crs.getInt(crs.getColumnIndex(FIELD_PIANO));
            crs.close();
        }
        catch(SQLiteException sqle)
        {
            sqle.printStackTrace();
        }

        return numeropiano;
    }

    /**
     * recupera tutti i piani dal db
     * @return ritorna un ArrayList con i numeri di tutti i piano sotto forma di String (ad es. "Piano 1")
     */
    public ArrayList<String> getAllPiani(){

        Cursor crs;
        ArrayList<String> piani = new ArrayList<>();
        try{
            crs = db.query(TBL_NAME, FIELD_ALL, null,null,null,null,null);
            while(crs.moveToNext())
            {
                piani.add("Piano " + crs.getInt(crs.getColumnIndex(FIELD_PIANO)));
            }
            crs.close();
        }catch(SQLiteException sqle){
            sqle.printStackTrace();
        }
        return piani;
    }

}
