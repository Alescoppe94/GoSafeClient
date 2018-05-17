package com.example.alessandro.gosafe;

import android.os.Bundle;
import android.widget.ImageView;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.example.alessandro.gosafe.database.DAOUtente;
import com.example.alessandro.gosafe.entity.Utente;
import com.example.alessandro.gosafe.server.RichiestaPercorso;

public class EmergenzaActivity extends DefaultActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergenza);
        DAOUtente daoUtente = new DAOUtente(this);
        daoUtente.open();
        Utente utente_attivo = daoUtente.findUtente();
        daoUtente.close();
        RichiestaPercorso richiestaPercorso = new RichiestaPercorso(utente_attivo);
        richiestaPercorso.visualizzaPercorso(this);

        //ImageView image = (ImageView) findViewById(R.id.imageViewProva);
        //image.setImageResource(R.drawable.q140);
    }
}
