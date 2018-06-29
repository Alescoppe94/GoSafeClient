package com.example.alessandro.gosafe.entity;

import java.util.LinkedList;

public class Percorso {

    private int id;
    private LinkedList<Tappa> tappe;

    public Percorso(LinkedList<Tappa> tappe) {
        this.tappe = tappe;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) { this.id = id;}

    public LinkedList<Tappa> getTappe() {
        return tappe;
    }


}
