package com.example.alessandro.gosafe;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.example.alessandro.gosafe.entity.Utente;
import com.example.alessandro.gosafe.server.Autenticazione;

public class RegistrazioneActivity extends AppCompatActivity {

    private EditText mUsername;
    private EditText mPassword;
    //private EditText mEmail;
    private EditText mNome;
    private EditText mCognome;
    private EditText mConfPassword;
    private Context ctx = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrazione);
        mUsername = (EditText) findViewById(R.id.username_reg);
        mPassword = (EditText) findViewById(R.id.password_reg);
        mConfPassword = (EditText) findViewById(R.id.confirmpassword_reg);
        //mEmail = (EditText) findViewById(R.id.email);
        mNome = (EditText) findViewById(R.id.nome);
        mCognome = (EditText) findViewById(R.id.cognome);
    }

    public void registrazione(View v) {
        final String username = mUsername.getText().toString();
        final String password = mPassword.getText().toString();
        final String confermaPassword = mConfPassword.getText().toString();
        //final String email = mEmail.getText().toString();
        final String nome = mNome.getText().toString();
        final String cognome = mCognome.getText().toString();

        mUsername.setError(null);
        mPassword.setError(null);
        mConfPassword.setError(null);
        //mEmail.setError(null);
        mNome.setError(null);
        mCognome.setError(null);

        boolean cancel = false;
        View focusView = null;

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

        /*if (TextUtils.isEmpty(email)) {
            mEmail.setError(getString(R.string.error_field_required));
            focusView = mEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmail.setError(getString(R.string.error_invalid_email));
            focusView = mEmail;
            cancel = true;
        }*/

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
            Utente utente = new Utente(username, password, /*email,*/ nome, cognome, false);
            Autenticazione autenticazione = new Autenticazione(utente);
            autenticazione.registrazioneUtente(this);
        }
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void resetForm(View v) {
        mUsername.setText("");
        mPassword.setText("");
        mConfPassword.setText("");
        mNome.setText("");
        mCognome.setText("");
        //mEmail.setText("");
    }

}
