package com.example.alessandro.gosafe;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Build;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
 * Classe di navigazione è la prima ad essere caricata dopo il login.
 */
public class VaiActivity extends DefaultActivity {

    private boolean load = true;
    private boolean drawn = false;
    float distance;
    float temp=10000000;
    String idbeacondestinazione;

    /*menu a tendina*/
    Spinner spinner;
    Beacon beaconD;
    int position;

    Context ctx;
    ArrayAdapter<String> adapter;
    private PinView imageViewPiano;
    private TextView textLabel;
    Utente user;
    private RichiestaPercorso richiestaPercorso;
    private Bitmap bitmap;

    /**
     * Metodo di inizializzazione dell'activity. Implementa l'interfaccia grafica e le funzionalità. Controlla che siano attivi anche i servizi di base
     * touch-screen.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vai);

        /*Apertura DB e recupero utente */
        DAOUtente daoUtente = new DAOUtente(this);
        daoUtente.open();
        user = daoUtente.findUtente();
        daoUtente.close();
        richiestaPercorso = new RichiestaPercorso(user);

        //controlla l'esecuzione del servizio che cerca i beacon
        if(!isMyServiceRunning(BluetoothLeService.class)){
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
                Intent s = new Intent(this, BluetoothLeService.class);            //rimanda l'utente al servizio, può essere modificato
                Bundle bundle = new Bundle();
                bundle.putSerializable("user", user);
                bundle.putLong("periodo", 15000);
                s.putExtras(bundle);
                startService(s);
            }
        }
        //controlla l'esecuzione del servizio che si occupa di controllare aggiornamenti dal server
        if(!isMyServiceRunning(CheckForDbUpdatesService.class)){
            Intent u = new Intent(this, CheckForDbUpdatesService.class);
            startService(u);
        }
        ctx = this;
        //inizializza il broadcast manager che consente al servizio della ricerca dei beacon di comunicare con la GUI
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mGattUpdateReceiver, new IntentFilter("updatepositionmap"));

        /*Apertura DB e recupero piani */
        DAOPiano daoPiano = new DAOPiano(this);
        daoPiano.open();
        ArrayList<String> piani = daoPiano.getAllPiani();
        daoPiano.close();

        /*Inizializzazione menù a tendina per scelta piani */
        spinner= (Spinner) findViewById(R.id.spinner);
        adapter = new ArrayAdapter<String> (this,android.R.layout.simple_spinner_dropdown_item,piani);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        textLabel = (TextView) findViewById(R.id.vaiLabel);
        imageViewPiano = (PinView) findViewById(R.id.imageViewPiano);

        /*Apertura DB e recupero beacon di posizione iniziale*/
        DAOBeacon daoBeacon = new DAOBeacon(this);
        daoBeacon.open();
        Beacon beaconMyPosition = daoBeacon.getBeaconById(user.getBeaconid());
        PointF pinMyPosition = new PointF(beaconMyPosition.getCoordx(), beaconMyPosition.getCoordy());
        imageViewPiano.setPinMyPosition(pinMyPosition);
        daoBeacon.close();

        //setta il listener sullo spinner per quando si cambia piano
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                changedPiano(adapterView, i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        /*Definizione gesture detector per le funzionalità touch screen*/
        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            //CALCOLO DEL PERCORSO E DISEGNO

            @Override
            public void onLongPress(MotionEvent e) {

                DAOBeacon daoBeacon = new DAOBeacon(ctx);
                daoBeacon.open();
                if (imageViewPiano.isReady()) {
                    PointF sCoord = imageViewPiano.viewToSourceCoord(e.getX(), e.getY());
                    if(load) {


                        /*BEACON DI PARTENZA
                        Definzione delle dcoordinate di partenza*/
                        String idbeacondipartenza = user.getBeaconid();
                        if(idbeacondipartenza!= null) {
                            ArrayList<Integer> xcoordandycoord = daoBeacon.getCoordsByIdBeacon(idbeacondipartenza);
                            int coordxpartenza = xcoordandycoord.get(0);
                            int coordypartenza = xcoordandycoord.get(1);
                            PointF mCoord = imageViewPiano.sourceToViewCoord((float) coordxpartenza, (float) coordypartenza);
                        }
                        load = false;
                    }

                    /*BEACON DI DESTINAZIONE
                    Calcola il beacon di destinazione + vicino rispetto al click dell'utente*/
                    Cursor cursor;
                    cursor = daoBeacon.getAllBeaconInPiano(position);

                    while (cursor.moveToNext()){
                        int coordx = cursor.getInt(cursor.getColumnIndex("coordx"));
                        int coordy = cursor.getInt(cursor.getColumnIndex("coordy"));
                        distance = (float)Math.sqrt(((sCoord.x-coordx)*(sCoord.x-coordx))+((sCoord.y-coordy)*(sCoord.y-coordy)));
                        if (distance < temp){
                            temp=distance;
                            idbeacondestinazione=cursor.getString(cursor.getColumnIndex("ID_beacon"));
                        }
                    }
                    beaconD = daoBeacon.getBeaconById(idbeacondestinazione);
                    PointF pin = new PointF(beaconD.getCoordx(), beaconD.getCoordy());
                    imageViewPiano.setPin(pin);
                    textLabel.setText("Ora clicca il pulsante VAI per raggiungere la destinazione");
                    temp=10000000;

                    imageViewPiano.setBool(true);
                } else {
                    Toast.makeText(getApplicationContext(), "Long press: Image not ready", Toast.LENGTH_SHORT).show();
                }
                daoBeacon.close();

            }
        });

        /*Inizializzazione listener evento di movimento sullo schermo*/
        imageViewPiano.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });

        /*Istanziazione della barra di navigazione in fondo allo schermo*/
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);

    }


    /**
     * Oggetto che si occupa di gestire la ricezione del broadcast qualora ci sia un cambio di beacon
     * a cui l'utente è connesso.
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            String idBeacon = intent.getStringExtra("device");
            user.setBeaconid(idBeacon);
            DAOBeacon daoBeacon = new DAOBeacon(ctx);
            daoBeacon.open();
            Beacon beacon = daoBeacon.getBeaconById(idBeacon);
            ArrayList<Integer> newPosition = daoBeacon.getCoordsByIdBeacon(idBeacon);
            daoBeacon.close();
            DAOPiano daoPiano = new DAOPiano(getApplicationContext());
            daoPiano.open();
            position = daoPiano.getNumeroPianoById(beacon.getPiano());
            daoPiano.close();
            ArrayAdapter<String> elements = (ArrayAdapter<String>) spinner.getAdapter();
            int spinnerposition = 10000;
            for(int i=0 ; i<elements.getCount() ; i++){
                if(String.valueOf(position).equals(elements.getItem(i).split(" ")[1]))
                    spinnerposition = i;
            }
            spinner.setSelection(spinnerposition, true);
            changedPiano(spinner, spinnerposition);
            int coordxpartenza = newPosition.get(0);
            int coordypartenza = newPosition.get(1);
            PointF mCoord = imageViewPiano.sourceToViewCoord((float) coordxpartenza, (float) coordypartenza);
            PointF newCoord = imageViewPiano.viewToSourceCoord(mCoord.x, mCoord.y);
            imageViewPiano.setPinMyPosition(newCoord);

        }
    };

    /**
     * Metodo eseguito quando si esce dall'app e rimane in background
     * elimina gli elementi grafici per evitare errori di memoria
     */
    @Override
    public void onPause(){
        imageViewPiano.recycle();
        if(bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        super.onPause();
    }

    /**
     * Metodo che viene eseguito quando l'app viene riaperta. Ripopola le immagini.
     */
    @Override
    public void onResume(){
        String piano = spinner.getSelectedItem().toString();
        String[] elems = piano.split(" ");
        bitmap = ImageLoader.loadImageFromStorage(elems[1], ctx);
        imageViewPiano.setImage(ImageSource.bitmap(bitmap));
        load = true;
        imageViewPiano.setBool(false);
        super.onResume();
    }

    /**
     * Metodo eseguito quando si termina l'applicazione o si cambia activity.
     * anche qui si eliminano elementi grafici per evitare problemi di memoria
     */
    @Override
    public void onDestroy(){
        if(bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        finish();
        super.onDestroy();

    }

    /**
     * Metodo che implementa il calcolo del percorso attraverso l'utilizzo della classe RichiestaPercorso nel
     * package server.
     * @param view
     */
    public void avviaPercorso(View view){

        /*Se le posizioni di partenza e arrivo sono settate e diverse tra loro, richiede il calcolo del percorso
        * in non emergenza.*/
        if(user.getPosition()!= null && beaconD != null && !user.getPosition().equals(beaconD.getId())) {
            imageViewPiano.setBool(false);
            DAOPiano daoPiano = new DAOPiano(this);
            daoPiano.open();
            imageViewPiano.setPianoArrivo(daoPiano.getNumeroPianoById(beaconD.getPiano()));
            daoPiano.close();
            richiestaPercorso = new RichiestaPercorso(user);
            DAOUtente daoUtente = new DAOUtente(this);
            daoUtente.open();
            user = daoUtente.findUtente();
            daoUtente.close();
            richiestaPercorso.ottieniPercorsoNoEmergenza(ctx, String.valueOf(idbeacondestinazione), imageViewPiano, position, spinner, user);
            drawn = true;
            textLabel.setText("Tieni Premuto il punto in cui desideri arrivare");
        }else if(user.getPosition() == null){
            /*Altrimenti se la posizione di partenza è nulla (utente non connesso a un beacon), visualizza messaggio di
            * non connessione al beacon*/
            AlertDialog posnotselected = new AlertDialog.Builder(ctx).create();
            posnotselected.setTitle("Attenzione!");
            posnotselected.setMessage(ctx.getString(R.string.nessunbeaconconnesso));
            posnotselected.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            posnotselected.show();
        } else if(beaconD == null){
            /*Altrimenti se la posizione di arrivo è nulla (nessun beacon è stato selezionato), visualizza messaggio di
            * avviso che nessuna destinazione è stata selezionata*/
            AlertDialog posnotselected = new AlertDialog.Builder(ctx).create();
            posnotselected.setTitle("Nessuna destinazione selezionata");
            posnotselected.setMessage(ctx.getString(R.string.nessunadestinazioneselezionata));
            posnotselected.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            posnotselected.show();
        }else{
            /*Altrimenti se i beacon di partenza e arrivo coincidono, viene visualizzato un messaggio di avviso
            * all'utente*/
            AlertDialog sameDestination = new AlertDialog.Builder(ctx).create();
            sameDestination.setTitle("Destinazione Coincide con Posizione Attuale");
            sameDestination.setMessage(ctx.getString(R.string.posizionecoincideconposizione));
            sameDestination.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            sameDestination.show();
        }
    }

    /**
     * Metodo che si occupa di visualizzare il piano corretto quando si cambia il piano nello spinner
     * @param adapterView rappresenta l'adapterview dello spinner
     * @param i contiene il numero dell'elemento cliccato
     */
    private void changedPiano(AdapterView<?> adapterView, int i) {
        String piano =(String) adapterView.getItemAtPosition(i);
        String[] elems = piano.split(" ");
        bitmap = ImageLoader.loadImageFromStorage(elems[1], ctx);
        position = Integer.parseInt(elems[1]);
        imageViewPiano.setImage(ImageSource.bitmap(bitmap));
        load = true;
        imageViewPiano.setBool(false);
        DAOBeacon daoBeacon = new DAOBeacon(getApplicationContext());
        daoBeacon.open();
        Beacon beaconMyPosition = daoBeacon.getBeaconById(user.getBeaconid());
        daoBeacon.close();
        DAOPiano daoPiano = new DAOPiano(getApplicationContext());
        daoPiano.open();
        int numeroPiano = daoPiano.getNumeroPianoById(beaconMyPosition.getPiano());
        daoPiano.close();
        if(String.valueOf(numeroPiano).equals(elems[1])){
            imageViewPiano.setPianoUtente(true);
        } else {
            imageViewPiano.setPianoUtente(false);
        }
        if(drawn){
            richiestaPercorso.cambiaPiano(imageViewPiano, position);
            imageViewPiano.setPianoSpinner(position);
        }
    }

    /**
     * Controlla se un certo servizio è in esecuzione
     * @param serviceClass nome del servizio da controllare
     * @return ritorna un booleano: True se è in esecuzione altrimenti Falso
     */
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
