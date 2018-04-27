package com.example.alessandro.gosafe.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Alessandro on 13/04/2018.
 */

public class DAOGeneric<T> {

    /*private DBHelper dbhelper;
    private Context ctx;
    private SQLiteDatabase db;

    public T open() throws SQLException {
        dbhelper = new DBHelper(ctx);
        try {
            db=dbhelper.getWritableDatabase();
        } catch (Exception e) {
            //gestire eccezioni
            e.printStackTrace();
        }
        return this;
    }

    public boolean update(T t)
    {
        //Cursor crs = db.rawQuery("select * from " +TBL_NAME,null);
        //String id = crs.getString(crs.getColumnIndex(FIELD_ID));
        ContentValues updateValues = createContentValues(t);
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

    public void close()
    {
        dbhelper.close();
    }
    */
}
