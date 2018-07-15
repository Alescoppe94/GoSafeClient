package com.example.alessandro.gosafe.server;

import android.app.Service;
import android.content.Intent;
import java.text.SimpleDateFormat;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Base64;

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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Classe che modella il servizio che controlla se ci sono aggiornamenti sul database del server
 */
public class CheckForDbUpdatesService extends Service {

    private HttpURLConnection connection;
    private Timer timer;
    private TimerTask timertask;

    /**
     * Metodo che parte subito all'avvio del servizio. in questo caso non fa nulla
     */
    @Override
    public void onCreate(){

    }

    /**
     * Metodo che parte ogni volta che il servizio viene avviato o riavviato.
     * Imposta il timer che gestisce il controllo periodico di aggiornamenti
     * @param intent intent che ha avviato il servizio
     * @param flags flags di impostazione
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        //timer che gestisce il loop di controllo degli aggiornamenti sul server
        timer = new Timer();
        timertask = new TimerTask() {
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

    /**
     * Metodo che gestisce la terminazione del servizio
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        timertask.cancel(); // si fermano i timer
        timer.cancel();
        timertask = null;
        timer = null;
    }

    /**
     * Metodo che si occupa di fare una chiamata GET al server per recuperare eventuali aggiiornamenti del database sul server.
     * se ci sono vengono inseriti nel db locale
     */
    private void checkForUpdates(){

        //controlla la connessione
        CheckConnessione checkConnessione = new CheckConnessione();
        boolean connesso = checkConnessione.checkConnessione(getApplicationContext());

        if(connesso) {

            String result = null;
            SharedPreferences prefs = getSharedPreferences("dblastupdate", MODE_PRIVATE);
            if (prefs != null) {
                long lastModified = prefs.getLong("last_update", 0); //va a prendere l'ultima modifica del db locale
                String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lastModified);

                try {
                    DAOUtente daoUtente = new DAOUtente(this);
                    daoUtente.open();
                    Utente utente = daoUtente.findUtente();
                    daoUtente.close();
                    if(utente != null) {
                        byte[] data = utente.getIdsessione().getBytes("UTF-8");
                        String base64 = Base64.encodeToString(data, Base64.DEFAULT);
                        SharedPreferences pref = getSharedPreferences("ipAddress", MODE_PRIVATE);
                        String path = pref.getString("ipAddress", null);
                        String request = "http://" + path +"/gestionemappe/db/secured/aggiornadb/" + formattedDate; //url a cui fare la richiesta. si manda la data dell'ultima modifica nell'url
                        URL url = new URL(request);
                        connection = (HttpURLConnection) url.openConnection();
                        //imposta l'header della richiesta
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setRequestProperty("Accept", "application/json");
                        connection.setRequestProperty("Authorization", "basic " + base64);
                        connection.connect();
                        //si analizza il risultato
                        int responseCode = connection.getResponseCode();
                        if (400 > responseCode || responseCode >= 500) {

                            StringBuilder sb = new StringBuilder();
                            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                            String inputLine;

                            //si scrive il risultato in una stringa
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
                //se il risultato è diverso da nullo o diverso da una stringa vuota viene invocato il metodo per aggiornare il db
                if (result != null && !result.equals("")) {
                    updateDB(result);
                }
            }
        }
    }

    /**
     * Metodo che si occupa di aggiornare il db con le informazioni provenienti dal server
     * @param result contiene il risultato della richiesta al server
     */
    private void updateDB(String result) {

        //viene trasformato in json
        JsonObject jsonResponse = new Gson().fromJson(result, JsonObject.class);

        //si controlla il tipo. può essere una modifica o un'inizializzazione da zero del db.
        if(jsonResponse.get("tipologia").getAsString().equals("modifica")) { //se è una modifica...

            //inserisce eventuali nuovi tronchi
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

            //inserisce eventuali nuovi beacon
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

            //inserisce eventuali nuovi piani
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

            //inserisce eventuali nuovi pesi
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

            //inserisce eventuali nuovi pesitronco
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
        else { // se entra qui viene ricreato il db
            DAOGeneric daoGeneric = new DAOGeneric(getApplicationContext());
            daoGeneric.open();
            daoGeneric.ricreaDb(jsonResponse);
            daoGeneric.close();
        }

        //quando c'è un update si modifica la data dell'ultimo aggiornamento
        SharedPreferences.Editor editor = getSharedPreferences("dblastupdate", MODE_PRIVATE).edit();
        editor.putLong("last_update", System.currentTimeMillis());
        editor.apply();

    }


}
