package com.example.alessandro.gosafe;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.alessandro.gosafe.entity.Utente;
import com.example.alessandro.gosafe.server.Autenticazione;

/**
 * Classe che implementa la form di login.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText mUsernameView;
    private EditText mPasswordView;
    private EditText mIpAddressView;
    private final static int REQUEST_ENABLE_BT=1;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    /**
     * Inizializza la classe di login
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //verifica se il bluetooth è attivo, chiedendo all'utente di attivarlo qualora non lo sia
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        //richiede accesso alla posizione altrimenti non funziona il rilevamento dei beacon
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

        mUsernameView = (EditText) findViewById(R.id.username);
        mUsernameView.requestFocus();

        //fa partire il login
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mIpAddressView = (EditText) findViewById(R.id.ipaddress);

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        //fa partire l'activity di signup
        Button mSignUpButton = (Button) findViewById(R.id.signup);
        mSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), RegistrazioneActivity.class);
                startActivity(i);
            }
        });

        /*Consente di mantenere la connessione anche dopo aver messo l'applicazione in background*/
        SharedPreferences.Editor editor = this.getSharedPreferences("isConnesso", this.MODE_PRIVATE).edit();
        editor.putBoolean("connesso", false);
        editor.apply();

    }

    /**
     * Prova a effettuare il login dell'utente sul server
     */
    private void attemptLogin() {

        mUsernameView.setError(null);
        mPasswordView.setError(null);

        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        /*Controlla se la password è valida e se l'utente ne ha inserita una.*/
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        /*Controlla la validità dello username*/
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {

            focusView.requestFocus();

        } else { // controlla l'indirizzo ip inserito
            if(mIpAddressView.getText().toString().length() < 2){
                SharedPreferences.Editor editor = getSharedPreferences("ipAddress", MODE_PRIVATE).edit();
                editor.putString("ipAddress", "10.0.2.2:8080");
                editor.commit();
            }else {
                SharedPreferences.Editor editor = getSharedPreferences("ipAddress", MODE_PRIVATE).edit();
                editor.putString("ipAddress", mIpAddressView.getText().toString());
                editor.commit();
            }
            Utente utente = new Utente(username, password);
            Autenticazione autenticazione = new Autenticazione(utente);
            autenticazione.autenticazioneUtente(this);
        }
    }

    /*Verifica che la password sia più lunga di 2 caratteri*/
    private boolean isPasswordValid(String password) {
        return password.length() > 2;
    }


    /**
     * Metodo che avvisa l'utente che il servizio di individuazione dei beacon non funziona se non si consente
     * l'accesso alla posizione
     * @param requestCode codice della richiesta
     * @param permissions permessi
     * @param grantResults garantire l'accesso
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (!(grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Funzionalità Limitata");
                    builder.setMessage("Dal momento che l'accesso alla posizione non è stata consentita, questa app non è in grado di individuare i beacon in background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

}

