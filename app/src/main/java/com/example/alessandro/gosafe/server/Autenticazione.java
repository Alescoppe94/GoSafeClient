package com.example.alessandro.gosafe.server;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.example.alessandro.gosafe.LoginActivity;
import com.example.alessandro.gosafe.MainActivity;
import com.example.alessandro.gosafe.R;
import com.example.alessandro.gosafe.entity.Utente;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created by Alessandro on 16/03/2018.
 */

public class Autenticazione {

    private Utente utente_attivo;
    private HttpURLConnection connection;
    private final String PATH = "http://10.0.2.2:8080";

    public Autenticazione(Utente utente_attivo) {
        this.utente_attivo = utente_attivo;
    }

    public void autenticazioneUtente(Context ctx/*, String token*/) {
        new autenticazioneUtenteTask(utente_attivo, ctx/*, token*/).execute();
    }

    private class autenticazioneUtenteTask extends AsyncTask<Void, Void, String> {
        private Utente utente;
        private Context ctx;
        private String token;
        private ProgressDialog login_in_corso;
        private AsyncTask<Void, Void, Boolean> execute;

        public autenticazioneUtenteTask(Utente utente, Context ctx/*, String token*/) {
            this.utente = utente;
            this.ctx = ctx;
            //this.token = token;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /*if (ctx instanceof LoginActivity) {
                login_in_corso = new ProgressDialog(ctx);
                login_in_corso.setIndeterminate(true);
                login_in_corso.setCancelable(false);
                login_in_corso.setCanceledOnTouchOutside(false);
                login_in_corso.setMessage(ctx.getString(R.string.login_in_corso));
                login_in_corso.show();
            }*/
            //execute = new controlloConnessioneTask().execute();
        }

        @Override
        protected String doInBackground(Void... arg0) {
            boolean connesso = true;

            /*try {
                connesso = execute.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }*/

            if (!connesso) {
                return null;
            } else {
                if (ctx instanceof LoginActivity) {
                    try {
                        Thread.sleep(1500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Gson gson = new Gson();
                String dati_login = gson.toJson(utente);

                try {
                    URL url = new URL(PATH + "/gestionemappe/utente/login");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.connect();

                    OutputStream os = connection.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                    osw.write(dati_login);
                    osw.flush();
                    osw.close();

                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    String inputLine;

                    while ((inputLine = br.readLine()) != null) {
                        sb.append(inputLine + "\n");
                    }

                    br.close();
                    return sb.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        try {
                            connection.disconnect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... arg0) {

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Intent i = new Intent(ctx, MainActivity.class);
            ctx.startActivity(i);

            /*if (ctx instanceof LoginActivity) {
                login_in_corso.dismiss();
            }*/

            /*if (result == null) {
                utente.loginLocale(ctx, false);
            } else if (result.contains("Password errata")) {
                AlertDialog password_errata = new AlertDialog.Builder(ctx).create();
                password_errata.setTitle("Password errata");
                password_errata.setMessage(ctx.getString(R.string.login_incorrect_password));
                password_errata.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                password_errata.show();
            } else if (result.contains("Utente non trovato")) {
                AlertDialog utente_non_trovato = new AlertDialog.Builder(ctx).create();
                utente_non_trovato.setTitle("Username errato");
                utente_non_trovato.setMessage(ctx.getString(R.string.login_incorrect_username));
                utente_non_trovato.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                utente_non_trovato.show();
            } else {
                JsonObject jobj = new Gson().fromJson(result, JsonObject.class);
                long id_utente = jobj.get("id_utente").getAsLong();
                String username = jobj.get("username").getAsString();
                String password = jobj.get("password").getAsString();
                String email = jobj.get("email").getAsString();
                String nome = jobj.get("nome").getAsString();
                String cognome = jobj.get("cognome").getAsString(); */

                /*if (token != null) {
                    new registrazioneTokenTask(token, id_utente).execute();
                }*/

                /*
                utente.setId_utente(id_utente);
                utente.setUsername(username);
                utente.setPassword(password);
                utente.setEmail(email);
                utente.setNome(nome);
                utente.setCognome(cognome);
                utente.setIs_autenticato(true);
                utente.registrazioneLocale(ctx);
                utente.loginLocale(ctx, true);
                */
            }
        }
}

