package com.example.alessandro.gosafe.server;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.Spinner;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.example.alessandro.gosafe.EmergenzaActivity;
import com.example.alessandro.gosafe.R;
import com.example.alessandro.gosafe.database.DAOBeacon;
import com.example.alessandro.gosafe.database.DAOPesiTronco;
import com.example.alessandro.gosafe.database.DAOTronco;
import com.example.alessandro.gosafe.entity.*;
import com.example.alessandro.gosafe.helpers.PinView;
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
    private final String PATH = "http://192.168.1.197:8080";
    private Utente utente_attivo;
    public Percorso percorsoPost;
    public Percorso percorsoEmergenza;
    private ArrayList<Integer> coorddelpercorso = new ArrayList<Integer>();
    private ArrayList<Integer> percorso = new ArrayList<Integer>();
    private ArrayList<String> percorsoEmer = new ArrayList<>();
    private ArrayList<Integer> coordEmergenza = new ArrayList<>();


    public RichiestaPercorso(Utente utente_attivo) {
        this.utente_attivo = utente_attivo;
    }


    public void cambiaPiano(PinView imageViewPiano, int posizione) {
        new CambiaPianoTask(imageViewPiano, posizione);
    }

    private class CambiaPianoTask {


        private PinView imageViewPiano;
        private int posizione;

        public CambiaPianoTask(PinView imageViewPiano, int posizione){
            this.imageViewPiano = imageViewPiano;
            this.posizione = posizione;
            disegnaPercorso();
        }



        protected void disegnaPercorso() {

            //imageViewPiano.play(new ArrayList<Integer>());
            coorddelpercorso.clear();

           for(int j=0; j<percorso.size(); j+=3 ){
                if(posizione == percorso.get(j+2)){
                    coorddelpercorso.add(percorso.get(j));
                    coorddelpercorso.add(percorso.get(j+1));
                }
            }
            //coorddelpercorso = daoBeacon.getCoords(percorso);  // Crea una lista in cui vengono contenuti le coordinate di tutti i beacon del percorso
            imageViewPiano.play(coorddelpercorso);

        }
    }

    public void ottieniPercorsoNoEmergenza(Context ctx, String beaconArr, PinView imageViewPiano, int posizione, Spinner spinner, Utente user) {
        this.utente_attivo = user;
        new OttieniPercorsoNoEmergenzaTask(ctx,beaconArr, imageViewPiano, posizione, spinner).execute();
    }

    private class OttieniPercorsoNoEmergenzaTask extends AsyncTask<Void, Void, String>{

        private final String beaconArr;
        private Context ctx;
        private PinView imageViewPiano;
        private AsyncTask<Void, Void, Boolean> execute;
        private boolean connesso;
        private int posizione;
        private Spinner spinner;
        private ProgressDialog calcolopercorso_in_corso;

        public OttieniPercorsoNoEmergenzaTask(Context ctx, String beaconArr, PinView imageViewPiano, int posizione, Spinner spinner){
            this.ctx = ctx;
            this.beaconArr = beaconArr;
            this.imageViewPiano = imageViewPiano;
            this.posizione = posizione;
            this.spinner = spinner;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            calcolopercorso_in_corso = new ProgressDialog(ctx);
            calcolopercorso_in_corso.setIndeterminate(true);
            calcolopercorso_in_corso.setCancelable(true);
            //calcolopercorso_in_corso.setCanceledOnTouchOutside(false);
            calcolopercorso_in_corso.setMessage(ctx.getString(R.string.calcolopercorsoincorso));
            calcolopercorso_in_corso.show();
            CheckConnessione checkConnessione = new CheckConnessione();
            connesso = checkConnessione.checkConnessione();

        }

        @Override
        protected String doInBackground(Void... voids) {

            if (!connesso) {
                return null;
            } else {
                try {
                    Thread.sleep(1500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    byte[] data = utente_attivo.getIdsessione().getBytes("UTF-8");
                    String base64 = android.util.Base64.encodeToString(data, Base64.DEFAULT);
                    System.out.println("BEACON DI ARRIVO IN RICH PERC: " +beaconArr);
                    URL url = new URL(PATH + "/gestionemappe/mappe/secured/calcolapercorso/"+utente_attivo.getBeaconid()+"/"+beaconArr+""); //Gli devo passare il beacon d'arrivo
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Authorization", "basic " + base64);
                    conn.connect();

                    int responseCode = conn.getResponseCode();
                    if(400 <= responseCode && responseCode <= 499){
                        calcolopercorso_in_corso.dismiss();
                        this.cancel(true);
                    }

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
        protected void onProgressUpdate(Void... arg0){

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(result==null){ //Se il server è giù...
                percorsoPost = calcolaPercorsoNoEmergenza(ctx, beaconArr);
                System.out.println("Percorso finale: "+percorsoPost);
            } else { //Se il server risponde...
                percorsoPost = new Gson().fromJson(result, Percorso.class);
                System.out.println("Percorso finale: "+percorsoPost.getTappe());

            }

            // Devo tradurre il Percorso percorsopost in Tappe poi in Tronchi poi in ArrayList<Integer> percorso
            //1 8 8 4 4 5
            percorso.add(percorsoPost.getTappe().get(0).getTronco().getBeaconEstremi().get(0).getCoordx());
            percorso.add(percorsoPost.getTappe().get(0).getTronco().getBeaconEstremi().get(0).getCoordy());
            percorso.add(percorsoPost.getTappe().get(0).getTronco().getBeaconEstremi().get(0).getPiano());
            for(int i = 1 ; i< percorsoPost.getTappe().size()-1;i++){ //1 8 8 4 4
                Tappa tappa = percorsoPost.getTappe().get(i);
                percorso.add(tappa.getTronco().getBeaconEstremi().get(0).getCoordx());
                percorso.add(tappa.getTronco().getBeaconEstremi().get(0).getCoordy());
                percorso.add(tappa.getTronco().getBeaconEstremi().get(0).getPiano());

            }
            percorso.add(percorsoPost.getTappe().get(percorsoPost.getTappe().size()-1).getTronco().getBeaconEstremi().get(0).getCoordx());            //1 8 8 4 4 5 ->
            percorso.add(percorsoPost.getTappe().get(percorsoPost.getTappe().size()-1).getTronco().getBeaconEstremi().get(0).getCoordy());
            percorso.add(percorsoPost.getTappe().get(percorsoPost.getTappe().size()-1).getTronco().getBeaconEstremi().get(0).getPiano());
            percorso.add(percorsoPost.getTappe().get(percorsoPost.getTappe().size()-1).getTronco().getBeaconEstremi().get(1).getCoordx());            //1 8 8 4 4 5 ->
            percorso.add(percorsoPost.getTappe().get(percorsoPost.getTappe().size()-1).getTronco().getBeaconEstremi().get(1).getCoordy());
            percorso.add(percorsoPost.getTappe().get(percorsoPost.getTappe().size()-1).getTronco().getBeaconEstremi().get(1).getPiano());

            if(percorso.get(2)==0) {
                imageViewPiano.setImage(ImageSource.resource(R.drawable.q140));
                posizione = 0;
                spinner.setSelection(percorso.get(2), true);
            }
            else{
                imageViewPiano.setImage(ImageSource.resource(R.drawable.q145));
                posizione = 1;
                spinner.setSelection(percorso.get(2), true);
            }

            for(int j=0; j<percorso.size(); j+=3 ){
                if(posizione == percorso.get(j+2)){
                    coorddelpercorso.add(percorso.get(j));
                    coorddelpercorso.add(percorso.get(j+1));
                }
            }
            //coorddelpercorso = daoBeacon.getCoords(percorso);  // Crea una lista in cui vengono contenuti le coordinate di tutti i beacon del percorso
            imageViewPiano.play(coorddelpercorso);
            calcolopercorso_in_corso.dismiss();
        }
    }

    public void visualizzaPercorso(Context ctx, PinView imageViewPiano) { new VisualizzaPercorsoTask(ctx, imageViewPiano).execute(); }

    private class VisualizzaPercorsoTask extends AsyncTask<Void,Void,String> {
        private Context ctx;
        private boolean connesso;
        private AsyncTask<Void, Void, Boolean> execute;
        private PinView imageViewPiano;

        public VisualizzaPercorsoTask(Context ctx, PinView imageViewPiano) {
            this.ctx = ctx;
            this.imageViewPiano = imageViewPiano;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            CheckConnessione checkConnessione = new CheckConnessione();
            connesso = checkConnessione.checkConnessione();
        }

        @Override
        protected String doInBackground(Void... voids) {

            if (!connesso) {
                return null;
            } else {
                try {
                    byte[] data = utente_attivo.getIdsessione().getBytes("UTF-8");
                    String base64 = android.util.Base64.encodeToString(data, Base64.DEFAULT);
                    URL url = new URL(PATH + "/gestionemappe/mappe/secured/visualizzapercorso/" + utente_attivo.getId_utente() + "/" + utente_attivo.getBeaconid());
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Authorization", "basic " + base64);
                    conn.connect();

                    int responseCode = conn.getResponseCode();
                    if(400 <= responseCode && responseCode <= 499){
                        this.cancel(true);
                    }

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
            if (result == null) {
                percorsoEmergenza = calcolaPercorsoEmergenza(utente_attivo.getBeaconid(),ctx);
            } else {
                Notifica notifica = new Gson().fromJson(result, Notifica.class);
                percorsoEmergenza = notifica.getPercorso();
            }
            // Devo tradurre il Percorso percorsoEmergenza in Tappe poi in Tronchi poi in ArrayList<Integer> percorso
            //1 8 8 4 4 5
            percorsoEmer.add(percorsoEmergenza.getTappe().get(0).getTronco().getBeaconEstremi().get(0).getId());
            for (int i = 1; i < percorsoEmergenza.getTappe().size() - 1; i++) { //1 8 8 4 4
                Tappa tappa = percorsoEmergenza.getTappe().get(i);
                percorsoEmer.add(tappa.getTronco().getBeaconEstremi().get(0).getId());

            }
            percorsoEmer.add(percorsoEmergenza.getTappe().get(percorsoEmergenza.getTappe().size() - 1).getTronco().getBeaconEstremi().get(0).getId());            //1 8 8 4 4 5 ->
            percorsoEmer.add(percorsoEmergenza.getTappe().get(percorsoEmergenza.getTappe().size() - 1).getTronco().getBeaconEstremi().get(1).getId());            //1 8 8 4 4 5 ->
            // 8 8 4 4

            //1 | 8 4 | 5

            System.out.println("Percorso in Rich Perc: " + percorsoEmer);

            DAOBeacon daoBeacon = new DAOBeacon(ctx);
            daoBeacon.open();
            coordEmergenza = daoBeacon.getCoords(percorsoEmer);  // Crea una lista in cui vengono contenuti le coordinate di tutti i beacon del percorso
            imageViewPiano.play(coordEmergenza);
            //Toast.makeText(getApplicationContext(), "Long press: " + ((int)sCoord.x) + ", " + ((int)sCoord.y), Toast.LENGTH_SHORT).show();
            daoBeacon.close();
        }
    }

    public Percorso calcolaPercorsoNoEmergenza(Context ctx, String beaconArr) {
        boolean emergenza = false;
        DAOBeacon beaconDAO = new DAOBeacon(ctx);
        beaconDAO.open();
        Beacon partenza = beaconDAO.getBeaconById(utente_attivo.getBeaconid());
        Beacon arrivo = beaconDAO.getBeaconById(beaconArr); // TODO:prendere dall'interfaccia
        beaconDAO.close();
        Percorso percorso;

        if (partenza != null && arrivo != null) {

            Map<LinkedList<Beacon>, Float> percorsoOttimo_costoOttimo =  calcoloDijkstra(partenza, arrivo, emergenza, ctx);

            LinkedList<Tappa> tappeOttime = new LinkedList<>();

            Map.Entry<LinkedList<Beacon>, Float> entry = percorsoOttimo_costoOttimo.entrySet().iterator().next();

            for(int i = 0; i < entry.getKey().size()-1; i++) {
                DAOTronco troncoDAO = new DAOTronco(ctx);
                troncoDAO.open();
                Tronco troncoOttimo = troncoDAO.getTroncoByBeacons(entry.getKey().get(i), entry.getKey().get(i+1));
                boolean direzione = troncoDAO.checkDirezioneTronco(troncoOttimo);
                troncoDAO.close();
                Tappa tappaOttima = new Tappa(troncoOttimo, direzione);
                tappeOttime.add(tappaOttima);
            }
            percorso = new Percorso(tappeOttime, partenza);
        }else{
            percorso = null;
        }
        return percorso;
    }

    public Percorso calcolaPercorsoEmergenza(String beaconPart, Context ctx) {
        DAOBeacon beaconDAO = new DAOBeacon(ctx);
        beaconDAO.open();
        Set<Beacon> pdr = beaconDAO.getAllPuntiDiRaccolta();
        Beacon partenza = beaconDAO.getBeaconById(beaconPart);
        beaconDAO.close();
        if (partenza != null) {
            boolean emergenza = true;
            Map<LinkedList<Beacon>, Float> percorsi_ottimi = new HashMap<>();
            Iterator<Beacon> n = pdr.iterator();
            while (n.hasNext()) {
                Beacon arrivo = n.next();

                Map<LinkedList<Beacon>, Float> percorsoOttimo_costoOttimo =  calcoloDijkstra(partenza, arrivo, emergenza, ctx);

                Map.Entry<LinkedList<Beacon>, Float> entry = percorsoOttimo_costoOttimo.entrySet().iterator().next();

                percorsi_ottimi.put(entry.getKey(), entry.getValue());
            }
            LinkedList<Beacon> percorso_def = new LinkedList<>();
            float costo_percorso_def = Float.MAX_VALUE;
            Iterator<Map.Entry<LinkedList<Beacon>, Float>> iter = percorsi_ottimi.entrySet().iterator();
            while (iter.hasNext()) {
                //Log.d("scelta percorso", "entrato");
                Map.Entry<LinkedList<Beacon>, Float> percorso_costo = iter.next();
                float costo_valore = percorso_costo.getValue();
                if (costo_valore < costo_percorso_def) {
                    percorso_def = percorso_costo.getKey();
                    costo_percorso_def = costo_valore;
                }
            }
            if (percorsi_ottimi.isEmpty()) {
                percorso_def.add(partenza);
            }
            LinkedList<Tappa> tappeOttime = new LinkedList<>();
            for(int i = 0; i < percorso_def.size()-1; i++) {
                DAOTronco troncoDAO = new DAOTronco(ctx);
                troncoDAO.open();
                Tronco troncoOttimo = troncoDAO.getTroncoByBeacons(percorso_def.get(i), percorso_def.get(i+1));
                boolean direzione = troncoDAO.checkDirezioneTronco(troncoOttimo);
                troncoDAO.close();
                Tappa tappaOttima = new Tappa(troncoOttimo, direzione);
                tappeOttime.add(tappaOttima);
            }
            Percorso percorso = new Percorso(tappeOttime, partenza);
            return percorso;
        }
        return null;
    }

    private Map<LinkedList<Beacon>, Float> calcoloDijkstra(Beacon partenza, Beacon arrivo, boolean emergenza, Context ctx){

        DAOTronco troncoDAO = new DAOTronco(ctx);
        troncoDAO.open();
        Set<Tronco> allTronchiEdificio = troncoDAO.getAllTronchi();
        troncoDAO.close();
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
                    costo = tronco.calcolaCosto(ctx);
                }else{
                    DAOPesiTronco pesiTroncoDAO = new DAOPesiTronco(ctx);
                    pesiTroncoDAO.open();
                    costo = pesiTroncoDAO.geValoreByPesoId(tronco.getId(), "l");
                    pesiTroncoDAO.close();
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

        System.out.println(beacon.getId());
        for(Beacon b : beacons){
            System.out.println(b.getId());
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

    }
}
