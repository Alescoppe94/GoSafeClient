package com.example.alessandro.gosafe.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.example.alessandro.gosafe.R;

import java.util.ArrayList;

/**
 * classe che estende view e consente di creare una imageview con funzionalità di disegno e di zoom
 */
public class PinView extends SubsamplingScaleImageView {

    private final Paint paint = new Paint();
    private final PointF vPin = new PointF();
    private final PointF nPin = new PointF();
    private PointF sPin;
    private PointF inizioPin;
    private PointF primobeacon = new PointF();
    private PointF secondobeacon = new PointF();
    private ArrayList<Integer> percorso;
    private Bitmap pin;
    private float vX;
    private float vY;
    private boolean bool = true;
    private boolean calcoloInCorso= true;
    private int pianoArrivo;
    private int pianoSpinner;
    private boolean isPianoUtente=false;

    private boolean percorsoConPiuPiani = false;

    /**
     * Metodo setter che imposta se il piano su cui si trova l'utente è lo stesso di quello su cui sitrova la destinazione
     * @param pianoUtente ritorna un true se posizione e destinazione sono sullo stesso piano
     */
    public void setPianoUtente(boolean pianoUtente) {
        isPianoUtente = pianoUtente;
    }

    /**
     * Metodo setter che imposta se si sta calcolando un percorso o meno
     * @param calcoloInCorso riceve un booleano settato a True se si sta calcolando un percorso
     */
    public void setCalcoloInCorso(boolean calcoloInCorso){
        this.calcoloInCorso = calcoloInCorso;
    }

    /**
     * Metodo setter che imposta il numero del piano selezionato sullo spinner
     * @param pianoSpinner riceve il numero del piano selezionato
     */
    public void setPianoSpinner(int pianoSpinner){
        this.pianoSpinner = pianoSpinner;
    }

    /**
     * Metodo setter che imposta un booleano che si assicura che venga disegnato prima il pin di arrivo rispetto al percorso
     * @param bool True se si vuole disegnare il pin di arrivo
     */
    public void setBool(boolean bool){
        this.bool = bool;
    }

    /**
     * Metodo setter che imposta il Piano di arrivo
     * @param pianoArrivo imposta il piano di arrivo
     */
    public void setPianoArrivo(int pianoArrivo){
        this.pianoArrivo = pianoArrivo;
    }

    /**
     * Metodo setter che imposta se un percorso contiene più piani o meno
     * @param percorsoConPiuPiani riceve un booleano settato a True se un percorso è su più piani
     */
    public void setPercorsoConPiuPiani(boolean percorsoConPiuPiani) {
        this.percorsoConPiuPiani = percorsoConPiuPiani;
    }

    /**
     * Costruttore
     * @param context riceve il Context
     */
    public PinView(Context context) {
        this(context, null);
    }

    /**
     * Costruttore
     * @param context riceve il context dell'applicazione
     * @param attr
     */
    public PinView(Context context, AttributeSet attr) {
        super(context, attr);
        initialise(); //inizializza i pin da disegnare sulla mappa
    }

    /**
     * Metodo che imposta il pin a partire da un punto
     * @param sPin contiene un PointF contenente le coordinate su cui disegnare il pin
     */
    public void setPin(PointF sPin) {
        this.sPin = sPin;
        initialise();
        invalidate(); // si assicura che il metodo ondraw() venga chiamato
    }

    /**
     * Metodo che imposta il percorso da disegnare
     * @param percorso riceve in input il percorso da disegnare
     */
    public void setPin(ArrayList<Integer> percorso) {
        this.percorso = percorso; //Setta il percorso. percorso = {Coord x Beacon1, Coord y Beacon1, Coord x Beacon2, Coord Y Beacon 2,...}
        initialise();
        invalidate();
    }

    /**
     * Metodo che imposta l'icona con la posizione dell'utente
     * @param sPin prende un PointF con le coordinate della posizione dell'utente
     */
    public void setPinMyPosition(PointF sPin) {
        this.inizioPin = sPin;
        initialiseMyPosition(); //inizializza l'icona che rappresenta la posizione dell'utente
        invalidate(); //si assicura la chiamata a onDraw()
    }

