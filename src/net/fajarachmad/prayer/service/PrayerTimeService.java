package net.fajarachmad.prayer.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import net.fajarachmad.prayer.R;
import net.fajarachmad.prayer.activity.AppConstant;
import net.fajarachmad.prayer.activity.PrayerTimeActivity;
import net.fajarachmad.prayer.model.Location;
import net.fajarachmad.prayer.model.Prayer;
import net.fajarachmad.prayer.model.PrayerTime;
import net.fajarachmad.prayer.notification.DismissButtonListener;
import net.fajarachmad.prayer.notification.NotificationPublisher;
import net.fajarachmad.prayer.receiver.PrayerTimeReceiver;
import net.fajarachmad.prayer.util.GPSTracker;
import net.fajarachmad.prayer.util.HttpRequestUtil;
import net.fajarachmad.prayer.util.PrayTime;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;

public class PrayerTimeService extends IntentService implements AppConstant {

	private SharedPreferences sharedPrefs;
	private List<String> tuningValues;
	private CountDownTimer timer;
	private AlarmManager alarmManager;
	private static MediaPlayer mediaPlayer;
	
	private Prayer prayer;
	private Gson gson;
	
	public PrayerTimeService() {
		super("PrayerTimeService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		populateTuningValue();
		gson = new Gson();
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		
		String action = intent.getStringExtra(ACTION);
		
		switch (action) {
		case ACTION_GET_PRAYER_TIME:
			populatePreviousOrDefaultValue();
			renderPrayerTimeToView();
			getPrayerTime(intent);
			save();
			break;
		case ACTION_PLAY_SOUND:
			playAlarmSound(intent);
			reloadPrayerTime(intent);
			break;
		case ACTION_STOP_SOUND:
			stopAlarmSound(intent);
		default:
			break;
		}
		
	}
	
	private void reloadPrayerTime(Intent intent) {
		populatePreviousOrDefaultValue();
		getPrayerTime(intent);
		save();
	}
	
	private void renderPrayerTimeToView() {
		String prayerJson = gson.toJson(prayer);
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(PrayerTimeReceiver.PROCESS_RESPONSE);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(ACTION, ACTION_PRAYER_TIME_CHANGED);
		broadcastIntent.putExtra(Prayer.class.getName(), prayerJson);
		sendBroadcast(broadcastIntent);
	}
	
	private void populatePreviousOrDefaultValue() {
		if (prayer == null)
			prayer = new Prayer();
		
		String prayerJson = sharedPrefs.getString(Prayer.class.getName(), null);
		if (prayerJson != null) {
			prayer = gson.fromJson(prayerJson, Prayer.class);
		}
		
	}
	
	private void save() {
		Editor editor = sharedPrefs.edit();
		String prayerJson = gson.toJson(prayer);
		editor.putString(Prayer.class.getName(), prayerJson);
		editor.commit();
	}
	
	private void playAlarmSound(Intent intent) {
		String sound = intent.getStringExtra(NOTIFICATION_SOUND);
		Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+ "://" + getPackageName() + "/raw/"+sound);
		mediaPlayer = MediaPlayer.create(getApplicationContext(), alarmSound);
		mediaPlayer.start();
	}
	
