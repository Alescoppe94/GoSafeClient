package com.example.alessandro.gosafe;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * Created by Luca on 12/04/18.
 */

public abstract class DefaultActivity extends AppCompatActivity {


    protected BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

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
    };

}