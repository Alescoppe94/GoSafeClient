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
    private static String LOG_TAG = "BluetoothLeService";
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private boolean mScanning;
    // Stops scanning after 10 seconds.
    private static int SCAN_PERIOD = 5000;
    private static int PAUSE_PERIOD = 5000;

    private String mBluetoothDeviceAddress;

    private Utente utente_attivo;

    private LinkedHashMap<String, Integer> beaconsDetected;


    @Override
    public void onCreate() {
        super.onCreate();
        getBluetoothAdapterAndLeScanner();
        mHandler = new Handler();
        mBluetoothDeviceAddress="";
        beaconsDetected = new LinkedHashMap<>();
        //sessione=new Sessione(this);
    }

    private void getBluetoothAdapterAndLeScanner() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //final Intent intent1 = new Intent("univpm.iot_for_emergency.View.Funzionali.Scansione");
        //sendBroadcast(intent1);
        utente_attivo = (Utente) intent.getExtras().getSerializable("user");
        final long period = (Long) intent.getExtras().getLong("periodo");
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                scanLeDevice(true);
                Log.d("partito", "started");
            }
        }, 5000, period);
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Lo scan si ferma dopo un tempo predefinito.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothLeScanner.stopScan(scanCallback);
                    Log.d("fermato", "stop");
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
                        System.out.println(beaconsDetected.entrySet().iterator().next().getKey());
                        utente_attivo.setPosition(beaconsDetected.entrySet().iterator().next().getKey(), getApplicationContext());
                        AggiornamentoInfoServer ai = new AggiornamentoInfoServer();
                        ai.aggiornamentoPosizione(utente_attivo);
                    }
                }
            }, SCAN_PERIOD);
            Log.d("fermato", "stop1");
            mBluetoothLeScanner.startScan(scanCallback);
        } else {
            Log.d("fermato", "stop2");
            beaconsDetected = null;
            beaconsDetected = new LinkedHashMap<>();
            mBluetoothLeScanner.stopScan(scanCallback);
        }
    }



    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public synchronized void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            final String deviceName = device.getName();
            Log.d("RSSI", String.valueOf(result.getRssi()) + " " + device.getName());
            mBluetoothDeviceAddress=device.getAddress();

            if (deviceName != null && deviceName.length() > 0) {
                if (deviceName.contains("XT1039") || deviceName.contains("OnePlus X") || deviceName.contains("SensorTag")) {
                    if(beaconsDetected.containsKey(mBluetoothDeviceAddress)) {
                        if (beaconsDetected.get(mBluetoothDeviceAddress) < result.getRssi())
                            beaconsDetected.put(mBluetoothDeviceAddress, result.getRssi());
                    }
                    else if(!beaconsDetected.containsKey(mBluetoothDeviceAddress))
                            beaconsDetected.put(mBluetoothDeviceAddress, result.getRssi());
                    //connect();
                    scanLeDevice(false);
                    final Intent intent = new Intent("updatepositionmap");
                    //Sessione sessione = new Sessione(getBaseContext());
                    //intent.putExtra("user", sessione.user());
                    intent.putExtra("device", mBluetoothDeviceAddress);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    Log.d("dispositivo", mBluetoothDeviceAddress);

                }
            } else {
                Log.e("Errore", String.valueOf(callbackType));
            }

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