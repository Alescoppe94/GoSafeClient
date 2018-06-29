package com.example.alessandro.gosafe;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.example.alessandro.gosafe.database.DAOUtente;
import com.example.alessandro.gosafe.entity.Utente;
import com.example.alessandro.gosafe.server.Autenticazione;


public class ModificaActivity extends DefaultActivity {

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
            DAOUtente daoUtente = new DAOUtente(this);
            daoUtente.open();
            Utente utente_old = daoUtente.findUtente();
            daoUtente.close();
            Utente utente = new Utente(id ,username, password, nome, cognome,utente_old.getBeaconid(),utente_old.getPercorsoid(),utente_old.getIs_autenticato(),utente_old.getToken(),utente_old.getIdsessione());
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
        Intent i = new Intent(this, ProfiloActivity.class);
        startActivity(i);
    }

}
