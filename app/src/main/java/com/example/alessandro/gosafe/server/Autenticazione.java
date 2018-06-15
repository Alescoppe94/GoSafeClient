package com.example.alessandro.gosafe.server;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.example.alessandro.gosafe.EmergenzaActivity;
import com.example.alessandro.gosafe.LoginActivity;
import com.example.alessandro.gosafe.ProfiloActivity;
import com.example.alessandro.gosafe.R;
import com.example.alessandro.gosafe.UserSessionManager;
import com.example.alessandro.gosafe.VaiActivity;
import com.example.alessandro.gosafe.beacon.BluetoothLeService;
import com.example.alessandro.gosafe.database.DAOBeacon;
import com.example.alessandro.gosafe.database.DAOUtente;
import com.example.alessandro.gosafe.entity.Utente;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
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

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Alessandro on 16/03/2018.
 */

public class Autenticazione {

    private UserSessionManager session;
    private Utente utente_attivo;
    private HttpURLConnection connection;
    private final String PATH = "http://10.0.2.2:8080";

    public Autenticazione(Utente utente_attivo) {
        this.utente_attivo = utente_attivo;
    }

    public void registrazioneUtente(Context ctx/*, String token*/) {
        session = new UserSessionManager(ctx);
        new registrazioneUtenteTask(utente_attivo, ctx/*, token*/).execute();
    }

    private class registrazioneUtenteTask extends AsyncTask<Void, Void, String> {
        private Utente utente;
        private Context ctx;
        //private String token;
        private ProgressDialog registrazione_in_corso;
        private boolean connesso;

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
            CheckConnessione checkConnessione = new CheckConnessione();
            connesso = checkConnessione.checkConnessione(ctx);
        }

