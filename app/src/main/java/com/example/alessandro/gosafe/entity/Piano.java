package com.example.alessandro.gosafe.entity;

public class Piano {

    private int id;
    private String immagine;
    private int piano;
    private Tronco[] tronchi;

    public Piano(int id, String immagine, int piano) {
        this.id = id;
        this.immagine = immagine;
        this.piano = piano;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImmagine() {
        return immagine;
    }

    public int getPiano() {
        return piano;
    }

}
