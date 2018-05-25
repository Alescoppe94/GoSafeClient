package com.example.alessandro.gosafe.entity;

public class Notifica {

    private int id;
    private int utenteId;
    private Percorso percorso;
    //private LocalDateTime dataorario;
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

    public int getUtenteId() {
        return utenteId;
    }

    public void setUtenteId(int utenteId) {
        this.utenteId = utenteId;
    }

    public Percorso getPercorso() {
        return percorso;
    }

    public void setPercorso(Percorso percorso) {
        this.percorso = percorso;
    }

    public String getMessaggio() {
        return messaggio;
    }

    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }
}