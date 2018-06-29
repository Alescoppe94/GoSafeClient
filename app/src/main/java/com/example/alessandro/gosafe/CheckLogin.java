package com.example.alessandro.gosafe;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.alessandro.gosafe.helpers.UserSessionManager;


public class CheckLogin extends AppCompatActivity {

    private UserSessionManager session;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        session = new UserSessionManager(getApplicationContext());
        session.checkLogin();
    }
    
}
