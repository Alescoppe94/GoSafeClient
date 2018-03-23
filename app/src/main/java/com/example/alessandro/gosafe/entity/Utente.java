package com.example.alessandro.gosafe.entity;

import android.content.Context;

import com.example.alessandro.gosafe.database.DAOUtente;

/**
 * Created by Alessandro on 16/03/2018.
 */

public class Utente {

    private long id_utente;
    private String username;
    private String password;
    private String nome;
    private String cognome;
    private int beaconid;
    private int percorsoid;
    private boolean is_autenticato;//true solo quando l'utente è autenticato sul server
    private String token;

    public Utente(String username, String password){

        this.username=username;
        this.password=password;

    }

    public int getBeaconid() {
        return beaconid;
    }

    public void setBeaconid(int beaconid) {
        this.beaconid = beaconid;
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

    public String getUsername() {
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

    public boolean getIs_autenticato() {
        return is_autenticato;
    }

    public void setIs_autenticato(boolean is_autenticato) {
        this.is_autenticato = is_autenticato;
    }

    public long getId_utente() {
        return id_utente;

    }


    public Utente(String username, String password, String nome, String cognome, int beaconid, int percorsoid, boolean is_autenticato, String token) {
        this.username = username;
        this.password = password;
        this.nome = nome;
        this.cognome = cognome;
        this.beaconid = beaconid;
        this.percorsoid = percorsoid;
        this.is_autenticato = is_autenticato;
        this.token = token;
    }

    public void setId_utente(long id){
        this.id_utente= id;
    }

    public void setIs_autenticato(Boolean autenticato){
        this.is_autenticato= autenticato;
    }


    public void registrazioneLocale(Context ctx) {
        DAOUtente u = new DAOUtente(ctx);
        u.open();
        u.save(this);
        u.close();
    }
}
