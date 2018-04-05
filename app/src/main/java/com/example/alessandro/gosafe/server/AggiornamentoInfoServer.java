package com.example.alessandro.gosafe.server;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.alessandro.gosafe.entity.Utente;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Alessandro on 03/04/2018.
 */

public class AggiornamentoInfoServer {

    public void aggiornamentoPosizione(String username, String posizione) {
        new AggiornamentoPosizioneTask(username, posizione).execute();
    }

    private class AggiornamentoPosizioneTask extends AsyncTask<Void, Void, String>{

        String username;
        String posizione;

        public AggiornamentoPosizioneTask(String username, String posizione) {
            this.username = username;
            this.posizione = posizione;
        }

        @Override
        protected String doInBackground(Void... voids) {


            String[] macAddressParts = posizione.split(":");
            String mac = "";

// convert hex string to byte values
            for(int i=0; i<6; i++){
                mac += macAddressParts[i];
            }

            try {
                String url = "http://192.168.1.60:8080/gestionemappe/utente/updateposition/"+ username + "/" + mac;
                URL request_url = new URL(url);
                HttpURLConnection conn = (HttpURLConnection)request_url.openConnection();
                conn.setConnectTimeout(100000);
                conn.setReadTimeout(100000);
                conn.setInstanceFollowRedirects(true);
                Log.d("stato", String.valueOf(conn.getResponseCode()));

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "ciao";
        }
    }

}
