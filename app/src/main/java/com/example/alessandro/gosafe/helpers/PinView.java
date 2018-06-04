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
import com.example.alessandro.gosafe.database.DAOBeacon;

import java.util.ArrayList;


public class PinView extends SubsamplingScaleImageView {

    private final Paint paint = new Paint();
    private final PointF vPin = new PointF();
    private final PointF nPin = new PointF();
    private PointF sPin;
    private PointF inizioPin;
    PointF primobeacon = new PointF();
    PointF secondobeacon = new PointF();
    private ArrayList<Integer> percorso;
    private Bitmap pin;
    float vX;
    float vY;




    public PinView(Context context) {
        this(context, null);
    }

    public PinView(Context context, AttributeSet attr) {
        super(context, attr);
        initialise();
    }

    public void setPin(PointF sPin) {
        this.sPin = sPin;
        initialise();
        invalidate();
    }

    public void setPin(PointF sPin, PointF inizioPin) {
        this.sPin = sPin;
        this.inizioPin = inizioPin;
        initialise();
        invalidate();
    }

    public void setPin(ArrayList<Integer> percorso) {
        this.percorso = percorso; //Setta il percorso. percorso = {Coord x Beacon1, Coord y Beacon1, Coord x Beacon2, Coord Y Beacon 2,...}
        System.out.println("Percorso in PinView: " +percorso);
        initialise();
        invalidate();
    }

    private void initialise() {
        float density = getResources().getDisplayMetrics().densityDpi;
        pin = BitmapFactory.decodeResource(this.getResources(), R.drawable.pushpin_blue);
        float w = (density/420f) * pin.getWidth();
        float h = (density/420f) * pin.getHeight();
        pin = Bitmap.createScaledBitmap(pin, (int)w, (int)h, true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Don't draw pin before image is ready so it doesn't move around during setup.
        if (!isReady()) {
            return;
        }

        paint.setAntiAlias(true);

        if (percorso!=null && pin != null) {
            /*A vPin vengono dati i valori x e y di sPin*/

            for(int i=0; i<=percorso.size()-4;i=i+2){ //Prende i beacon a 2 a 2 e disegna la linea

                primobeacon.set(percorso.get(i), percorso.get(i+1));  //Prende il primo beacon
                secondobeacon.set(percorso.get(i+2),percorso.get(i+3)); //Prende il secondo beacon
                sourceToViewCoord(primobeacon,nPin);
                sourceToViewCoord(secondobeacon,vPin);
                vX = vPin.x - (pin.getWidth()/2);
                vY = vPin.y - pin.getHeight();

                paint.setColor(Color.RED);
                paint.setStrokeWidth(10);
                canvas.drawLine(nPin.x, nPin.y, vPin.x, vPin.y, paint); //Disegna la linea

            }
            if(percorso.size()!=0) {
                canvas.drawBitmap(pin, vX, vY, paint);
            }
        }

        if (pin != null && sPin != null) {
            initialise();
            invalidate();
            sourceToViewCoord(sPin,vPin);
            vX = vPin.x - (pin.getWidth()/2);
            vY = vPin.y - pin.getHeight();
            canvas.drawBitmap(pin, vX, vY, paint);
            //invalidate();

        }

    }

    public void play(PointF punto) {
        System.out.println(punto.toString());
        setPin(punto);
        //SubsamplingScaleImageView.AnimationBuilder animationBuilder = pinView.animateScaleAndCenter(scale, punto);

    }

    public void play(PointF punto, PointF inizio) {
        System.out.println(punto.toString());
        setPin(punto, inizio);
        //SubsamplingScaleImageView.AnimationBuilder animationBuilder = pinView.animateScaleAndCenter(scale, punto);

    }

    public void play(ArrayList<Integer> percorso) {
        setPin(percorso);
        //SubsamplingScaleImageView.AnimationBuilder animationBuilder = pinView.animateScaleAndCenter(scale, punto);

    }

}
