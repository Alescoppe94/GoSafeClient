package com.example.alessandro.gosafe;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Alessandro on 08/03/2018.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String REG_TOKEN = "REG_TOKEN";

    public void onTokenRefresh(){

        String recent_token = FirebaseInstanceId.getInstance().getToken();
        Log.d(REG_TOKEN, recent_token);

    }
}
