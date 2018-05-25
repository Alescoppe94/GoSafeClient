package com.example.alessandro.gosafe.entity;

import java.util.LinkedList;

/**
 * Created by Alessandro on 12/04/2018.
 */

public class Percorso {

    private int id;
    private LinkedList<Tappa> tappe;
    private Beacon beaconPartenza;

    public Percorso(int id, LinkedList<Tappa> tappe, Beacon beaconPartenza) {
        this.id = id;
        this.tappe = tappe;
        this.beaconPartenza = beaconPartenza;
    }

    public Percorso(LinkedList<Tappa> tappe, Beacon beaconPartenza) {
        this.tappe = tappe;
        this.beaconPartenza = beaconPartenza;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) { this.id = id;}

    public LinkedList<Tappa> getTappe() {
        return tappe;
    }

    public void setTappe(LinkedList<Tappa> tappe) {
        this.tappe = tappe;
    }

    public Beacon getBeaconPartenza() {
        return beaconPartenza;
    }

    public void setBeaconPartenza(Beacon beaconPartenza) {
        this.beaconPartenza = beaconPartenza;
    }

}
