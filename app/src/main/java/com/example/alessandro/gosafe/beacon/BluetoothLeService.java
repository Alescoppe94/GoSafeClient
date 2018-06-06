package com.example.alessandro.gosafe.beacon;


import android.app.ActivityManager;
import android.app.Service;

/**
 * Created by Alessandro on 30/03/2018.
 */

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.alessandro.gosafe.database.DAOUtente;
import com.example.alessandro.gosafe.entity.Utente;
import com.example.alessandro.gosafe.server.AggiornamentoInfoServer;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import static android.content.ContentValues.TAG;


/*Service che si occupa di connettere e leggere i dati dal Beacon.*/

public class BluetoothLeService extends Service {

    private BluetoothAdapter mBluetoothAdapter;

    private Utente utente_attivo;

    private boolean mScanning;
    private Handler mHandler;
    private Timer mTimer;

    private long scanPeriod;
    private int signalStrength;

    private BluetoothLeScanner mBluetoothLeScanner;

    private LinkedHashMap<String, Integer> beaconsDetected;

    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new Handler();

        this.scanPeriod = 3000;
        //this.signalStrength = signalStrength;

        beaconsDetected = new LinkedHashMap<>();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mScanning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //final Intent intent1 = new Intent("univpm.iot_for_emergency.View.Funzionali.Scansione");
        //sendBroadcast(intent1);
        utente_attivo = (Utente) intent.getExtras().getSerializable("user");
        final long period = (Long) intent.getExtras().getLong("periodo");
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isScanning()){
                    scanLeDevice(true);
                    Log.d("partito", "started");
                }
            }
        }, 10000, period);
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean isScanning() {
        return mScanning;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mTimer.cancel();
        mTimer = null;

    }


    // If you want to scan for only specific types of peripherals,
    // you can instead call startLeScan(UUID[], BluetoothAdapter.LeScanCallback),
    // providing an array of UUID objects that specify the GATT services your app supports.
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            //Utils.toast(ma.getApplicationContext(), "Starting BLE scan...");

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Utils.toast(ma.getApplicationContext(), "Stopping BLE scan...");

                    Log.d("partito2", "started");

                    mScanning = false;
                    mBluetoothLeScanner.stopScan(mLeScanCallback);

                    orderByValue(beaconsDetected, new Comparator<Integer>() {
                        @Override
                        public int compare(Integer integer, Integer t1) {
                            int i = integer.compareTo(t1);
                            if(i != 0)
                                i = -i;
                            return i;
                        }
                    });
                    if(beaconsDetected.entrySet().iterator().hasNext()) {
                        String posizione = beaconsDetected.entrySet().iterator().next().getKey();
                        if(!utente_attivo.getBeaconid().equals(posizione)) {
                            utente_attivo.setPosition(posizione, getApplicationContext());
                            AggiornamentoInfoServer ai = new AggiornamentoInfoServer();
                            ai.aggiornamentoPosizione(utente_attivo, getApplicationContext());
                            final Intent intent = new Intent("updatepositionmap");
                            intent.putExtra("device", posizione);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                            Log.d("dispositivo", posizione);
                        }
                    }
                    scanLeDevice(false);

                }
            }, scanPeriod);

            mScanning = true;
            mBluetoothLeScanner.startScan(mLeScanCallback);
            Log.d("partito1", "started");
//            mBluetoothAdapter.startLeScan(uuids, mLeScanCallback);
        }
        else {
            mScanning = false;
            beaconsDetected = null;
            beaconsDetected = new LinkedHashMap<>();
            Log.d("partito3", "started");
        }
    }

    // Device scan callback.
    private ScanCallback mLeScanCallback = new ScanCallback() {

                @Override
                public void onScanResult(final int callbackType, final ScanResult result) {
                    super.onScanResult(callbackType, result);
                    BluetoothDevice device = result.getDevice();
                    final String deviceName = device.getName();
                    Log.d("RSSI", String.valueOf(result.getRssi()) + " " + device.getName());
                    final String mBluetoothDeviceAddress=device.getAddress();

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