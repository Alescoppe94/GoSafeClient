package com.example.alessandro.gosafe;

import android.content.Intent;
import android.support.design.widget.BottomNavigationView;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.alessandro.gosafe.beacon.BluetoothLeService;
import com.example.alessandro.gosafe.database.DAOUtente;
import com.example.alessandro.gosafe.entity.Utente;
import com.example.alessandro.gosafe.server.Autenticazione;
import com.example.alessandro.gosafe.server.CheckForDbUpdatesService;

public class ProfiloActivity extends DefaultActivity  {

    private TextView mTextMessage;
    private TextView username;
    private TextView nomeCognome;

    public static final int PICK_IMAGE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilo);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        username= (TextView)findViewById(R.id.textViewUsername);
        nomeCognome = (TextView) findViewById(R.id.textViewNomeCognome);

        DAOUtente daoUtente = new DAOUtente(this);
        daoUtente.open();
        Utente utente;
        utente = daoUtente.findUtente();
        daoUtente.close();

        nomeCognome.setText(utente.getNome()+" "+utente.getCognome());
        username.setText(utente.getUsername());

        /*Roba per settare icona nel bottomNavigationView*/
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);

    }


    public void goToModificaProfilo(View view){
        Intent i;
        i = new Intent(getApplicationContext(), ModificaActivity.class);
        startActivity(i);
    }

    public void logout (View view){
        DAOUtente daoutente = new DAOUtente(this);
        daoutente.open();
        Utente utente = daoutente.findUtente();
        daoutente.close();
        stopService(new Intent(this, CheckForDbUpdatesService.class));
        stopService(new Intent(this, BluetoothLeService.class));
        Autenticazione autenticazione = new Autenticazione(utente);
        autenticazione.logoutUtente(ProfiloActivity.this);
    }


}
