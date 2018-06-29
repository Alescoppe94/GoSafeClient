package com.example.alessandro.gosafe.entity;

public class Tappa {

    private int id;
    private Tronco tronco;
    private boolean direzione;

    public Tappa(Tronco tronco, boolean direzione) {
        this.id=0;
        this.tronco = tronco;
        this.direzione = direzione;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) { this.id = id; }

    public Tronco getTronco() {
        return tronco;
    }
}
