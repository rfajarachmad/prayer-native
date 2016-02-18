package net.fajarachmad.prayer.notification;

import net.fajarachmad.prayer.activity.AppConstant;
import net.fajarachmad.prayer.service.PrayerTimeService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationPublisher extends BroadcastReceiver implements AppConstant {

    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";

    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        String sound = intent.getStringExtra(NOTIFICATION_SOUND);
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notificationManager.notify(id, notification);
        
        //Start sound
        Intent serviceIntent = new Intent(context, PrayerTimeService.class);
        serviceIntent.putExtra(ACTION, ACTION_PLAY_SOUND);
        serviceIntent.putExtra(NOTIFICATION_SOUND, sound);
        context.startService(serviceIntent);
        
    }
}
