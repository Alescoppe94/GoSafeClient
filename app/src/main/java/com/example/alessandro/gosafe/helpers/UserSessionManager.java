package com.example.alessandro.gosafe.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.alessandro.gosafe.EmergenzaActivity;
import com.example.alessandro.gosafe.LoginActivity;
import com.example.alessandro.gosafe.VaiActivity;


/**
 * Classe che implementa la sessione dell'utente dell'applicazione, permettendo all'utente di rimanere loggato anche
 * quando l'applicazione viene chiusa o messa in background o il cellulare viene spento.
 */
public class UserSessionManager {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context _context;
    private int PRIVATE_MODE = 0;
    private static final String PREFER_NAME = "AndroidExamplePref";
    private static final String IS_USER_LOGIN = "IsUserLoggedIn";
    private static final String KEY_NAME = "name";
    private static final String KEY_USERNAME = "username";

    /**
     * costruttore
     * @param context prende in input il context dell'applicazione
     */
    public UserSessionManager(Context context){
        this._context=context;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * metodo che crea la sessione una volta effettuato il login
     * @param name nome della variabile che va a costituire la chiave
     * @param username username dell'utente che va a costituire il valore nello shared preferences
     */
    public void createUserLoginSession(String name, String username){
        editor.putBoolean(IS_USER_LOGIN, true);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_USERNAME, username);
        editor.commit();
    }

    /**
     * metodo che controlla se è stato effettuato il login o meno
     */
    public void checkLogin(){
        SharedPreferences sharedPreferences = _context.getSharedPreferences("isEmergenza", _context.MODE_PRIVATE);
        boolean emergenza = sharedPreferences.getBoolean("emergenza",false);
        //se utente non è loggato lo reindirizza alla schermata di login
        if(!this.isUserLoggedIn()){
            Intent i = new Intent(_context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _context.startActivity(i);
        }
        //altrimenti se è loggato e non è emergenza lo reindirizza su VaiActivity oppure se è emergenza su EmergenzaActivity
        else if(!emergenza){
            Intent i = new Intent(_context, VaiActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _context.startActivity(i);
        } else {
            Intent i = new Intent(_context, EmergenzaActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _context.startActivity(i);
        }
    }

    /**
     * metodo chiamato al logout per terminare la sessione
     */
    public void logOutUser(){
        editor.clear();
        editor.commit();
        Intent i = new Intent(_context, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(i);
    }

    /**
     * metodo che controlla se un utente è loggato
     * @return ritrona un booleano settato a True se è loggato altrimenti False
     */
    private boolean isUserLoggedIn(){
        return pref.getBoolean(IS_USER_LOGIN, false);
    }

}
