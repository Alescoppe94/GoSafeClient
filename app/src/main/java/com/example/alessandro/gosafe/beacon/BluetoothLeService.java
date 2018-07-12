package com.example.alessandro.gosafe.beacon;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.alessandro.gosafe.entity.Utente;
import com.example.alessandro.gosafe.server.AggiornamentoInfoServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * E' un servizio che si occupa di rilevare i bacons e comunicare all'interfaccia e al db
 * il beacon a cui si è connessi. Viene avviato al login e mantenuto sempre attivo.
 */
public class BluetoothLeService extends Service {

    private Utente utente_attivo;
    private boolean mScanning;
    private Handler mHandler;
    private Timer mTimer;       //conserva il thread per il loop di scansione dei beacon
    private long scanPeriod;    //contiene la durata della scansione
    private BluetoothLeScanner mBluetoothLeScanner;
    private LinkedHashMap<String, Integer> beaconsDetected;  //contiene i beacon individuati

    /**
     * metodo che viene eseguito al primo avvio del servizio. Inizializza il servizio.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new Handler();

        this.scanPeriod = 3000; //contiene ogni quanto fare la scansione

        beaconsDetected = new LinkedHashMap<>(); //contiene i beacon individuati. Inizializzata vuota.

        //inizializza le variabili per lavorare con i beacon
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mScanning = false;
    }

    /**
     * metodo chiamato ogni volta che il servizio viene riavviato. La prima volta parte subito dopo onCreate()
     * inizializza il timer per far partire il loop di scansione.
     * @param intent contiene l'intent che ha chiamato il servizio
     * @param flags
     * @param startId
     * @return contiene il servizio appena creato per il sistema
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        utente_attivo = (Utente) intent.getExtras().getSerializable("user");
        final long period = intent.getExtras().getLong("periodo");
        mTimer = new Timer();
        //il loop di scansione dei beacon. viene lanciato run ogni period
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
                if(mBluetoothAdapter.isEnabled()) {  // se il bluetooth è attivo e non è in corso una scansione può partire scanLeDevice
                    mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                    mScanning = false;
                    if (!isScanning()) {
                        scanLeDevice(true); //metodo che gestisce scansione vera e propria
                    }
                }
            }
        }, 10000, period);
        return Service.START_NOT_STICKY;
    }

    /**
     * metodo di avvio del servizio che fa il bind con l'applicazione di quest'ultimo
     * @param intent contiene l'intent chiamante
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * metodo che ci informa se è in corso una scansione
     * @return booleano settato a Vero se è in corso una scansione, altrimenti Falso
     */
    public boolean isScanning() {
        return mScanning;
    }

    /**
     * metodo chiamato quando il servizio viene distrutto
     */
    @Override
    public void onDestroy(){
        super.onDestroy();
        mTimer.cancel();  //viene fermato il loop di scansione
        mTimer = null;

    }

    /**
     * metodo che gestisce un ciclo di scansione
     * @param enable indica se la scansione può avvenire o meno
     */
    private void scanLeDevice(final boolean enable) {
        try {
            if (enable) {

                mScanning = true;

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() { //gestisce lo stop dello scan
                        try {
                            mBluetoothLeScanner.stopScan(mLeScanCallback); //ferma lo scan

                            orderByValue(beaconsDetected, new Comparator<Integer>() {
                                @Override
                                public int compare(Integer integer, Integer t1) {  //ordina i beacon per distanza il primo della lista è il più vicino
                                    int i = integer.compareTo(t1);
                                    if (i != 0)
                                        i = -i;
                                    return i;
                                }
                            });
                            if (beaconsDetected.entrySet().iterator().hasNext()) {  //se c'è un beacon
                                String posizione = beaconsDetected.entrySet().iterator().next().getKey();
                                if (!utente_attivo.getBeaconid().equals(posizione)) { //se è diverso da quello a cui era connesso l'utente
                                    utente_attivo.setPosition(posizione, getApplicationContext()); //setta la nuova posizione
                                    AggiornamentoInfoServer ai = new AggiornamentoInfoServer(); //avvia l'aggiornamento del server
                                    ai.aggiornamentoPosizione(utente_attivo, getApplicationContext());
                                    final Intent intent = new Intent("updatepositionmap"); // e notifica la gui per aggiornare la posizione sulla mappa
                                    intent.putExtra("device", posizione);
                                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                                }
                            }
                            scanLeDevice(false); //ferma lo scan
                        }catch(Exception e){
                            e.printStackTrace();
                        }

                    }
                }, scanPeriod);  //è la durata dello scan
                mBluetoothLeScanner.startScan(mLeScanCallback); //quando enable=True  si avvia lo scan
            } else { //se enable = false svuota la lista dei beacon individuati e setta lo scanning a false
                mScanning = false;
                beaconsDetected.clear();
            }
        }catch(Exception e){
            mScanning = false;
            beaconsDetected.clear();
        }
    }

    /**
     *
     * ogni volta che viene individuato un beacon viene richiamata questa callback
     */
    private ScanCallback mLeScanCallback = new ScanCallback() {

                @Override
                public void onScanResult(final int callbackType, final ScanResult result) {
                    super.onScanResult(callbackType, result);
                    //recupera le informazioni sul beacon
                    BluetoothDevice device = result.getDevice();
                    final String deviceName = device.getName();
                    final String mBluetoothDeviceAddress=device.getAddress();

                    //handler che gestisce i beacon da prendere in considerazione. verifica che non siano già stati individuati
                    //seleziona solo quelli che hanno il nome che contiene una certa stringa ad esempio "SensorTag"
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (deviceName != null && deviceName.length() > 0) {
                                if (deviceName.contains("XT1039") || deviceName.contains("OnePlus X") || deviceName.contains("SensorTag")) {
                                    if(beaconsDetected.containsKey(mBluetoothDeviceAddress)) {
                                        if (beaconsDetected.get(mBluetoothDeviceAddress) < result.getRssi())
                                            beaconsDetected.put(mBluetoothDeviceAddress, result.getRssi());
                                    }
                                    else if(!beaconsDetected.containsKey(mBluetoothDeviceAddress))
                                        beaconsDetected.put(mBluetoothDeviceAddress, result.getRssi());

                                }
                            } else {
                                Log.e("Errore", String.valueOf(callbackType));
                            }

                        }
                    });
                }
            };

    /**
     * metodo che ordina le liste
     * @param m la lista da ordinare
     * @param c comparator
     * @param <K> chiave di un elemento della lista
     * @param <V> valore di un elemento della lista
     */
    static <K, V> void orderByValue(LinkedHashMap<K, V> m, final Comparator<? super V> c) {
        List<Map.Entry<K, V>> entries = new ArrayList<>(m.entrySet());

        Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> lhs, Map.Entry<K, V> rhs) {
                return c.compare(lhs.getValue(), rhs.getValue());
            }
        });

        m.clear();
        for(Map.Entry<K, V> e : entries) {
            m.put(e.getKey(), e.getValue());
        }
    }
}