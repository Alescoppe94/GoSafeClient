package com.example.alessandro.gosafe.entity;

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
    private String token;

    public Utente(String username, String password){

        this.username=username;
        this.password=password;

    }

    public Utente(String username, String password, String token){

        this.username=username;
        this.password=password;
        this.token=token;

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


}
