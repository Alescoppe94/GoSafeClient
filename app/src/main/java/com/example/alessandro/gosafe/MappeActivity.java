package com.example.alessandro.gosafe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MappeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ScaleGestureDetector SGD;
    private Matrix matrix = new Matrix();
    private ImageView imageView;
    private Float scale =1f;
    Spinner spinner;
    ArrayAdapter<CharSequence> adapter;
    private TextView textView;
    private ImageView imageViewPiano;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mappe);

        spinner=(Spinner)findViewById(R.id.spinner);
        adapter=ArrayAdapter.createFromResource(this,R.array.piani,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        //textView=(TextView) findViewById(R.id.mostraPianoTextView);

        final Bitmap bMap145 = BitmapFactory.decodeResource(getResources(),R.drawable.quota145);
        final Bitmap bMap150 = BitmapFactory.decodeResource(getResources(),R.drawable.quota150);
        final Bitmap bMap155 = BitmapFactory.decodeResource(getResources(),R.drawable.quota155);
        final Bitmap bMapScaled145 = Bitmap.createScaledBitmap(bMap145,100,100, true);
        final Bitmap bMapScaled150 = Bitmap.createScaledBitmap(bMap150,100,100, true);
        final Bitmap bMapScaled155 = Bitmap.createScaledBitmap(bMap155,100,100, true);

        imageViewPiano = (ImageView) findViewById(R.id.imageViewPiano);
        //imageViewPiano.setImageResource(R.drawable.spillo);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int position = spinner.getSelectedItemPosition();
                Toast.makeText(getBaseContext(),adapterView.getItemAtPosition(i)+" selected",Toast.LENGTH_LONG).show();
                switch(position){
                    case 0:
                        imageViewPiano.setImageBitmap(bMapScaled145);
                        break;
                    case 1:
                        imageViewPiano.setImageBitmap(bMapScaled145);
                        break;
                    case 2:
                        imageViewPiano.setImageBitmap(bMapScaled150);
                        break;
                    case 3:
                        imageViewPiano.setImageBitmap(bMapScaled155);
                        break;
                }
                //imageViewPiano.setImageResource(R.drawable.quota145);

                //textView.setText(adapterView.getItemAtPosition(i).toString());

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //imageView = (ImageView) findViewById(R.id.q140);
        SGD = new ScaleGestureDetector(this, new ScaleListener());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);

        Intent i = getIntent();
        switch(i.getStringExtra("selezione")){
            case "vai":
                bottomNavigationView.setSelectedItemId(R.id.menu_vai);
                break;
            case "mappe":
                bottomNavigationView.setSelectedItemId(R.id.menu_mappe);
                break;
            case "profilo":
                bottomNavigationView.setSelectedItemId(R.id.menu_profilo);
                break;
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Intent i;
                        switch (item.getItemId()) {
                            case R.id.menu_vai:
                                i = new Intent(getApplicationContext(), VaiActivity.class);
                                i.putExtra("selezione", "vai");
                                startActivity(i);
                                break;

                            case R.id.menu_mappe:
                                i = new Intent(getApplicationContext(), MappeActivity.class);
                                i.putExtra("selezione", "mappe");
                                startActivity(i);
                                break;

                            case R.id.menu_profilo:
                                i = new Intent(getApplicationContext(), ProfiloActivity.class);
                                i.putExtra("selezione", "profilo");
                                startActivity(i);
                                break;
                        }
                        return true;
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale = scale * detector.getScaleFactor();
            scale = Math.max(0.1f, Math.min(scale, 5f));
            matrix.setScale(scale, scale);
            //imageView.setImageMatrix(matrix);
            return true;
        }
    }

}
