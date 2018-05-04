package com.example.alessandro.gosafe.server;

import android.content.Context;
import android.os.AsyncTask;
import com.example.alessandro.gosafe.entity.Beacon;
import com.example.alessandro.gosafe.entity.Percorso;
import com.example.alessandro.gosafe.entity.Tappa;
import com.example.alessandro.gosafe.entity.Tronco;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class RichiestaPercorso {

    private HttpURLConnection conn;
    private final String PATH = "http://10.0.2.2:8080";

    public RichiestaPercorso() {
    }

    public void ottieniPercorsoNoEmergenza(Context ctx) {
        new OttieniPercorsoNoEmergenzaTask(ctx).execute();
    }

    private class OttieniPercorsoNoEmergenzaTask extends AsyncTask<Void, Void, String>{

        private Context ctx;
        private AsyncTask<Void, Void, Boolean> execute;

        public OttieniPercorsoNoEmergenzaTask(Context ctx){
            this.ctx = ctx;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            execute = new checkConnessioneTask().execute();
        }

        @Override
        protected String doInBackground(Void... voids) {

            boolean connesso = false;

            try {
                connesso = execute.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            if (!connesso) {
                return null;
            } else {
                try {
                    URL url = new URL(PATH + "/gestionemappe/mappe/calcolapercorso/1/3");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.setRequestMethod("GET");
                    conn.connect();

                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    String inputLine;

                    while ((inputLine = br.readLine()) != null) {
                        sb.append(inputLine + "\n");
                    }

                    br.close();
                    return sb.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (conn != null) {
                        try {
                            conn.disconnect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Percorso percorso;
            if(result==null){
                //percorso = calcolaPercorsoNoEmergenza();
            } else {
                percorso = new Gson().fromJson(result, Percorso.class);
            }
            //TODO: disegnare su mappa il percorso su mappa
        }
    }

    private class checkConnessioneTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            try {
                conn = (HttpURLConnection) new URL(PATH + "/").openConnection();
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                conn.setRequestMethod("HEAD");
                int responseCode = conn.getResponseCode();
                return (200 <= responseCode && responseCode <= 399);
            } catch (IOException exception) {
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Void... arg0) {

        }

        @Override
        protected void onPostExecute(Boolean result) {

        }
    }



    //TODO: 1 - valutare se lasciare metodi di calcoloPercorso qui o inserirli in un "controller"
    //      2 - dao come fo?
    //      3 - da dove prendo beaconPart e beaconArr?

    /*private Percorso calcolaPercorsoNoEmergenza() {
        Beacon partenza = beaconDAO.getBeaconById(beaconPart);
        boolean emergenza = false;
        Beacon arrivo = beaconDAO.getBeaconById(beaconArr);
        Percorso percorso;

        if (partenza != null && arrivo != null) {

            Map<LinkedList<Beacon>, Float> percorsoOttimo_costoOttimo =  calcoloDijkstra(partenza, arrivo, emergenza);

            LinkedList<Tappa> tappeOttime = new LinkedList<>();

            Map.Entry<LinkedList<Beacon>, Float> entry = percorsoOttimo_costoOttimo.entrySet().iterator().next();

            for(int i = 0; i < entry.getKey().size()-1; i++) {
                Tronco troncoOttimo = troncoDAO.getTroncoByBeacons(entry.getKey().get(i), entry.getKey().get(i+1));
                boolean direzione = troncoDAO.checkDirezioneTronco(troncoOttimo);
                Tappa tappaOttima = new Tappa(troncoOttimo, direzione);
                tappeOttime.add(tappaOttima);
            }
            percorso = new Percorso(tappeOttime, partenza);
        }else{
            percorso = null;
        }
        return percorso;
    }



    private Map<LinkedList<Beacon>, Float> calcoloDijkstra(Beacon partenza, Beacon arrivo, boolean emergenza){

        Set<Tronco> allTronchiEdificio = troncoDAO.getAllTronchi();
        Map<LinkedList<Beacon>, Float> costi_percorsi = new HashMap<>();
        Beacon beacon_controllato = partenza;
        ArrayList<Beacon> beacon_visitati = new ArrayList<>();
        LinkedList<Beacon> percorso_ottimo_parziale = new LinkedList<>();
        percorso_ottimo_parziale.add(beacon_controllato);
        float costo_percorso_ottimo_parziale = 0;
        costi_percorsi.put(percorso_ottimo_parziale, costo_percorso_ottimo_parziale);
        while (!compare(beacon_controllato,arrivo)) {
            Set<Tronco> tronchi_collegati = new HashSet<>();
            Iterator<Tronco> i = allTronchiEdificio.iterator();
            while (i.hasNext()) {
                Tronco tronco = i.next();
                ArrayList<Beacon> beacons = tronco.getBeaconEstremi();
                if (compare(beacons, beacon_controllato)) {
                    boolean tronco_visitato = false;
                    Iterator<Beacon> j = beacons.iterator();
                    while (j.hasNext()) {
                        Beacon beacon = j.next();
                        if (compare(beacon_visitati,beacon))
                            tronco_visitato = true;
                    }
                    if (!tronco_visitato)
                        tronchi_collegati.add(tronco);
                }
            }
            Iterator<Tronco> k = tronchi_collegati.iterator();
            while (k.hasNext()) {
                Tronco tronco = k.next();
                LinkedList<Beacon> percorso_parziale = new LinkedList<>();
                percorso_parziale.addAll(percorso_ottimo_parziale);
                float costo_percorso_parziale = costo_percorso_ottimo_parziale;
                ArrayList<Beacon> beacons = tronco.getBeaconEstremi();
                Iterator<Beacon> j = beacons.iterator();
                while (j.hasNext()) {
                    Beacon beacon = j.next();
                    if (!compare(beacon, beacon_controllato)) {
                        percorso_parziale.add(beacon);
                    }
                }
                Float costo;
                if (emergenza){
                    costo = tronco.calcolaCosto();
                }else{
                    costo = pesiTroncoDAO.geValoreByPesoId(tronco.getId(), "l");
                }
                costo_percorso_parziale += costo;
                Beacon beacon_finale = percorso_parziale.getLast();
                boolean inserito = false;
                Map<LinkedList<Beacon>, Float> costi_percorsi_old = new HashMap<>();
                costi_percorsi_old.putAll(costi_percorsi);
                Iterator<Map.Entry<LinkedList<Beacon>, Float>> it = costi_percorsi_old.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<LinkedList<Beacon>, Float> percorso_costo = it.next();
                    LinkedList<Beacon> percorso_esistente = percorso_costo.getKey();
                    if (compare(percorso_esistente.getLast(),beacon_finale)) {
                        inserito = true;
                        float costo_valore = percorso_costo.getValue();
                        if (costo_percorso_parziale < costo_valore) {
                            costi_percorsi.remove(percorso_esistente);
                            costi_percorsi.put(percorso_parziale, costo_percorso_parziale);
                        }
                    }
                }
                if (!inserito) {
                    costi_percorsi.put(percorso_parziale, costo_percorso_parziale);
                }
            }
            costi_percorsi.remove(percorso_ottimo_parziale);

            LinkedList<Beacon> percorso_scelto = new LinkedList<>();
            float costo_percorso_scelto = Float.MAX_VALUE;
            Iterator<Map.Entry<LinkedList<Beacon>, Float>> it = costi_percorsi.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<LinkedList<Beacon>, Float> percorso_costo = it.next();
                float costo_valore = percorso_costo.getValue();
                if (costo_valore < costo_percorso_scelto) {
                    percorso_scelto = percorso_costo.getKey();
                    costo_percorso_scelto = costo_valore;
                }
            }
            beacon_visitati.add(beacon_controllato);
            if (percorso_scelto.isEmpty())
                percorso_scelto.add(arrivo);
            beacon_controllato = percorso_scelto.getLast();
            percorso_ottimo_parziale = percorso_scelto;
            costo_percorso_ottimo_parziale = costo_percorso_scelto;
        }

        Map<LinkedList<Beacon>, Float> percorsoOttimo_costoOttimo = new HashMap<>();
        percorsoOttimo_costoOttimo.put(percorso_ottimo_parziale, costo_percorso_ottimo_parziale);
        return percorsoOttimo_costoOttimo;

    }

    private boolean compare(ArrayList<Beacon> beacons, Beacon beacon){

        boolean contenuto = false;

        for(Beacon b : beacons){
            if(b.getId().equals(beacon.getId())){
                contenuto = true;
            }
        }

        return contenuto;

    }

    private boolean compare(Beacon beacon1, Beacon beacon2){

        boolean uguali = false;

        if(beacon1.getId().equals(beacon2.getId())){
            uguali = true;
        }

        return uguali;

    }*/
}
