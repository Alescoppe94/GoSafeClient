package com.example.alessandro.gosafe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.example.alessandro.gosafe.helpers.PinView;

public class MappeActivity extends DefaultActivity{

    private ScaleGestureDetector SGD;
    private Matrix matrix = new Matrix();
    private ImageView imageView;
    private Float scale =1f;

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

        spinner=(Spinner)findViewById(R.id.spinner);
        adapter=ArrayAdapter.createFromResource(this,R.array.piani,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        imageViewPiano = (PinView) findViewById(R.id.imageViewPiano);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int position = spinner.getSelectedItemPosition();
                Toast.makeText(getBaseContext(), adapterView.getItemAtPosition(i) + " selected", Toast.LENGTH_LONG).show();
                switch (position) {
                    case 0:
                        imageViewPiano.setImage(ImageSource.resource(R.drawable.q140));
                        break;
                    case 1:
                        imageViewPiano.setImage(ImageSource.resource(R.drawable.quota145));
                        break;
                    case 2:
                        imageViewPiano.setImage(ImageSource.resource(R.drawable.quota150));
                        break;
                    case 3:
                        imageViewPiano.setImage(ImageSource.resource(R.drawable.quota155));
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
                    Toast.makeText(getApplicationContext(), "Single tap: " + ((int)sCoord.x) + ", " + ((int)sCoord.y), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Single tap: Image not ready", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            @Override
            public void onLongPress(MotionEvent e) {
                if (imageViewPiano.isReady()) {
                    PointF sCoord = imageViewPiano.viewToSourceCoord(e.getX(), e.getY());
                    imageViewPiano.play(sCoord);
                    Toast.makeText(getApplicationContext(), "Long press: " + ((int)sCoord.x) + ", " + ((int)sCoord.y), Toast.LENGTH_SHORT).show();
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

        /*Roba per cambiare pagina al click sulla bottomNavigationView*/
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        /*Roba per settare icona nel bottomNavigationView*/
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);
    }

}
