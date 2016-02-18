package net.fajarachmad.prayer.activity;

public interface AppConstant {
	
	public static String GOOGLE_TIMEZONE_API = "https://maps.googleapis.com/maps/api/timezone/json?";
	public static String API_KEY = "AIzaSyAZVavLEgDEwXa-iOwRu_hmnco7X-YbNBI";
	
	public static int NOTIFICATION_SETTING_ID = 2;
	public static int APP_SETTING_ID = 1;
	
	public static String ACTION = "action";
	public static String ACTION_PLAY_SOUND = "net.fajarachmad.prayer.play_sound";
	public static String ACTION_STOP_SOUND = "net.fajarachmad.prayer.stop_sound";
	public static String ACTION_GET_PRAYER_TIME = "net.fajarachmad.prayer.get_prayer_time";
	public static String ACTION_PRAYER_TIME_CHANGED = "net.fajarachmad.prayer.prayer_time_changed";
	public static String ACTION_REMAINING_TIME_CHANGED = "net.fajarachmad.prayer.remaining_time_changed";
	
	public static String NOTIFICATION_SOUND = "notificationSound";
	
	public static String FAJR_ID = "fajr";
	public static String ASR_ID = "asr";
	public static String DHUHR_ID = "dhuhr";
	public static String MAGHRIB_ID = "maghrib";
	public static String ISHA_ID = "isha";
	public static String SUNSET_ID = "sunset";
	
	public static String DEFAULT_SOUND = "azan1";
	public static String DEFAULT_NOTIFY_TIME = "5";
	public static String DEFAULT_LANGUAGE = "en";
	public static String DEFAULT_CALC_METHOD = "3";
	public static String DEFAULT_ASR_METHOD = "0";
	public static float DEFAULT_MANUAL_TUNE = (float) 0.5;
	public static double DEFAULT_LATITUDE = -6.2087634;
	public static double DEFAULT_LONGITUDE = 106.84559899999999;
	public static int DEFAULT_TIMEZONE = 7;
	
	public static String PREF_LANGUAGE_KEY = "prefLanguage";
	public static String PREF_CALULATION_METHOD_KEY = "prefCalculationMethod";
	public static String PREF_ASR_METHOD_KEY = "prefAsrMethod";
	public static String PREF_DISABLED_NOTIFICATION_KEY = "prefDisabledNotification";
	public static String PREF_AUTODETECT_LOCATION_KEY = "prefAutoDetectLocation";
	
	public static String PREF_TUNE_FAJR_KEY = "tune_fajr";
	public static String PREF_TUNE_DHUHR_KEY = "tune_dhuhr";
	public static String PREF_TUNE_ASR_KEY = "tune_asr";
	public static String PREF_TUNE_MAGHRIB_KEY = "tune_maghrib";
	public static String PREF_TUNE_ISHA_KEY = "tune_isha";
	public static String PREF_TUNE_RESET_KEY = "tune_reset";
	
	public static String PREF_ONPRAY_ALARM_KEY = "prefOnPrayAlarm";
	public static String PREF_ONPRAY_SOUND_KEY = "prefSoundOnPray";
	public static String PREF_BEFOREPRAY_ALARM_KEY = "prefBeforePrayAlarm";
	public static String PREF_BEFOREPRAY_NOTIFY_KEY = "prefNotifyBefore";
	public static String PREF_BEFOREPRAY_SOUND_KEY = "prefSoundBeforePray";
	
	public static String PREF_FAJR_ONPRAY_ALARM_KEY = "prefOnPrayAlarmFajr";
	public static String PREF_FAJR_ONPRAY_SOUND_KEY = "prefSoundOnPrayFajr";
	public static String PREF_FAJR_BEFOREPRAY_ALARM_KEY = "prefBeforePrayAlarmFajr";
	public static String PREF_FAJR_BEFOREPRAY_NOTIFY_KEY = "prefNotifyBeforeFajr";
	public static String PREF_FAJR_BEFOREPRAY_SOUND_KEY = "prefSoundBeforePrayFajr";
	
	public static String PREF_DHUHR_ONPRAY_ALARM_KEY = "prefOnPrayAlarmDhuhr";
	public static String PREF_DHUHR_ONPRAY_SOUND_KEY = "prefSoundOnPrayDhuhr";
	public static String PREF_DHUHR_BEFOREPRAY_ALARM_KEY = "prefBeforePrayAlarmDhuhr";
	public static String PREF_DHUHR_BEFOREPRAY_NOTIFY_KEY = "prefNotifyBeforeDhuhr";
	public static String PREF_DHUHR_BEFOREPRAY_SOUND_KEY = "prefSoundBeforePrayDhuhr";
	
	public static String PREF_ASR_ONPRAY_ALARM_KEY = "prefOnPrayAlarmAsr";
	public static String PREF_ASR_ONPRAY_SOUND_KEY = "prefSoundOnPrayAsr";
	public static String PREF_ASR_BEFOREPRAY_ALARM_KEY = "prefBeforePrayAlarmAsr";
	public static String PREF_ASR_BEFOREPRAY_NOTIFY_KEY = "prefNotifyBeforeAsr";
	public static String PREF_ASR_BEFOREPRAY_SOUND_KEY = "prefSoundBeforePrayAsr";
	
	public static String PREF_MAGHRIB_ONPRAY_ALARM_KEY = "prefOnPrayAlarmMaghrib";
	public static String PREF_MAGHRIB_ONPRAY_SOUND_KEY = "prefSoundOnPrayMaghrib";
	public static String PREF_MAGHRIB_BEFOREPRAY_ALARM_KEY = "prefBeforePrayAlarmMaghrib";
	public static String PREF_MAGHRIB_BEFOREPRAY_NOTIFY_KEY = "prefNotifyBeforeMaghrib";
	public static String PREF_MAGHRIB_BEFOREPRAY_SOUND_KEY = "prefSoundBeforePrayMaghrib";
	
	public static String PREF_ISHA_ONPRAY_ALARM_KEY = "prefOnPrayAlarmIsha";
	public static String PREF_ISHA_ONPRAY_SOUND_KEY = "prefSoundOnPrayIsha";
	public static String PREF_ISHA_BEFOREPRAY_ALARM_KEY = "prefBeforePrayAlarmIsha";
	public static String PREF_ISHA_BEFOREPRAY_NOTIFY_KEY = "prefNotifyBeforeIsha";
	public static String PREF_ISHA_BEFOREPRAY_SOUND_KEY = "prefSoundBeforePrayIsha";
	
}
