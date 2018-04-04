package com.example.alessandro.gosafe.entity;

import java.io.Serializable;

/**
 * Created by Alessandro on 16/03/2018.
 */

public class Utente implements Serializable { //aggiunto serializable per mandare l'intent da autenticazionetask a Mainactivity

    private long id_utente;
    private String username;
    private String password;
    private String email;
    private String nome;
    private String cognome;
    private boolean is_autenticato;//true solo quando l'utente è autenticato sul server
    private String token;
    private String position = "";

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

    public String getUsername(){
        return username;
    }

    public void setPosition(String pos){
        position = pos;
    }

    public String getPosition(){
        return position;
    }



}
