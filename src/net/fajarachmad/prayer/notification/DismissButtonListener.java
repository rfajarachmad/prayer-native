package net.fajarachmad.prayer.notification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import net.fajarachmad.prayer.activity.AppConstant;
import net.fajarachmad.prayer.service.PrayerTimeService;

public class DismissButtonListener extends BroadcastReceiver implements AppConstant{

	@Override
	public void onReceive(Context context, Intent intent) {
		
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
		
        Intent serviceIntent = new Intent(context, PrayerTimeService.class);
        serviceIntent.putExtra(ACTION, ACTION_STOP_SOUND);
        context.startService(serviceIntent);
		
	}

}
