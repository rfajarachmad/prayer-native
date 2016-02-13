package net.fajarachmad.prayer.activity;

import net.fajarachmad.prayer.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public class NotificationSetupActivity extends PreferenceActivity implements AppConstant {
	
	SharedPreferences sharedPrefs;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		String prayID = getIntent().getStringExtra("PrayID");
		String prayName = getIntent().getStringExtra("PrayName");
		String title = getResources().getString(R.string.notification_setup_title, prayName);
		
		this.setTitle(title);
		sharedPrefs =  PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = sharedPrefs.edit();
		editor.putString("NotifPrayId", prayID);
		
		boolean onPrayAlarm = false;
		String onPraySound = DEFAULT_SOUND;
		boolean beforePrayAlrm = false;
		String beforePrayNotif = DEFAULT_NOTIFY_TIME;
		String beforePraySound = DEFAULT_SOUND;
		
		switch (prayID) {
		case FAJR_ID:
			onPrayAlarm = sharedPrefs.getBoolean(PREF_FAJR_BEFOREPRAY_ALARM_KEY, true);
			onPraySound = sharedPrefs.getString(PREF_FAJR_ONPRAY_SOUND_KEY, DEFAULT_SOUND);
			beforePrayAlrm = sharedPrefs.getBoolean(PREF_FAJR_BEFOREPRAY_ALARM_KEY, true);
			beforePrayNotif = sharedPrefs.getString(PREF_FAJR_BEFOREPRAY_NOTIFY_KEY, DEFAULT_NOTIFY_TIME);
			beforePraySound = sharedPrefs.getString(PREF_FAJR_BEFOREPRAY_SOUND_KEY, DEFAULT_SOUND);
			break;
		case DHUHR_ID:
			onPrayAlarm = sharedPrefs.getBoolean(PREF_DHUHR_BEFOREPRAY_ALARM_KEY, true);
			onPraySound = sharedPrefs.getString(PREF_DHUHR_ONPRAY_SOUND_KEY, DEFAULT_SOUND);
			beforePrayAlrm = sharedPrefs.getBoolean(PREF_DHUHR_BEFOREPRAY_ALARM_KEY, true);
			beforePrayNotif = sharedPrefs.getString(PREF_DHUHR_BEFOREPRAY_NOTIFY_KEY, DEFAULT_NOTIFY_TIME);
			beforePraySound = sharedPrefs.getString(PREF_DHUHR_BEFOREPRAY_SOUND_KEY, DEFAULT_SOUND);
			break;
		case ASR_ID:
			onPrayAlarm = sharedPrefs.getBoolean(PREF_ASR_BEFOREPRAY_ALARM_KEY, true);
			onPraySound = sharedPrefs.getString(PREF_ASR_ONPRAY_SOUND_KEY, DEFAULT_SOUND);
			beforePrayAlrm = sharedPrefs.getBoolean(PREF_ASR_BEFOREPRAY_ALARM_KEY, true);
			beforePrayNotif = sharedPrefs.getString(PREF_ASR_BEFOREPRAY_NOTIFY_KEY, DEFAULT_NOTIFY_TIME);
			beforePraySound = sharedPrefs.getString(PREF_ASR_BEFOREPRAY_SOUND_KEY, DEFAULT_SOUND);
			break;
		case MAGHRIB_ID:
			onPrayAlarm = sharedPrefs.getBoolean(PREF_MAGHRIB_BEFOREPRAY_ALARM_KEY, true);
			onPraySound = sharedPrefs.getString(PREF_MAGHRIB_ONPRAY_SOUND_KEY, DEFAULT_SOUND);
			beforePrayAlrm = sharedPrefs.getBoolean(PREF_MAGHRIB_BEFOREPRAY_ALARM_KEY, true);
			beforePrayNotif = sharedPrefs.getString(PREF_MAGHRIB_BEFOREPRAY_NOTIFY_KEY, DEFAULT_NOTIFY_TIME);
			beforePraySound = sharedPrefs.getString(PREF_MAGHRIB_BEFOREPRAY_SOUND_KEY, DEFAULT_SOUND);
			break;
		case ISHA_ID:
			onPrayAlarm = sharedPrefs.getBoolean(PREF_ISHA_BEFOREPRAY_ALARM_KEY, true);
			onPraySound = sharedPrefs.getString(PREF_ISHA_ONPRAY_SOUND_KEY, DEFAULT_SOUND);
			beforePrayAlrm = sharedPrefs.getBoolean(PREF_ISHA_BEFOREPRAY_ALARM_KEY, true);
			beforePrayNotif = sharedPrefs.getString(PREF_ISHA_BEFOREPRAY_NOTIFY_KEY, DEFAULT_NOTIFY_TIME);
			beforePraySound = sharedPrefs.getString(PREF_ISHA_BEFOREPRAY_SOUND_KEY, DEFAULT_SOUND);
			break;
		default:
			break;
		}
		
		editor.putBoolean(PREF_ONPRAY_ALARM_KEY, onPrayAlarm);
		editor.putString(PREF_ONPRAY_SOUND_KEY, onPraySound);
		editor.putBoolean(PREF_BEFOREPRAY_ALARM_KEY, beforePrayAlrm);
		editor.putString(PREF_BEFOREPRAY_NOTIFY_KEY, beforePrayNotif);
		editor.putString(PREF_BEFOREPRAY_SOUND_KEY, beforePraySound);
		
		editor.commit();
		
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.notification_setup_layout);
		
		findPreference(PREF_ONPRAY_SOUND_KEY).setEnabled(onPrayAlarm);
		findPreference(PREF_BEFOREPRAY_SOUND_KEY).setEnabled(beforePrayAlrm);
		findPreference(PREF_BEFOREPRAY_NOTIFY_KEY).setEnabled(beforePrayAlrm);
		
		setListener();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		finish();
		return true;
	}
	
	@SuppressWarnings("deprecation")
	private void setListener() {
		findPreference(PREF_ONPRAY_ALARM_KEY).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object object) {
				findPreference(PREF_ONPRAY_SOUND_KEY).setEnabled((boolean)object);
				return true;
			}
		});
		
		findPreference(PREF_BEFOREPRAY_ALARM_KEY).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object object) {
				findPreference(PREF_BEFOREPRAY_SOUND_KEY).setEnabled((boolean)object);
				findPreference(PREF_BEFOREPRAY_NOTIFY_KEY).setEnabled((boolean)object);
				return true;
			}
		});
		
		findPreference(PREF_ONPRAY_SOUND_KEY).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				findPreference(PREF_ONPRAY_SOUND_KEY).setSummary(getValueByKey(R.array.soundValues, R.array.sounds, newValue.toString()));
				((ListPreference)findPreference(PREF_ONPRAY_SOUND_KEY)).setValue(newValue.toString());
				return true;
			}
		});
		
		findPreference(PREF_BEFOREPRAY_SOUND_KEY).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				findPreference(PREF_BEFOREPRAY_SOUND_KEY).setSummary(getValueByKey(R.array.soundValues, R.array.sounds, newValue.toString()));
				((ListPreference)findPreference(PREF_BEFOREPRAY_SOUND_KEY)).setValue(newValue.toString());
				return true;
			}
		});
		
		findPreference(PREF_BEFOREPRAY_NOTIFY_KEY).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				findPreference(PREF_BEFOREPRAY_NOTIFY_KEY).setSummary(getValueByKey(R.array.notifyBeforeValue, R.array.notifyBefore, newValue.toString()));
				((ListPreference)findPreference(PREF_BEFOREPRAY_NOTIFY_KEY)).setValue(newValue.toString());
				return true;
			}
		});
	};
	
	private String getValueByKey(int id, int valueId, String key) {
		int i = -1;
        for (String cc: getResources().getStringArray(id)) {
            i++;
            if (cc.equals(key))
                break;
        }
        return getResources().getStringArray(valueId)[i];
	}
	
}
