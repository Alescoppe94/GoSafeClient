package com.example.alessandro.gosafe.entity;

/**
 * Created by Alessandro on 12/04/2018.
 */

public class Beacon {

    private String id;
    private boolean is_puntodiraccola;
    private Piano piano;

    public Beacon() {
    }

    public Beacon(String id, boolean is_puntodiraccola, Piano piano) {
        this.id = id;
        this.is_puntodiraccola = is_puntodiraccola;
        this.piano = piano;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) { this.id = id; }

    public boolean is_puntodiraccola() {
        return is_puntodiraccola;
    }

    public void setIs_puntodiraccola(boolean is_puntodiraccola) {
        this.is_puntodiraccola = is_puntodiraccola;
    }

    public Piano getPiano() {
        return piano;
    }

    public void setPiano(Piano piano) {
        this.piano = piano;
    }

}
