package com.example.alessandro.gosafe;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.example.alessandro.gosafe.beacon.BluetoothLeService;
import com.example.alessandro.gosafe.database.DAOBeacon;
import com.example.alessandro.gosafe.database.DAOUtente;
import com.example.alessandro.gosafe.entity.Beacon;
import com.example.alessandro.gosafe.entity.Utente;
import com.example.alessandro.gosafe.helpers.PinView;
import com.example.alessandro.gosafe.server.CheckForDbUpdatesService;
import com.example.alessandro.gosafe.server.DbDownloadFirstBoot;
import com.example.alessandro.gosafe.server.RichiestaPercorso;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;

public class VaiActivity extends DefaultActivity {

    private ScaleGestureDetector SGD;
    private Matrix matrix = new Matrix();
    private ImageView imageView;
    private Float scale =1f;
    private PointF newCoord;
    private boolean load = true;
    private boolean drawn = false;
    float distance;
    float temp=10000000;
    int idbeacondestinazione;
    ArrayList<Integer> percorso;
    ArrayList<Integer> coorddelpercorso;
    DAOUtente daoUtente;

    /*roba per menu a tendina*/
    Spinner spinner;
    private PinView pinView;
    int x;
    int y;
    DAOBeacon daoBeacon;
    int position;

    Context ctx;
    ArrayAdapter<CharSequence> adapter;
    private PinView imageViewPiano;
    Utente user;

    //private UserSessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mappe);

        /*session = new UserSessionManager(getApplicationContext());
        if(session.checkLogin()){
            finish();
        }*/

        ctx = this;
        DbDownloadFirstBoot dbDownload = new DbDownloadFirstBoot();
        dbDownload.dbdownloadFirstBootAsyncTask(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mGattUpdateReceiver, new IntentFilter("updatepositionmap"));
        percorso = new ArrayList<Integer>();

       /*BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            DAOUtente daoUtente = new DAOUtente(this);
            daoUtente.open();
            user = daoUtente.findUtente();
            daoUtente.close();
            Intent s = new Intent(this, BluetoothLeService.class);            //rimanda l'utente al servizio, può essere modificato
            Bundle bundle = new Bundle();
            bundle.putSerializable("user", user);
            bundle.putLong("periodo", 20000);
            s.putExtras(bundle);
            startService(s);
        }*/
        daoBeacon = new DAOBeacon(this);
        daoBeacon.open();
        daoUtente = new DAOUtente(this);
        daoUtente.open();

        Intent u = new Intent(this, CheckForDbUpdatesService.class);
        startService(u);

