package net.fajarachmad.prayer.receiver;

import com.google.gson.Gson;

import net.fajarachmad.prayer.activity.AppConstant;
import net.fajarachmad.prayer.activity.PrayerTimeActivity;
import net.fajarachmad.prayer.model.Prayer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PrayerTimeReceiver extends BroadcastReceiver implements AppConstant{
	
	public static final String PROCESS_RESPONSE = Intent.ACTION_VIEW;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		String action = intent.getStringExtra(ACTION);
		
		switch (action) {
		case ACTION_PRAYER_TIME_CHANGED:
			onPrayerTimeChanged(intent);
			break;
		case ACTION_REMAINING_TIME_CHANGED:
			onRemainingTimeChanged(intent);
			break;

		default:
			break;
		}
	}
	
	private void onRemainingTimeChanged(Intent intent) {
		String remainingTime = intent.getStringExtra("remainingTime");
		if (remainingTime != null) {
			if (PrayerTimeActivity.getInstance() != null) {
				PrayerTimeActivity.getInstance().updateRemainingTime(remainingTime);
			}
		}
	}
	
	private void onPrayerTimeChanged(Intent intent) {
		String prayerString = intent.getStringExtra(Prayer.class.getName());
		if (prayerString != null) {
			Gson gson = new Gson();
			Prayer prayer = gson.fromJson(prayerString, Prayer.class);
			if (PrayerTimeActivity.getInstance() != null) {
				PrayerTimeActivity.getInstance().renderPrayerValue(prayer);
			}
		}
	}

}
