package com.example.alessandro.gosafe.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.example.alessandro.gosafe.EmergenzaActivity;
import com.example.alessandro.gosafe.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Classe servizio che gestisce l'arrivo della notifica inviata da firebase
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    /**
     * Metodo eseguito ogni volta che arriva un messaggio da firebase
     * @param remoteMessage rappresenta la notifica firebase
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //imposta l'emergenza a true all'interno dell'applicazione
        SharedPreferences.Editor editor = getSharedPreferences("isEmergenza", MODE_PRIVATE).edit();
        editor.putBoolean("emergenza", true);
        editor.apply();

        Map<String, String> data = remoteMessage.getData();

        //gestore delle notifiche
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //gestisce le notifiche da oreo in poi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("default", "My Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configura il canale di notifica
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        //impostazioni generali per tutte le versioni di android
        Intent intent = new Intent(this, EmergenzaActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "default");
        notificationBuilder.setContentTitle(data.get("title"));
        notificationBuilder.setContentText(data.get("body"));
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        notificationManager.notify(0, notificationBuilder.build());


    }

}
