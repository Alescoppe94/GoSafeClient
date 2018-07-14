package com.example.alessandro.gosafe;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.alessandro.gosafe.helpers.UserSessionManager;

/**
 * classe che viene eseguita all'avvio dell'applicazione che si occupa di verificare se è stato fatto il login
 */
public class CheckLogin extends AppCompatActivity {

    private UserSessionManager session;

    /**
     * metodo eseguito alla creazione. verifica se è stato effettuato il login.
     * @param savedInstanceState parametro necessario per la classe
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        session = new UserSessionManager(getApplicationContext());
        session.checkLogin();
    }
    
}
