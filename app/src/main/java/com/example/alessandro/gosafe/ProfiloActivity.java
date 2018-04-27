package com.example.alessandro.gosafe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alessandro.gosafe.database.DAOUtente;
import com.example.alessandro.gosafe.entity.Utente;

public class ProfiloActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

    public static final int SELECTED_PICTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilo);

        ImageView mIcon = findViewById(R.id.Profile);
        TextView name = (TextView)findViewById(R.id.Name);
        TextView username = (TextView) findViewById(R.id.usernameTextView);
        final Button logoutButton = (Button) findViewById(R.id.logout);

        //imageView=(ImageView)findViewById(R.id.Profile);

        Button mFollow = findViewById(R.id.modificaButton);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.homer);
        RoundedBitmapDrawable mDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        mDrawable.setCircular(true);
        mIcon.setImageDrawable(mDrawable);

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

        DAOUtente daoUtente = new DAOUtente(this);
        daoUtente.open();
        Utente utente;
        utente = daoUtente.findUtente();
        daoUtente.close();

        name.setText(utente.getNome()+" "+utente.getCognome()/*.toString()*/);
        username.setText(utente.getUsername());

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

    public void goToModificaProfilo(View view){
        Intent i;
        i = new Intent(getApplicationContext(), ModificaActivity.class);
        startActivity(i);
    }

    public void indietro(View view){
        setContentView(R.layout.content_profilo);
    }

    public void chooseImmagineProfilo(View view){

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivity(intent);
    }

    public void logout (View view){
        DAOUtente daoutente = new DAOUtente(this);
        daoutente.open();
        daoutente.deleteAll();
        daoutente.close();

        Intent i;
        i = new Intent(getApplicationContext(), LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        //finish();

    }

}
