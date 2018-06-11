package com.example.alessandro.gosafe;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.example.alessandro.gosafe.database.DAOBeacon;
import com.example.alessandro.gosafe.database.DAOPiano;
import com.example.alessandro.gosafe.database.DAOUtente;
import com.example.alessandro.gosafe.entity.Utente;
import com.example.alessandro.gosafe.helpers.ImageLoader;
import com.example.alessandro.gosafe.helpers.PinView;
import com.example.alessandro.gosafe.server.RichiestaPercorso;

import java.util.ArrayList;

public class EmergenzaActivity extends DefaultActivity {

    private PinView imageViewPiano;
    private Utente utente_attivo;

    private Bitmap bitmap;

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
        if(utente_attivo.getBeaconid() != null) {
            DAOPiano daoPiano = new DAOPiano(this);
            daoPiano.open();
            DAOBeacon daoBeacon = new DAOBeacon(this);
            daoBeacon.open();
            bitmap = ImageLoader.loadImageFromStorage(String.valueOf(daoPiano.getNumeroPianoById(daoBeacon.getBeaconById(utente_attivo.getBeaconid()).getPiano())), this);
            imageViewPiano=(PinView) findViewById(R.id.imageViewPiano);
            imageViewPiano.setImage(ImageSource.bitmap(bitmap));
            RichiestaPercorso richiestaPercorso = new RichiestaPercorso(utente_attivo);
            richiestaPercorso.visualizzaPercorso(this, imageViewPiano);
            daoPiano.close();
            daoBeacon.close();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Attenzione!");
            builder.setMessage("Non sei connesso a nessun beacon.\nSei sicuro di trovarti nell'edificio?");

            builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    SharedPreferences.Editor editor = getSharedPreferences("isEmergenza", MODE_PRIVATE).edit();
                    editor.putBoolean("emergenza", false);
                    editor.apply();
                    startActivity(new Intent(getApplicationContext(), VaiActivity.class));
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }

        //ImageView image = (ImageView) findViewById(R.id.imageViewProva);
        //image.setImageResource(R.drawable.q140);
    }

    public boolean salvo(MenuItem item){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Sei salvo?");
        builder.setMessage("Una volta cliccato si non riceverai più informazioni sulla via di fuga. Continuare?");

        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                SharedPreferences.Editor editor = getSharedPreferences("isEmergenza", MODE_PRIVATE).edit();
                editor.putBoolean("emergenza", false);
                editor.apply();
                startActivity(new Intent(getApplicationContext(), VaiActivity.class));
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

        return true;
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
        imageViewPiano.invalidate();
        super.onResume();
    }


    @Override
    public void onDestroy(){
        bitmap.recycle();
        bitmap = null;
        finish();
        super.onDestroy();

    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            final String action = intent.getAction();
            String idBeacon = intent.getStringExtra("device");
            SharedPreferences sharedPreferences = context.getSharedPreferences("isConnesso", context.MODE_PRIVATE);
            boolean connesso = sharedPreferences.getBoolean("connesso",false);
            if(connesso) {
                DAOUtente daoUtente = new DAOUtente(context);
                daoUtente.open();
                utente_attivo = daoUtente.findUtente();
                daoUtente.close();
                RichiestaPercorso richiestaPercorso = new RichiestaPercorso(utente_attivo);
                richiestaPercorso.visualizzaPercorso(context, imageViewPiano);
            }
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
