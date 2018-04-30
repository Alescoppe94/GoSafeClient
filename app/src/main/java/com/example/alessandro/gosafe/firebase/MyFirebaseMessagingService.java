package com.example.alessandro.gosafe.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.example.alessandro.gosafe.R;
import com.example.alessandro.gosafe.VaiActivity;
import com.example.alessandro.gosafe.server.AggiornamentoInfoServer;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

/**
 * Created by Alessandro on 08/03/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){

        if(remoteMessage.getNotification().getTitle().equals("Aggiornamento")){

            String body = remoteMessage.getNotification().getBody();
            AggiornamentoInfoServer aggiornamentoInfoServer = new AggiornamentoInfoServer();
            aggiornamentoInfoServer.aggiornamentoDbClient(body);

        }else if(remoteMessage.getNotification().getTitle().equals("Java")) {
            Intent intent = new Intent(this, VaiActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
            notificationBuilder.setContentTitle("FCM NOTIFICATION");
            notificationBuilder.setContentText(remoteMessage.getNotification().getBody());
            notificationBuilder.setAutoCancel(true);
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
            notificationBuilder.setContentIntent(pendingIntent);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, notificationBuilder.build());
        }

    }

}
