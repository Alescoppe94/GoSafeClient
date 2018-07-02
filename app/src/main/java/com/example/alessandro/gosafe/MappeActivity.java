package com.example.alessandro.gosafe;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.design.widget.BottomNavigationView;
import android.os.Bundle;
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

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.example.alessandro.gosafe.database.DAOPiano;
import com.example.alessandro.gosafe.helpers.ImageLoader;
import com.example.alessandro.gosafe.helpers.PinView;

import java.util.ArrayList;

/**
 * Classe che implementa la possibilità di visualizzare le mappe.
 */
public class MappeActivity extends DefaultActivity{

    Spinner spinner;

    ArrayAdapter<String> adapter;
    private PinView imageViewPiano;

    private Bitmap bitmap;

    /**
     * Inizializza la classe MappeActivity e setta i metodi per la gesture detection.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mappe);

        DAOPiano daoPiano = new DAOPiano(this);
        daoPiano.open();

        ArrayList<String> piani = daoPiano.getAllPiani();
        daoPiano.close();

        spinner= (Spinner) findViewById(R.id.spinner);
        adapter = new ArrayAdapter<String> (this,android.R.layout.simple_spinner_dropdown_item,piani);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        imageViewPiano = (PinView) findViewById(R.id.imageViewPiano);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String piano =(String) adapterView.getItemAtPosition(i);
                String[] elems = piano.split(" ");
                bitmap = ImageLoader.loadImageFromStorage(elems[1], getApplicationContext());
                while(bitmap == null){
                    imageViewPiano.invalidate();
                    bitmap = ImageLoader.loadImageFromStorage(elems[1], getApplicationContext());
                }
                imageViewPiano.setImage(ImageSource.bitmap(bitmap));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //serve per definire le gesture da rilevare, per ora lo uso solo per settare il pin con long press
        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener());

        //setto il listener per l'evento
        imageViewPiano.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });

        /*Roba per cambiare pagina al click sulla bottomNavigationView*/
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        /*Roba per settare icona nel bottomNavigationView*/
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);
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
        String piano = spinner.getSelectedItem().toString();
        String[] elems = piano.split(" ");
        bitmap = ImageLoader.loadImageFromStorage(elems[1], this);
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

}
