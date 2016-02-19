package net.fajarachmad.prayer.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import net.fajarachmad.prayer.R;
import net.fajarachmad.prayer.adapter.ScreenSlidePagerAdapter;
import net.fajarachmad.prayer.fragment.PrayerInfoFragment;
import net.fajarachmad.prayer.model.Location;
import net.fajarachmad.prayer.model.Prayer;
import net.fajarachmad.prayer.service.PrayerTimeService;

public class PrayerTimeActivity extends FragmentActivity implements AppConstant{

	private Prayer prayer;
	private SharedPreferences sharedPrefs;
	private List<String> tuningValues;
	private ViewPager mPager;
	private ScreenSlidePagerAdapter mPagerAdapter;
	private static PrayerTimeActivity activity;
	
	public static PrayerTimeActivity getInstance() {
		return activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		activity = this;
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		setLocale();
		setContentView(R.layout.prayer_time_layout);
		setComponentListener();
		populateTuningValue();
		stopAlarmSound();
		closeNotification();
		updateNotificationIcon();
		// Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(0);
        
		
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		Location newLocation = getIntent().getParcelableExtra(Location.class.getName());
		
		Gson gson = new Gson();
		Intent service = new Intent(this, PrayerTimeService.class);
		if (newLocation != null) {
			String newLocationJson = gson.toJson(newLocation);
			service.putExtra(Location.class.getName(), newLocationJson);
		}
		service.putExtra(ACTION, ACTION_GET_PRAYER_TIME);
		startService(service);
	}
	
	private void closeNotification() {
		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
	}
	
	private void stopAlarmSound() {
		Intent service = new Intent(this, PrayerTimeService.class);
		service.putExtra(ACTION, ACTION_STOP_SOUND);
		startService(service);
	}
	
	public void updateValue(String value) {
		((TextView) findViewById(R.id.location_address)).setText(value);
	}
	
	private void sendPrayerTimeService(Prayer prayer) {
		Gson gson = new Gson();
		String prayerJson = gson.toJson(prayer);
		Intent service = new Intent(this, PrayerTimeService.class);
		service.putExtra(Prayer.class.getName(), prayerJson);
		service.putExtra(ACTION, ACTION_GET_PRAYER_TIME);
		startService(service);
	}
	
	private void setLocale() {
		Locale locale = new Locale(sharedPrefs.getString(PREF_LANGUAGE_KEY, DEFAULT_LANGUAGE)); 
	    Locale.setDefault(locale);
	    Configuration config = new Configuration();
	    config.locale = locale;
	    getApplicationContext().getResources().updateConfiguration(config,getApplicationContext().getResources().getDisplayMetrics());
	}
	
	private void populateTuningValue () {
		tuningValues = new ArrayList<String>();
		for (int i = -60; i <= 60; i++) {
			tuningValues.add(String.valueOf(i));
		}
	}
	
