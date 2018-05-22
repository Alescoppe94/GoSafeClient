package com.example.alessandro.gosafe.server;

import android.app.Service;
import android.content.Intent;
import java.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.alessandro.gosafe.database.*;
import com.example.alessandro.gosafe.entity.Beacon;
import com.example.alessandro.gosafe.entity.Piano;
import com.example.alessandro.gosafe.entity.Tronco;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Alessandro on 03/05/2018.
 */

public class CheckForDbUpdatesService extends Service {

    private HttpURLConnection connection;

    @Override
    public void onCreate(){

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        new Timer().schedule(new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                checkForUpdates();
            }
        }, 5000, 15000); //gli aggiornamenti vengono controllati ogni 15 secondi si può cambiare
        return super.onStartCommand(intent, flags, startId);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void checkForUpdates(){
        String result = null;
        File dbpath = getApplicationContext().getDatabasePath("gosafe.db");
        long lastModified = dbpath.lastModified();
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lastModified);

        try {
            String request = "http://10.0.2.2:8080/gestionemappe/db/aggiornadb/" + formattedDate;
            URL url = new URL(request);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.connect();

            StringBuilder sb = new StringBuilder();
            /*StringBuilder sbe = new StringBuilder();
            BufferedReader bre = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"));
            String inputeLine;
            while ((inputeLine = bre.readLine()) != null) {
                sbe.append(inputeLine + "\n");
            }
            System.out.println(sbe.toString());
            bre.close();*/
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String inputLine;

            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine + "\n");
            }

            br.close();
            result = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if(result != null){
            updateDB(result);
        }
    }

    private void updateDB(String result) {

        JsonObject jsonResponse = new Gson().fromJson(result, JsonObject.class);

        if(jsonResponse.get("tipologia").getAsString().equals("modifica")) {

            if (jsonResponse.get("tronco").getAsJsonArray().size() != 0) {

                DAOTronco troncodao = new DAOTronco(getApplicationContext());
                troncodao.open();
                JsonArray tronchiArray = jsonResponse.get("tronco").getAsJsonArray();

                for (JsonElement jsonTronco : tronchiArray) {
                    JsonObject jsonObject = jsonTronco.getAsJsonObject();
                    Beacon beaconA = new Beacon();
                    beaconA.setId(jsonObject.get("beaconAId").getAsString());
                    Beacon beaconB = new Beacon();
                    jsonObject.get("beaconBId").getAsString();
                    beaconB.setId(jsonObject.get("beaconBId").getAsString());
                    ArrayList<Beacon> beaconEstremi = new ArrayList<>();
                    beaconEstremi.add(beaconA);
                    beaconEstremi.add(beaconB);
                    Tronco tronco = new Tronco(jsonObject.get("id").getAsInt(), jsonObject.get("agibile").getAsBoolean(), beaconEstremi, jsonObject.get("area").getAsFloat());
                    boolean notInDb = troncodao.save(tronco);
                    if (!notInDb) {
                        troncodao.update(tronco);

                    }
                }
                troncodao.close();
            }

            if (jsonResponse.get("beacon").getAsJsonArray().size() != 0) {

                DAOBeacon beacondao = new DAOBeacon(getApplicationContext());
                beacondao.open();
                JsonArray beaconArray = jsonResponse.get("beacon").getAsJsonArray();

                for (JsonElement jsonBeacon : beaconArray) {
                    JsonObject jsonObject = jsonBeacon.getAsJsonObject();
                    Piano piano = new Piano();
                    piano.setId(jsonObject.get("pianoId").getAsInt());
                    Beacon beacon = new Beacon(jsonObject.get("id").getAsString(), jsonObject.get("is_puntodiraccolta").getAsBoolean(), piano, jsonObject.get("coordx").getAsFloat(), jsonObject.get("coordy").getAsFloat());
                    boolean notInDb = beacondao.save(beacon);
                    if (!notInDb) {
                        beacondao.update(beacon);

                    }
                }
                beacondao.close();
            }

            if (jsonResponse.get("piano").getAsJsonArray().size() != 0) {

                DAOPiano pianodao = new DAOPiano(getApplicationContext());
                pianodao.open();
                JsonArray beaconArray = jsonResponse.get("piano").getAsJsonArray();

                for (JsonElement jsonPiano : beaconArray) {
                    JsonObject jsonObject = jsonPiano.getAsJsonObject();
                    Piano piano = new Piano(jsonObject.get("id").getAsInt(), jsonObject.get("immagine").getAsString(), jsonObject.get("piano").getAsInt());
                    boolean notInDb = pianodao.save(piano);
                    if (!notInDb) {
                        pianodao.update(piano);

                    }
                }
                pianodao.close();
            }

            if (jsonResponse.get("peso").getAsJsonArray().size() != 0) {

                DAOPeso pesodao = new DAOPeso(getApplicationContext());
                pesodao.open();
                JsonArray beaconArray = jsonResponse.get("peso").getAsJsonArray();

                for (JsonElement jsonPeso : beaconArray) {
                    JsonObject jsonObject = jsonPeso.getAsJsonObject();
                    boolean notInDb = pesodao.save(jsonObject.get("id").getAsInt(), jsonObject.get("nome").getAsString(), jsonObject.get("coefficiente").getAsFloat());
                    if (!notInDb) {
                        pesodao.update(jsonObject.get("id").getAsInt(), jsonObject.get("nome").getAsString(), jsonObject.get("coefficiente").getAsFloat());

                    }
                }
                pesodao.close();
            }

            if (jsonResponse.get("pesitronco").getAsJsonArray().size() != 0) {

                DAOPesiTronco pesitroncodao = new DAOPesiTronco(getApplicationContext());
                pesitroncodao.open();
                JsonArray beaconArray = jsonResponse.get("pesitronco").getAsJsonArray();

                for (JsonElement jsonPesitronco : beaconArray) {
                    JsonObject jsonObject = jsonPesitronco.getAsJsonObject();
                    boolean notInDb = pesitroncodao.save(jsonObject.get("id").getAsInt(), jsonObject.get("troncoId").getAsInt(), jsonObject.get("pesoId").getAsInt(), jsonObject.get("valore").getAsFloat());
                    if (!notInDb) {
                        pesitroncodao.update(jsonObject.get("id").getAsInt(), jsonObject.get("troncoId").getAsInt(), jsonObject.get("pesoId").getAsInt(), jsonObject.get("valore").getAsFloat());

                    }
                }
                pesitroncodao.close();
            }
        }
        else {
            DAOGeneric daoGeneric = new DAOGeneric(getApplicationContext());
            daoGeneric.open();
            daoGeneric.ricreaDb(jsonResponse);
            daoGeneric.close();
        }
    }


}
