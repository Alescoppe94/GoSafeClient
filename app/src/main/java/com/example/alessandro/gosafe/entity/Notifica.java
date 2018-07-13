package com.example.alessandro.gosafe.entity;

/**
 * classe che modella l'entity Notifica
 */
public class Notifica {

    private int id;
    private int utenteId;
    private Percorso percorso;
    private String messaggio;

    /**
     * costruttore
     * @param utenteId id dell'utente a cui è rivolta la notifica
     * @param percorso percorso ricevuto dal server
     * @param messaggio messaggio ricevuto sal server
     */
    public Notifica(int utenteId, Percorso percorso, String messaggio) {
        this.utenteId = utenteId;
        this.percorso = percorso;
        this.messaggio = messaggio;
    }

    /**
     * metodo getter che recupera l'id della notifica
     * @return ritorna l'id della notifica
     */
    public int getId() {
        return id;
    }

    /**
     * metodo setter che imposta l'id della notifica
     * @param id prende l'id della notifica
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * metodo getter che recupera il percorso ricevuto dal server
     * @return ritorna il percorso. e' il percorso di evacuazione
     */
    public Percorso getPercorso() {
        return percorso;
    }

}