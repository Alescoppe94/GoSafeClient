package com.example.alessandro.gosafe.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.example.alessandro.gosafe.entity.Beacon;
import com.example.alessandro.gosafe.entity.Piano;
import com.example.alessandro.gosafe.entity.Tronco;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class DAOGeneric{

    private DBHelper dbhelper;
    private Context ctx;
    private SQLiteDatabase db;

    public DAOGeneric(Context ctx)
    {
        this.ctx=ctx;
    }

    public DAOGeneric open() throws SQLException {
        dbhelper = new DBHelper(ctx);
        try {
            db=dbhelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public void close()
    {
        dbhelper.close();
    }

    public void ricreaDb(JsonObject jsonResponse) {
        JsonArray tronchiArray = jsonResponse.get("tronco").getAsJsonArray();
        JsonArray beaconArray = jsonResponse.get("beacon").getAsJsonArray();
        JsonArray pianiArray = jsonResponse.get("piano").getAsJsonArray();
        JsonArray pesiArray = jsonResponse.get("peso").getAsJsonArray();
        JsonArray pesitronchiArray = jsonResponse.get("pesitronco").getAsJsonArray();
        db.beginTransaction();
        try {
            db.execSQL("DROP TABLE IF EXISTS " + DAOTronco.TBL_NAME);
            db.execSQL(DBHelper.getTableTronco());
            for (JsonElement jsonTronco : tronchiArray) {
                JsonObject jsonObject = jsonTronco.getAsJsonObject();
                ArrayList<Beacon> beaconEstremi = new ArrayList<>();
                beaconEstremi.add(new Beacon(jsonObject.get("beaconAId").getAsString()));
                beaconEstremi.add(new Beacon(jsonObject.get("beaconBId").getAsString()));
                Tronco tronco = new Tronco(jsonObject.get("id").getAsInt(), jsonObject.get("agibile").getAsBoolean(), beaconEstremi, jsonObject.get("area").getAsFloat());
                DAOTronco daoTronco = new DAOTronco(ctx);
                ContentValues cv = daoTronco.createContentValues(tronco);
                db.insert("Tronco", null, cv);
            }
            db.execSQL("DROP TABLE IF EXISTS " + DAOBeacon.TBL_NAME);
            db.execSQL(DBHelper.getTableBeacon());
            for (JsonElement jsonBeacon : beaconArray) {
                JsonObject jsonObject = jsonBeacon.getAsJsonObject();
                Beacon beacon = new Beacon(jsonObject.get("id").getAsString(), jsonObject.get("is_puntodiraccolta").getAsBoolean(), jsonObject.get("pianoId").getAsInt(), jsonObject.get("coordx").getAsInt(), jsonObject.get("coordy").getAsInt());
                DAOBeacon daoBeacon = new DAOBeacon(ctx);
                ContentValues cv = daoBeacon.createContentValues(beacon);
                db.insert("Beacon", null, cv);
            }
            db.execSQL("DROP TABLE IF EXISTS " + DAOPiano.TBL_NAME);
            db.execSQL(DBHelper.getTablePiano());
            for (JsonElement jsonPiano : pianiArray) {
                JsonObject jsonObject = jsonPiano.getAsJsonObject();
                Piano piano = new Piano(jsonObject.get("id").getAsInt(), jsonObject.get("immagine").getAsString(), jsonObject.get("piano").getAsInt());
                DAOPiano daoPiano = new DAOPiano(ctx);
                ContentValues cv = daoPiano.createContentValues(piano);
                db.insert("Piano", null, cv);
                byte[] decodedString = Base64.decode(jsonObject.get("immagine").getAsString(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                saveToInternalStorage(decodedByte, jsonObject.get("piano").getAsInt());
            }
            db.execSQL("DROP TABLE IF EXISTS " + DAOPeso.TBL_NAME);
            db.execSQL(DBHelper.getTablePeso());
            for (JsonElement jsonPeso : pesiArray) {
                JsonObject jsonObject = jsonPeso.getAsJsonObject();
                DAOPeso daoPeso= new DAOPeso(ctx);
                ContentValues cv = daoPeso.createContentValues(jsonObject.get("id").getAsInt(), jsonObject.get("nome").getAsString(), jsonObject.get("coefficiente").getAsFloat());
                db.insert("Peso", null, cv);
            }
            db.execSQL("DROP TABLE IF EXISTS " + DAOPesiTronco.TBL_NAME);
            db.execSQL(DBHelper.getTablePesitronco());
            for (JsonElement jsonPesiTronco : pesitronchiArray) {
                JsonObject jsonObject = jsonPesiTronco.getAsJsonObject();
                DAOPesiTronco daoPesiTronco= new DAOPesiTronco(ctx);
                ContentValues cv = daoPesiTronco.createContentValues(jsonObject.get("id").getAsInt(), jsonObject.get("troncoId").getAsInt(), jsonObject.get("pesoId").getAsInt(), jsonObject.get("valore").getAsFloat());
                db.insert("PesiTronco", null, cv);
            }
            db.setTransactionSuccessful();
        } catch(Exception e){
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Questa funziona
     * @param bitmapImage  una cosa
     * @param numeroPiano  un'altra cosa
     */
    private void saveToInternalStorage(Bitmap bitmapImage, int numeroPiano){
        ContextWrapper cw = new ContextWrapper(ctx);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"q"+ numeroPiano +".png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 90, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
