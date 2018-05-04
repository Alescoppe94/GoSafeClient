package com.example.alessandro.gosafe.entity;

/**
 * Created by Alessandro on 12/04/2018.
 */

public class Piano {

    private int id;
    private String immagine;
    private int piano;
    private Tronco[] tronchi;

    public Piano(){

    }

    public Piano(int id, String immagine, int piano) {
        this.id = id;
        this.immagine = immagine;
        this.piano = piano;
    }

    public Piano(int id, String immagine, int piano, Tronco[] tronchi) {
        this.id = id;
        this.immagine = immagine;
        this.piano = piano;
        this.tronchi = tronchi;
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

    public void setImmagine(String immagine) {
        this.immagine = immagine;
    }

    public int getPiano() {
        return piano;
    }

    public void setPiano(int piano) {
        this.piano = piano;
    }

    public Tronco[] getTronchi() {
        return tronchi;
    }

    public void setTronchi(Tronco[] tronchi) {
        this.tronchi = tronchi;
    }

}
