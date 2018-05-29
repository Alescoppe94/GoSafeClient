package com.example.alessandro.gosafe;

import android.media.Image;
import android.os.Bundle;
import android.widget.ImageView;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.example.alessandro.gosafe.database.DAOUtente;
import com.example.alessandro.gosafe.entity.Utente;
import com.example.alessandro.gosafe.helpers.PinView;
import com.example.alessandro.gosafe.server.RichiestaPercorso;

public class EmergenzaActivity extends DefaultActivity {

    private PinView imageViewPiano;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergenza);
        imageViewPiano=(PinView) findViewById(R.id.imageViewPiano);
        imageViewPiano.setImage(ImageSource.resource(R.drawable.q140));
        DAOUtente daoUtente = new DAOUtente(this);
        daoUtente.open();
        Utente utente_attivo = daoUtente.findUtente();
        daoUtente.close();
        RichiestaPercorso richiestaPercorso = new RichiestaPercorso(utente_attivo);
        richiestaPercorso.visualizzaPercorso(this, imageViewPiano);

        //ImageView image = (ImageView) findViewById(R.id.imageViewProva);
        //image.setImageResource(R.drawable.q140);
    }
}
