package com.example.alessandro.gosafe.entity;

/**
 * classe che modella l'entity Piano
 */
public class Piano {

    private int id;
    private String immagine;
    private int piano;
    private Tronco[] tronchi;

    /**
     * costruttore
     * @param id id del Piano
     * @param immagine immagine del Piano
     * @param piano numero del Piano
     */
    public Piano(int id, String immagine, int piano) {
        this.id = id;
        this.immagine = immagine;
        this.piano = piano;
    }

    /**
     * metodo getter per recuperare l'id del Piano
     * @return ritorna l'id del Piano
     */
    public int getId() {
        return id;
    }

    /**
     * metodo setter che imposta l'id del Piano
     * @param id riceve l'id da impostare
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * metodo getter per recuperare l'immagine
     * @return ritorna l'immagine sotto forma di stringa base 64
     */
    public String getImmagine() {
        return immagine;
    }

    /**
     * metodo getter che ritorna il numero del Piano
     * @return ritorna il numero del Piano
     */
    public int getPiano() {
        return piano;
    }

}
