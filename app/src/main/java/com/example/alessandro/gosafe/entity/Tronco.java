package com.example.alessandro.gosafe.entity;

import java.util.ArrayList;

/**
 * Created by Alessandro on 12/04/2018.
 */

public class Tronco {

    private int id;
    private boolean agibile;
    private ArrayList<Beacon> beaconEstremi;
    private float area;

    public Tronco(int id,boolean agibile, ArrayList<Beacon> beaconEstremi, float area) {
        this.id = id;
        this.agibile = agibile;
        this.beaconEstremi = beaconEstremi;
        this.area = area;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isAgibile() {
        return agibile;
    }

    public void setAgibile(boolean agibile) {
        this.agibile = agibile;
    }

    public ArrayList<Beacon> getBeaconEstremi() {
        return beaconEstremi;
    }

    public void setBeaconEstremi(ArrayList<Beacon> beaconEstremi) {
        this.beaconEstremi = beaconEstremi;
    }

    public float getArea() {
        return area;
    }

    public void setArea(float area) {
        this.area = area;
    }

    /*public float calcolaCosto(){
        PesiTroncoDAO pesiTroncoDAO = new PesiTroncoDAO();
        HashMap<Float, Float> coeffVal = pesiTroncoDAO.getPesiTronco(this.id);
        Iterator<Map.Entry<Float, Float>> it = coeffVal.entrySet().iterator();
        float costo = 0;
        while (it.hasNext()) {
            Map.Entry<Float, Float> coeff_val = it.next();
            costo += (coeff_val.getKey()*coeff_val.getValue()); //TODO: valutare se aggiungere los e lunghezza
        }
        setCosto(costo);
        return costo;
    }*/

}
