package com.example.alessandro.gosafe.entity;

import android.content.Context;

import com.example.alessandro.gosafe.database.DAOUtente;
import com.example.alessandro.gosafe.firebase.MyFirebaseInstanceIdService;

import java.io.Serializable;

/**
 * Classe che si occupa di modellare l'entity Utente
 */
public class Utente implements Serializable { //aggiunto serializable per mandare l'intent da autenticazionetask a Mainactivity

    private long id;
    private String username;
    private String password;
    private String nome;
    private String cognome;
    private String beaconId;
    private int percorsoid;
    private boolean is_autenticato;//true solo quando l'utente è autenticato sul server
    private String token;
    private String idsessione;

    /**
     * Costruttore
     * @param username username dell'utente
     * @param password password dell'utente
     */
    public Utente(String username, String password){

        this.username=username;
        this.password=password;
        this.token = MyFirebaseInstanceIdService.get_token();

    }

    /**
     * Costruttore
     * @param username username dell'utente
     * @param password password dell'utente
     * @param nome nome dell'utente
     * @param cognome cognome dell'utente
     * @param is_autenticato booleano che indica se l'utente è autenticato sul server
     */
    public Utente(String username, String password, String nome, String cognome, boolean is_autenticato) {
        this.username = username;
        this.password = password;
        this.nome = nome;
        this.cognome = cognome;
        this.is_autenticato = is_autenticato;
        this.token = MyFirebaseInstanceIdService.get_token();
    }

    /**
     * Costruttore
     * @param id_utente id dell'utente
     * @param username username dell'utente
     * @param password password dell'utente
     * @param nome nome dell'utente
     * @param cognome cognome dell'utente
     * @param beaconid id del beacon a cui è connesso l'utente
     * @param percorsoid id del percorso di fuga assegnato all'utente
     * @param is_autenticato booleano che indica se l'utente è autenticato sul server
     * @param token token firebase assegnato all'utente
     * @param idsessione token di sessione assegnato dal server all'utente
     */
    public Utente(long id_utente, String username, String password, String nome, String cognome, String beaconid, int percorsoid, boolean is_autenticato, String token, String idsessione) {
        this.id = id_utente;
        this.username = username;
        this.password = password;
        this.nome = nome;
        this.cognome = cognome;
        this.beaconId = beaconid;
        this.percorsoid = percorsoid;
        this.is_autenticato = is_autenticato;
        this.token = token;
        this.idsessione = idsessione;
    }

    /**
     * Costruttore
     * @param username username dell'utente
     * @param password password dell'utente
     * @param nome nome dell'utente
     * @param cognome cognome dell'utente
     * @param beaconid id del beacon a cui è connesso l'utente
     * @param percorsoid id del percorso di fuga assegnato all'utente
     * @param is_autenticato booleano che indica se l'utente è autenticato sul server
     * @param token token firebase assegnato all'utente
     */
    public Utente(String username, String password, String nome, String cognome, String beaconid, int percorsoid, boolean is_autenticato, String token) {
        this.username = username;
        this.password = password;
        this.nome = nome;
        this.cognome = cognome;
        this.beaconId = beaconid;
        this.percorsoid = percorsoid;
        this.is_autenticato = is_autenticato;
        this.token = token;
    }

    /**
     * Metodo getter per recuperare l'id del beacon a cui l'utente è connesso
     * @return ritorna una stringa con l'id del beacon
     */
    public String getBeaconid() {
        return beaconId;
    }

    /**
     * Metodo setter per impostare il beacon a cui l'utente è connesso
     * @param beaconid id del nuovo beeacona cui l'utente è connesso
     */
    public void setBeaconid(String beaconid) {
        this.beaconId = beaconid;
    }

    /**
     * Metodo getter che recupera l'id del percorso assegnato all'utente
     * @return ritorna un intero con l'id del percorso
     */
    public int getPercorsoid() {
        return percorsoid;
    }

    /**
     * Metodo getter per recuperare il token di firebase assegnato all'utente
     * @return ritorna una stringa con il token firebase
     */
    public String getToken() {
        return token;
    }

    /**
     * Metodo setter che imposta l'id dell'utente
     * @param id riceve in input l'id dell'utente
     */
    public void setId_utente(long id){
        this.id= id;
    }

    /**
     * Metodo setter che imposta se l'utente è autenticato sul server
     * @param autenticato riceve un booleano di valore diverso in base a se l'utente è autenticato o meno
     */
    public void setIs_autenticato(Boolean autenticato){
        this.is_autenticato= autenticato;
    }

    /**
     * Metodo getter per recuperare se l'utente è autenticato
     * @return ritorna un booleano di valore diverso in base al fatto che l'utente è autenticato
     */
    public boolean getIs_autenticato() {
        return is_autenticato;
    }

    /**
     * Metodo getter per recuperare lo username dell'utente
     * @return ritorna lo username
     */
    public String getUsername(){
        return username;
    }

    /**
     * Metodo setter che imposta lo username dell'utente
     * @param username username da impostare nell'oggetto utente
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Metodo getter che recupera la password dell'utente
     * @return ritorna la password dell'utente
     */
    public String getPassword() {
        return password;
    }

    /**
     * Metodo setter per impostare la password
     * @param password riceve come input la password da impostare
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Metodo getter per recuperare il nome dell'utente
     * @return ritorna il nome dell'utente
     */
    public String getNome() {
        return nome;
    }

    /**
     * Metodo setter per impostare il nome
     * @param nome nome da settare nell'utente
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Metodo getter per recuperare il cognome dell'utente
     * @return ritorna il cognome
     */
    public String getCognome() {
        return cognome;
    }

    /**
     * Metodo setter per impostare il cognome dell'utente
     * @param cognome cognome da impostare
     */
    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    /**
     * Metodo che imposta la posizione dell'utente sul db e nella variabile beaconId della classe
     * @param pos rappresenta il beacon a cui è connesso l'utente
     * @param ctx Context dell'applicazione necessaria per aprire la connessione al db
     */
    public void setPosition(String pos, Context ctx){
        beaconId = pos;
        DAOUtente u = new DAOUtente(ctx);
        u.open();
        u.update(this);
        u.close();
    }

    /**
     * Metodo getter per recuperare la posizione dell'utente
     * @return ritorna la posizione dell'utente. è una stringa con il MAC address del beacon
     */
    public String getPosition(){
        return beaconId;
    }

    /**
     * Metodo getter per recuperare l'id dell'utente
     * @return ritorna l'id dell'utente
     */
    public long getId_utente() {
        return id;
    }

    /**
     * Metodo getter per recuperare l'id di sessione dell'utente
     * @return ritorna l'id di sessione salvato nell'oggetto utente
     */
    public String getIdsessione() {
        return idsessione;
    }

    /**
     * Metodo setter per impostare l'id sessione nell'oggetto utente
     * @param idsessione id della sessione ricevuto dal server
     */
    public void setIdsessione(String idsessione) {
        this.idsessione = idsessione;
    }

    /**
     * Metodo che si occupa di salvare nel db locale l'utente che si è appena loggato/registrato.
     * @param ctx riceve il Context dell'applicazione necessario per realizzare la connessione con il db
     */
    public void registrazioneLocale(Context ctx) {
        DAOUtente u = new DAOUtente(ctx);
        u.open();
        if(!u.save(this))
            u.update(this);
        u.close();
    }



}
