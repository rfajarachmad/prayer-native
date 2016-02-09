package net.fajarachmad.prayer.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.fajarachmad.prayer.R;
import net.fajarachmad.prayer.model.Location;
import net.fajarachmad.prayer.model.Prayer;
import net.fajarachmad.prayer.model.PrayerTime;
import net.fajarachmad.prayer.notification.NotificationPublisher;
import net.fajarachmad.prayer.util.GPSTracker;
import net.fajarachmad.prayer.util.HttpRequestUtil;
import net.fajarachmad.prayer.util.PrayTime;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

public class PrayerTimeActivity extends Activity {

	private static String GOOGLE_TIMEZONE_API = "https://maps.googleapis.com/maps/api/timezone/json?";
	private static String API_KEY = "AIzaSyAZVavLEgDEwXa-iOwRu_hmnco7X-YbNBI";

	/** Called when the activity is first created. */
	private Prayer prayer;
	private CountDownTimer timer;
	private NotificationManager notificationManager;
	private SharedPreferences sharedPrefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.prayer_time_layout);
		
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		Location newLocation = getIntent().getParcelableExtra(Location.class.getName());
		prayer = new Prayer();
		
		populatePreviousOrDefaultValue(prayer);
		
		notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);;
		
		Location currentLocation;
		if (newLocation == null) {
			currentLocation = getCurrentLocation(prayer);
		} else {
			currentLocation = newLocation;
		}

		prayer.setLocation(currentLocation);
		
		prayer.getLocation().setTimezone(getTimezone(prayer));;
		try {
			getPrayTime(prayer);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		save(prayer);
		renderPrayerValue(prayer);
		findViewById(R.id.location_address).setOnClickListener(
				new LocationButtonListener());
		
		findViewById(R.id.tbl_row_fajr).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(PrayerTimeActivity.this, NotificationSetupActivity.class);
				i.putExtra("PrayID", "fajr");
	            startActivityForResult(i, 2);
			}
		});;

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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private Notification getNotification(String title, String content) {
    	NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.ic_launcher);
        return builder.build();
    }

	private int getTimezone(Prayer prayer) {

		int timeZoneOffset = prayer.getLocation().getTimezone();
		boolean isAutoDetectLocation = sharedPrefs.getBoolean("prefAutoDetectLocation", true); 
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

				String text = String.valueOf(hrs) + " HRS "
						+ String.valueOf(mnt) + " MINS LEFT UNTIL "
						+ prayer.getNextPrayer().getPrayName();

				((TextView) findViewById(R.id.remaining_time)).setText(text);

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

			String dateFromStr = dateOlnyFormat.format(prayer.getToday()) + " "
					+ prayer.getPrayerTimes().get(i).getPrayTime() + ":00";
			String dateToStr = dateOlnyFormat.format(prayer.getToday()) + " "
					+ prayer.getPrayerTimes().get(i + 1).getPrayTime() + ":00";

			dateFrom = dateTimeFormat.parse(dateFromStr);
			dateTo = dateTimeFormat.parse(dateToStr);

			Log.d("Prayer", prayer.getPrayerTimes().get(i).getPrayTime()
					+ "-->" + prayer.getPrayerTimes().get(i + 1).getPrayTime()
					+ "-->" + dateFrom + "-->" + dateTo);

			if (prayer.getToday().compareTo(dateFrom) > 0
					&& prayer.getToday().compareTo(dateTo) < 0) {
				prayer.setCurrentPrayer(new PrayerTime(prayer.getPrayerTimes()
						.get(i).getPrayName(), prayer.getPrayerTimes().get(i)
						.getPrayTime(), dateFrom));
				prayer.setNextPrayer(new PrayerTime(prayer.getPrayerTimes()
						.get(i + 1).getPrayName(), prayer.getPrayerTimes()
						.get(i + 1).getPrayTime(), dateTo));
				solved = true;
			}
		}

		if (!solved) {

			Calendar cal = Calendar.getInstance();
			cal.setTime(prayer.getToday());
			cal.add(Calendar.DATE, 1);
			Date tomorrow = cal.getTime();

			String dateFromStr = dateOlnyFormat.format(prayer.getToday()) + " "
					+ prayer.getPrayerTimes().get(i).getPrayTime() + ":00";
			String dateToStr = dateOlnyFormat.format(tomorrow) + " "
					+ prayer.getPrayerTimes().get(0).getPrayTime() + ":00";

			dateFrom = dateTimeFormat.parse(dateFromStr);
			dateTo = dateTimeFormat.parse(dateToStr);

			prayer.setCurrentPrayer(new PrayerTime(prayer.getPrayerTimes()
					.get(i).getPrayName(), prayer.getPrayerTimes().get(i)
					.getPrayTime(), dateFrom));
			prayer.setNextPrayer(new PrayerTime(prayer.getPrayerTimes().get(0)
					.getPrayName(), prayer.getPrayerTimes().get(0)
					.getPrayTime(), dateTo));
		}
	}

	private void renderPrayerValue(Prayer prayer) {
		((TextView) findViewById(R.id.location_address)).setText(prayer
				.getLocation().getCity()
				+ ", "
				+ prayer.getLocation().getCountry());
		((TextView) findViewById(R.id.next_prayer)).setText(prayer
				.getNextPrayer().getPrayName());
		((TextView) findViewById(R.id.next_pray_time)).setText(prayer
				.getNextPrayer().getPrayTime());

		for (int i = 0; i < prayer.getPrayerTimes().size(); i++) {
			Log.i("Prayer", prayer.getPrayerTimes().get(i).getPrayName()
					+ " - " + prayer.getPrayerTimes().get(i).getPrayTime());

			TextView txtName = null;
			TextView txtTime = null;

			switch (prayer.getPrayerTimes().get(i).getPrayName()) {

			case "Fajr":
				txtName = (TextView) findViewById(R.id.prayer_fajr_name);
				txtTime = (TextView) findViewById(R.id.prayer_fajr_time);
				break;

			case "Dhuhr":
				txtName = (TextView) findViewById(R.id.prayer_dhuhr_name);
				txtTime = (TextView) findViewById(R.id.prayer_dhuhr_time);
				break;

			case "Asr":
				txtName = (TextView) findViewById(R.id.prayer_asr_name);
				txtTime = (TextView) findViewById(R.id.prayer_asr_time);
				break;

			case "Maghrib":
				txtName = (TextView) findViewById(R.id.prayer_maghrib_name);
				txtTime = (TextView) findViewById(R.id.prayer_maghrib_time);
				break;

			case "Isha":
				txtName = (TextView) findViewById(R.id.prayer_isha_name);
				txtTime = (TextView) findViewById(R.id.prayer_isha_time);
				break;

			default:
				break;
			}

			if (txtName != null && txtTime != null) {
				txtName.setText(prayer.getPrayerTimes().get(i).getPrayName());
				txtTime.setText(prayer.getPrayerTimes().get(i).getPrayTime());
			}

		}
	}

	private Location getCurrentLocation(Prayer prayer) {
		GPSTracker gpsTracker = new GPSTracker(this);
		Location location = prayer.getLocation();
		boolean isAutoDetectLocation = sharedPrefs.getBoolean("prefAutoDetectLocation", true);
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
		offsets[0] = prayer.getSetting().getFajrtuning();
		offsets[1] = 0;
		offsets[2] = prayer.getSetting().getDhuhrtuning();
		offsets[3] = prayer.getSetting().getAsrtuning();
		offsets[4] = 0;
		offsets[5] = prayer.getSetting().getMaghribtuning();
		offsets[6] = prayer.getSetting().getIshatuning();
		
		prayers.setCalcMethod(prayer.getSetting().getMethod());
		prayers.setAsrJuristic(prayer.getSetting().getAsrMethod());
		prayers.tune(offsets);
		ArrayList<String> prayerTimes = prayers.getPrayerTimes(cal, latitude,
				longitude, timezone);
		ArrayList<String> prayerNames = prayers.getTimeNames();
		
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-d H:m");
		SimpleDateFormat dateOlnyFormat = new SimpleDateFormat("yyyy-MM-d");
		
		notificationManager.cancelAll();
		for (int i = 0; i < prayerTimes.size(); i++) {
			Log.i("Prayer", prayerNames.get(i) + " - " + prayerTimes.get(i));

			TextView txtName = null;
			TextView txtTime = null;

			if (!prayerNames.get(i).equals("Sunset")) {
				String prayerDateTimeStr = dateOlnyFormat.format(prayer.getToday()) + " "+ prayerTimes.get(i) + ":00";
				Date prayerDateaTime = dateTimeFormat.parse(prayerDateTimeStr);
				list.add(new PrayerTime(prayerNames.get(i), prayerTimes.get(i), prayerDateaTime));
				
				//send notification
				if (prayerDateaTime.compareTo(now) > 0) {
					long delay = prayerDateaTime.getTime() - now.getTime();
					String message = "It's time to pray "+prayerNames.get(i);
					String title = prayerNames.get(i)+" Notification";
					scheduleNotification(getNotification(title, message), i, Long.valueOf(delay).intValue());
				}
			}

			switch (prayerNames.get(i)) {

			case "Fajr":
				txtName = (TextView) findViewById(R.id.prayer_fajr_name);
				txtTime = (TextView) findViewById(R.id.prayer_fajr_time);
				break;

			case "Dhuhr":
				txtName = (TextView) findViewById(R.id.prayer_dhuhr_name);
				txtTime = (TextView) findViewById(R.id.prayer_dhuhr_time);
				break;

			case "Asr":
				txtName = (TextView) findViewById(R.id.prayer_asr_name);
				txtTime = (TextView) findViewById(R.id.prayer_asr_time);
				break;

			case "Maghrib":
				txtName = (TextView) findViewById(R.id.prayer_maghrib_name);
				txtTime = (TextView) findViewById(R.id.prayer_maghrib_time);
				break;

			case "Isha":
				txtName = (TextView) findViewById(R.id.prayer_isha_name);
				txtTime = (TextView) findViewById(R.id.prayer_isha_time);
				break;

			default:
				break;
			}

			if (txtName != null && txtTime != null) {
				txtName.setText(prayerNames.get(i));
				txtTime.setText(prayerTimes.get(i));
			}

		}

		return list;
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
            startActivityForResult(i, 1);
            break;
 
        }
 
        return true;
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case 1:
			setSetting();
			break;

		}

	}
	
	private void setSetting() {
		String language = sharedPrefs.getString("prefLanguage", "en");
		int method = Integer.valueOf(sharedPrefs.getString("prefCalculationMethod", "3"));
		int asrMethod = Integer.valueOf(sharedPrefs.getString("prefAsrMethod", "0"));
		boolean disableNotification = sharedPrefs.getBoolean("prefDisabledNotification", false);
		float fajrtuning = sharedPrefs.getFloat("tune_fajr", 0);
		float dhuhrtuning = sharedPrefs.getFloat("tune_dhuhr", 0);
		float asrtuning = sharedPrefs.getFloat("tune_asr", 0);
		float maghribtuning = sharedPrefs.getFloat("tune_maghrib", 0);
		float ishatuning = sharedPrefs.getFloat("tune_isha", 0);
		
		List<String> values = new ArrayList<String>();
		for (int i = -60; i <= 60; i++) {
			values.add(String.valueOf(i));
		}
		
		prayer.getSetting().setLanguage(language);
		prayer.getSetting().setMethod(method);
		prayer.getSetting().setAsrMethod(asrMethod);
		prayer.getSetting().setDisablednotification(disableNotification);
		prayer.getSetting().setFajrtuning(resolveTuningValue(Float.valueOf(fajrtuning), values));
		prayer.getSetting().setDhuhrtuning(resolveTuningValue(Float.valueOf(dhuhrtuning), values));
		prayer.getSetting().setAsrtuning(resolveTuningValue(Float.valueOf(asrtuning), values));
		prayer.getSetting().setMaghribtuning(resolveTuningValue(Float.valueOf(maghribtuning), values));
		prayer.getSetting().setIshatuning(resolveTuningValue(Float.valueOf(ishatuning), values));
		
		try {
			getPrayTime(prayer);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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