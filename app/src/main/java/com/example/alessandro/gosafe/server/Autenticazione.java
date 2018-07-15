package com.example.alessandro.gosafe.server;

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

import com.example.alessandro.gosafe.EmergenzaActivity;
import com.example.alessandro.gosafe.LoginActivity;
import com.example.alessandro.gosafe.ProfiloActivity;
import com.example.alessandro.gosafe.R;
import com.example.alessandro.gosafe.helpers.UserSessionManager;
import com.example.alessandro.gosafe.VaiActivity;
import com.example.alessandro.gosafe.beacon.BluetoothLeService;
import com.example.alessandro.gosafe.database.DAOUtente;
import com.example.alessandro.gosafe.entity.Utente;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.Context.MODE_PRIVATE;

/**
 * Classe che gestisce la fase di autenticazione, registrazione e logout.
 */
public class Autenticazione {

    private UserSessionManager session;
    private Utente utente_attivo;
    private HttpURLConnection connection;

    /**
     * Costruttore
     * @param utente_attivo riceve l'utente attivo che vuole fare un'operazione
     */
    public Autenticazione(Utente utente_attivo) {
        this.utente_attivo = utente_attivo;
    }

    /**
     * Metodo che lancia l'AsyncTask di registrazione. Crea anche una sessione
     * @param ctx
     */
    public void registrazioneUtente(Context ctx) {
        session = new UserSessionManager(ctx);
        new registrazioneUtenteTask(utente_attivo, ctx).execute();
    }

    /**
     * Classe che si occupa dell'AsyncTask della reditrazione
     */
    private class registrazioneUtenteTask extends AsyncTask<Void, Void, String> {
        private Utente utente;
        private Context ctx;
        private ProgressDialog registrazione_in_corso;
        private boolean connesso;

        /**
         * Costruttore
         * @param utente utente che si vuole registrare
         * @param ctx context dell'applicazione
         */
        public registrazioneUtenteTask(Utente utente, Context ctx) {
            this.utente = utente;
            this.ctx = ctx;
        }

        /**
         * Metodo eseguito prima dell'AsyncTask vero e proprio. Si occupa di istanziare il ProgressDialog e di controllare la connessione al server
         */
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

