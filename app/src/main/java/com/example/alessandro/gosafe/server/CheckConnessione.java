package com.example.alessandro.gosafe.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import static android.content.Context.MODE_PRIVATE;

public class CheckConnessione{


    public CheckConnessione() {

    }

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

    private class checkConnessioneTask extends AsyncTask<Void, Void, Boolean> {

        Context ctx;

        public checkConnessioneTask(Context ctx){
            this.ctx = ctx;
        }


        @Override
        protected Boolean doInBackground(Void... arg0) {
            try {
                SharedPreferences prefs = ctx.getSharedPreferences("ipAddress", MODE_PRIVATE);
                String path = prefs.getString("ipAddress", null);
                HttpURLConnection conn;
                conn = (HttpURLConnection) new URL("http://" + path + "/").openConnection();
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                conn.setRequestMethod("HEAD");
                int responseCode = conn.getResponseCode();
                return (200 <= responseCode && responseCode <= 399);
            } catch (IOException exception) {
                return false;
            }
        }

    }

}
