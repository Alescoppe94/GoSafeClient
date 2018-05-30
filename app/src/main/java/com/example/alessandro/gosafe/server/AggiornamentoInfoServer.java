package com.example.alessandro.gosafe.server;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.example.alessandro.gosafe.entity.Utente;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Alessandro on 03/04/2018.
 */

public class AggiornamentoInfoServer {

    public void aggiornamentoPosizione(Utente utente) {
        new AggiornamentoPosizioneTask(utente).execute();
    }

    private class AggiornamentoPosizioneTask extends AsyncTask<Void, Void, String>{

        Utente utente;
        private boolean connesso;

        public AggiornamentoPosizioneTask(Utente utente) {
            this.utente = utente;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            CheckConnessione checkConnessione = new CheckConnessione();
            connesso = checkConnessione.checkConnessione();
        }

        @Override
        protected String doInBackground(Void... voids) {

            if (!connesso) {
                return null;
            } else {
                HttpURLConnection conn = null;

                Gson gson = new Gson();
                String dati_pos = gson.toJson(utente);

                try {
                    byte[] data = utente.getIdsessione().getBytes("UTF-8");
                    String base64 = Base64.encodeToString(data,Base64.DEFAULT);
                    URL url = new URL("http://192.168.1.197:8080/gestionemappe/utente/secured/updateposition");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("Authorization", "basic " + base64);
                    conn.setInstanceFollowRedirects(true);
                    conn.connect();

                    OutputStream os = conn.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                    osw.write(dati_pos);
                    osw.flush();
                    osw.close();

                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    String inputLine;

                    while ((inputLine = br.readLine()) != null) {
                        sb.append(inputLine + "\n");
                    }

                    br.close();
                    return sb.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (conn != null) {
                        try {
                            conn.disconnect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return null;
        }
    }

}
