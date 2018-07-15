package com.example.alessandro.gosafe.entity;

/**
 * classe che modella l'entity beacon
 */
public class Beacon {

    private String id;
    private boolean is_puntodiraccolta;
    private int piano;
    private int coordx;
    private int coordy;

    /**
     * Costruttore
     */
    public Beacon(){}

    /**
     * Costruttore
     * @param id prende l'id del beacon
     */
    public Beacon(String id){
        this.id = id;
    }

    /**
     * Costruttore
     * @param id id del beacon
     * @param is_puntodiraccolta booleano indicante se è un punto di raccolta
     * @param piano intero con il numero del piano
     * @param coordx intero con la coordinata x sulla mappa del beacon
     * @param coordy intero con la coordinata y sulla mappa del beacon
     */
    public Beacon(String id, boolean is_puntodiraccolta, int piano, int coordx, int coordy) {
        this.id = id;
        this.is_puntodiraccolta = is_puntodiraccolta;
        this.piano = piano;
        this.coordx = coordx;
        this.coordy = coordy;
    }

    /**
     * Metodo getter per l'id del beacon
     * @return ritorna l'id del beacon
     */
    public String getId() {
        return id;
    }

    /**
     * Metodo setter per l'id del beacon
     * @param id id del beacon da settare
     */
    public void setId(String id) { this.id = id; }

    /**
     * Metodo getter se è punto di raccolta
     * @return ritorna un booleano in base al fatto che è un punto di raccolta o meno
     */
    public boolean is_puntodiraccola() {
        return is_puntodiraccolta;
    }

    /**
     * Metodo getter che recupera l'intero del piano
     * @return ritorna l'intero del piano
     */
    public int getPiano() {
        return piano;
    }

    /**
     * Metodo getter che recupera la coordinata x del beacon
     * @return ritorna la coordinata x
     */
    public int getCoordx() {
        return coordx;
    }

    /**
     * Metodo getter che recupera la coordinata y del beacon
     * @return ritorna la coordinata y del beacon
     */
    public int getCoordy() {
        return coordy;
    }
}
