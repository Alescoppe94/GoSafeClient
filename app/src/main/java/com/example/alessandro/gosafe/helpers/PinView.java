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


public class PinView extends SubsamplingScaleImageView {

    private final Paint paint = new Paint();
    private final PointF vPin = new PointF();
    private final PointF nPin = new PointF();
    private PointF sPin;
    private PointF inizioPin;
    private Bitmap pin;

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

        if (sPin != null && pin != null) {
            /*A vPin vengono dati i valori x e y di sPin*/
            sourceToViewCoord(sPin, vPin);
            sourceToViewCoord(inizioPin, nPin);
            float vX = vPin.x - (pin.getWidth()/2);
            float vY = vPin.y - pin.getHeight();
            //canvas.drawBitmap(pin, vX, vY, paint);

            /*Disegna linea*/
            paint.setColor(Color.RED);
            paint.setStrokeWidth(10);
            canvas.drawLine(nPin.x, nPin.y, vPin.x, vPin.y, paint);
            canvas.drawBitmap(pin, vX, vY, paint);
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

}
