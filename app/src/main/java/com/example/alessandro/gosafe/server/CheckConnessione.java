package com.example.alessandro.gosafe.server;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class CheckConnessione{

    private final String PATH = "http://192.168.1.60:8080";

    public CheckConnessione() {
    }

    public boolean checkConnessione(){
        AsyncTask<Void, Void, Boolean> execute = new checkConnessioneTask().execute();
        boolean connesso = false;
        try {
            connesso = execute.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return connesso;
    }

    private class checkConnessioneTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            try {
                HttpURLConnection conn;
                conn = (HttpURLConnection) new URL(PATH + "/").openConnection();
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                conn.setRequestMethod("HEAD");
                int responseCode = conn.getResponseCode();
                return (200 <= responseCode && responseCode <= 399);
            } catch (IOException exception) {
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Void... arg0) {

        }

        @Override
        protected void onPostExecute(Boolean result) {

        }
    }

}
