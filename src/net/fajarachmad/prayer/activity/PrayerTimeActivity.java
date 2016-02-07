package net.fajarachmad.prayer.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
	private int timezone;
	private CountDownTimer timer;
	private NotificationManager notificationManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.prayer_time_layout);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		Location newLocation = getIntent().getParcelableExtra(Location.class.getName());
		prayer = new Prayer();
		notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);;
		
		Location currentLocation;
		if (newLocation == null) {
			currentLocation = getCurrentLocation();
		} else {
			currentLocation = newLocation;
		}

		prayer.setLocation(currentLocation);

		timezone = getTimezone(prayer);
		try {
			getPrayTime(prayer, timezone);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		renderPrayerValue(prayer);
		findViewById(R.id.location_address).setOnClickListener(
				new LocationButtonListener());

	}

	@Override
	public void onBackPressed() {
		finish();
		System.exit(0);
	}

	private void getPrayTime(Prayer prayer, int timezone) throws ParseException {
		List<PrayerTime> prayerTimes = getPrayerTime(
				prayer.getLocation().getLatitude(), prayer.getLocation().getLongitude(),
				timezone);
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

		int timeZoneOffset = 0;
		TimeZone timezone = TimeZone.getDefault();
		timeZoneOffset = timezone.getRawOffset() / (60 * 60 * 1000);
		if (prayer.getLocation().getLatitude() != 0
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

	private Location getCurrentLocation() {
		GPSTracker gpsTracker = new GPSTracker(this);
		Location location = new Location();
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

		return location;
	}

	private List<PrayerTime> getPrayerTime(double latitude, double longitude,
			int timezone) throws ParseException {
		PrayTime prayers = new PrayTime();
		List<PrayerTime> list = new ArrayList<PrayerTime>();
		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		
		prayers.setCalcMethod(prayer.getSetting().getMethod());
		prayers.setAsrJuristic(prayer.getSetting().getAsrMethod());
		
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
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String language = sharedPrefs.getString("prefLanguage", "en");
		int method = Integer.valueOf(sharedPrefs.getString("prefCalculationMethod", "3"));
		int asrMethod = Integer.valueOf(sharedPrefs.getString("prefAsrMethod", "0"));
		boolean disableNotification = sharedPrefs.getBoolean("prefDisabledNotification", false);
		
		prayer.getSetting().setLanguage(language);
		prayer.getSetting().setMethod(method);
		prayer.getSetting().setAsrMethod(asrMethod);
		prayer.getSetting().setDisablednotification(disableNotification);
		
		try {
			getPrayTime(prayer, timezone);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		renderPrayerValue(prayer);
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