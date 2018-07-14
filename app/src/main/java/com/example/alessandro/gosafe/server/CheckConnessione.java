package com.example.alessandro.gosafe.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import static android.content.Context.MODE_PRIVATE;

/**
 * classe che si occupa di controllare la connessione al server
 */
public class CheckConnessione{


    /**
     * costruttore
     */
    public CheckConnessione() {

    }

    /**
     * metodo che fa partire l'AsyncTask che si occupa di controllare la connessione al server.
     * salva il risultato nelle SharedPreferences
     * @param ctx context dell'applicazione
     * @return ritorna lo stato della connessione
     */
    public boolean checkConnessione(Context ctx){
        AsyncTask<Void, Void, Boolean> execute = new checkConnessioneTask(ctx).execute();
        boolean connesso = false;
        try {
            connesso = execute.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        SharedPreferences.Editor editor = ctx.getSharedPreferences("isConnesso", ctx.MODE_PRIVATE).edit();
        if(connesso){
            editor.putBoolean("connesso", true).apply();
        } else {
            editor.putBoolean("connesso", false).apply();
        }
        return connesso;
    }

    /**
     * classe che modella l'AsyncTask che controlla la connessione al server
     */
    private class checkConnessioneTask extends AsyncTask<Void, Void, Boolean> {

        Context ctx;

        /**
         * costruttore
         * @param ctx context dell'applicazione
         */
        public checkConnessioneTask(Context ctx){
            this.ctx = ctx;
        }


        /**
         * metodo che rappresenta l'AsyncTask vero e proprio. Fa una richiesta GET al server per controllare la connessione
         * @param arg0 è un parametro vuoto
         * @return ritorna un risultato booleano: true se c'è connessione, False altrimenti
         */
        @Override
        protected Boolean doInBackground(Void... arg0) {
            try {
                SharedPreferences prefs = ctx.getSharedPreferences("ipAddress", MODE_PRIVATE);
                String path = prefs.getString("ipAddress", null);
                HttpURLConnection conn;
                conn = (HttpURLConnection) new URL("http://" + path + "/").openConnection();
                //imposta l'header della richiesta
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                conn.setRequestMethod("HEAD");
                int responseCode = conn.getResponseCode(); //prende la risposta del server
                return (200 <= responseCode && responseCode <= 399);
            } catch (IOException exception) {
                return false;
            }
        }

    }

}
