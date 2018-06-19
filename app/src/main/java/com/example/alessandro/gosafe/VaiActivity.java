package com.example.alessandro.gosafe;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.example.alessandro.gosafe.beacon.BluetoothLeService;
import com.example.alessandro.gosafe.database.DAOBeacon;
import com.example.alessandro.gosafe.database.DAOPiano;
import com.example.alessandro.gosafe.database.DAOUtente;
import com.example.alessandro.gosafe.entity.Beacon;
import com.example.alessandro.gosafe.entity.Piano;
import com.example.alessandro.gosafe.entity.Utente;
import com.example.alessandro.gosafe.helpers.ImageLoader;
import com.example.alessandro.gosafe.helpers.PinView;
import com.example.alessandro.gosafe.server.CheckForDbUpdatesService;
import com.example.alessandro.gosafe.server.DbDownloadFirstBoot;
import com.example.alessandro.gosafe.server.RichiestaPercorso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;

public class VaiActivity extends DefaultActivity {

    //private PointF newCoord;
    private boolean load = true;
    private boolean drawn = false;
    float distance;
    float temp=10000000;
    String idbeacondestinazione;

    /*roba per menu a tendina*/
    Spinner spinner;
    private PinView pinView;
    int x;
    int y;
    Beacon beaconD;
    int position;

    Context ctx;
    ArrayAdapter<String> adapter;
    private PinView imageViewPiano;
    private TextView textLabel;
    Utente user;
    private RichiestaPercorso richiestaPercorso;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vai);

        ctx = this;
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mGattUpdateReceiver, new IntentFilter("updatepositionmap"));


        DAOUtente daoUtente = new DAOUtente(this);
        daoUtente.open();
        user = daoUtente.findUtente();
        daoUtente.close();
        richiestaPercorso = new RichiestaPercorso(user);

        DAOPiano daoPiano = new DAOPiano(this);
        daoPiano.open();
        ArrayList<String> piani = daoPiano.getAllPiani();
        daoPiano.close();

        //Spinner
        spinner= (Spinner) findViewById(R.id.spinner);
        adapter = new ArrayAdapter<String> (this,android.R.layout.simple_spinner_dropdown_item,piani);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        textLabel = (TextView) findViewById(R.id.vaiLabel);
        imageViewPiano = (PinView) findViewById(R.id.imageViewPiano);

        DAOBeacon daoBeacon = new DAOBeacon(this);
        daoBeacon.open();
        Beacon beaconMyPosition = daoBeacon.getBeaconById(user.getBeaconid());
        PointF pinMyPosition = new PointF(beaconMyPosition.getCoordx(), beaconMyPosition.getCoordy());
        imageViewPiano.setPinMyPosition(pinMyPosition);
        daoBeacon.close();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(getBaseContext(), adapterView.getItemAtPosition(i) + " selected", Toast.LENGTH_LONG).show();
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
                    //cancella vecchio percorso
                    //chiama richiestapercorso
                    richiestaPercorso.cambiaPiano(imageViewPiano, position);
                    imageViewPiano.setPianoSpinner(position);
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

                DAOBeacon daoBeacon = new DAOBeacon(ctx);
                daoBeacon.open();
                if (imageViewPiano.isReady()) {
                    PointF sCoord = imageViewPiano.viewToSourceCoord(e.getX(), e.getY());
                    if(load) {


                        //BEACON DI PARTENZA
                        //Definisco le 2 coordinate di partenza che prendo da beaconId dell'utente loggato
                        String idbeacondipartenza = user.getBeaconid();
                        if(idbeacondipartenza!= null) {
                            ArrayList<Integer> xcoordandycoord = daoBeacon.getCoordsByIdBeacon(idbeacondipartenza);
                            int coordxpartenza = xcoordandycoord.get(0);
                            int coordypartenza = xcoordandycoord.get(1);
                            PointF mCoord = imageViewPiano.sourceToViewCoord((float) coordxpartenza, (float) coordypartenza);
                            PointF newCoord = imageViewPiano.viewToSourceCoord(mCoord.x, mCoord.y);
                        }
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
                            idbeacondestinazione=cursor.getString(cursor.getColumnIndex("ID_beacon"));  //da sostituire int con string
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

    }


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            final String action = intent.getAction();
            String idBeacon = intent.getStringExtra("device");
            Toast.makeText(getApplicationContext(), idBeacon, Toast.LENGTH_LONG).show();
            DAOBeacon daoBeacon = new DAOBeacon(ctx);
            daoBeacon.open();
            //Beacon beacon = daoBeacon.getBeaconById(idBeacon);  devo lavorarci
            ArrayList<Integer> newPosition = daoBeacon.getCoordsByIdBeacon(idBeacon);
            daoBeacon.close();
            int coordxpartenza = newPosition.get(0);
            int coordypartenza = newPosition.get(1);
            PointF mCoord = imageViewPiano.sourceToViewCoord((float) coordxpartenza, (float) coordypartenza);
            PointF newCoord = imageViewPiano.viewToSourceCoord(mCoord.x, mCoord.y);
            imageViewPiano.setPinMyPosition(newCoord);

        }
    };

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
        String piano = spinner.getSelectedItem().toString();
        String[] elems = piano.split(" ");
        bitmap = ImageLoader.loadImageFromStorage(elems[1], ctx);
        imageViewPiano.setImage(ImageSource.bitmap(bitmap));
        load = true;
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

    public void avviaPercorso(View view){
        //RICHIESTA DEL PERCORSO: PROBLEMA: Il calcolo del percorso in RichiestaPercorso.java viene fatto dopox
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

}
