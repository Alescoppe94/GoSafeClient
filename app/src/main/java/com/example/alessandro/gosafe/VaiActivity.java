package com.example.alessandro.gosafe;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import java.util.Set;

public class VaiActivity extends DefaultActivity {

    private ScaleGestureDetector SGD;
    private Matrix matrix = new Matrix();
    private ImageView imageView;
    private Float scale =1f;
    private PointF newCoord;
    private boolean load = true;
    int position;
    DAOBeacon beacon;
    float distance;
    float temp;
    int idbeacon;

    /*roba per menu a tendina*/
    Spinner spinner;
    private PinView pinView;
    int x;
    int y;

    ArrayAdapter<CharSequence> adapter;
    private PinView imageViewPiano;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mappe);

        DbDownloadFirstBoot dbDownload = new DbDownloadFirstBoot();
        dbDownload.dbdownloadFirstBootAsyncTask(this);
        beacon = new DAOBeacon(this);
        beacon.open();

        /*BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            DAOUtente daoUtente = new DAOUtente(this);
            daoUtente.open();
            Utente user = daoUtente.findUtente();
            daoUtente.close();
            Intent s = new Intent(this, BluetoothLeService.class);            //rimanda l'utente al servizio, può essere modificato
            Bundle bundle = new Bundle();
            bundle.putSerializable("user", user);
            bundle.putLong("periodo", 20000);
            s.putExtras(bundle);
            startService(s);
        }*/

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
                        break;
                    case 1:
                        imageViewPiano.setImage(ImageSource.resource(R.drawable.q145));
                        load = true;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //serve per definire le gesture da rilevare, per ora lo uso solo per settare il pin con long press
        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (imageViewPiano.isReady()) {
                    PointF sCoord = imageViewPiano.viewToSourceCoord(e.getX(), e.getY());
                    if (load){
                        /*Permette di capire quali sono i corrispettivi su schermo dei veri punti della mappa*/

                       /* Set<Beacon> punti = beacon.getAllBeacon(position);
                        System.out.println("Crs "+crs.toString());
                        for(int i=0; i < punti.size(); i++)
                        {
                            System.out.println("Piano "+);
                        }*/
                        //crs.close();

                  //      Cursor crs = beacon.getAllBeacon(position);
                    //    Log.v("Cursor Object ", DatabaseUtils.dumpCursorToString(crs));

                        PointF mCoord = imageViewPiano.sourceToViewCoord((float) 346 , (float) 1072);
                        newCoord = imageViewPiano.viewToSourceCoord(mCoord.x,mCoord.y);
                        load = false;
                    }
                    imageViewPiano.play(sCoord, newCoord);
                    Toast.makeText(getApplicationContext(), "Single tap: " + ((int)sCoord.x) + ", " + ((int)sCoord.y), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Single tap: Image not ready", Toast.LENGTH_SHORT).show();
                }
                return true;

                   /* if (imageViewPiano.isReady()) {
                        PointF sCoord = imageViewPiano.viewToSourceCoord(e.getX(), e.getY());
                        if(load) {
                        /*Permette di capire quali sono i corrispettivi su schermo dei veri punti della mappa*/

                       /*     PointF mCoord = imageViewPiano.sourceToViewCoord((float) 346 , (float) 1072);
                            newCoord = imageViewPiano.viewToSourceCoord(mCoord.x,mCoord.y);
                            load = false;
                        }
                        Cursor cursor;
                        cursor = beacon.getAllBeacon(position);
                        Log.v("Cursor Object", DatabaseUtils.dumpCursorToString(cursor));
                        while (cursor.moveToNext()){
                            int coordx = cursor.getInt(cursor.getColumnIndex("coordx"));
                            int coordy = cursor.getInt(cursor.getColumnIndex("coordy"));
                            distance = (float)Math.sqrt(((sCoord.x-coordx)*(sCoord.x-coordx))+((sCoord.y-coordy)*(sCoord.y-coordy)));
                            if (distance < temp){
                                temp=distance;
                                idbeacon=cursor.getInt(cursor.getColumnIndex("ID_beacon"));
                            }
                        }
                        System.out.println("Id del beacon + vicino: "+idbeacon);
                        temp=10000000;
                        imageViewPiano.play(sCoord, newCoord);
                        Toast.makeText(getApplicationContext(), "Long press: " + ((int)sCoord.x) + ", " + ((int)sCoord.y), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Long press: Image not ready", Toast.LENGTH_SHORT).show();
                    }
                return true;*/
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

    private void calcolaPercorso() {
        DAOUtente daoUtente = new DAOUtente(this);
        daoUtente.open();
        Utente utente_attivo = daoUtente.findUtente();
        daoUtente.close();
        RichiestaPercorso richiestaPercorso = new RichiestaPercorso(utente_attivo);
        richiestaPercorso.ottieniPercorsoNoEmergenza(this);
    }

    @Override
    public void onDestroy(){
        beacon.close();
        super.onDestroy();
    }
}
