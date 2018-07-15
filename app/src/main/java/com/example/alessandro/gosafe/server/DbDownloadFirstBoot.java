package com.example.alessandro.gosafe.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Base64;

import com.example.alessandro.gosafe.database.DAOGeneric;
import com.example.alessandro.gosafe.database.DAOUtente;
import com.example.alessandro.gosafe.entity.Utente;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import static android.content.Context.MODE_PRIVATE;

/**
 * Metodo che si occupa di scaricare il db al primo avvio
 */
public class DbDownloadFirstBoot {

    AsyncTask<Void, Void, String> task;

    /**
     * Metodo che si occupa di avviare l'AsyncTask che si occupa di scaricare il db
     * @param ctx context dell'applicazione
     */
    public void dbdownloadFirstBootAsyncTask(Context ctx){
        task = new DbDownloadFirstBootAsyncTask(ctx).execute();
    }

    /**
     * Metodo che ritorna il risultato dell'operazione di inserimento
     * @return ritorna il risultato sotto forma di stringa
     */
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

    /**
     * Classe che modella l'AsyncTask che si occupa di scaricare il db al primo avvio
     */
    private class DbDownloadFirstBootAsyncTask extends AsyncTask<Void, Void, String>{

        Context ctx;
        private boolean connesso;

        /**
         * Costruttore
         * @param ctx context dell'applicazione
         */
        public DbDownloadFirstBootAsyncTask(Context ctx){

            this.ctx=ctx;

        }

        /**
         * Metodo avviato prima di eseguire l'AsyncTask vero e proprio. Controlla la connessione
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            CheckConnessione checkConnessione = new CheckConnessione();
            connesso = checkConnessione.checkConnessione(ctx);
        }

        /**
         * Metodo che rappresenta l'AsyncTask vero e proprio. Fa una richiesta GET al server per ottenere il db.
         * @param voids è un parametro nullo
         * @return ritorna il risultato
         */
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
                    SharedPreferences prefs = ctx.getSharedPreferences("ipAddress", MODE_PRIVATE);
                    String path = prefs.getString("ipAddress", null);
                    URL url = new URL("http://" + path +"/gestionemappe/db/secured/download"); //url della richiesta
                    connection = (HttpURLConnection) url.openConnection();
                    //imposta l'header della richiesta
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Authorization", "basic " + base64);
                    connection.connect();

                    //si analizza il codice della risposta
                    int responseCode = connection.getResponseCode();
                    if(400 <= responseCode && responseCode <= 499){
                        this.cancel(true);
                    }

                    StringBuilder sb = new StringBuilder();

                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    String inputLine;

                    //si scrive la risposta in una stringa
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

        /**
         * Metodo eseguito dopo doInBackground. E' un metodo di raccordo tra AsyncTask e Main thread.
         * Si occupa di analizzare la risposta del server con le tabelle del db scaricate.
         * @param result risultato del server
         */
        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);

            //se il server non è offline
            if(result!=null) {
                Gson gson = new Gson();

                JsonObject tabelleJson = gson.fromJson(result, JsonObject.class);

                DAOGeneric daoGeneric = new DAOGeneric(ctx);
                daoGeneric.open();
                daoGeneric.ricreaDb(tabelleJson); //si ricrea il db
                daoGeneric.close();

                //si setta la data di ultimo update
                SharedPreferences.Editor editor = ctx.getSharedPreferences("dblastupdate", MODE_PRIVATE).edit();
                editor.putLong("last_update", System.currentTimeMillis());
                editor.apply();
            }
        }

    }

}
