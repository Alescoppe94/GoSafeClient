package com.example.alessandro.gosafe.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.example.alessandro.gosafe.entity.Beacon;
import com.example.alessandro.gosafe.entity.Piano;
import com.example.alessandro.gosafe.entity.Tronco;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by Alessandro on 13/04/2018.
 */

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
            //gestire eccezioni
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
                int piano;
                piano = jsonObject.get("pianoId").getAsInt();
                Beacon beacon = new Beacon(jsonObject.get("id").getAsString(), jsonObject.get("is_puntodiraccolta").getAsBoolean(), piano, jsonObject.get("coordx").getAsInt(), jsonObject.get("coordy").getAsInt());
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
}
