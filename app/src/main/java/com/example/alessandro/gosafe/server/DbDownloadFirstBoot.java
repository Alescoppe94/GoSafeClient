package com.example.alessandro.gosafe.server;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Base64;

import com.example.alessandro.gosafe.R;
import com.example.alessandro.gosafe.database.DAOGeneric;
import com.example.alessandro.gosafe.database.DAOUtente;
import com.example.alessandro.gosafe.entity.Utente;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Alessandro on 16/05/2018.
 */

public class DbDownloadFirstBoot {

    AsyncTask<Void, Void, String> task;

    public void dbdownloadFirstBootAsyncTask(Context ctx){
        task = new DbDownloadFirstBootAsyncTask(ctx).execute();
    }

    public String getResult(){
        try {
            return task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class DbDownloadFirstBootAsyncTask extends AsyncTask<Void, Void, String>{

        Context ctx;
        private boolean connesso;

        public DbDownloadFirstBootAsyncTask(Context ctx){

            this.ctx=ctx;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            CheckConnessione checkConnessione = new CheckConnessione();
            connesso = checkConnessione.checkConnessione(ctx);
        }

        @Override
        protected String doInBackground(Void... voids) {

            if(!connesso){
                return null;
            } else {
                HttpURLConnection connection = null;

                try {
                    DAOUtente daoUtente = new DAOUtente(ctx);
                    daoUtente.open();
                    Utente utente = daoUtente.findUtente();
                    daoUtente.close();
                    byte[] data = utente.getIdsessione().getBytes("UTF-8");
                    String base64 = Base64.encodeToString(data,Base64.DEFAULT);
                    URL url = new URL("http://10.0.2.2:8080/gestionemappe/db/secured/download");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Authorization", "basic " + base64);
                    connection.connect();

                    int responseCode = connection.getResponseCode();
                    if(400 <= responseCode && responseCode <= 499){
                        this.cancel(true);
                    }

                    StringBuilder sb = new StringBuilder();

                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    String inputLine;

                    while ((inputLine = br.readLine()) != null) {
                        sb.append(inputLine + "\n");
                    }

                    br.close();
                    return sb.toString();
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
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);

            if(result!=null) {
                Gson gson = new Gson();

                JsonObject tabelleJson = gson.fromJson(result, JsonObject.class);

                DAOGeneric daoGeneric = new DAOGeneric(ctx);
                daoGeneric.open();
                daoGeneric.ricreaDb(tabelleJson);
                daoGeneric.close();

                SharedPreferences.Editor editor = ctx.getSharedPreferences("dblastupdate", MODE_PRIVATE).edit();
                editor.putLong("last_update", System.currentTimeMillis());
                editor.apply();
            }
        }

    }

}
