package com.example.alessandro.gosafe;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.alessandro.gosafe.database.DAOUtente;
import com.example.alessandro.gosafe.entity.Utente;
import com.example.alessandro.gosafe.server.Autenticazione;

/**
 * Created by Luca on 29/03/18.
 */

public class ModificaActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private EditText modificaNomeText;
    private EditText modificaCognomeText;
    private EditText modificaUsernameText;
    private EditText passwordText;
    private EditText confermaPasswordText;
    private long id_utente;
    DAOUtente daoUtente = new DAOUtente(this);
    Utente utente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifica);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        daoUtente.open();

        utente = daoUtente.findUtente();
        daoUtente.close();


        id_utente = utente.getId_utente();
        System.out.println("Id "+id_utente);
        modificaNomeText = (EditText) findViewById(R.id.modificaNomeText);
        modificaCognomeText = (EditText) findViewById(R.id.modificaCognomeText);
        modificaUsernameText = (EditText) findViewById(R.id.modificaUsernameText);
        passwordText = (EditText) findViewById(R.id.passwordText);
        confermaPasswordText = (EditText) findViewById(R.id.confermaPasswordText);

        passwordText.setText(utente.getPassword());
        confermaPasswordText.setText(utente.getPassword());
        modificaUsernameText.setText(utente.getUsername());
        modificaCognomeText.setText(utente.getCognome());
        modificaNomeText.setText(utente.getNome());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Intent i;
                        switch (item.getItemId()) {
                            case R.id.menu_vai:
                                i = new Intent(getApplicationContext(), VaiActivity.class);
                                i.putExtra("selezione", "vai");
                                startActivity(i);
                                break;

                            case R.id.menu_mappe:
                                i = new Intent(getApplicationContext(), MappeActivity.class);
                                i.putExtra("selezione", "mappe");
                                startActivity(i);
                                break;

                            case R.id.menu_profilo:
                                i = new Intent(getApplicationContext(), ProfiloActivity.class);
                                i.putExtra("selezione", "profilo");
                                startActivity(i);
                                break;

                        }
                        return true;
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

   public void confermaModifica (View view){

        final long id = id_utente;
        final String username = modificaUsernameText.getText().toString();
        final String password = passwordText.getText().toString();
        final String confermaPassword = confermaPasswordText.getText().toString();
        final String nome = modificaNomeText.getText().toString();
        final String cognome = modificaCognomeText.getText().toString();

        modificaUsernameText.setError(null);
        passwordText.setError(null);
        confermaPasswordText.setError(null);
        modificaNomeText.setError(null);
        modificaCognomeText.setError(null);

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(username)) {
            modificaUsernameText.setError(getString(R.string.error_field_required));
            focusView = modificaUsernameText;
            cancel = true;
        } else if (username.length() <= 2) {
            modificaUsernameText.setError(getString(R.string.error_invalid_username));
            focusView = modificaUsernameText;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            passwordText.setError(getString(R.string.error_field_required));
            focusView = passwordText;
            cancel = true;
        } else if (password.length() <= 4) {
            passwordText.setError(getString(R.string.error_invalid_password));
            focusView = passwordText;
            cancel = true;
        } else if (username.equals(password)) {
            passwordText.setError(getString(R.string.error_invalid_password2));
            focusView = passwordText;
            cancel = true;
        }

        if (TextUtils.isEmpty(confermaPassword) || !confermaPassword.equals(password)) {
            confermaPasswordText.setError(getString(R.string.error_invalid_password3));
            focusView = confermaPasswordText;
            cancel = true;
        }

        if (TextUtils.isEmpty(nome)) {
            modificaNomeText.setError(getString(R.string.error_field_required));
            focusView = modificaNomeText;
            cancel = true;
        }

        if (TextUtils.isEmpty(cognome)) {
            modificaCognomeText.setError(getString(R.string.error_field_required));
            focusView = modificaCognomeText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            Utente utente = new Utente(id ,username, password, nome, cognome, "", 0, false, "ciao");
            Autenticazione autenticazione = new Autenticazione(utente);
            autenticazione.updateUtente(this);

        }

    }

    public void annullamodifica(View v){
        passwordText.setText(utente.getPassword());
        confermaPasswordText.setText(utente.getPassword());
        modificaUsernameText.setText(utente.getUsername());
        modificaCognomeText.setText(utente.getCognome());
        modificaNomeText.setText(utente.getNome());
    }

}
