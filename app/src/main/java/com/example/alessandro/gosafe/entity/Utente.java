package com.example.alessandro.gosafe.entity;

import android.content.Context;

import com.example.alessandro.gosafe.database.DAOUtente;
import com.example.alessandro.gosafe.firebase.MyFirebaseInstanceIdService;

import java.io.Serializable;

/**
 * Created by Alessandro on 16/03/2018.
 */

public class Utente implements Serializable { //aggiunto serializable per mandare l'intent da autenticazionetask a Mainactivity

    private long id;
    private String username;
    private String password;
    private String nome;
    private String cognome;
    private String beaconId = "";
    private int percorsoid;
    private boolean is_autenticato;//true solo quando l'utente è autenticato sul server
    private String token;
    //private String email;

    public Utente(String username, String password){

        this.username=username;
        this.password=password;
        this.token = MyFirebaseInstanceIdService.get_token();

    }

    public Utente(String username, String password, /*String email,*/ String nome, String cognome, boolean is_autenticato) {
        this.username = username;
        this.password = password;
        //this.email = email;
        this.nome = nome;
        this.cognome = cognome;
        this.is_autenticato = is_autenticato;
        this.token = MyFirebaseInstanceIdService.get_token();
    }

    public Utente(long id_utente, String username, String password, String nome, String cognome, String beaconid, int percorsoid, boolean is_autenticato, String token) {
        this.id = id_utente;
        this.username = username;
        this.password = password;
        this.nome = nome;
        this.cognome = cognome;
        this.beaconId = beaconid;
        this.percorsoid = percorsoid;
        this.is_autenticato = is_autenticato;
        this.token = token;
    }

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

    public String getBeaconid() {
        return beaconId;
    }

    public void setBeaconid(String beaconid) {
        this.beaconId = beaconid;
    }

    public int getPercorsoid() {
        return percorsoid;
    }

    public void setPercorsoid(int percorsoid) {
        this.percorsoid = percorsoid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setId_utente(long id){
        this.id= id;
    }

    public void setIs_autenticato(Boolean autenticato){
        this.is_autenticato= autenticato;
    }

    public boolean getIs_autenticato() {
        return is_autenticato;
    }

    public String getUsername(){
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public void setPosition(String pos){
        beaconId = pos;
    }

    public String getPosition(){
        return beaconId;
    }

    public long getId_utente() {
        return id;
    }

    //public void setId_utente(long id){this.id_utente= id;}

    //public void setIs_autenticato(Boolean autenticato){this.is_autenticato= autenticato;}

    public void registrazioneLocale(Context ctx) {
        DAOUtente u = new DAOUtente(ctx);
        u.open();
        u.save(this);
        u.close();
    }



}
