package com.example.alessandro.gosafe;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.example.alessandro.gosafe.entity.Utente;
import com.example.alessandro.gosafe.server.Autenticazione;

/**
 * Classe che implementa la funzionalità di registrazione degli utenti
 */
public class RegistrazioneActivity extends AppCompatActivity {

    private EditText mUsername;
    private EditText mPassword;
    private EditText mNome;
    private EditText mCognome;
    private EditText mConfPassword;
    private EditText mIpAddress;

    /**
     * Inizializzazione della classe, le caselle di input vengono salvate in delle variabili
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrazione);
        mUsername = (EditText) findViewById(R.id.username_reg);
        mUsername.requestFocus();
        mPassword = (EditText) findViewById(R.id.password_reg);
        mConfPassword = (EditText) findViewById(R.id.confirmpassword_reg);
        mNome = (EditText) findViewById(R.id.nome);
        mCognome = (EditText) findViewById(R.id.cognome);
        mIpAddress = (EditText) findViewById(R.id.ipaddress);
    }

    /**
     * Esecuzione della registrazione dell'utente
     * @param v
     */
    public void registrazione(View v) {
        final String username = mUsername.getText().toString();
        final String password = mPassword.getText().toString();
        final String confermaPassword = mConfPassword.getText().toString();
        final String nome = mNome.getText().toString();
        final String cognome = mCognome.getText().toString();

        mUsername.setError(null);
        mPassword.setError(null);
        mConfPassword.setError(null);
        mNome.setError(null);
        mCognome.setError(null);

        boolean cancel = false;
        View focusView = null;

        /*Verifica dell'obbliglatorietà dei campi richiesti*/
        if (TextUtils.isEmpty(username)) {
            mUsername.setError(getString(R.string.error_field_required));
            focusView = mUsername;
            cancel = true;
        } else if (username.length() <= 2) {
            mUsername.setError(getString(R.string.error_invalid_username));
            focusView = mUsername;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            mPassword.setError(getString(R.string.error_field_required));
            focusView = mPassword;
            cancel = true;
        } else if (password.length() <= 4) {
            mPassword.setError(getString(R.string.error_invalid_password));
            focusView = mPassword;
            cancel = true;
        } else if (username.equals(password)) {
            mPassword.setError(getString(R.string.error_invalid_password2));
            focusView = mPassword;
            cancel = true;
        }

        if (TextUtils.isEmpty(confermaPassword) || !confermaPassword.equals(password)) {
            mConfPassword.setError(getString(R.string.error_invalid_password3));
            focusView = mConfPassword;
            cancel = true;
        }

        if (TextUtils.isEmpty(nome)) {
            mNome.setError(getString(R.string.error_field_required));
            focusView = mNome;
            cancel = true;
        }

        if (TextUtils.isEmpty(cognome)) {
            mCognome.setError(getString(R.string.error_field_required));
            focusView = mCognome;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            if(mIpAddress.getText().toString().length() < 2){
                SharedPreferences.Editor editor = getSharedPreferences("ipAddress", MODE_PRIVATE).edit();
                editor.putString("ipAddress", "10.0.2.2:8080");
                editor.commit();
            }else {
                SharedPreferences.Editor editor = getSharedPreferences("ipAddress", MODE_PRIVATE).edit();
                editor.putString("ipAddress", mIpAddress.getText().toString());
                editor.commit();
            }
            Utente utente = new Utente(username, password, /*email,*/ nome, cognome, false);
            Autenticazione autenticazione = new Autenticazione(utente);
            autenticazione.registrazioneUtente(this);
        }
    }

    /**
     * Resetta la form di registrazione.
     * @param v
     */
    public void resetForm(View v) {
        mUsername.setText("");
        mPassword.setText("");
        mConfPassword.setText("");
        mNome.setText("");
        mCognome.setText("");
    }

}