        /**
         * AsyncTask vero e proprio. Non è più eseguito sul Thread principale. Compie una chiamata POST al server con le informazioni
         * del nuovo utente registrato.
         * @param arg0 argomento che effettivamente è vuoto
         * @return ritorna l'esito dell'operazione sotto forma di una stringa
         */
        @Override
        protected String doInBackground(Void... arg0) {

            if (!connesso) {
                return null;
            } else {

                Gson gson = new Gson();
                String dati_reg = gson.toJson(utente);

                try {
                    SharedPreferences prefs = ctx.getSharedPreferences("ipAddress", MODE_PRIVATE);
                    String path = prefs.getString("ipAddress", null);
                    URL url = new URL("http://" + path + "/gestionemappe/utente/registrazione"); //url al quale viene fatta la richiesta
                    connection = (HttpURLConnection) url.openConnection();
                    //si setta l'header della richiesta
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.connect();

                    //si scrivono i dati sullo stream di output
                    OutputStream os = connection.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                    osw.write(dati_reg);
                    osw.flush();
                    osw.close();

                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    String inputLine;

                    //si legge la risposta dal server
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

        /**
         * Metodo che viene eseguito subito dopo doInBackgroud(). E' di raccordo tra l'AsyncTask e il thread principale.
         * Analizza il risultato ricevuto dal server facendo il parsing del json di risposta. Verifica se la registrazione ha avuto
         * successo o meno
         * @param result risultato ricevuto dal server
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            registrazione_in_corso.dismiss();

            //si converte il risultato in formato stringa in un json
            JsonObject jsonResponse = new Gson().fromJson(result, JsonObject.class);

            //se il risultato è nullo non ci si può registrare
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
            } else if(jsonResponse.has("esito")) { //se contiene un esito si va a vedere che tipo di esito è
                if (jsonResponse.get("esito").getAsString().equals("Username in uso")) { //entra nell'if se lo username è in uso
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
            //se entra nell'else significa che la registrazione ha avuto successo
            else {
                //si crea la sessione
                JsonObject jobj = new Gson().fromJson(result, JsonObject.class);
                long id_utente = jobj.get("id_utente").getAsLong();
                String idsessione = jobj.get("idsessione").getAsString();

                //si controlla se c'è un'emergenza
                final boolean emergenza = jobj.get("emergenza").getAsBoolean();
                SharedPreferences.Editor editor = ctx.getSharedPreferences("isEmergenza", MODE_PRIVATE).edit();
                editor.putBoolean("emergenza", emergenza);
                editor.apply();

                //si impostano le informazioni dell'utente
                utente.setId_utente(id_utente);
                utente.setIs_autenticato(true);
                utente.setBeaconid("12");
                utente.setIdsessione(idsessione);
                utente.registrazioneLocale(ctx);

                //si scarica il db
                DbDownloadFirstBoot dbDownload = new DbDownloadFirstBoot();
                dbDownload.dbdownloadFirstBootAsyncTask(ctx);

                session.createUserLoginSession("User Session", utente.getUsername());

                dbDownload.getResult();

                //metodo che avvia i servizi di base dell'app
                startUpServices(ctx);

                //messaggio di successo
                AlertDialog accesso_dopo_registrazione = new AlertDialog.Builder(ctx).create();
                accesso_dopo_registrazione.setTitle("Registrazione effettuata con successo");
                accesso_dopo_registrazione.setMessage(ctx.getString(R.string.registrazione_effettuata));
                accesso_dopo_registrazione.setCanceledOnTouchOutside(false);
                accesso_dopo_registrazione.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //utente.loginLocale(ctx, true);
                                if(!emergenza) {
                                    Intent i = new Intent(ctx, VaiActivity.class); //se non c'è un'emergenza si viene dirottati su VaiActivity
                                    ctx.startActivity(i);
                                } else {
                                    Intent i = new Intent(ctx, EmergenzaActivity.class); //se c'è emergenza si viene dirottati su EmergenzaActivity
                                    ctx.startActivity(i);
                                }

                            }
                        });
                accesso_dopo_registrazione.show();
            }

        }
    }


    /**
     * Metodo che si occupa di lanciare l'AsyncTask che si occupa dell'autenticazione
     * @param ctx context dell'applicazione
     */
    public void autenticazioneUtente(Context ctx) {
        session = new UserSessionManager(ctx);
        new autenticazioneUtenteTask(utente_attivo, ctx).execute();
    }

    /**
     * Classe che gestisce l'AsyncTask dell'autenticazione
     */
    private class autenticazioneUtenteTask extends AsyncTask<Void, Void, String> {
        private Utente utente;
        private Context ctx;
        private ProgressDialog login_in_corso;
        private boolean connesso;

        /**
         * Costruttore
         * @param utente utente da loggare
         * @param ctx context dell'applicazione
         */
        public autenticazioneUtenteTask(Utente utente, Context ctx) {
            this.utente = utente;
            this.ctx = ctx;
        }

        /**
         * Metodo eseguito prima dell'AsyncTask vero e proprio. Crea un ProgressDialog. Controlla anche la connessione al server
         */
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

        /**
         * Metodo che esegue l'AsyncTask vero e proprio. esegue una richiesta POST al server per fare l'autenticazione
         * @param arg0 in questo caso è vuoto
         * @return ritorna una stringa con l'esito dell'operazione
         */
        @Override
        protected String doInBackground(Void... arg0) {

            //controlla la connessione
            if (!connesso) {
                return null;
            } else {

                Gson gson = new Gson();
                String dati_login = gson.toJson(utente);

                try {
                    SharedPreferences prefs = ctx.getSharedPreferences("ipAddress", MODE_PRIVATE);
                    String path = prefs.getString("ipAddress", null);
                    String request = "http://" + path + "/gestionemappe/utente/login"; //url a cui fare la richiesta
                    URL url = new URL(request);
                    connection = (HttpURLConnection) url.openConnection();
                    //imposta l'header della richiesta
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.connect();

                    //scrive l'utente da loggare nell'output stream
                    OutputStream os = connection.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                    osw.write(dati_login);
                    osw.flush();
                    osw.close();

                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    String inputLine;

                    //legge la risposta del server
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

        /**
         * Metodo che viene eseguito non appena doInBackground termina. E' di raccordo tra l'AsyncTask e il thread principale
         * Analizza la risposta del server per verificare se il login ha avuto successo o meno
         * @param result risultato del server
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            JsonObject jsonResponse = new Gson().fromJson(result, JsonObject.class);

            //se result è nullo il login non ha avuto successo perchè il server è offline
            if (result == null) {
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
            } else {
                if (jsonResponse.has("esito")) { //se ha un esito si controlla che tipo di errore l'utente ha commesso
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
                } else { // se entra in questo else significa che il login ha avuto successo
                    JsonObject jobj = new Gson().fromJson(result, JsonObject.class);
                    JsonObject utenteJson =(JsonObject) jobj.get("utente");
                    long id_utente = utenteJson.get("id").getAsLong();
                    String username = utenteJson.get("username").getAsString();
                    String password = utenteJson.get("password").getAsString();
                    String beaconId = "31"; //si assegna un beacon provvisorio iniziale. poi verrà aggiornato con la connessione ai beacon
                    String nome = utenteJson.get("nome").getAsString();
                    String cognome = utenteJson.get("cognome").getAsString();
                    String idsessione = utenteJson.get("idsessione").getAsString();

                    boolean emergenza = jobj.get("emergenza").getAsBoolean();
                    SharedPreferences.Editor editor = ctx.getSharedPreferences("isEmergenza", MODE_PRIVATE).edit();
                    editor.putBoolean("emergenza", emergenza);
                    editor.apply();

                    //imposta gli attributi dell'utente dopo aver fatto il parsing della risposta json
                    utente.setId_utente(id_utente);
                    utente.setUsername(username);
                    utente.setPassword(password);
                    utente.setBeaconid(beaconId);
                    utente.setNome(nome);
                    utente.setCognome(cognome);
                    utente.setIdsessione(idsessione);
                    utente.setIs_autenticato(true);
                    utente.registrazioneLocale(ctx);
                    session.createUserLoginSession("User Session", utente.getUsername());

                    //scarica il db all'avvio
                    DbDownloadFirstBoot dbDownload = new DbDownloadFirstBoot();
                    dbDownload.dbdownloadFirstBootAsyncTask(ctx);

                    dbDownload.getResult();

                    //avvia i servizi di base dell'applicazione
                    startUpServices(ctx);

                    //esegue un activity diversa in base a se c'è una situazione di emergenza o meno
                    if(!emergenza) {
                        Intent i = new Intent(ctx, VaiActivity.class);
                        ctx.startActivity(i);
                    } else {
                        Intent i = new Intent(ctx, EmergenzaActivity.class);
                        ctx.startActivity(i);
                    }


                }
            }
            if (ctx instanceof LoginActivity) {
                login_in_corso.dismiss();
            }
        }
    }

    /**
     * Metodo che avvia l'AsyncTask che si occupa dell'update dell'utente
     * @param ctx context dell'applicazione
     */
    public void updateUtente(Context ctx) {
        new UpdateUtenteTask(utente_attivo, ctx).execute();
    }

    /**
     * Classe che modella l'AsyncTask utilizzato per fare l'update delle informazioni dell'utente sul server
     */
    private class UpdateUtenteTask extends AsyncTask<Void, Void, String> {
        private Utente utente;
        private Context ctx;
        private ProgressDialog update_in_corso;
        private boolean connesso;

        /**
         * Costruttore
         * @param utente utente da aggiornare
         * @param ctx context dell'applicazione
         */
        public UpdateUtenteTask(Utente utente, Context ctx) {
            this.utente = utente;
            this.ctx = ctx;
        }

        /**
         * Metodo eseguito prima dell'AsyncTask vero e proprio che crea un ProgressDialog e controlla la connettività al server
         */
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

        /**
         * Metodo che rappresenta l'AsyncTask vero e proprio. Si occupa di fare una richiesta Post al server con le informazioni aggiornate dell'utente
         * @param arg0 è un parametro in questo caso vuota
         * @return ritorna l'esito dell'operazione sul server
         */
        @Override
        protected String doInBackground(Void... arg0) {

            if (!connesso) {
                return null;
            } else {

                Gson gson = new Gson();
                String dati_reg = gson.toJson(utente);

                try {
                    SharedPreferences prefs = ctx.getSharedPreferences("ipAddress", MODE_PRIVATE);
                    String path = prefs.getString("ipAddress", null);
                    byte[] data = utente.getIdsessione().getBytes("UTF-8");
                    String base64 = Base64.encodeToString(data,Base64.DEFAULT);
                    URL url = new URL("http://" + path + "/gestionemappe/utente/secured/modifica"); // url verso cui fare la richiest
                    connection = (HttpURLConnection) url.openConnection();
                    //imposta l'header della richiesta
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Authorization", "basic " + base64);
                    connection.connect();

                    //scrive il json contenente i dati da aggiornare sullo stream di output
                    OutputStream os = connection.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                    osw.write(dati_reg);
                    osw.flush();
                    osw.close();

                    //controlla l'esito dell'operazione
                    int responseCode = connection.getResponseCode();
                    if(400 <= responseCode && responseCode <= 499){
                        update_in_corso.dismiss();
                        this.cancel(true);
                    }

                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    String inputLine;

                    //scrive la risposta in una stringa
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

        /**
         * Metodo eseguito dopo doInBackground. E' di raccordo tra l'AsyncTask e il Thread principale.
         * Analizza il risultato se ha avuto successo o meno la modifica.
         * @param result contiene il risultato del server
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            update_in_corso.dismiss();

            JsonObject jsonResponse = new Gson().fromJson(result, JsonObject.class);

            //se result è null significa che la modifica non ha avuto successo in quanto il server è offline
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
            } else if(jsonResponse.has("esito")) { //se contiene un esito si va ad analizzare l'esito
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
                } else { // se entra in questo else significa che la modifica ha avuto successo
                    JsonObject jobj = new Gson().fromJson(result, JsonObject.class);
                    String successo = jobj.get("esito").getAsString();

                    //si fa l'update dell'utente anche nel db locale
                    if (successo.equals("success")) {
                        DAOUtente daoutente = new DAOUtente(ctx);
                        daoutente.open();
                        daoutente.update(utente);
                        daoutente.close();
                    }


                    //si reindirizza l'utente nell'activity profilo
                    Intent i;
                    i = new Intent(ctx, ProfiloActivity.class);
                    i.putExtra("selezione", "profilo");
                    ctx.startActivity(i);
                }
            }

        }
    }

    /**
     * Metodo che si occupa di avviare l'AsyncTask che si occupa del logout
     * @param ctx context dell'applicazione
     */
    public void logoutUtente(Context ctx) {

        session = new UserSessionManager(ctx);
        new LogoutUtenteTask(utente_attivo, ctx).execute();
    }

    /**
     * Classe che modella l'AsyncTask che si occupa di fare il logout dell'utente
     */
    private class LogoutUtenteTask extends AsyncTask<Void, Void, String>{

        private Utente utente;
        private Context ctx;
        private boolean connesso;

        /**
         * Costruttore
         * @param utente utente che deve fare il logout
         * @param ctx context dell'applicazione
         */
        public LogoutUtenteTask(Utente utente, Context ctx) {
            this.utente = utente;
            this.ctx = ctx;
        }

        /**
         * Metodo eseguito prima dell'AsyncTask vero e proprio. controlla la connessione
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            CheckConnessione checkConnessione = new CheckConnessione();
            connesso = checkConnessione.checkConnessione(ctx);
        }

        /**
         * Metodo che rappresenta l'AsyncTask vero e proprio. Si occupa di fare una richiesta put al server che
         * modifica lo stato dell'utente da connesso a sconnesso
         * @param voids parametro che è vuoto
         * @return ritorna l'esito dell'operazione sul server
         */
        @Override
        protected String doInBackground(Void... voids) {

            if (!connesso) {
                return null;
            } else {
                HttpURLConnection conn = null;

                Gson gson = new Gson();
                String dati_utente = gson.toJson(utente);

                try {
                    SharedPreferences prefs = ctx.getSharedPreferences("ipAddress", MODE_PRIVATE);
                    String path = prefs.getString("ipAddress", null);
                    byte[] data = utente.getIdsessione().getBytes("UTF-8");
                    String base64 = Base64.encodeToString(data,Base64.DEFAULT);
                    URL url = new URL("http://" + path + "/gestionemappe/utente/secured/logout"); //url verso cui viene fatta la richiesta
                    conn = (HttpURLConnection) url.openConnection();
                    //viene settato l'header della richiesta
                    conn.setDoOutput(true);
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("Authorization", "basic " + base64);
                    conn.setInstanceFollowRedirects(true);
                    conn.connect();

                    //viene scritto nello stream di output l'utente di cui fare il logout
                    OutputStream os = conn.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                    osw.write(dati_utente);
                    osw.flush();
                    osw.close();

                    //si controlla l'esito
                    int responseCode = conn.getResponseCode();
                    if(400 <= responseCode && responseCode <= 499){
                        this.cancel(true);
                    }

                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    String inputLine;

                    //si scrive la risposta in una stringa
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

        /**
         * Metodo eseguito dopo il doInBackground. è di raccordo tra thread principale e l'AsyncTask.
         * Analizza la risposta del server per vedere se il logout ha avuto successo o meno
         * @param result risultato dell'operazione sul server
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) { // se il server non è online non si può fare il logout
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
            } else { //se è online si termina la sessione e si svuota il db locale dall'utente loggato
                DAOUtente daoUtente = new DAOUtente(ctx);
                daoUtente.open();
                daoUtente.deleteAll();
                daoUtente.close();
                session.logOutUser();
            }
        }


    }

    /**
     * Metodo che si occupa di avviare i servizi di base dell'applicazione.
     * in particolare fa partire la scansione dei beacon e il controllo di aggiornamenti sul server
     * @param ctx context dell'applicazione
     */
    private void startUpServices(Context ctx){
        Utente user;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) { // se l'applicazione supporta il bluetooth ed ha una versione superiore o uguale a Lollipop parte il servizio
            DAOUtente daoUtente = new DAOUtente(ctx);
            daoUtente.open();
            user = daoUtente.findUtente();
            daoUtente.close();
            Intent s = new Intent(ctx, BluetoothLeService.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("user", user);
            bundle.putLong("periodo", 15000); //setta il periodo di scansione pari a 15 secondi
            s.putExtras(bundle);
            ctx.startService(s); //parte il servizio vero e proprio
        }

        Intent u = new Intent(ctx, CheckForDbUpdatesService.class); //parte il servizio che controlla aggiornamenti sul server
        ctx.startService(u);

    }

}