	private void stopAlarmSound(Intent intent) {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
		}
	}
	
	private void getPrayerTime(Intent intent) {
		String newLocationJson = intent.getStringExtra(Location.class.getName());
		Location newLocation = null;
		
		if (newLocationJson != null) {
			newLocation = gson.fromJson(newLocationJson, Location.class);
		}
		
		try {
			Location currentLocation;
			if (newLocation == null) {
				currentLocation = getCurrentLocation();
			} else {
				currentLocation = newLocation;
			}
			
			prayer.getLocation().setTimezone(getTimezone());;
			
			prayer.setLocation(currentLocation);
			List<PrayerTime> prayerTimes = getPrayerTime(prayer.getLocation().getLatitude(), prayer.getLocation().getLongitude(), prayer.getLocation().getTimezone());
			prayer.setPrayerTimes(prayerTimes);
			getCurrentPrayer();
			
			renderPrayerTimeToView();
			
			showRemainingTime();
		} catch (ParseException e1) {
			Log.e("Prayer", e1.getMessage());
		}
	}
	
	private int getTimezone() {

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
	
	private Location getCurrentLocation() {
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
				gpsTracker.showSettingsAlert();
			}
		}
		
		return location;
	}
	
	private void getCurrentPrayer() throws ParseException {
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
			
			
			String dateFromStr = dateOlnyFormat.format(new Date()) + " " + prayTimeFrom + ":00";
			String dateToStr = dateOlnyFormat.format(new Date()) + " " + prayTimeTo + ":00";

			dateFrom = dateTimeFormat.parse(dateFromStr);
			dateTo = dateTimeFormat.parse(dateToStr);

			Log.d("Prayer", prayTimeFrom
					+ "-->" + prayTimeTo
					+ "-->" + dateFrom + "-->" + dateTo);

			if (new Date().compareTo(dateFrom) > 0 && new Date().compareTo(dateTo) < 0) {
				prayer.setCurrentPrayer(new PrayerTime(prayIdFrom,prayNameFrom, prayTimeFrom, dateFrom));
				prayer.setNextPrayer(new PrayerTime(prayIdTo, prayNameTo, prayTimeTo, dateTo));
				solved = true;
				break;
			}
		}

		if (!solved) {

			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
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
	
	private void showRemainingTime() {
		Date current = new Date();
		long deadline = prayer.getNextPrayer().getPrayDate().getTime() - current.getTime();
		
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
				//Looper.myLooper().quit();
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

				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(PrayerTimeReceiver.PROCESS_RESPONSE);
				broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
				broadcastIntent.putExtra(ACTION, ACTION_REMAINING_TIME_CHANGED);
				broadcastIntent.putExtra("remainingTime", text);
				sendBroadcast(broadcastIntent);
			}

		}.start();
		Looper.loop(); 
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
			
			if (!prayId.equals(SUNSET_ID) && !prayId.equals(SUNRISE_ID)) {
				String prayerDateTimeStr = dateOlnyFormat.format(new Date()) + " "+ prayTime + ":00";
				Date prayerDateaTime = dateTimeFormat.parse(prayerDateTimeStr);
				
				list.add(new PrayerTime(prayId, prayName, prayTime, prayerDateaTime));
				
				evaluateNotification(i, prayId, prayName, prayerDateaTime);
			}
		}

		return list;
	}
	
	private void evaluateNotification(int idx, String prayId, String prayName, Date date) {
		cancelNotification(idx);
		cancelNotification(idx+10);
		long delay = date.getTime() - new Date().getTime();
		if (delay > 0) {
			sendPrayAlarm(idx, prayId, prayName, delay);
		}
	}
	
	private void sendPrayAlarm(int id, String prayId, String parayerName, long delay){
		String message = getResources().getString(R.string.notif_on_prayer)+" "+parayerName;
		String title = getResources().getString(R.string.notif_title);
		boolean isNotificationDisable = sharedPrefs.getBoolean(PREF_DISABLED_NOTIFICATION_KEY, false);
		
		switch (prayId) {
		case FAJR_ID:
			cancelNotification(id+20);
			cancelNotification(id+10+20);
			if (sharedPrefs.getBoolean(PREF_FAJR_ONPRAY_ALARM_KEY, false) && !isNotificationDisable) {
				String sound = sharedPrefs.getString(PREF_FAJR_ONPRAY_SOUND_KEY, DEFAULT_SOUND);
				scheduleNotification(getNotification(title, message), id, Long.valueOf(delay).intValue(), sound);
				int delayTomorrow = Long.valueOf(delay).intValue() + 86400000;
				scheduleNotification(getNotification(title, message), id+20, delayTomorrow, sound);
			}

			if (sharedPrefs.getBoolean(PREF_FAJR_BEFOREPRAY_ALARM_KEY, false) && !isNotificationDisable) {
				int beforeTimeMinute = Integer.valueOf(sharedPrefs.getString(PREF_FAJR_BEFOREPRAY_NOTIFY_KEY, "0"));
				long beforeTimeMilis = beforeTimeMinute * 60  * 1000;
				long newDelay = delay - beforeTimeMilis;
				if (newDelay > 0) {
					String sound = sharedPrefs.getString(PREF_FAJR_BEFOREPRAY_SOUND_KEY, DEFAULT_SOUND);
					message = beforeTimeMinute+" "+getResources().getString(R.string.notif_before_pray)+" "+parayerName;
					scheduleNotification(getNotification(title, message), id+10, Long.valueOf(newDelay).intValue(), sound);
					int delayTomorrow = Long.valueOf(newDelay).intValue() + 86400000;
					scheduleNotification(getNotification(title, message), id+10+20, delayTomorrow, sound);
				}
				
			}
			break;
		case DHUHR_ID:
			if (sharedPrefs.getBoolean(PREF_DHUHR_ONPRAY_ALARM_KEY, false) && !isNotificationDisable) {
				String sound = sharedPrefs.getString(PREF_DHUHR_ONPRAY_SOUND_KEY, DEFAULT_SOUND);
				scheduleNotification(getNotification(title, message), id, Long.valueOf(delay).intValue(), sound);
			} 
			if (sharedPrefs.getBoolean(PREF_DHUHR_BEFOREPRAY_ALARM_KEY, false) && !isNotificationDisable) {
				int beforeTimeMinute = Integer.valueOf(sharedPrefs.getString(PREF_DHUHR_BEFOREPRAY_NOTIFY_KEY, "0"));
				long beforeTimeMilis = beforeTimeMinute * 60  * 1000;
				long newDelay = delay - beforeTimeMilis;
				if (newDelay > 0) {
					String sound = sharedPrefs.getString(PREF_DHUHR_BEFOREPRAY_SOUND_KEY, DEFAULT_SOUND);
					message = beforeTimeMinute+" "+getResources().getString(R.string.notif_before_pray)+" "+parayerName;
					scheduleNotification(getNotification(title, message), id+10, Long.valueOf(newDelay).intValue(), sound);
				}
			} 
			break;
		case ASR_ID:
			if (sharedPrefs.getBoolean(PREF_ASR_ONPRAY_ALARM_KEY, false) && !isNotificationDisable) {
				String sound = sharedPrefs.getString(PREF_ASR_ONPRAY_SOUND_KEY, DEFAULT_SOUND);
				scheduleNotification(getNotification(title, message), id, Long.valueOf(delay).intValue(), sound);
			} 
			if (sharedPrefs.getBoolean(PREF_ASR_BEFOREPRAY_ALARM_KEY, false) && !isNotificationDisable) {
				int beforeTimeMinute = Integer.valueOf(sharedPrefs.getString(PREF_ASR_BEFOREPRAY_NOTIFY_KEY, "0"));
				long beforeTimeMilis = beforeTimeMinute * 60  * 1000;
				long newDelay = delay - beforeTimeMilis;
				if (newDelay > 0) {
					String sound = sharedPrefs.getString(PREF_ASR_BEFOREPRAY_SOUND_KEY, DEFAULT_SOUND);
					message = beforeTimeMinute+" "+getResources().getString(R.string.notif_before_pray)+" "+parayerName;
					scheduleNotification(getNotification(title, message), id+10, Long.valueOf(newDelay).intValue(), sound);
				}
				
			}
			break;
		case MAGHRIB_ID:
			if (sharedPrefs.getBoolean(PREF_MAGHRIB_ONPRAY_ALARM_KEY, false) && !isNotificationDisable) {
				String sound = sharedPrefs.getString(PREF_MAGHRIB_ONPRAY_SOUND_KEY, DEFAULT_SOUND);
				scheduleNotification(getNotification(title, message), id, Long.valueOf(delay).intValue(), sound);
			}
			if (sharedPrefs.getBoolean(PREF_MAGHRIB_BEFOREPRAY_ALARM_KEY, false) && !isNotificationDisable) {
				int beforeTimeMinute = Integer.valueOf(sharedPrefs.getString(PREF_MAGHRIB_BEFOREPRAY_NOTIFY_KEY, "0"));
				long beforeTimeMilis = beforeTimeMinute * 60  * 1000;
				long newDelay = delay - beforeTimeMilis;
				if (newDelay > 0) {
					String sound = sharedPrefs.getString(PREF_MAGHRIB_BEFOREPRAY_SOUND_KEY, DEFAULT_SOUND);
					message = beforeTimeMinute+" "+getResources().getString(R.string.notif_before_pray)+" "+parayerName;
					scheduleNotification(getNotification(title, message), id+10, Long.valueOf(newDelay).intValue(), sound);
				}
			} 
			break;
		case ISHA_ID:
			if (sharedPrefs.getBoolean(PREF_ISHA_ONPRAY_ALARM_KEY, false) && !isNotificationDisable) {
				String sound = sharedPrefs.getString(PREF_ISHA_ONPRAY_SOUND_KEY, DEFAULT_SOUND);
				scheduleNotification(getNotification(title, message), id, Long.valueOf(delay).intValue(), sound);
			}
			if (sharedPrefs.getBoolean(PREF_ISHA_BEFOREPRAY_ALARM_KEY, false) && !isNotificationDisable) {
				int beforeTimeMinute = Integer.valueOf(sharedPrefs.getString(PREF_ISHA_BEFOREPRAY_NOTIFY_KEY, "0"));
				long beforeTimeMilis = beforeTimeMinute * 60  * 1000;
				long newDelay = delay - beforeTimeMilis;
				if (newDelay > 0) {
					String sound = sharedPrefs.getString(PREF_ISHA_BEFOREPRAY_SOUND_KEY, DEFAULT_SOUND);
					message = beforeTimeMinute+" "+getResources().getString(R.string.notif_before_pray)+" "+parayerName;
					scheduleNotification(getNotification(title, message), id+10, Long.valueOf(newDelay).intValue(), sound);
				}
			} 
			break;
		default:
			break;
		}
		
	}
	
	private void scheduleNotification(Notification notification, int id, int delay, String sound) {

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, id);
        notificationIntent.putExtra(NOTIFICATION_SOUND, sound);
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

    private Notification getNotification(String title, String content) {
    	
    	Intent notificationIntent = new Intent(this, PrayerTimeActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        
        Intent dismissIntent = new Intent(this, DismissButtonListener.class);
        PendingIntent pendingButtonIntent = PendingIntent.getBroadcast(this, 0,  dismissIntent, 0);
        
    	NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setAutoCancel(false);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentIntent(contentIntent);
        builder.addAction(R.drawable.turn_notifications_off_button, getResources().getString(R.string.notif_dismiss), pendingButtonIntent);
        return builder.build();
    }
	
	private void populateTuningValue () {
		tuningValues = new ArrayList<String>();
		for (int i = -60; i <= 60; i++) {
			tuningValues.add(String.valueOf(i));
		}
	}
	
	private int resolveTuningValue(float value, List<String> values) {
		int index = (int) (value * values.toArray(new String[0]).length);
		index = Math.min(index, values.toArray(new String[0]).length - 1);
		return Integer.valueOf(values.toArray(new String[0])[index]);
		
	}

}
