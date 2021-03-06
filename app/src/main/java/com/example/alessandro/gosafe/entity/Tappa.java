package com.example.alessandro.gosafe.entity;

/**
 * Created by Alessandro on 12/04/2018.
 */

public class Tappa {

    private int id;
    private int percorsoId;
    private Tronco tronco;
    private boolean direzione;

    public Tappa(Tronco tronco, int percorsoId) {
        this.id=0;
        this.percorsoId= percorsoId;
        this.tronco = tronco;
    }

    public Tappa(Tronco tronco, boolean direzione) {
        this.id=0;
        this.tronco = tronco;
        this.direzione = direzione;
    }

    public Tappa(Tronco tronco) {
        this.tronco = tronco;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) { this.id = id; }

    public Tronco getTronco() {
        return tronco;
    }

    public void setTronco(Tronco tronco) {
        this.tronco = tronco;
    }

    public int getPercorsoId() {
        return percorsoId;
    }

    public void setPercorsoId(int percorsoId) {
        this.percorsoId = percorsoId;
    }

    public boolean isDirezione() {
        return direzione;
    }

    public void setDirezione(boolean direzione) {
        this.direzione = direzione;
    }
}
