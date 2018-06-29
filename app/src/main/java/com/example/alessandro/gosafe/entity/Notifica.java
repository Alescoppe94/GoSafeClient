package com.example.alessandro.gosafe.entity;

public class Notifica {

    private int id;
    private int utenteId;
    private Percorso percorso;
    private String messaggio;

    public Notifica(int utenteId, Percorso percorso, String messaggio) {
        this.utenteId = utenteId;
        this.percorso = percorso;
        this.messaggio = messaggio;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Percorso getPercorso() {
        return percorso;
    }

}