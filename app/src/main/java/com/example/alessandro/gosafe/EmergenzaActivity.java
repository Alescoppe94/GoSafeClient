package com.example.alessandro.gosafe;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.example.alessandro.gosafe.beacon.BluetoothLeService;
import com.example.alessandro.gosafe.database.DAOBeacon;
import com.example.alessandro.gosafe.database.DAOPiano;
import com.example.alessandro.gosafe.database.DAOUtente;
import com.example.alessandro.gosafe.entity.Beacon;
import com.example.alessandro.gosafe.entity.Utente;
import com.example.alessandro.gosafe.helpers.ImageLoader;
import com.example.alessandro.gosafe.helpers.PinView;
import com.example.alessandro.gosafe.server.CheckForDbUpdatesService;
import com.example.alessandro.gosafe.server.RichiestaPercorso;

import java.util.ArrayList;

/**
 * Classe che implementa la scgermata di emergenza
 */
public class EmergenzaActivity extends DefaultActivity {

    private PinView imageViewPiano;
    private Utente utente_attivo;

    private Bitmap bitmap;
    private RichiestaPercorso richiestaPercorso;
    private ArrayList<String> percorsoEmer;

    /**
     * Inizializza la classe di EmergenzaActivity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergenza);

        DAOUtente daoUtente = new DAOUtente(this);
        daoUtente.open();
        utente_attivo = daoUtente.findUtente();
        daoUtente.close();

        if(!isMyServiceRunning(BluetoothLeService.class)){
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
                Intent s = new Intent(this, BluetoothLeService.class);            //rimanda l'utente al servizio, può essere modificato
                Bundle bundle = new Bundle();
                bundle.putSerializable("user", utente_attivo);
                bundle.putLong("periodo", 15000);
                s.putExtras(bundle);
                startService(s);
            }
        }
        if(!isMyServiceRunning(CheckForDbUpdatesService.class)){
            Intent u = new Intent(this, CheckForDbUpdatesService.class);
            startService(u);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mGattUpdateReceiver, new IntentFilter("updatepositionmap"));
        SharedPreferences.Editor editor = getSharedPreferences("isEmergenza", MODE_PRIVATE).edit();
        editor.putBoolean("emergenza", true);
        editor.apply();
        imageViewPiano=(PinView) findViewById(R.id.imageViewPiano);
        richiestaPercorso = new RichiestaPercorso(utente_attivo);
        if(utente_attivo.getBeaconid() != null) {
            DAOBeacon daoBeacon = new DAOBeacon(this);
            daoBeacon.open();
            Beacon beaconUtente = daoBeacon.getBeaconById(utente_attivo.getBeaconid());
            PointF pinMyPosition = new PointF(beaconUtente.getCoordx(), beaconUtente.getCoordy());
            imageViewPiano.setPinMyPosition(pinMyPosition);
            imageViewPiano.setPianoUtente(true);
            DAOPiano daoPiano = new DAOPiano(this);
            daoPiano.open();
            bitmap = ImageLoader.loadImageFromStorage(String.valueOf(daoPiano.getNumeroPianoById(daoBeacon.getBeaconById(utente_attivo.getBeaconid()).getPiano())), this);
            imageViewPiano = (PinView) findViewById(R.id.imageViewPiano);
            imageViewPiano.setImage(ImageSource.bitmap(bitmap));
            daoPiano.close();
            daoBeacon.close();
            if(!beaconUtente.is_puntodiraccola()) {
                richiestaPercorso.visualizzaPercorso(this, imageViewPiano);
            } else {
                showAlertSalvo();
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Attenzione!");
            builder.setMessage("Non sei connesso a nessun beacon.\nSei sicuro di trovarti nell'edificio?");

            builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    SharedPreferences.Editor editor = getSharedPreferences("isEmergenza", MODE_PRIVATE).edit();
                    editor.putBoolean("emergenza", false);
                    editor.apply();
                    startActivity(new Intent(getApplicationContext(), VaiActivity.class));
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }
    }



    public boolean salvo(MenuItem item){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Sei salvo?");
        builder.setMessage("Una volta cliccato si non riceverai più informazioni sulla via di fuga. Continuare?");

        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                SharedPreferences.Editor editor = getSharedPreferences("isEmergenza", MODE_PRIVATE).edit();
                editor.putBoolean("emergenza", false);
                editor.apply();
                startActivity(new Intent(getApplicationContext(), VaiActivity.class));
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

        return true;
    }


    @Override
    public void onPause(){
        imageViewPiano.recycle();
        if(bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        super.onPause();
    }

    @Override
    public void onResume(){
        DAOPiano daoPiano = new DAOPiano(this);
        DAOBeacon daoBeacon = new DAOBeacon(this);
        daoPiano.open();
        daoBeacon.open();
        int numeropiano = daoPiano.getNumeroPianoById(daoBeacon.getBeaconById(utente_attivo.getBeaconid()).getPiano());
        daoBeacon.close();
        daoPiano.close();
        bitmap = ImageLoader.loadImageFromStorage(String.valueOf(numeropiano), this);
        imageViewPiano.setImage(ImageSource.bitmap(bitmap));

        imageViewPiano.setBool(false);
        super.onResume();
    }


    @Override
    public void onDestroy(){
        if(bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        finish();
        super.onDestroy();

    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            String idBeacon = intent.getStringExtra("device");
            utente_attivo.setBeaconid(idBeacon);
            DAOBeacon daoBeacon = new DAOBeacon(context);
            daoBeacon.open();
            Beacon beaconVecchio = daoBeacon.getBeaconById(utente_attivo.getBeaconid());
            Beacon beaconNuovo = daoBeacon.getBeaconById(idBeacon);
            DAOPiano daoPiano = new DAOPiano(context);
            daoPiano.open();
            int numeropiano = daoPiano.getNumeroPianoById(daoBeacon.getBeaconById(utente_attivo.getBeaconid()).getPiano());
            daoPiano.close();
            bitmap = ImageLoader.loadImageFromStorage(String.valueOf(numeropiano), context);
            imageViewPiano.setImage(ImageSource.bitmap(bitmap));
            imageViewPiano.setBool(false);
            PointF pinMyPosition = new PointF(beaconNuovo.getCoordx(), beaconNuovo.getCoordy());
            imageViewPiano.setPinMyPosition(pinMyPosition);

            if(!beaconNuovo.is_puntodiraccola()) {

                SharedPreferences sharedPreferences = context.getSharedPreferences("isConnesso", context.MODE_PRIVATE);
                boolean connesso = sharedPreferences.getBoolean("connesso", false);
                if (connesso) {
                    DAOUtente daoUtente = new DAOUtente(context);
                    daoUtente.open();
                    utente_attivo = daoUtente.findUtente();
                    daoUtente.close();
                    richiestaPercorso.setUtente_attivo(utente_attivo);
                    richiestaPercorso.visualizzaPercorso(context, imageViewPiano);
                    richiestaPercorso.getResult();
                } else {
                    percorsoEmer = richiestaPercorso.getPercorsoEmer();
                    if (percorsoEmer.contains(beaconNuovo.getId())) {                          // significa che il pupo è nel percorso giusto, quindi va pulito il disegno
                        int indexBeaconUtente = percorsoEmer.indexOf(beaconNuovo.getId());
                        for (int i = 0; i < indexBeaconUtente; i++) {
                            percorsoEmer.remove(i);
                        }
                        imageViewPiano.play(daoBeacon.getCoords(percorsoEmer));
                    } else {                                                                    //significa che il pupo è fuori strada, va quindi ricalcolato il percorso
                        richiestaPercorso.setUtente_attivo(utente_attivo);
                        richiestaPercorso.visualizzaPercorso(context, imageViewPiano);
                        richiestaPercorso.getResult();
                    }

                }
                if (beaconVecchio.getPiano() != beaconNuovo.getPiano()) {
                    DAOPiano pianoDao = new DAOPiano(context);
                    pianoDao.open();
                    bitmap = ImageLoader.loadImageFromStorage(String.valueOf(daoPiano.getNumeroPianoById(beaconNuovo.getPiano())), context);
                    imageViewPiano.setImage(ImageSource.bitmap(bitmap));
                    pianoDao.close();
                }
                ArrayList<Integer> newPosition = daoBeacon.getCoordsByIdBeacon(idBeacon);
                daoBeacon.close();
                int coordxpartenza = newPosition.get(0);
                int coordypartenza = newPosition.get(1);
                PointF mCoord = imageViewPiano.sourceToViewCoord((float) coordxpartenza, (float) coordypartenza);
                PointF newCoord = imageViewPiano.viewToSourceCoord(mCoord.x, mCoord.y);
                imageViewPiano.setPin(newCoord);

            } else {
                showAlertSalvo();
            }

        }
    };

    private void showAlertSalvo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Attenzione!");
        builder.setMessage("E' in corso un' emergenza ma ti trovi in un punto di raccolta.\nSii prudente e resta dove ti trovi!");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                SharedPreferences.Editor editor = getSharedPreferences("isEmergenza", MODE_PRIVATE).edit();
                editor.putBoolean("emergenza", false);
                editor.apply();
                startActivity(new Intent(getApplicationContext(), VaiActivity.class));
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
