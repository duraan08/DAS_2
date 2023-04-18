package com.example.juego_das;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ServicioFirebase extends FirebaseMessagingService {
    public ServicioFirebase() {
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    public void onMessageReceived (RemoteMessage remoteMessage){
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().size() > 0){
            Log.d("Prueba_Mensaje", "El mensaje en el if es --> " + remoteMessage.getData());
        }
        if (remoteMessage.getNotification() != null){
            Log.d("Prueba_Mensaje", "El mensaje es --> " + remoteMessage.getNotification().getBody());

            NotificationManager elManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(ServicioFirebase.this, "id_canal");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationChannel elCanal = new NotificationChannel("id_canal", "Mensajeria_FCM", NotificationManager.IMPORTANCE_DEFAULT);
                elManager.createNotificationChannel(elCanal);
            }

            elBuilder.setSmallIcon(android.R.drawable.alert_light_frame)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setVibrate(new long[] {0, 1000, 500, 1000})
                    .setAutoCancel(false);
            elManager.notify(1, elBuilder.build());
        }
    }
}