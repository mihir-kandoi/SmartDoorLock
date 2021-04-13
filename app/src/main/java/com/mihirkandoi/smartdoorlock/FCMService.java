package com.mihirkandoi.smartdoorlock;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FCMService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d("FCM Token", s);
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("token_file", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("FCM Token", s);
        editor.apply();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Intent intentStartStream = new Intent(getApplicationContext(), MyBroadcastReceiver.class);
        Intent intentUnlockDoor = new Intent(getApplicationContext(), MyBroadcastReceiver.class);
        Intent startApp = new Intent(this, MainActivity.class);
        startApp.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intentStartStream.putExtra("action", "start stream");
        intentUnlockDoor.putExtra("action", "unlock door");
        PendingIntent startStream = PendingIntent.getBroadcast(getApplicationContext(), 0, intentStartStream, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent unlockDoor = PendingIntent.getBroadcast(getApplicationContext(), 0, intentUnlockDoor, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent PIstartApp = PendingIntent.getActivity(this, 0, startApp, 0);

        Map<String,String> data = remoteMessage.getData();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "visitors")
                .setSmallIcon(R.drawable.ic_baseline_doorbell_24)
                .setContentTitle(data.get("title"))
                .setContentText(data.get("body"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .addAction(R.drawable.ic_baseline_lock_open_24, "Unlock door", unlockDoor)
                .addAction(R.drawable.ic_baseline_videocam_24, "Start stream", startStream)
                .setContentIntent(PIstartApp)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        // notificationId is a unique int for each notification that you must define
        Notification notification = builder.build();
        notification.defaults |= Notification.DEFAULT_ALL;
        notificationManager.notify(0, notification);
    }
}