        @Override
        protected String doInBackground(Void... arg0) {

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
                String idsessione = jobj.get("idsessione").getAsString();
                //new registrazioneTokenTask(token, id_utente).execute();

                final boolean emergenza = jobj.get("emergenza").getAsBoolean();
                SharedPreferences.Editor editor = ctx.getSharedPreferences("isEmergenza", MODE_PRIVATE).edit();
                editor.putBoolean("emergenza", emergenza);
                editor.apply();

                utente.setId_utente(id_utente);
                utente.setIs_autenticato(true);
                utente.setIdsessione(idsessione);
                utente.registrazioneLocale(ctx);

                DbDownloadFirstBoot dbDownload = new DbDownloadFirstBoot();
                dbDownload.dbdownloadFirstBootAsyncTask(ctx);

                startUpServices(ctx);

                session.createUserLoginSession("User Session", utente.getUsername());

                dbDownload.getResult();

                AlertDialog accesso_dopo_registrazione = new AlertDialog.Builder(ctx).create();
                accesso_dopo_registrazione.setTitle("Registrazione effettuata con successo");
                accesso_dopo_registrazione.setMessage(ctx.getString(R.string.registrazione_effettuata));
                accesso_dopo_registrazione.setCanceledOnTouchOutside(false);
                accesso_dopo_registrazione.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //utente.loginLocale(ctx, true);
                                if(!emergenza) {
                                    Intent i = new Intent(ctx, VaiActivity.class);
                                    ctx.startActivity(i);
                                } else {
                                    Intent i = new Intent(ctx, EmergenzaActivity.class);
                                    ctx.startActivity(i);
                                }

                            }
                        });
                accesso_dopo_registrazione.show();
            }

        }
    }


    public void autenticazioneUtente(Context ctx/*, String token*/) {
        session = new UserSessionManager(ctx);
        new autenticazioneUtenteTask(utente_attivo, ctx/*, token*/).execute();
    }

    private class autenticazioneUtenteTask extends AsyncTask<Void, Void, String> {
        private Utente utente;
        private Context ctx;
        /*private String token;*/
        private ProgressDialog login_in_corso;
        private boolean connesso;

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
                CheckConnessione checkConnessione = new CheckConnessione();
                connesso = checkConnessione.checkConnessione(ctx);
            }
        }

        @Override
        protected String doInBackground(Void... arg0) {

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
                    System.out.println(sb.toString());
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

            if (result == null) {
                DAOUtente daoUtente = new DAOUtente(ctx);
                daoUtente.open();
                Utente utentedb = daoUtente.getUserByUsername(utente.getUsername());
                daoUtente.close();
                if(utentedb != null) {
                    if(utente.getPassword().equals(utentedb.getPassword())) {
                        utente.setId_utente(utentedb.getId_utente());
                        utente.setUsername(utentedb.getUsername());
                        utente.setPassword(utentedb.getPassword());
                        utente.setNome(utentedb.getNome());
                        utente.setCognome(utentedb.getCognome());
                        utente.setIs_autenticato(true);
                        session.createUserLoginSession("User Session", utente.getUsername());
                        //TODO: startUpService?
                        Intent i = new Intent(ctx, VaiActivity.class);
                        ctx.startActivity(i);
                    } else {
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
                    }
                } else {
                    AlertDialog utente_non_trovato = new AlertDialog.Builder(ctx).create();
                    utente_non_trovato.setTitle("Impossibile effettuare il login");
                    utente_non_trovato.setMessage(ctx.getString(R.string.error_invalid_login_hostuser));
                    utente_non_trovato.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    utente_non_trovato.show();

                    //TODO: startUpService?
                    //TODO: vai all'interfaccia dell'utente anonimo
                }

            } else {
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
                    JsonObject utenteJson =(JsonObject) jobj.get("utente");
                    long id_utente = utenteJson.get("id").getAsLong();
                    String username = utenteJson.get("username").getAsString();
                    String password = utenteJson.get("password").getAsString();
                    String beaconId = "1"; //jobj.get("beaconId").getAsString(); //TODO: da sistemare
                    String nome = utenteJson.get("nome").getAsString();
                    String cognome = utenteJson.get("cognome").getAsString();
                    String idsessione = utenteJson.get("idsessione").getAsString();

                    boolean emergenza = jobj.get("emergenza").getAsBoolean();
                    SharedPreferences.Editor editor = ctx.getSharedPreferences("isEmergenza", MODE_PRIVATE).edit();
                    editor.putBoolean("emergenza", emergenza);
                    editor.apply();
                /*if (token != null) {
                    new registrazioneTokenTask(token, id_utente).execute();
                }*/

                    utente.setId_utente(id_utente);
                    utente.setUsername(username);
                    utente.setPassword(password);
                    utente.setBeaconid(beaconId);
                    utente.setNome(nome);
                    utente.setCognome(cognome);
                    utente.setIdsessione(idsessione);
                    utente.setIs_autenticato(true);
                    utente.registrazioneLocale(ctx);
                    //utente.loginLocale(ctx, true);
                    session.createUserLoginSession("User Session", utente.getUsername());

                    DbDownloadFirstBoot dbDownload = new DbDownloadFirstBoot();
                    dbDownload.dbdownloadFirstBootAsyncTask(ctx);

                    dbDownload.getResult();

                    startUpServices(ctx);

                    if(!emergenza) {
                        Intent i = new Intent(ctx, VaiActivity.class);
                        ctx.startActivity(i);
                    } else {
                        Intent i = new Intent(ctx, EmergenzaActivity.class);
                        ctx.startActivity(i);
                    }

                }
            }
        }
    }

    public void updateUtente(Context ctx/*, String token*/) {
        new UpdateUtenteTask(utente_attivo, ctx/*, token*/).execute();
    }

    private class UpdateUtenteTask extends AsyncTask<Void, Void, String> {
        private Utente utente;
        private Context ctx;
        //private String token;
        private ProgressDialog update_in_corso;
        private boolean connesso;

        public UpdateUtenteTask(Utente utente, Context ctx/*, String token*/) {
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
            CheckConnessione checkConnessione = new CheckConnessione();
            connesso = checkConnessione.checkConnessione(ctx);
        }

        @Override
        protected String doInBackground(Void... arg0) {

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
                    byte[] data = utente.getIdsessione().getBytes("UTF-8");
                    String base64 = Base64.encodeToString(data,Base64.DEFAULT);
                    URL url = new URL(PATH + "/gestionemappe/utente/secured/modifica");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Authorization", "basic " + base64);
                    connection.connect();

                    OutputStream os = connection.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                    osw.write(dati_reg);
                    osw.flush();
                    osw.close();

                    int responseCode = connection.getResponseCode();
                    if(400 <= responseCode && responseCode <= 499){
                        update_in_corso.dismiss();
                        this.cancel(true);
                    }

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
                AlertDialog update_impossibile = new AlertDialog.Builder(ctx).create();
                update_impossibile.setTitle("Impossibile effettuare la modifica");
                update_impossibile.setMessage(ctx.getString(R.string.server_not_respond_update_info_utente));
                update_impossibile.setCanceledOnTouchOutside(false);
                update_impossibile.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                update_impossibile.show();
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

    public void logoutUtente(Context ctx) {

        session = new UserSessionManager(ctx);
        new LogoutUtenteTask(utente_attivo, ctx).execute();
    }

    private class LogoutUtenteTask extends AsyncTask<Void, Void, String>{

        private Utente utente;
        private Context ctx;
        private ProgressDialog logout_in_corso;
        private boolean connesso;

        public LogoutUtenteTask(Utente utente, Context ctx) {
            this.utente = utente;
            this.ctx = ctx;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            CheckConnessione checkConnessione = new CheckConnessione();
            connesso = checkConnessione.checkConnessione(ctx);
        }

        @Override
        protected String doInBackground(Void... voids) {

            if (!connesso) {
                return null;
            } else {
                HttpURLConnection conn = null;

                Gson gson = new Gson();
                String dati_utente = gson.toJson(utente);

                try {
                    byte[] data = utente.getIdsessione().getBytes("UTF-8");
                    String base64 = Base64.encodeToString(data,Base64.DEFAULT);
                    URL url = new URL(PATH+ "/gestionemappe/utente/secured/logout");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("Authorization", "basic " + base64);
                    conn.setInstanceFollowRedirects(true);
                    conn.connect();

                    OutputStream os = conn.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                    osw.write(dati_utente);
                    osw.flush();
                    osw.close();

                    int responseCode = conn.getResponseCode();
                    if(400 <= responseCode && responseCode <= 499){
                        this.cancel(true);
                    }

                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    String inputLine;

                    while ((inputLine = br.readLine()) != null) {
                        sb.append(inputLine + "\n");
                    }

                    br.close();
                    return sb.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (conn != null) {
                        try {
                            conn.disconnect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                AlertDialog logout_impossibile = new AlertDialog.Builder(ctx).create();
                logout_impossibile.setTitle("Impossibile effettuare il logout");
                logout_impossibile.setMessage(ctx.getString(R.string.server_not_respond_logout));
                logout_impossibile.setCanceledOnTouchOutside(false);
                logout_impossibile.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                logout_impossibile.show();
            } else {
                DAOUtente daoUtente = new DAOUtente(ctx);
                daoUtente.open();
                daoUtente.deleteAll();
                daoUtente.close();
                /*Intent i;
                i = new Intent(ctx, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ctx.startActivity(i); */
                session.logOutUser();
            }
        }


    }

    private void startUpServices(Context ctx){
        Utente user;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
            DAOUtente daoUtente = new DAOUtente(ctx);
            daoUtente.open();
            user = daoUtente.findUtente();
            daoUtente.close();
            Intent s = new Intent(ctx, BluetoothLeService.class);            //rimanda l'utente al servizio, può essere modificato
            Bundle bundle = new Bundle();
            bundle.putSerializable("user", user);
            bundle.putLong("periodo", 15000);
            s.putExtras(bundle);
            ctx.startService(s);
        }
        DAOBeacon daoBeacon = new DAOBeacon(ctx);
        daoBeacon.open();
        DAOUtente daoUtente = new DAOUtente(ctx);
        daoUtente.open();

        user = daoUtente.findUtente();


        Intent u = new Intent(ctx, CheckForDbUpdatesService.class);
        ctx.startService(u);

    }

}

