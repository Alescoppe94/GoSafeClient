package com.example.alessandro.gosafe.server;

import android.app.Service;
import android.content.Intent;
import java.text.SimpleDateFormat;

import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;

import com.example.alessandro.gosafe.database.*;
import com.example.alessandro.gosafe.entity.Beacon;
import com.example.alessandro.gosafe.entity.Piano;
import com.example.alessandro.gosafe.entity.Tronco;
import com.example.alessandro.gosafe.entity.Utente;
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
    private Timer timer;
    private TimerTask timertask;

    @Override
    public void onCreate(){

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        timer = new Timer();
        timertask = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                checkForUpdates();
            }
        }; //gli aggiornamenti vengono controllati ogni 15 secondi si può cambiare
        timer.scheduleAtFixedRate(timertask,0,15000);
        return super.onStartCommand(intent, flags, startId);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        //stopSelf();
        super.onDestroy();
        timertask.cancel();
        timer.cancel();
        timertask = null;
        timer = null;
    }

    private void checkForUpdates(){

        CheckConnessione checkConnessione = new CheckConnessione();
        boolean connesso = checkConnessione.checkConnessione();

        if(connesso) {

            String result = null;
            File dbpath = getApplicationContext().getDatabasePath("gosafe.db");
            SharedPreferences prefs = getSharedPreferences("dblastupdate", MODE_PRIVATE);
            if (prefs != null) {
                long lastModified = prefs.getLong("last_update", 0);
                String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lastModified);

                try {
                    DAOUtente daoUtente = new DAOUtente(this);
                    daoUtente.open();
                    Utente utente = daoUtente.findUtente();
                    daoUtente.close();
                    if(utente != null) {
                        byte[] data = utente.getIdsessione().getBytes("UTF-8");
                        String base64 = Base64.encodeToString(data, Base64.DEFAULT);
                        String request = "http://192.168.1.197:8080/gestionemappe/db/secured/aggiornadb/" + formattedDate;
                        URL url = new URL(request);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setRequestProperty("Accept", "application/json");
                        connection.setRequestProperty("Authorization", "basic " + base64);
                        connection.connect();
                        int responseCode = connection.getResponseCode();
                        if (400 > responseCode || responseCode >= 500) {

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
                        }
                    }
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
                if (result != null && !result.equals("")) {
                    updateDB(result);
                }
            }
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
                    Beacon beacon = new Beacon(jsonObject.get("id").getAsString(), jsonObject.get("is_puntodiraccolta").getAsBoolean(), jsonObject.get("pianoId").getAsInt(), jsonObject.get("coordx").getAsInt(), jsonObject.get("coordy").getAsInt());
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

        SharedPreferences.Editor editor = getSharedPreferences("dblastupdate", MODE_PRIVATE).edit();
        editor.putLong("last_update", System.currentTimeMillis());
        editor.apply();

    }


}
