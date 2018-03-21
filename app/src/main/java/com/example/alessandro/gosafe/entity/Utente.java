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
    private String email;
    private String nome;
    private String cognome;
    private boolean is_autenticato;//true solo quando l'utente è autenticato sul server

    public Utente(String username, String password){

        this.username=username;
        this.password=password;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public boolean isIs_autenticato() {
        return is_autenticato;
    }

    public void setIs_autenticato(boolean is_autenticato) {
        this.is_autenticato = is_autenticato;
    }

    public long getId_utente() {
        return id_utente;

    }


    public Utente(String username, String password, String email, String nome, String cognome, boolean is_autenticato) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.nome = nome;
        this.cognome = cognome;
        this.is_autenticato = is_autenticato;
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
