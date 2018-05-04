package com.example.alessandro.gosafe.server;

import android.app.Service;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.alessandro.gosafe.database.DAOTronco;
import com.example.alessandro.gosafe.entity.Beacon;
import com.example.alessandro.gosafe.entity.Tronco;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
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
        }, 5000, 150000);
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
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());  //da sostituire con l'ultimo aggiornamento

        try {
            String request = "http://10.0.2.2:8080/gestionemappe/db/aggiornadb/"+ formattedDate;
            URL url = new URL(request);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            //connection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
            //connection.setRequestProperty("Accept","*/*");
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

            JsonObject jsonResponse = new Gson().fromJson(result, JsonObject.class);
            if (jsonResponse.get("tronco").getAsJsonArray().size() != 0) {

                DAOTronco troncodao = new DAOTronco(getApplicationContext());
                troncodao.open();
                JsonArray tronchiArray = jsonResponse.get("tronco").getAsJsonArray();
                for (JsonElement jsonTronco : tronchiArray){
                    JsonObject jsonObject = jsonTronco.getAsJsonObject();
                    Beacon beaconA = new Beacon();
                    beaconA.setId(jsonObject.get("beaconAId").getAsString());
                    Beacon beaconB = new Beacon();
                    jsonObject.get("beaconBId").getAsString();
                    beaconB.setId(jsonObject.get("beaconBId").getAsString());
                    ArrayList<Beacon> beaconEstremi = new ArrayList<>();
                    beaconEstremi.add(beaconA);
                    beaconEstremi.add(beaconB);
                    Tronco tronco = new Tronco(jsonObject.get("id").getAsInt(), jsonObject.get("agibile").getAsBoolean(), beaconEstremi,  jsonObject.get("area").getAsFloat());
                    boolean notInDb = troncodao.save(tronco);
                    if(!notInDb){
                        troncodao.update(tronco);

                    }
                }
            }

        }
    }
}
