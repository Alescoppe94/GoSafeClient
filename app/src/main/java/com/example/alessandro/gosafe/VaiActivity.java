package com.example.alessandro.gosafe;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.alessandro.gosafe.beacon.BluetoothLeService;
import com.example.alessandro.gosafe.database.DAOUtente;
import com.example.alessandro.gosafe.entity.Utente;
import com.example.alessandro.gosafe.server.CheckForDbUpdatesService;

public class VaiActivity extends DefaultActivity {

    Spinner spinnerVai;
    ArrayAdapter<CharSequence> adapter;
    ImageView imageViewVai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vai);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
        }

        Intent u = new Intent(this, CheckForDbUpdatesService.class);
        startService(u);

        //Spinner
        spinnerVai= (Spinner) findViewById(R.id.spinnerVai);
        adapter=ArrayAdapter.createFromResource(this,R.array.piani,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVai.setAdapter(adapter);
        imageViewVai = (ImageView) findViewById(R.id.imageViewPianoVai);
        spinnerVai.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int position = spinnerVai.getSelectedItemPosition();
                Toast.makeText(getBaseContext(),adapterView.getItemAtPosition(i)+" selected",Toast.LENGTH_LONG).show();
                switch(position){
                    case 0:
                        imageViewVai.setImageResource(R.drawable.q140);
                        break;
                    case 1:
                        imageViewVai.setImageResource(R.drawable.quota145);
                        break;
                    case 2:
                        imageViewVai.setImageResource(R.drawable.quota150);
                        break;
                    case 3:
                        imageViewVai.setImageResource(R.drawable.quota155);
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);

    }

}
