package com.example.alessandro.gosafe.entity;

import java.util.LinkedList;

/**
 * classe che modella l'entity Percorso
 */
public class Percorso {

    private int id;
    private LinkedList<Tappa> tappe;

    /**
     * costruttore
     * @param tappe riceve in input l'elenco delle Tappe
     */
    public Percorso(LinkedList<Tappa> tappe) {
        this.tappe = tappe;
    }

    /**
     * metodo getter che ritorna l'id del Percorso
     * @return ritorna l'id del Piano
     */
    public int getId() {
        return id;
    }

    /**
     * metodo setter che imposta l'id del Percorso
     * @param id
     */
    public void setId(int id) { this.id = id;}

    /**
     * metodo getter che recupera le Tappe di un Percorso
     * @return ritorna un LinkedList con le Tappe del Percorso
     */
    public LinkedList<Tappa> getTappe() {
        return tappe;
    }


}
