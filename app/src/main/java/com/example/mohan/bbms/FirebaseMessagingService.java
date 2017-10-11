package com.example.mohan.bbms;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

import static android.support.v4.app.NotificationCompat.DEFAULT_VIBRATE;
import static android.support.v4.app.NotificationCompat.FLAG_SHOW_LIGHTS;
import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;
import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        showNotification(remoteMessage.getData().get("message"));
    }

    private void showNotification(String message) {

        Intent intent;

        SharedPreferences sharedPreferences = getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("isLoggedIn", false)) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, availableRequests.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle("Someone needs your help!")
                .setContentText(message)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setSmallIcon(R.drawable.splash)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.splash))
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSound(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.a_3))
                .setDefaults(DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_MAX);

        Notification n = builder.build();
        n.flags |= Notification.FLAG_NO_CLEAR | FLAG_SHOW_LIGHTS | PRIORITY_MAX;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, n);
    }
}