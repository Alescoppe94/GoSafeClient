package com.example.alessandro.gosafe.entity;

/**
 * Classe che modella l'entity Tappa. serve per dare una direzione a un Tronco
 */
public class Tappa {

    private int id;
    private Tronco tronco;
    private boolean direzione;

    /**
     * Costruttore
     * @param tronco tronco di cui si vuole salvare la direzione
     * @param direzione direzione del tronco
     */
    public Tappa(Tronco tronco, boolean direzione) {
        this.id=0;
        this.tronco = tronco;
        this.direzione = direzione;
    }

    /**
     * Metodo getter che recupera l'id
     * @return ritorna l'id della Tappa
     */
    public int getId() {
        return id;
    }

    /**
     * Metodo setter che imposta l'id della Tappa
     * @param id riceve in input l'id da impostare
     */
    public void setId(int id) { this.id = id; }

    /**
     * Metodo getter che consente di estrarre dalla Tappa il Tronco corrispondente
     * @return ritorna il Tronco contenuto nella Tappa
     */
    public Tronco getTronco() {
        return tronco;
    }
}
