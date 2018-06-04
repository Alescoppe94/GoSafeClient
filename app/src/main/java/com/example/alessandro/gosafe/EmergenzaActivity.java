package com.example.alessandro.gosafe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.example.alessandro.gosafe.database.DAOBeacon;
import com.example.alessandro.gosafe.database.DAOUtente;
import com.example.alessandro.gosafe.entity.Utente;
import com.example.alessandro.gosafe.helpers.PinView;
import com.example.alessandro.gosafe.server.RichiestaPercorso;

import java.util.ArrayList;

public class EmergenzaActivity extends DefaultActivity {

    private PinView imageViewPiano;
    Utente utente_attivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergenza);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mGattUpdateReceiver, new IntentFilter("updatepositionmap"));
        SharedPreferences.Editor editor = getSharedPreferences("isEmergenza", MODE_PRIVATE).edit();
        editor.putBoolean("emergenza", true);
        editor.apply();
        imageViewPiano=(PinView) findViewById(R.id.imageViewPiano);
        imageViewPiano.setImage(ImageSource.resource(R.drawable.q140));
        DAOUtente daoUtente = new DAOUtente(this);
        daoUtente.open();
        utente_attivo = daoUtente.findUtente();
        daoUtente.close();
        RichiestaPercorso richiestaPercorso = new RichiestaPercorso(utente_attivo);
        richiestaPercorso.visualizzaPercorso(this, imageViewPiano);

        //ImageView image = (ImageView) findViewById(R.id.imageViewProva);
        //image.setImageResource(R.drawable.q140);
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            final String action = intent.getAction();
            String idBeacon = intent.getStringExtra("device");
            DAOUtente daoUtente = new DAOUtente(context);
            daoUtente.open();
            utente_attivo = daoUtente.findUtente();
            daoUtente.close();
            RichiestaPercorso richiestaPercorso = new RichiestaPercorso(utente_attivo);
            richiestaPercorso.visualizzaPercorso(context, imageViewPiano);
            DAOBeacon daoBeacon = new DAOBeacon(context);
            daoBeacon.open();
            ArrayList<Integer> newPosition = daoBeacon.getCoordsByIdBeacon(idBeacon);
            daoBeacon.close();
            int coordxpartenza = newPosition.get(0);
            int coordypartenza = newPosition.get(1);
            PointF mCoord = imageViewPiano.sourceToViewCoord((float) coordxpartenza, (float) coordypartenza);
            PointF newCoord = imageViewPiano.viewToSourceCoord(mCoord.x, mCoord.y);
            imageViewPiano.setPin(newCoord);

        }
    };

}
