package com.example.alessandro.gosafe.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Base64;

import com.example.alessandro.gosafe.entity.Utente;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.Context.MODE_PRIVATE;

/**
 * classe che si occupa di inviare le informazioni aggiornate al server
 */
public class AggiornamentoInfoServer {

    /**
     * metodo per creare l'asyncTask che si occupa di mantenere aggiornato il server
     * @param utente utente connesso
     * @param ctx contesto dell'applicazione
     */
    public void aggiornamentoPosizione(Utente utente, Context ctx) {
        new AggiornamentoPosizioneTask(utente, ctx).execute();
    }

    /**
     * classe privata che gestisce l'AsyncTask che invia la posizione aggiornata dell'utente al server
     */
    private class AggiornamentoPosizioneTask extends AsyncTask<Void, Void, String>{

        Utente utente;
        private boolean connesso;
        private Context ctx;

        /**
         * costruttore
         * @param utente utente connesso
         * @param ctx contesto dell'applicazione
         */
        public AggiornamentoPosizioneTask(Utente utente, Context ctx) {
            this.utente = utente;
            this.ctx = ctx;
        }

        /**
         * metodo eseguito prima dell'avvio del task. nello specifico controlla la connessione al server
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            CheckConnessione checkConnessione = new CheckConnessione();
            connesso = checkConnessione.checkConnessione(ctx);
        }

        /**
         * metodo che costituisce l'AsyncTask vero e proprio. E' eseguito su un thread separato rispetto al principale.
         * si occupa di fare una richiesta PUT al server che va a modificare la posizione dell'utente
         * @param voids indica che non prende parametri
         * @return ritorna una stringa vuota o una stringa con un messaggio di successo in base all'esito.
         */
        @Override
        protected String doInBackground(Void... voids) {

            if (!connesso) {
                return null;
            } else {
                HttpURLConnection conn = null;

                Gson gson = new Gson();
                String dati_pos = gson.toJson(utente);

                try {
                    SharedPreferences prefs = ctx.getSharedPreferences("ipAddress", MODE_PRIVATE);
                    String path = prefs.getString("ipAddress", null);
                    byte[] data = utente.getIdsessione().getBytes("UTF-8");
                    String base64 = Base64.encodeToString(data,Base64.DEFAULT);
                    URL url = new URL("http://" + path +"/gestionemappe/utente/secured/updateposition"); //url verso cui la richiesta viene fatta
                    conn = (HttpURLConnection) url.openConnection();
                    //metodi che impostano l'header della richiesta
                    conn.setDoOutput(true);
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("Authorization", "basic " + base64);
                    conn.setInstanceFollowRedirects(true);
                    conn.connect();

                    //scrittura delle informazioni sulla posizione in output
                    OutputStream os = conn.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                    osw.write(dati_pos);
                    osw.flush();
                    osw.close();

                    //controlla l'esito della richiesta
                    int responseCode = conn.getResponseCode();
                    if(400 <= responseCode && responseCode <= 499){
                        this.cancel(true);
                        StringBuilder sbe = new StringBuilder();
                        BufferedReader bre = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                        String inputeLine;
                        while ((inputeLine = bre.readLine()) != null) {
                            sbe.append(inputeLine + "\n");
                        }
                        System.out.println(sbe.toString());
                        bre.close();
                    }else{
                        StringBuilder sb = new StringBuilder();
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        String inputLine;

                        while ((inputLine = br.readLine()) != null) {
                            sb.append(inputLine + "\n");
                        }

                        br.close();
                        return sb.toString();
                    }
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
