package net.fajarachmad.prayer.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.fajarachmad.prayer.R;
import net.fajarachmad.prayer.adapter.ScreenSlidePagerAdapter;
import net.fajarachmad.prayer.fragment.PrayerInfoFragment;
import net.fajarachmad.prayer.model.Location;
import net.fajarachmad.prayer.model.Prayer;
import net.fajarachmad.prayer.model.PrayerTime;
import net.fajarachmad.prayer.notification.NotificationPublisher;
import net.fajarachmad.prayer.util.GPSTracker;
import net.fajarachmad.prayer.util.HttpRequestUtil;
import net.fajarachmad.prayer.util.PrayTime;

import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class PrayerTimeActivity extends FragmentActivity implements AppConstant{

	private static String GOOGLE_TIMEZONE_API = "https://maps.googleapis.com/maps/api/timezone/json?";
	private static String API_KEY = "AIzaSyAZVavLEgDEwXa-iOwRu_hmnco7X-YbNBI";
	
	private static final int NUM_PAGES = 5;

	/** Called when the activity is first created. */
	private Prayer prayer;
	private CountDownTimer timer;
	private SharedPreferences sharedPrefs;
	private AlarmManager alarmManager;
	private List<String> tuningValues;
	private ViewPager mPager;
	private ScreenSlidePagerAdapter mPagerAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		setLocale();
		setContentView(R.layout.prayer_time_layout);
		setComponentListener();
		populateTuningValue();
		
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
		prayer = new Prayer();
		populatePreviousOrDefaultValue(prayer);
		
		//Define Location
		Location currentLocation;
		if (newLocation == null) {
			currentLocation = getCurrentLocation(prayer);
		} else {
			currentLocation = newLocation;
		}
		prayer.setLocation(currentLocation);
		
		//Timezone
		prayer.getLocation().setTimezone(getTimezone(prayer));;
		
		
		try {
			getPrayTime(prayer);
		} catch (ParseException e) {
			Log.e("Prayer", e.getMessage(), e);
		}
		
		save(prayer);
		renderPrayerValue(prayer);
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
	
	private void save(Prayer prayer) {
		Editor editor = sharedPrefs.edit();
		editor.putString("addressLine", prayer.getLocation().getAddressLine());
		editor.putString("city", prayer.getLocation().getCity());
		editor.putString("country", prayer.getLocation().getCountry());
		editor.putInt("timezone", prayer.getLocation().getTimezone());
		editor.putString("postalCode", prayer.getLocation().getPostalCode());
		editor.putFloat("latitude", Double.valueOf(prayer.getLocation().getLatitude()).floatValue());
		editor.putFloat("longitude", Double.valueOf(prayer.getLocation().getLongitude()).floatValue());
		
		editor.commit();
	}
	
	
	private void populatePreviousOrDefaultValue(Prayer prayer) {
		prayer.getLocation().setAddressLine(sharedPrefs.getString("addressLine", ""));
		prayer.getLocation().setCity(sharedPrefs.getString("city", "South Jakarta"));
		prayer.getLocation().setCountry(sharedPrefs.getString("country", "Indonesia"));
		prayer.getLocation().setLatitude(sharedPrefs.getFloat("latitude", Double.valueOf(-6.2087634).floatValue()));
		prayer.getLocation().setLongitude(sharedPrefs.getFloat("longitude", Double.valueOf(106.84559899999999).floatValue()));
		prayer.getLocation().setPostalCode(sharedPrefs.getString("postalCode", ""));
		prayer.getLocation().setTimezone(sharedPrefs.getInt("timezone", 7));
	}

	@Override
	public void onBackPressed() {
		finish();
		System.exit(0);
	}

	private void getPrayTime(Prayer prayer) throws ParseException {
		List<PrayerTime> prayerTimes = getPrayerTime(
				prayer.getLocation().getLatitude(), prayer.getLocation().getLongitude(),
				prayer.getLocation().getTimezone());
		prayer.setPrayerTimes(prayerTimes);

		try {
			getCurrentPrayer(prayer);
			showRemainingTime(prayer);
		} catch (ParseException e) {
			Log.e("Prayer", e.getMessage());
		}
	}
	
	private void scheduleNotification(Notification notification, int id, int delay) {

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, id);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }
	
	private void cancelNotification(int id) {
		Intent notificationIntent = new Intent(this, NotificationPublisher.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.cancel(pendingIntent);
	}

    private Notification getNotification(String title, String content, String sound) {
    	
    	Intent notificationIntent = new Intent(this, PrayerTimeActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        
    	NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setContentIntent(contentIntent);
        builder.setAutoCancel(true);
        Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+ "://" + getPackageName() + "/raw/"+sound);
        builder.setSound(alarmSound);
        builder.setSmallIcon(R.drawable.ic_launcher);
        return builder.build();
    }

	private int getTimezone(Prayer prayer) {

		int timeZoneOffset = prayer.getLocation().getTimezone();
		boolean isAutoDetectLocation = sharedPrefs.getBoolean(PREF_AUTODETECT_LOCATION_KEY, true); 
		if (isAutoDetectLocation && prayer.getLocation().getLatitude() != 0
				&& prayer.getLocation().getLongitude() != 0) {
			try {
				Date date = new Date();
				String url = GOOGLE_TIMEZONE_API + "location="
						+ prayer.getLocation().getLatitude() + ","
						+ prayer.getLocation().getLongitude() + "&timestamp="
						+ date.getTime() / 1000 + "&key=" + API_KEY;
				String jsonResult = HttpRequestUtil.GET(url);
				JSONObject jObj = new JSONObject(jsonResult);
				int rawOffset = jObj.getInt("rawOffset");
				timeZoneOffset = rawOffset / 60 / 60;
			} catch (Exception e) {
				Log.e("Prayer", e.getMessage());
			}
		}

		return timeZoneOffset;

	}

	private void showRemainingTime(final Prayer prayer) {
		Date current = new Date();
		long deadline = prayer.getNextPrayer().getPrayDate().getTime()
				- current.getTime();
		
		if (timer != null) {
			timer.cancel();
		}
		
		timer = new CountDownTimer(deadline, 60000) {

			@Override
			public void onFinish() {
				/*
				 * try { getCurrentPrayer(prayer); showRemainingTime(prayer); }
				 * catch (ParseException e) { Log.e("Prayer", e.getMessage()); }
				 */
			}

			@Override
			public void onTick(long millisUntilFinished) {
				long mnt = Double.valueOf(
						Math.floor(millisUntilFinished / 1000 / 60) % 60)
						.longValue();
				long hrs = Double
						.valueOf(
								Math.floor(millisUntilFinished
										/ (1000 * 60 * 60) % 24)).longValue();

				String text = String.valueOf(hrs) + " " +getResources().getString(R.string.pref_hours)+" "
						+ String.valueOf(mnt) + " "+getResources().getString(R.string.pref_minutes)+" "+getResources().getString(R.string.left_until)
						+ " "+prayer.getNextPrayer().getPrayName();

				//((TextView) findViewById(R.id.remaining_time)).setText(text);
				//mPagerAdapter.getPrayerInfoFragment().getNextRemainingTime().setText(text);
				PrayerInfoFragment page = (PrayerInfoFragment) mPagerAdapter.getFragment(0);
				if (page == null) {
					mPagerAdapter.setRemainigTime(text);
				} else {
					page.updateRemainingTime(text);
				}
			}

		}.start();
	}

	private void getCurrentPrayer(Prayer prayer) throws ParseException {
		Log.i("Prayer", "Getting current prayer");

		Date dateFrom;
		Date dateTo;
		boolean solved = false;
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-d H:m");
		SimpleDateFormat dateOlnyFormat = new SimpleDateFormat("yyyy-MM-d");

		int i = 0;

		for (i = 0; i < prayer.getPrayerTimes().size() - 1; i++) {
			
			String prayTimeFrom = prayer.getPrayerTimes().get(i).getPrayTime();
			String prayNameFrom = prayer.getPrayerTimes().get(i).getPrayName();
			String prayIdFrom = prayer.getPrayerTimes().get(i).getPrayId();
			
			String prayTimeTo = prayer.getPrayerTimes().get(i + 1).getPrayTime();
			String prayNameTo = prayer.getPrayerTimes().get(i + 1).getPrayName();
			String prayIdTo = prayer.getPrayerTimes().get(i + 1).getPrayId();
			
			
			String dateFromStr = dateOlnyFormat.format(prayer.getToday()) + " " + prayTimeFrom + ":00";
			String dateToStr = dateOlnyFormat.format(prayer.getToday()) + " " + prayTimeTo + ":00";

			dateFrom = dateTimeFormat.parse(dateFromStr);
			dateTo = dateTimeFormat.parse(dateToStr);

			Log.d("Prayer", prayTimeFrom
					+ "-->" + prayTimeTo
					+ "-->" + dateFrom + "-->" + dateTo);

			if (prayer.getToday().compareTo(dateFrom) > 0 && prayer.getToday().compareTo(dateTo) < 0) {
				prayer.setCurrentPrayer(new PrayerTime(prayIdFrom,prayNameFrom, prayTimeFrom, dateFrom));
				prayer.setNextPrayer(new PrayerTime(prayIdTo, prayNameTo, prayTimeTo, dateTo));
				solved = true;
			}
		}

		if (!solved) {

			Calendar cal = Calendar.getInstance();
			cal.setTime(prayer.getToday());
			cal.add(Calendar.DATE, 1);
			Date tomorrow = cal.getTime();
			
			String prayTimeFrom = prayer.getPrayerTimes().get(i).getPrayTime();
			String prayNameFrom = prayer.getPrayerTimes().get(i).getPrayName();
			String prayIdFrom = prayer.getPrayerTimes().get(i).getPrayId();
			
			String prayTimeTo = prayer.getPrayerTimes().get(0).getPrayTime();
			String prayNameTo = prayer.getPrayerTimes().get(0).getPrayName();
			String prayIdTo = prayer.getPrayerTimes().get(0).getPrayId();

			String dateFromStr = dateOlnyFormat.format(prayer.getToday()) + " " + prayTimeFrom + ":00";
			String dateToStr = dateOlnyFormat.format(tomorrow) + " " + prayTimeTo + ":00";

			dateFrom = dateTimeFormat.parse(dateFromStr);
			dateTo = dateTimeFormat.parse(dateToStr);

			prayer.setCurrentPrayer(new PrayerTime(prayIdFrom, prayNameFrom, prayTimeFrom, dateFrom));
			prayer.setNextPrayer(new PrayerTime(prayIdTo, prayNameTo,  prayTimeTo, dateTo));
		}
	}

	private void renderPrayerValue(Prayer prayer) {
		((TextView) findViewById(R.id.location_address)).setText(prayer.getLocation().getCity()+", "+prayer.getLocation().getCountry());
		//((TextView) findViewById(R.id.next_prayer)).setText(prayer.getNextPrayer().getPrayName());
		//((TextView) findViewById(R.id.next_pray_time)).setText(prayer.getNextPrayer().getPrayTime());
		//((TextView) findViewById(R.id.upcoming_prayer)).setText(getResources().getString(R.string.upcoming_prayer));
		
		/*mPagerAdapter.getPrayerInfoFragment().setNextPrayerName(prayer.getNextPrayer().getPrayName());
		mPagerAdapter.getPrayerInfoFragment().setNextPrayerTime(prayer.getNextPrayer().getPrayTime());
		mPagerAdapter.getPrayerInfoFragment().setRemainigTime(getResources().getString(R.string.upcoming_prayer));*/
		PrayerInfoFragment page = (PrayerInfoFragment) mPagerAdapter.getFragment(0);
		if (page == null) {
			mPagerAdapter.setNextPrayerName(prayer.getNextPrayer().getPrayName());
			mPagerAdapter.setNextPrayerTime(prayer.getNextPrayer().getPrayTime());
			mPagerAdapter.setUpcomingPray(getResources().getString(R.string.upcoming_prayer));
		} else {
			page.updateValue(prayer.getNextPrayer().getPrayName(), prayer.getNextPrayer().getPrayTime(), getResources().getString(R.string.upcoming_prayer));
		}
		
		
		
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

	private Location getCurrentLocation(Prayer prayer) {
		GPSTracker gpsTracker = new GPSTracker(this);
		Location location = prayer.getLocation();
		boolean isAutoDetectLocation = sharedPrefs.getBoolean(PREF_AUTODETECT_LOCATION_KEY, true);
		if (isAutoDetectLocation) {
			if (gpsTracker.getIsGPSTrackingEnabled()) {
				double latitude = gpsTracker.getLatitude();
				double longitude = gpsTracker.getLongitude();

				String country = gpsTracker.getCountryName(this);

				String city = null;
				String postalCode;
				String addressLine;
				if (country != null) {
					city = gpsTracker.getLocality(this);
					postalCode = gpsTracker.getPostalCode(this);
					addressLine = gpsTracker.getAddressLine(this);
					location.setCity(city);
					location.setAddressLine(addressLine);
					location.setPostalCode(postalCode);
					location.setCountry(country);
				}

				Log.i("Prayer", "Latitude: " + latitude);
				Log.i("Prayer", "Longitude: " + longitude);
				Log.i("Prayer", "Country: " + country);
				Log.i("Prayer", "City: " + city);

				location.setLatitude(latitude);
				location.setLongitude(longitude);

			} else {
				// can't get location
				// GPS or Network is not enabled
				// Ask user to enable GPS/network in settings
				gpsTracker.showSettingsAlert();
			}
		}
		
		return location;
	}

	private List<PrayerTime> getPrayerTime(double latitude, double longitude,
			int timezone) throws ParseException {
		PrayTime prayers = new PrayTime();
		List<PrayerTime> list = new ArrayList<PrayerTime>();
		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		
		int[] offsets = new int[7];
		offsets[0] = Integer.valueOf(resolveTuningValue(Float.valueOf(sharedPrefs.getFloat(PREF_TUNE_FAJR_KEY, 0)), tuningValues));
		offsets[1] = 0;
		offsets[2] = Integer.valueOf(resolveTuningValue(Float.valueOf(sharedPrefs.getFloat(PREF_TUNE_DHUHR_KEY, 0)), tuningValues));
		offsets[3] = Integer.valueOf(resolveTuningValue(Float.valueOf(sharedPrefs.getFloat(PREF_TUNE_ASR_KEY, 0)), tuningValues));
		offsets[4] = 0;
		offsets[5] = Integer.valueOf(resolveTuningValue(Float.valueOf(sharedPrefs.getFloat(PREF_TUNE_MAGHRIB_KEY, 0)), tuningValues));
		offsets[6] = Integer.valueOf(resolveTuningValue(Float.valueOf(sharedPrefs.getFloat(PREF_TUNE_ISHA_KEY, 0)), tuningValues));
		
		prayers.setCalcMethod(Integer.valueOf(sharedPrefs.getString(PREF_CALULATION_METHOD_KEY, DEFAULT_CALC_METHOD)));
		prayers.setAsrJuristic(Integer.valueOf(sharedPrefs.getString(PREF_ASR_METHOD_KEY, DEFAULT_ASR_METHOD)));
		prayers.tune(offsets);
		ArrayList<String> prayerTimes = prayers.getPrayerTimes(cal, latitude,
				longitude, timezone);
		ArrayList<String> prayerNames = prayers.getTimeNames();
		
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-d H:m");
		SimpleDateFormat dateOlnyFormat = new SimpleDateFormat("yyyy-MM-d");
		
		for (int i = 0; i < prayerTimes.size(); i++) {
			String prayId = prayerNames.get(i);
			String prayTime = prayerTimes.get(i);
			Log.i("Prayer",  prayId+ " - " + prayTime);
			
			String prayName = "";
			switch (prayId) {

			case FAJR_ID:
				prayName = getResources().getString(R.string.prayer_fajr_name);
				break;

			case DHUHR_ID:
				prayName = getResources().getString(R.string.prayer_dhuhr_name);
				break;

			case ASR_ID:
				prayName = getResources().getString(R.string.prayer_asr_name);
				break;

			case MAGHRIB_ID:
				prayName = getResources().getString(R.string.prayer_maghrib_name);
				break;

			case ISHA_ID:
				prayName = getResources().getString(R.string.prayer_isha_name);
				break;

			default:
				break;
			}
			
			if (!prayId.equals(SUNSET_ID)) {
				String prayerDateTimeStr = dateOlnyFormat.format(prayer.getToday()) + " "+ prayTime + ":00";
				Date prayerDateaTime = dateTimeFormat.parse(prayerDateTimeStr);
				list.add(new PrayerTime(prayId, prayName, prayTime, prayerDateaTime));
				
				//send notification
				cancelNotification(i);
				cancelNotification(i+10);
				if (prayerDateaTime.compareTo(now) > 0) {
					long delay = prayerDateaTime.getTime() - now.getTime();
					sendPrayAlarm(i, prayId, prayName, delay);
				}
			}
		}

		return list;
	}
	
	
	private void sendPrayAlarm(int id, String prayId, String parayerName, long delay){
		String message = getResources().getString(R.string.notif_on_prayer)+parayerName;
		String title = getResources().getString(R.string.notif_title);
		boolean isNotificationDisable = sharedPrefs.getBoolean(PREF_DISABLED_NOTIFICATION_KEY, false);
		
		switch (prayId) {
		case FAJR_ID:
			if (sharedPrefs.getBoolean(PREF_FAJR_ONPRAY_ALARM_KEY, false) && !isNotificationDisable) {
				String sound = sharedPrefs.getString(PREF_FAJR_ONPRAY_SOUND_KEY, DEFAULT_SOUND);
				scheduleNotification(getNotification(title, message, sound), id, Long.valueOf(delay).intValue());
				((ImageView)findViewById(R.id.onpray_icon_fajr)).setImageDrawable(getResources().getDrawable(R.drawable.volume_high));
			} else {
				((ImageView)findViewById(R.id.onpray_icon_fajr)).setImageDrawable(getResources().getDrawable(R.drawable.volume_mute));
			}

			if (sharedPrefs.getBoolean(PREF_FAJR_BEFOREPRAY_ALARM_KEY, false) && !isNotificationDisable) {
				int beforeTimeMinute = Integer.valueOf(sharedPrefs.getString(PREF_FAJR_BEFOREPRAY_NOTIFY_KEY, "0"));
				long beforeTimeMilis = beforeTimeMinute * 60  * 1000;
				long newDelay = delay - beforeTimeMilis;
				if (newDelay > 0) {
					String sound = sharedPrefs.getString(PREF_FAJR_BEFOREPRAY_SOUND_KEY, DEFAULT_SOUND);
					message = beforeTimeMinute+" "+getResources().getString(R.string.notif_before_pray)+" "+parayerName;
					scheduleNotification(getNotification(title, message, sound), id+10, Long.valueOf(newDelay).intValue());
					((ImageView)findViewById(R.id.before_pray_icon_fajr)).setImageDrawable(getResources().getDrawable(R.drawable.alarm));
				}
				
			} else {
				((ImageView)findViewById(R.id.before_pray_icon_fajr)).setImageDrawable(null);
			}
			break;
		case DHUHR_ID:
			if (sharedPrefs.getBoolean(PREF_DHUHR_ONPRAY_ALARM_KEY, false) && !isNotificationDisable) {
				String sound = sharedPrefs.getString(PREF_DHUHR_ONPRAY_SOUND_KEY, DEFAULT_SOUND);
				scheduleNotification(getNotification(title, message, sound), id, Long.valueOf(delay).intValue());
				((ImageView)findViewById(R.id.onpray_icon_dhuhr)).setImageDrawable(getResources().getDrawable(R.drawable.volume_high));
			} else {
				((ImageView)findViewById(R.id.onpray_icon_dhuhr)).setImageDrawable(getResources().getDrawable(R.drawable.volume_mute));
			}
			if (sharedPrefs.getBoolean(PREF_DHUHR_BEFOREPRAY_ALARM_KEY, false) && !isNotificationDisable) {
				int beforeTimeMinute = Integer.valueOf(sharedPrefs.getString(PREF_DHUHR_BEFOREPRAY_NOTIFY_KEY, "0"));
				long beforeTimeMilis = beforeTimeMinute * 60  * 1000;
				long newDelay = delay - beforeTimeMilis;
				if (newDelay > 0) {
					String sound = sharedPrefs.getString(PREF_DHUHR_BEFOREPRAY_SOUND_KEY, DEFAULT_SOUND);
					message = beforeTimeMinute+" "+getResources().getString(R.string.notif_before_pray)+" "+parayerName;
					scheduleNotification(getNotification(title, message, sound), id+10, Long.valueOf(newDelay).intValue());
					((ImageView)findViewById(R.id.before_pray_icon_dhuhr)).setImageDrawable(getResources().getDrawable(R.drawable.alarm));
				}
			} else {
				((ImageView)findViewById(R.id.before_pray_icon_dhuhr)).setImageDrawable(null);
			}
			break;
		case ASR_ID:
			if (sharedPrefs.getBoolean(PREF_ASR_ONPRAY_ALARM_KEY, false) && !isNotificationDisable) {
				String sound = sharedPrefs.getString(PREF_ASR_ONPRAY_SOUND_KEY, DEFAULT_SOUND);
				scheduleNotification(getNotification(title, message, sound), id, Long.valueOf(delay).intValue());
				((ImageView)findViewById(R.id.onpray_icon_asr)).setImageDrawable(getResources().getDrawable(R.drawable.volume_high));
			} else {
				((ImageView)findViewById(R.id.onpray_icon_asr)).setImageDrawable(getResources().getDrawable(R.drawable.volume_mute));
			}
			if (sharedPrefs.getBoolean(PREF_ASR_BEFOREPRAY_ALARM_KEY, false) && !isNotificationDisable) {
				int beforeTimeMinute = Integer.valueOf(sharedPrefs.getString(PREF_ASR_BEFOREPRAY_NOTIFY_KEY, "0"));
				long beforeTimeMilis = beforeTimeMinute * 60  * 1000;
				long newDelay = delay - beforeTimeMilis;
				if (newDelay > 0) {
					String sound = sharedPrefs.getString(PREF_ASR_BEFOREPRAY_SOUND_KEY, DEFAULT_SOUND);
					message = beforeTimeMinute+" "+getResources().getString(R.string.notif_before_pray)+" "+parayerName;
					scheduleNotification(getNotification(title, message, sound), id+10, Long.valueOf(newDelay).intValue());
					((ImageView)findViewById(R.id.before_pray_icon_asr)).setImageDrawable(getResources().getDrawable(R.drawable.alarm));
				}
				
			} else {
				((ImageView)findViewById(R.id.before_pray_icon_asr)).setImageDrawable(null);
			}
			break;
		case MAGHRIB_ID:
			if (sharedPrefs.getBoolean(PREF_MAGHRIB_ONPRAY_ALARM_KEY, false) && !isNotificationDisable) {
				String sound = sharedPrefs.getString(PREF_MAGHRIB_ONPRAY_SOUND_KEY, DEFAULT_SOUND);
				scheduleNotification(getNotification(title, message, sound), id, Long.valueOf(delay).intValue());
				((ImageView)findViewById(R.id.onpray_icon_maghrib)).setImageDrawable(getResources().getDrawable(R.drawable.volume_high));
			} else {
				((ImageView)findViewById(R.id.onpray_icon_maghrib)).setImageDrawable(getResources().getDrawable(R.drawable.volume_mute));
			}
			if (sharedPrefs.getBoolean(PREF_MAGHRIB_BEFOREPRAY_ALARM_KEY, false) && !isNotificationDisable) {
				int beforeTimeMinute = Integer.valueOf(sharedPrefs.getString(PREF_MAGHRIB_BEFOREPRAY_NOTIFY_KEY, "0"));
				long beforeTimeMilis = beforeTimeMinute * 60  * 1000;
				long newDelay = delay - beforeTimeMilis;
				if (newDelay > 0) {
					String sound = sharedPrefs.getString(PREF_MAGHRIB_BEFOREPRAY_SOUND_KEY, DEFAULT_SOUND);
					message = beforeTimeMinute+" "+getResources().getString(R.string.notif_before_pray)+" "+parayerName;
					scheduleNotification(getNotification(title, message, sound), id+10, Long.valueOf(newDelay).intValue());
					((ImageView)findViewById(R.id.before_pray_icon_maghrib)).setImageDrawable(getResources().getDrawable(R.drawable.alarm));
				}
				
			} else {
				((ImageView)findViewById(R.id.before_pray_icon_maghrib)).setImageDrawable(null);
			}
			break;
		case ISHA_ID:
			if (sharedPrefs.getBoolean(PREF_ISHA_ONPRAY_ALARM_KEY, false) && !isNotificationDisable) {
				String sound = sharedPrefs.getString(PREF_ISHA_ONPRAY_SOUND_KEY, DEFAULT_SOUND);
				scheduleNotification(getNotification(title, message, sound), id, Long.valueOf(delay).intValue());
				((ImageView)findViewById(R.id.onpray_icon_isha)).setImageDrawable(getResources().getDrawable(R.drawable.volume_high));
			} else {
				((ImageView)findViewById(R.id.onpray_icon_isha)).setImageDrawable(getResources().getDrawable(R.drawable.volume_mute));
			}
			if (sharedPrefs.getBoolean(PREF_ISHA_BEFOREPRAY_ALARM_KEY, false) && !isNotificationDisable) {
				int beforeTimeMinute = Integer.valueOf(sharedPrefs.getString(PREF_ISHA_BEFOREPRAY_NOTIFY_KEY, "0"));
				long beforeTimeMilis = beforeTimeMinute * 60  * 1000;
				long newDelay = delay - beforeTimeMilis;
				if (newDelay > 0) {
					String sound = sharedPrefs.getString(PREF_ISHA_BEFOREPRAY_SOUND_KEY, DEFAULT_SOUND);
					message = beforeTimeMinute+" "+getResources().getString(R.string.notif_before_pray)+" "+parayerName;
					scheduleNotification(getNotification(title, message, sound), id+10, Long.valueOf(newDelay).intValue());
					((ImageView)findViewById(R.id.before_pray_icon_isha)).setImageDrawable(getResources().getDrawable(R.drawable.alarm));
				}
				
			} else {
				((ImageView)findViewById(R.id.before_pray_icon_isha)).setImageDrawable(null);
			}
			break;
		default:
			break;
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
			recalculatePrayerTime();
			break;
		case NOTIFICATION_SETTING_ID:
			setNotificationSetting();
			break;
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
		recalculatePrayerTime();
	}
	
	private void recalculatePrayerTime() {
		try {
			getPrayTime(prayer);
		} catch (ParseException e) {
			Log.e("Prayer",e.getMessage(),e);
		}
		renderPrayerValue(prayer);
	}
	
	private int resolveTuningValue(float value, List<String> values) {
		int index = (int) (value * values.toArray(new String[0]).length);
		index = Math.min(index, values.toArray(new String[0]).length - 1);
		return Integer.valueOf(values.toArray(new String[0])[index]);
		
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