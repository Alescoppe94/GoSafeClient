package com.example.alessandro.gosafe;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.alessandro.gosafe.R;
import com.example.alessandro.gosafe.UserSessionManager;
import com.example.alessandro.gosafe.VaiActivity;

/**
 * Created by Luca on 31/05/18.
 */

public class CheckLogin extends AppCompatActivity {

    private UserSessionManager session;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        session = new UserSessionManager(getApplicationContext());
        session.checkLogin();
    }
    
}
