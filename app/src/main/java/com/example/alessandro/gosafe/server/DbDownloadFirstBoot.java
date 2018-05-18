package com.example.alessandro.gosafe.server;

import android.content.Context;
import android.os.AsyncTask;

import com.example.alessandro.gosafe.database.DAOGeneric;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Alessandro on 16/05/2018.
 */

public class DbDownloadFirstBoot {


    public void dbdownloadFirstBootAsyncTask(Context ctx){
        new DbDownloadFirstBootAsyncTask(ctx).execute();
    }

    private class DbDownloadFirstBootAsyncTask extends AsyncTask<Void, Void, String>{

        Context ctx;

        public DbDownloadFirstBootAsyncTask(Context ctx){

            this.ctx=ctx;

        }

        @Override
        protected String doInBackground(Void... voids) {

            HttpURLConnection connection = null;

            try {
                URL url = new URL("http://192.168.1.197:8080/gestionemappe/db/download");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.connect();

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

            return null;
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);

            Gson gson = new Gson();

            JsonObject tabelleJson = gson.fromJson(result, JsonObject.class);

            DAOGeneric daoGeneric = new DAOGeneric(ctx);
            daoGeneric.open();
            daoGeneric.ricreaDb(tabelleJson);
            daoGeneric.close();

        }

    }

}
