package com.example.alessandro.gosafe.entity;

/**
 * Created by Alessandro on 12/04/2018.
 */

public class Beacon {

    private String id;
    private boolean is_puntodiraccolta;
    private Piano piano;
    

    public Beacon(String id, boolean is_puntodiraccolta, Piano piano) {
        this.id = id;
        this.is_puntodiraccolta = is_puntodiraccolta;
        this.piano = piano;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) { this.id = id; }

    public boolean is_puntodiraccola() {
        return is_puntodiraccolta;
    }

    public void setIs_puntodiraccolta(boolean is_puntodiraccolta) {
        this.is_puntodiraccolta = is_puntodiraccolta;
    }

    public Piano getPiano() {
        return piano;
    }

    public void setPiano(Piano piano) {
        this.piano = piano;
    }

}
