package com.example.alessandro.gosafe.entity;

/**
 * Classe che modella l'entity Piano
 */
public class Piano {

    private int id;
    private String immagine;
    private int piano;
    private Tronco[] tronchi;

    /**
     * Costruttore
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
     * Metodo getter per recuperare l'id del Piano
     * @return ritorna l'id del Piano
     */
    public int getId() {
        return id;
    }

    /**
     * Metodo setter che imposta l'id del Piano
     * @param id riceve l'id da impostare
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Metodo getter per recuperare l'immagine
     * @return ritorna l'immagine sotto forma di stringa base 64
     */
    public String getImmagine() {
        return immagine;
    }

    /**
     * Metodo getter che ritorna il numero del Piano
     * @return ritorna il numero del Piano
     */
    public int getPiano() {
        return piano;
    }

}
