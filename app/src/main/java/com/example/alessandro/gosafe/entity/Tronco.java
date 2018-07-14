package com.example.alessandro.gosafe.entity;

import android.content.Context;
import com.example.alessandro.gosafe.database.DAOPesiTronco;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * classe che modella l'entity Tronco
 */
public class Tronco {

    private int id;
    private boolean agibile;
    private ArrayList<Beacon> beaconEstremi;
    private float area;

    /**
     * costruttore
     * @param id id del Tronco
     * @param agibile booleano che indica l'agibilità. non viene utilizzato
     * @param beaconEstremi beacon estremi del Tronco
     * @param area area del tronco
     */
    public Tronco(int id,boolean agibile, ArrayList<Beacon> beaconEstremi, float area) {
        this.id = id;
        this.agibile = agibile;
        this.beaconEstremi = beaconEstremi;
        this.area = area;
    }

    /**
     * metodo setter per impostare l'id del Tronco
     * @param id riceve l'id del Tronco
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * metodo getter che recupera l'id del Tronco
     * @return ritorna l'id del Tronco
     */
    public int getId() {
        return id;
    }

    /**
     * metodo getter che recupera l'agibilità di un Tronco. non viene utilizzato
     * @return ritorna un booleano con l'agibilità
     */
    public boolean isAgibile() {
        return agibile;
    }

    /**
     * metodo getter che recupera i beacon estremi di un Tronco
     * @return ritorna Un arrayList con i beacons estremi
     */
    public ArrayList<Beacon> getBeaconEstremi() {
        return beaconEstremi;
    }

    /**
     * metodo getter per recuperare l'area di un Tronco
     * @return ritrona l'area di un Tronco
     */
    public float getArea() {
        return area;
    }

    /**
     * metodo che calcola il costo di un Tronco a partire dai pesiTronco
     * @param ctx riceve il Context dell'applicazione per stabilire la connessione con il db
     * @return ritorna il costo calcolato
     */
    public float calcolaCosto(Context ctx){
        DAOPesiTronco pesiTroncoDAO = new DAOPesiTronco(ctx);
        pesiTroncoDAO.open();
        HashMap<Float, Float> coeffVal = pesiTroncoDAO.getPesiTronco(this.id);
        pesiTroncoDAO.close();
        Iterator<Map.Entry<Float, Float>> it = coeffVal.entrySet().iterator();
        float costo = 0;
        while (it.hasNext()) {
            Map.Entry<Float, Float> coeff_val = it.next();
            costo += (coeff_val.getKey()*coeff_val.getValue());
        }
        return costo;

    }
}
