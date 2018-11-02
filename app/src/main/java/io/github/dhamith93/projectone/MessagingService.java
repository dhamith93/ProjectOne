package io.github.dhamith93.projectone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.core.app.NotificationCompat;

public class MessagingService extends FirebaseMessagingService {
    public MessagingService() { }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d("TOKEN_KEY", s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("TOKEN_KEY", "From: " + remoteMessage.getFrom());
        showNotification(remoteMessage.getNotification().getTitle() ,remoteMessage.getNotification().getBody());
    }

    private void showNotification(String title, String msg) {
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[]{new Intent(this, HomeActivity.class)}, 0);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_google)
                .setContentTitle(title)
                .setContentText(msg)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
}
