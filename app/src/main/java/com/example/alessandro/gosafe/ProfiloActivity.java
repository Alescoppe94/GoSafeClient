package com.example.alessandro.gosafe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

        //ImageView mIcon = findViewById(R.id.Profile);

        DAOUtente daoUtente = new DAOUtente(this);
        daoUtente.open();
        Utente utente;
        utente = daoUtente.findUtente();
        daoUtente.close();

        nomeCognome.setText(utente.getNome()+" "+utente.getCognome()/*.toString()*/);
        username.setText(utente.getUsername());

        /*Roba per settare icona nel bottomNavigationView*/
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);

    }


    public void pickanimage(View v){
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE) {
            //TODO: action
        }
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
