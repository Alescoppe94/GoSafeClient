package com.example.alessandro.gosafe.firebase;

import android.net.Uri;
import android.util.Log;

import com.example.alessandro.gosafe.entity.Utente;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Alessandro on 08/03/2018.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String REG_TOKEN = "REG_TOKEN";
    private static String token = null;

    public static String recent_token;

    public void onTokenRefresh(){

        recent_token = FirebaseInstanceId.getInstance().getToken();  //da sistemare il caso in cui uno fa il login da offline e poi si connette a una rete: bisogna verificare che il token sia quello giusto sul server
        Log.d(REG_TOKEN, recent_token);
        MyFirebaseInstanceIdService.token=recent_token;
        //sendPost(recent_token);

    }

    /*public void sendPost(final String recent_token) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://10.0.2.2:8080/gestionemappe/db/tokentest");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("token", recent_token);

                    Log.i("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG" , conn.getResponseMessage());

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }*/

    //public static void set_token(String token){ MyFirebaseInstanceIdService.token=token; }

    public static String get_token(){

        token = FirebaseInstanceId.getInstance().getToken();
        return MyFirebaseInstanceIdService.token;

    }

}
