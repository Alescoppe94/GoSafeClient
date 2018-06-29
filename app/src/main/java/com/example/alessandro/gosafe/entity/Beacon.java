package com.example.alessandro.gosafe.entity;

public class Beacon {

    private String id;
    private boolean is_puntodiraccolta;
    private int piano;
    private int coordx;
    private int coordy;

    public Beacon(){}

    public Beacon(String id){
        this.id = id;
    }

    public Beacon(String id, boolean is_puntodiraccolta, int piano, int coordx, int coordy) {
        this.id = id;
        this.is_puntodiraccolta = is_puntodiraccolta;
        this.piano = piano;
        this.coordx = coordx;
        this.coordy = coordy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) { this.id = id; }

    public boolean is_puntodiraccola() {
        return is_puntodiraccolta;
    }

    public int getPiano() {
        return piano;
    }

    public int getCoordx() {
        return coordx;
    }

    public int getCoordy() {
        return coordy;
    }
}
