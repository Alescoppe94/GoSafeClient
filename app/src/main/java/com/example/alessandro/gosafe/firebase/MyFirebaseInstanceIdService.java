package com.example.alessandro.gosafe.firebase;


import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String REG_TOKEN = "REG_TOKEN";
    private static String token = null;

    public static String recent_token;

    public void onTokenRefresh(){

        recent_token = FirebaseInstanceId.getInstance().getToken();
        MyFirebaseInstanceIdService.token=recent_token;

    }

    public static String get_token(){

        token = FirebaseInstanceId.getInstance().getToken();
        return MyFirebaseInstanceIdService.token;

    }

}