    /**
     * Inizializza il pin da rappresentare sulla mappa come destinazione
     */
    private void initialise() {
        float density = getResources().getDisplayMetrics().densityDpi;
        pin = BitmapFactory.decodeResource(this.getResources(), R.drawable.round_place_black_24);
        float w = (density/420f) * pin.getWidth();
        float h = (density/420f) * pin.getHeight();
        pin = Bitmap.createScaledBitmap(pin, (int)w, (int)h, true);
    }

    /**
     * Inizializza l'icona che rappresenta la posizione dell'utente sulla mappa
     */
    private void initialiseMyPosition() {
        float density = getResources().getDisplayMetrics().densityDpi;
        pin = BitmapFactory.decodeResource(this.getResources(), R.drawable.round_my_location_black_24);
        float w = (density/420f) * pin.getWidth()/1.6f;
        float h = (density/420f) * pin.getHeight()/1.6f;
        pin = Bitmap.createScaledBitmap(pin, (int)w, (int)h, true);
    }

    /**
     * Metodo che viene chiamato ogni volta che c'è una modifica all'interfaccia grafica. viene invocato in maniera automatica.
     * Si può forzare la chiamata con invalidate
     * @param canvas oggetto che rappresenta la view su cui disegnare
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT);

        // non disegna il pin fino a quando l'immagine non è caricata correttamente
        if (!isReady()) {
            return;
        }

        paint.setAntiAlias(true);

        //disegna la linea del percorso
        if (percorso!=null && pin != null) {

            for(int i=0; i<=percorso.size()-4;i=i+2){ //Prende i beacon a 2 a 2 e disegna la linea

                primobeacon.set(percorso.get(i), percorso.get(i+1));  //Prende il primo beacon
                secondobeacon.set(percorso.get(i+2),percorso.get(i+3)); //Prende il secondo beacon
                sourceToViewCoord(primobeacon,nPin);
                sourceToViewCoord(secondobeacon,vPin);
                vX = vPin.x - (pin.getWidth()/2);
                vY = vPin.y - pin.getHeight();

                paint.setColor(Color.RED);
                paint.setStrokeWidth(10);
                canvas.drawLine(nPin.x, nPin.y, vPin.x, vPin.y, paint); //Disegna la linea che rappresenta il percorso

            }
            //serve a impedire il disegno del pin d'arrivo su piani diversi in presenza del percorso e a impedire
            //che si sposti il pin in caso in cui si rilanci calcoloPercorso
         if(percorso.size()!=0 && pianoArrivo == pianoSpinner && calcoloInCorso && !percorsoConPiuPiani) {
                initialise();
                invalidate();
                sourceToViewCoord(new PointF(percorso.get(percorso.size()-2), percorso.get(percorso.size()-1)),vPin);
                vX = vPin.x - (pin.getWidth()/2);
                vY = vPin.y - pin.getHeight();
                canvas.drawBitmap(pin, vX, vY, paint);
            }
        }

        //Disegna il pin di arrivo prima di avviare il percorso
       if (bool && pin != null && sPin != null) {
            initialise();
            invalidate();
            sourceToViewCoord(sPin,vPin);
            vX = vPin.x - (pin.getWidth()/2);
            vY = vPin.y - pin.getHeight();
            canvas.drawBitmap(pin, vX, vY, paint);
        }
        else{
            this.sPin = null;
            bool = true;
        }

        //disegna la posizione dell'utente sulla mappa
        if(inizioPin!=null && isPianoUtente){
            initialiseMyPosition();
            invalidate();
            sourceToViewCoord(inizioPin,vPin);
            vX = vPin.x - (pin.getWidth()/2);
            vY = vPin.y - pin.getHeight()/2;
            canvas.drawBitmap(pin, vX, vY, paint);
        }

    }

    /**
     * Si occupa di invocare il metodo per disegnare il percorso
     * @param percorso percorso da disegnare
     */
    public void play(ArrayList<Integer> percorso) {
        setPin(percorso);

    }
}