	private void setComponentListener() {
		findViewById(R.id.location_address).setOnClickListener(
				new LocationButtonListener());
		
		findViewById(R.id.cardview_fajr).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(PrayerTimeActivity.this, NotificationSetupActivity.class);
				i.putExtra("PrayID", FAJR_ID);
				i.putExtra("PrayName", getResources().getString(R.string.prayer_fajr_name));
	            startActivityForResult(i, NOTIFICATION_SETTING_ID);
			}
		});
		
		findViewById(R.id.cardview_dhuhr).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(PrayerTimeActivity.this, NotificationSetupActivity.class);
				i.putExtra("PrayID", DHUHR_ID);
				i.putExtra("PrayName", getResources().getString(R.string.prayer_dhuhr_name));
	            startActivityForResult(i, NOTIFICATION_SETTING_ID);
			}
		});
		
		findViewById(R.id.cardview_asr).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(PrayerTimeActivity.this, NotificationSetupActivity.class);
				i.putExtra("PrayID", ASR_ID);
				i.putExtra("PrayName", getResources().getString(R.string.prayer_asr_name));
	            startActivityForResult(i, NOTIFICATION_SETTING_ID);
			}
		});
		
		findViewById(R.id.cardview_maghrib).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(PrayerTimeActivity.this, NotificationSetupActivity.class);
				i.putExtra("PrayID", MAGHRIB_ID);
				i.putExtra("PrayName", getResources().getString(R.string.prayer_maghrib_name));
	            startActivityForResult(i, NOTIFICATION_SETTING_ID);
			}
		});

		findViewById(R.id.cardview_isha).setOnClickListener(new OnClickListener() {
	
			@Override
			public void onClick(View v) {
				Intent i = new Intent(PrayerTimeActivity.this, NotificationSetupActivity.class);
				i.putExtra("PrayID", ISHA_ID);
				i.putExtra("PrayName", getResources().getString(R.string.prayer_isha_name));
		        startActivityForResult(i, NOTIFICATION_SETTING_ID);
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		finish();
		System.exit(0);
	}

	public void updateRemainingTime(String value) {
		PrayerInfoFragment page = (PrayerInfoFragment) mPagerAdapter.getFragment(0);
		if (page == null) {
			mPagerAdapter.setRemainigTime(value);
		} else {
			page.updateRemainingTime(value);
		}
	}

	public void renderPrayerValue(Prayer prayer) {
		((TextView) findViewById(R.id.location_address)).setText(prayer.getLocation().getCity()+", "+prayer.getLocation().getCountry());
		
		String nextPrayerName = prayer.getNextPrayer() != null ? prayer.getNextPrayer().getPrayName() : "";
		String nextPrayerTime = prayer.getNextPrayer() != null ? prayer.getNextPrayer().getPrayTime() : "";
		
		PrayerInfoFragment page = (PrayerInfoFragment) mPagerAdapter.getFragment(0);
		if (page == null) {
			mPagerAdapter.setNextPrayerName(nextPrayerName);
			mPagerAdapter.setNextPrayerTime(nextPrayerTime);
			mPagerAdapter.setUpcomingPray(getResources().getString(R.string.upcoming_prayer));
		} else {
			page.updateValue(nextPrayerName, nextPrayerTime, getResources().getString(R.string.upcoming_prayer));
		}
		
		if (prayer.getPrayerTimes() != null) {
			populatePrayerTimeValue(prayer);
		}
		
	}
	
	private void populatePrayerTimeValue(Prayer prayer) {
		for (int i = 0; i < prayer.getPrayerTimes().size(); i++) {
			
			String prayId = prayer.getPrayerTimes().get(i).getPrayId();
			String prayName = prayer.getPrayerTimes().get(i).getPrayName();
			String prayTime = prayer.getPrayerTimes().get(i).getPrayTime();
			
			Log.i("Prayer", prayName+ " - " + prayTime);

			TextView txtName = null;
			TextView txtTime = null;

			switch (prayId) {

			case FAJR_ID:
				txtName = (TextView) findViewById(R.id.prayer_fajr_name);
				txtTime = (TextView) findViewById(R.id.prayer_fajr_time);
				break;

			case DHUHR_ID:
				txtName = (TextView) findViewById(R.id.prayer_dhuhr_name);
				txtTime = (TextView) findViewById(R.id.prayer_dhuhr_time);
				break;

			case ASR_ID:
				txtName = (TextView) findViewById(R.id.prayer_asr_name);
				txtTime = (TextView) findViewById(R.id.prayer_asr_time);
				break;

			case MAGHRIB_ID:
				txtName = (TextView) findViewById(R.id.prayer_maghrib_name);
				txtTime = (TextView) findViewById(R.id.prayer_maghrib_time);
				break;

			case ISHA_ID:
				txtName = (TextView) findViewById(R.id.prayer_isha_name);
				txtTime = (TextView) findViewById(R.id.prayer_isha_time);
				break;

			default:
				break;
			}

			if (txtName != null && txtTime != null) {
				txtName.setText(prayName);
				txtTime.setText(prayTime);
			}

		}
	}

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
 
        case R.id.menu_settings:
            Intent i = new Intent(this, SettingActivity.class);
            startActivityForResult(i, APP_SETTING_ID);
            break;
 
        }
 
        return true;
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case APP_SETTING_ID:
			sendPrayerTimeService(prayer);
			break;
		case NOTIFICATION_SETTING_ID:
			setNotificationSetting();
			updateNotificationIcon();
			break;
		}

	}
	
	private void updateNotificationIcon() {
		updateFajrNotificationIcon();
		
		updateDhuhrNotificationIcon();
		
		updateAsrNotificationIcon();
		
		updateMaghribNotificationIcon();
		
		updateIshaNotificationIcon();
	}

	private void updateFajrNotificationIcon() {
		if (sharedPrefs.getBoolean(PREF_FAJR_ONPRAY_ALARM_KEY, true)) {
			((ImageView) findViewById(R.id.onpray_icon_fajr)).setImageDrawable(getResources().getDrawable(R.drawable.volume_high));
		} else {
			((ImageView) findViewById(R.id.onpray_icon_fajr)).setImageDrawable(getResources().getDrawable(R.drawable.volume_mute));
		}
		if (sharedPrefs.getBoolean(PREF_FAJR_BEFOREPRAY_ALARM_KEY, true)) {
			((ImageView) findViewById(R.id.before_pray_icon_fajr)).setImageDrawable(getResources().getDrawable(R.drawable.alarm));
		} else {
			((ImageView) findViewById(R.id.before_pray_icon_fajr)).setImageDrawable(null);
		}
	}

	private void updateDhuhrNotificationIcon() {
		if (sharedPrefs.getBoolean(PREF_DHUHR_ONPRAY_ALARM_KEY, true)) {
			((ImageView) findViewById(R.id.onpray_icon_dhuhr)).setImageDrawable(getResources().getDrawable(R.drawable.volume_high));
		} else {
			((ImageView) findViewById(R.id.onpray_icon_dhuhr)).setImageDrawable(getResources().getDrawable(R.drawable.volume_mute));
		}
		if (sharedPrefs.getBoolean(PREF_DHUHR_BEFOREPRAY_ALARM_KEY, true)) {
			((ImageView) findViewById(R.id.before_pray_icon_dhuhr)).setImageDrawable(getResources().getDrawable(R.drawable.alarm));
		} else {
			((ImageView) findViewById(R.id.before_pray_icon_dhuhr)).setImageDrawable(null);
		}
	}

	private void updateAsrNotificationIcon() {
		if (sharedPrefs.getBoolean(PREF_ASR_ONPRAY_ALARM_KEY, true)) {
			((ImageView) findViewById(R.id.onpray_icon_asr)).setImageDrawable(getResources().getDrawable(R.drawable.volume_high));
		} else {
			((ImageView) findViewById(R.id.onpray_icon_asr)).setImageDrawable(getResources().getDrawable(R.drawable.volume_mute));
		}
		if (sharedPrefs.getBoolean(PREF_ASR_BEFOREPRAY_ALARM_KEY, true)) {
			((ImageView) findViewById(R.id.before_pray_icon_asr)).setImageDrawable(getResources().getDrawable(R.drawable.alarm));
		} else {
			((ImageView) findViewById(R.id.before_pray_icon_asr)).setImageDrawable(null);
		}
	}

	private void updateMaghribNotificationIcon() {
		if (sharedPrefs.getBoolean(PREF_MAGHRIB_ONPRAY_ALARM_KEY, true)) {
			((ImageView) findViewById(R.id.onpray_icon_maghrib)).setImageDrawable(getResources().getDrawable(R.drawable.volume_high));
		} else {
			((ImageView) findViewById(R.id.onpray_icon_maghrib)).setImageDrawable(getResources().getDrawable(R.drawable.volume_mute));
		}
		if (sharedPrefs.getBoolean(PREF_MAGHRIB_BEFOREPRAY_ALARM_KEY, true)) {
			((ImageView) findViewById(R.id.before_pray_icon_maghrib)).setImageDrawable(getResources().getDrawable(R.drawable.alarm));
		} else {
			((ImageView) findViewById(R.id.before_pray_icon_maghrib)).setImageDrawable(null);
		}
	}

	private void updateIshaNotificationIcon() {
		if (sharedPrefs.getBoolean(PREF_ISHA_ONPRAY_ALARM_KEY, true)) {
			((ImageView) findViewById(R.id.onpray_icon_isha)).setImageDrawable(getResources().getDrawable(R.drawable.volume_high));
		} else {
			((ImageView) findViewById(R.id.onpray_icon_isha)).setImageDrawable(getResources().getDrawable(R.drawable.volume_mute));
		}
		if (sharedPrefs.getBoolean(PREF_ISHA_BEFOREPRAY_ALARM_KEY, true)) {
			((ImageView) findViewById(R.id.before_pray_icon_isha)).setImageDrawable(getResources().getDrawable(R.drawable.alarm));
		} else {
			((ImageView) findViewById(R.id.before_pray_icon_isha)).setImageDrawable(null);
		}
	}
	
	private void setNotificationSetting() {
		String notifPrayId = sharedPrefs.getString("NotifPrayId", null);
		
		Boolean prefOnPrayAlarm = sharedPrefs.getBoolean(PREF_ONPRAY_ALARM_KEY, false);
		String prefSoundOnPray = sharedPrefs.getString(PREF_ONPRAY_SOUND_KEY, null);
		Boolean prefBeforePrayAlarm = sharedPrefs.getBoolean(PREF_BEFOREPRAY_ALARM_KEY, false);
		String prefNotifyBefore = sharedPrefs.getString(PREF_BEFOREPRAY_NOTIFY_KEY, DEFAULT_NOTIFY_TIME);
		String prefSoundBeforePray = sharedPrefs.getString(PREF_BEFOREPRAY_SOUND_KEY, DEFAULT_SOUND);
		
		Editor editor = sharedPrefs.edit();
		
		switch (notifPrayId) {
		case FAJR_ID:
			editor.putBoolean(PREF_FAJR_ONPRAY_ALARM_KEY, prefOnPrayAlarm);
			editor.putString(PREF_FAJR_ONPRAY_SOUND_KEY, prefSoundOnPray);
			editor.putBoolean(PREF_FAJR_BEFOREPRAY_ALARM_KEY, prefBeforePrayAlarm);
			editor.putString(PREF_FAJR_BEFOREPRAY_NOTIFY_KEY, prefNotifyBefore);
			editor.putString(PREF_FAJR_BEFOREPRAY_SOUND_KEY, prefSoundBeforePray);
			break;
		
		case DHUHR_ID:
			editor.putBoolean(PREF_DHUHR_ONPRAY_ALARM_KEY, prefOnPrayAlarm);
			editor.putString(PREF_DHUHR_ONPRAY_SOUND_KEY, prefSoundOnPray);
			editor.putBoolean(PREF_DHUHR_BEFOREPRAY_ALARM_KEY, prefBeforePrayAlarm);
			editor.putString(PREF_DHUHR_BEFOREPRAY_NOTIFY_KEY, prefNotifyBefore);
			editor.putString(PREF_DHUHR_BEFOREPRAY_SOUND_KEY, prefSoundBeforePray);
			break;
		
		case ASR_ID:
			editor.putBoolean(PREF_ASR_ONPRAY_ALARM_KEY, prefOnPrayAlarm);
			editor.putString(PREF_ASR_ONPRAY_SOUND_KEY, prefSoundOnPray);
			editor.putBoolean(PREF_ASR_BEFOREPRAY_ALARM_KEY, prefBeforePrayAlarm);
			editor.putString(PREF_ASR_BEFOREPRAY_NOTIFY_KEY, prefNotifyBefore);
			editor.putString(PREF_ASR_BEFOREPRAY_SOUND_KEY, prefSoundBeforePray);
			break;
		
		case MAGHRIB_ID:
			editor.putBoolean(PREF_MAGHRIB_ONPRAY_ALARM_KEY, prefOnPrayAlarm);
			editor.putString(PREF_MAGHRIB_ONPRAY_SOUND_KEY, prefSoundOnPray);
			editor.putBoolean(PREF_MAGHRIB_BEFOREPRAY_ALARM_KEY, prefBeforePrayAlarm);
			editor.putString(PREF_MAGHRIB_BEFOREPRAY_NOTIFY_KEY, prefNotifyBefore);
			editor.putString(PREF_MAGHRIB_BEFOREPRAY_SOUND_KEY, prefSoundBeforePray);
			break;
			
		case ISHA_ID:
			editor.putBoolean(PREF_ISHA_ONPRAY_ALARM_KEY, prefOnPrayAlarm);
			editor.putString(PREF_ISHA_ONPRAY_SOUND_KEY, prefSoundOnPray);
			editor.putBoolean(PREF_ISHA_BEFOREPRAY_ALARM_KEY, prefBeforePrayAlarm);
			editor.putString(PREF_ISHA_BEFOREPRAY_NOTIFY_KEY, prefNotifyBefore);
			editor.putString(PREF_ISHA_BEFOREPRAY_SOUND_KEY, prefSoundBeforePray);
			break;
		default:
			break;
		}
		
		editor.commit();
		sendPrayerTimeService(prayer);
	}
	
	class LocationButtonListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			Log.i("Prayer", "Location Button clicked");

			Intent intent = new Intent(PrayerTimeActivity.this,
					LocationSettingActivity.class);
			startActivity(intent);
		}

	}
}