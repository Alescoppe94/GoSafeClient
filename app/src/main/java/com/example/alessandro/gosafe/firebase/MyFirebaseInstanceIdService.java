package com.example.alessandro.gosafe.firebase;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Classe che modella il servizio che gestisce Firebase
 */
public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static String token = null;

    public static String recent_token;

    /**
     * Metodo eseguito quando il token firebase subisce un refresh
     */
    public void onTokenRefresh(){

        recent_token = FirebaseInstanceId.getInstance().getToken();
        MyFirebaseInstanceIdService.token=recent_token;

    }

    /**
     * Metodo get per recuperare il token firebase
     * @return ritrona il token
     */
    public static String get_token(){

        token = FirebaseInstanceId.getInstance().getToken();
        return MyFirebaseInstanceIdService.token;

    }

}