        //Spinner
        spinner= (Spinner) findViewById(R.id.spinner);
        adapter=ArrayAdapter.createFromResource(this,R.array.piani,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        imageViewPiano = (PinView) findViewById(R.id.imageViewPiano);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                position = spinner.getSelectedItemPosition();
                Toast.makeText(getBaseContext(), adapterView.getItemAtPosition(i) + " selected", Toast.LENGTH_LONG).show();
                switch (position) {
                    case 0:
                        imageViewPiano.setImage(ImageSource.resource(R.drawable.q140));
                        load = true;
                        if(drawn){
                            //cancella vecchio percorso
                            //chiama richiestapercorso
                            RichiestaPercorso richiestaPercorso = new RichiestaPercorso(user);
                            richiestaPercorso.ottieniPercorsoNoEmergenza(ctx,String.valueOf(idbeacondestinazione), imageViewPiano, position);
                        }
                        break;
                    case 1:
                        imageViewPiano.setImage(ImageSource.resource(R.drawable.q145));
                        load = true;
                        if(drawn){
                            //cancella vecchio percorso
                            //chiama richiestapercorso
                            RichiestaPercorso richiestaPercorso = new RichiestaPercorso(user);
                            richiestaPercorso.ottieniPercorsoNoEmergenza(ctx,String.valueOf(idbeacondestinazione), imageViewPiano, position);
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //serve per definire le gesture da rilevare, per ora lo uso solo per settare il pin con long press
        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            //CALCOLO DEL PERCORSO E DISEGNO

            @Override
            public void onLongPress(MotionEvent e) {
                if (imageViewPiano.isReady()) {
                    PointF sCoord = imageViewPiano.viewToSourceCoord(e.getX(), e.getY());
                    if(load) {


                        //BEACON DI PARTENZA
                        //Definisco le 2 coordinate di partenza che prendo da beaconId dell'utente loggato
                        daoUtente.open();
                        user = daoUtente.findUtente();
                        daoUtente.close();
                        String idbeacondipartenza = user.getBeaconid();
                        ArrayList<Integer> xcoordandycoord= daoBeacon.getCoordsByIdBeacon(idbeacondipartenza);
                        int coordxpartenza = xcoordandycoord.get(0);
                        int coordypartenza = xcoordandycoord.get(1);
                        PointF mCoord = imageViewPiano.sourceToViewCoord((float) coordxpartenza, (float) coordypartenza);
                        newCoord = imageViewPiano.viewToSourceCoord(mCoord.x,mCoord.y);
                        load = false;
                    }

                    //BEACON DI DESTINAZIONE
                    Cursor cursor;
                    cursor = daoBeacon.getAllBeaconInPiano(position);
                    Log.v("Cursor Object", DatabaseUtils.dumpCursorToString(cursor));

                    //Calcola il beacon di destinazione + vicino rispetto al click dell'utente
                    while (cursor.moveToNext()){
                        int coordx = cursor.getInt(cursor.getColumnIndex("coordx"));
                        int coordy = cursor.getInt(cursor.getColumnIndex("coordy"));
                        distance = (float)Math.sqrt(((sCoord.x-coordx)*(sCoord.x-coordx))+((sCoord.y-coordy)*(sCoord.y-coordy)));
                        System.out.println("Distanza: " +distance);
                        if (distance < temp){
                            temp=distance;
                            idbeacondestinazione=cursor.getInt(cursor.getColumnIndex("ID_beacon"));
                        }
                    }

                    temp=10000000;


                    //RICHIESTA DEL PERCORSO: PROBLEMA: Il calcolo del percorso in RichiestaPercorso.java viene fatto dopo
                    RichiestaPercorso richiestaPercorso = new RichiestaPercorso(user);
                    richiestaPercorso.ottieniPercorsoNoEmergenza(ctx,String.valueOf(idbeacondestinazione), imageViewPiano, position);
                    drawn = true;
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Long press: Image not ready", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (imageViewPiano.isReady()) {
                    PointF sCoord = imageViewPiano.viewToSourceCoord(e.getX(), e.getY());
                    Toast.makeText(getApplicationContext(), "Double tap: " + ((int)sCoord.x) + ", " + ((int)sCoord.y), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Double tap: Image not ready", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        //setto il listener per l'evento
        imageViewPiano.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);

        /*Button avviaPercorsoButton = (Button) findViewById(R.id.avvia_percorso_button);
        avviaPercorsoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calcolaPercorso();
            }
        });*/
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            final String action = intent.getAction();
            String prova = intent.getStringExtra("device");
            Toast.makeText(getApplicationContext(), prova, Toast.LENGTH_LONG).show();             //vedere di seguito perchè non funziona
            ArrayList<Integer> newPosition = daoBeacon.getCoordsByIdBeacon(prova);
            int coordxpartenza = newPosition.get(0);
            int coordypartenza = newPosition.get(1);
            PointF mCoord = imageViewPiano.sourceToViewCoord((float) coordxpartenza, (float) coordypartenza);
            newCoord = imageViewPiano.viewToSourceCoord(mCoord.x,mCoord.y);
            imageViewPiano.setPin(newCoord);

        }
    };

    /*private void calcolaPercorso() {
        DAOUtente daoUtente = new DAOUtente(this);
        daoUtente.open();
        Utente utente_attivo = daoUtente.findUtente();
        daoUtente.close();
        RichiestaPercorso richiestaPercorso = new RichiestaPercorso(utente_attivo);
        richiestaPercorso.ottieniPercorsoNoEmergenza(this);
    }*/

    @Override
    public void onDestroy(){
        daoBeacon.close();
        daoUtente.close();
        super.onDestroy();

    }

}
