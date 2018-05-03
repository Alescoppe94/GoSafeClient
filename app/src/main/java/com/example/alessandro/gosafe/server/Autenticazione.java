package com.example.alessandro.gosafe.server;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.example.alessandro.gosafe.LoginActivity;
import com.example.alessandro.gosafe.ProfiloActivity;
import com.example.alessandro.gosafe.R;
import com.example.alessandro.gosafe.VaiActivity;
import com.example.alessandro.gosafe.database.DAOUtente;
import com.example.alessandro.gosafe.entity.Utente;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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

    public void registrazioneUtente(Context ctx/*, String token*/) {
        new registrazioneUtenteTask(utente_attivo, ctx/*, token*/).execute();
    }

    private class registrazioneUtenteTask extends AsyncTask<Void, Void, String> {
        private Utente utente;
        private Context ctx;
        //private String token;
        private ProgressDialog registrazione_in_corso;
        //private AsyncTask<Void, Void, Boolean> execute;

        public registrazioneUtenteTask(Utente utente, Context ctx/*, String token*/) {
            this.utente = utente;
            this.ctx = ctx;
            //this.token = token;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            registrazione_in_corso = new ProgressDialog(ctx);
            registrazione_in_corso.setIndeterminate(true);
            registrazione_in_corso.setCancelable(false);
            registrazione_in_corso.setCanceledOnTouchOutside(false);
            registrazione_in_corso.setMessage(ctx.getString(R.string.registrazione_in_corso));
            registrazione_in_corso.show();
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
                try {
                    Thread.sleep(1500);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Gson gson = new Gson();
                String dati_reg = gson.toJson(utente);

                try {
                    URL url = new URL(PATH + "/gestionemappe/utente/registrazione");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.connect();

                    OutputStream os = connection.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                    osw.write(dati_reg);
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
            registrazione_in_corso.dismiss();

            JsonObject jsonResponse = new Gson().fromJson(result, JsonObject.class);

            if (result == null) {
                AlertDialog registrazione_impossibile = new AlertDialog.Builder(ctx).create();
                registrazione_impossibile.setTitle("Impossibile effettuare la registrazione");
                registrazione_impossibile.setMessage(ctx.getString(R.string.server_not_respond_registrazione));
                registrazione_impossibile.setCanceledOnTouchOutside(false);
                registrazione_impossibile.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                registrazione_impossibile.show();
            } else if(jsonResponse.has("esito")) {
                if (jsonResponse.get("esito").getAsString().equals("Username in uso")) {
                    AlertDialog username_in_uso = new AlertDialog.Builder(ctx).create();
                    username_in_uso.setTitle("Username già in uso");
                    username_in_uso.setMessage(ctx.getString(R.string.registrazione_username_in_uso));
                    username_in_uso.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    username_in_uso.show();
                }
            }
            else {
                JsonObject jobj = new Gson().fromJson(result, JsonObject.class);
                long id_utente = jobj.get("id_utente").getAsLong();
                //new registrazioneTokenTask(token, id_utente).execute();
                utente.setId_utente(id_utente);
                utente.setIs_autenticato(true);
                //utente.registrazioneLocale(ctx);

                AlertDialog accesso_dopo_registrazione = new AlertDialog.Builder(ctx).create();
                accesso_dopo_registrazione.setTitle("Registrazione effettuata con successo");
                accesso_dopo_registrazione.setMessage(ctx.getString(R.string.registrazione_effettuata));
                accesso_dopo_registrazione.setCanceledOnTouchOutside(false);
                accesso_dopo_registrazione.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //utente.loginLocale(ctx, true);
                                Intent i = new Intent(ctx, VaiActivity.class);
                                ctx.startActivity(i);
                            }
                        });
                accesso_dopo_registrazione.show();
            }

        }
    }


    public void autenticazioneUtente(Context ctx/*, String token*/) {
        new autenticazioneUtenteTask(utente_attivo, ctx/*, token*/).execute();
    }

    private class autenticazioneUtenteTask extends AsyncTask<Void, Void, String> {
        private Utente utente;
        private Context ctx;
        /*private String token;*/
        private ProgressDialog login_in_corso;
        private AsyncTask<Void, Void, Boolean> execute;

        public autenticazioneUtenteTask(Utente utente, Context ctx/*, String token*/) {
            this.utente = utente;
            this.ctx = ctx;
            /*this.token = token;*/
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (ctx instanceof LoginActivity) {
                login_in_corso = new ProgressDialog(ctx);
                login_in_corso.setIndeterminate(true);
                login_in_corso.setCancelable(false);
                login_in_corso.setCanceledOnTouchOutside(false);
                login_in_corso.setMessage(ctx.getString(R.string.login_in_corso));
                login_in_corso.show();
            }
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
                    String request = PATH + "/gestionemappe/utente/login";
                    URL url = new URL(request);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    //connection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
                    //connection.setRequestProperty("Accept","*/*");
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

            JsonObject jsonResponse = new Gson().fromJson(result, JsonObject.class);

            if (ctx instanceof LoginActivity) {
                login_in_corso.dismiss();
            }

            /*if (result == null) {
                utente.loginLocale(ctx, false);
            } else */
            if (jsonResponse.has("esito")) {
                if (jsonResponse.get("esito").getAsString().equals("ERROR: Password errata")) {
                    AlertDialog password_errata = new AlertDialog.Builder(ctx).create();
                    password_errata.setTitle("Password errata");
                    password_errata.setMessage(ctx.getString(R.string.error_incorrect_password));
                    password_errata.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    password_errata.show();
                } else if (jsonResponse.get("esito").getAsString().equals("ERROR: Utente non trovato")) {
                    AlertDialog utente_non_trovato = new AlertDialog.Builder(ctx).create();
                    utente_non_trovato.setTitle("Username errato");
                    utente_non_trovato.setMessage(ctx.getString(R.string.error_invalid_username));
                    utente_non_trovato.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    utente_non_trovato.show();
                }
            } else {
                JsonObject jobj = new Gson().fromJson(result, JsonObject.class);
                long id_utente = jobj.get("id").getAsLong();
                String username = jobj.get("username").getAsString();
                String password = jobj.get("password").getAsString();
                //String email = jobj.get("email").getAsString();
                String nome = jobj.get("nome").getAsString();
                String cognome = jobj.get("cognome").getAsString();

                /*if (token != null) {
                    new registrazioneTokenTask(token, id_utente).execute();
                }*/

                utente.setId_utente(id_utente);
                utente.setUsername(username);
                utente.setPassword(password);
                //utente.setEmail(email);
                utente.setNome(nome);
                utente.setCognome(cognome);
                utente.setIs_autenticato(true);
                utente.registrazioneLocale(ctx);
                //utente.loginLocale(ctx, true);

                Intent i = new Intent(ctx, VaiActivity.class);

                ctx.startActivity(i);
            }
        }
    }

    public void updateUtente(Context ctx/*, String token*/) {
        new updateUtenteTask(utente_attivo, ctx/*, token*/).execute();
    }

    private class updateUtenteTask extends AsyncTask<Void, Void, String> {
        private Utente utente;
        private Context ctx;
        //private String token;
        private ProgressDialog update_in_corso;
        //private AsyncTask<Void, Void, Boolean> execute;

        public updateUtenteTask(Utente utente, Context ctx/*, String token*/) {
            this.utente = utente;
            this.ctx = ctx;
            //this.token = token;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            update_in_corso = new ProgressDialog(ctx);
            update_in_corso.setIndeterminate(true);
            update_in_corso.setCancelable(false);
            update_in_corso.setCanceledOnTouchOutside(false);
            update_in_corso.setMessage(ctx.getString(R.string.update_in_corso));
            update_in_corso.show();
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
                try {
                    Thread.sleep(1500);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Gson gson = new Gson();
                String dati_reg = gson.toJson(utente);

                try {
                    URL url = new URL(PATH + "/gestionemappe/utente/modifica");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.connect();

                    OutputStream os = connection.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                    osw.write(dati_reg);
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
            update_in_corso.dismiss();

            JsonObject jsonResponse = new Gson().fromJson(result, JsonObject.class);

            if (result == null) {
                AlertDialog registrazione_impossibile = new AlertDialog.Builder(ctx).create();
                registrazione_impossibile.setTitle("Impossibile effettuare la modifica");
                registrazione_impossibile.setMessage(ctx.getString(R.string.server_not_respond_registrazione));
                registrazione_impossibile.setCanceledOnTouchOutside(false);
                registrazione_impossibile.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                registrazione_impossibile.show();
            } else if(jsonResponse.has("esito")) {
                if (jsonResponse.get("esito").getAsString().equals("Username in uso")) {
                    AlertDialog username_in_uso = new AlertDialog.Builder(ctx).create();
                    username_in_uso.setTitle("Username già in uso");
                    username_in_uso.setMessage(ctx.getString(R.string.registrazione_username_in_uso));
                    username_in_uso.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    username_in_uso.show();
                } else {
                    JsonObject jobj = new Gson().fromJson(result, JsonObject.class);
                    String successo = jobj.get("esito").getAsString();
                    //new registrazioneTokenTask(token, id_utente).execute();*/

                    Log.d("caione", successo);

                    if (successo.equals("success")) {
                        DAOUtente daoutente = new DAOUtente(ctx);
                        daoutente.open();
                        daoutente.update(utente);
                        daoutente.close();
                    }


                    Intent i;
                    i = new Intent(ctx, ProfiloActivity.class);
                    i.putExtra("selezione", "profilo");
                    ctx.startActivity(i);
                }
            }

        }
    }

}

