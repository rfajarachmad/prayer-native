package net.fajarachmad.prayer.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.fajarachmad.prayer.R;
import net.fajarachmad.prayer.preference.SliderPreference;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public class SettingActivity extends PreferenceActivity implements AppConstant {
	
	SharedPreferences sharedPrefs;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setTitle(R.string.menu_settings);
		addPreferencesFromResource(R.xml.setting_layout);
		
		sharedPrefs =  PreferenceManager.getDefaultSharedPreferences(this);
		
		List<String> values = new ArrayList<String>();
		
		for (int i = -60; i <= 60; i++) {
			values.add(String.valueOf(i));
		}
		
		((SliderPreference)findPreference(PREF_TUNE_FAJR_KEY)).setSummary(values.toArray(new String[0]));;
		((SliderPreference)findPreference(PREF_TUNE_DHUHR_KEY)).setSummary(values.toArray(new String[0]));;
		((SliderPreference)findPreference(PREF_TUNE_ASR_KEY)).setSummary(values.toArray(new String[0]));;
		((SliderPreference)findPreference(PREF_TUNE_MAGHRIB_KEY)).setSummary(values.toArray(new String[0]));;
		((SliderPreference)findPreference(PREF_TUNE_ISHA_KEY)).setSummary(values.toArray(new String[0]));;
		
		((SliderPreference)findPreference(PREF_TUNE_FAJR_KEY)).setDialogMessage(((SliderPreference)findPreference(PREF_TUNE_FAJR_KEY)).getSummary());
		((SliderPreference)findPreference(PREF_TUNE_DHUHR_KEY)).setDialogMessage(((SliderPreference)findPreference(PREF_TUNE_DHUHR_KEY)).getSummary());
		((SliderPreference)findPreference(PREF_TUNE_ASR_KEY)).setDialogMessage(((SliderPreference)findPreference(PREF_TUNE_ASR_KEY)).getSummary());
		((SliderPreference)findPreference(PREF_TUNE_MAGHRIB_KEY)).setDialogMessage(((SliderPreference)findPreference(PREF_TUNE_MAGHRIB_KEY)).getSummary());
		((SliderPreference)findPreference(PREF_TUNE_ISHA_KEY)).setDialogMessage(((SliderPreference)findPreference(PREF_TUNE_ISHA_KEY)).getSummary());
		
		Preference resetPref = findPreference(PREF_TUNE_RESET_KEY);
		resetPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@SuppressLint("UseValueOf")
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				((SliderPreference)findPreference(PREF_TUNE_FAJR_KEY)).setValue(DEFAULT_MANUAL_TUNE);
				((SliderPreference)findPreference(PREF_TUNE_DHUHR_KEY)).setValue(DEFAULT_MANUAL_TUNE);
				((SliderPreference)findPreference(PREF_TUNE_ASR_KEY)).setValue(DEFAULT_MANUAL_TUNE);
				((SliderPreference)findPreference(PREF_TUNE_MAGHRIB_KEY)).setValue(DEFAULT_MANUAL_TUNE);
				((SliderPreference)findPreference(PREF_TUNE_ISHA_KEY)).setValue(DEFAULT_MANUAL_TUNE);
				return false;
			}
		});
		
		findPreference(PREF_LANGUAGE_KEY).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object value) {
				findPreference(PREF_LANGUAGE_KEY).setSummary(getValueByKey(R.array.languageValues, R.array.language, value.toString()));
				((ListPreference)findPreference(PREF_LANGUAGE_KEY)).setValue(value.toString());
			     setLocale(value.toString());
			     Intent intent = new Intent(SettingActivity.this, SettingActivity.class);
			     intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			     startActivity(intent);
			
				return false;
			}
		});
		
		findPreference(PREF_CALULATION_METHOD_KEY).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object value) {
				findPreference(PREF_CALULATION_METHOD_KEY).setSummary(getValueByKey(R.array.calulationMethodValues, R.array.calulationMethod, value.toString()));
				((ListPreference)findPreference(PREF_CALULATION_METHOD_KEY)).setValue(value.toString());
				return false;
			}
		});
		
		findPreference(PREF_ASR_METHOD_KEY).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object value) {
				findPreference(PREF_ASR_METHOD_KEY).setSummary(getValueByKey(R.array.ashMethodValues, R.array.asrMethod, value.toString()));
				((ListPreference)findPreference(PREF_ASR_METHOD_KEY)).setValue(value.toString());
				return false;
			}
		});
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        finish();
	    return true;
	}
	
	private String getValueByKey(int id, int valueId, String key) {
		int i = -1;
        for (String cc: getResources().getStringArray(id)) {
            i++;
            if (cc.equals(key))
                break;
        }
        return getResources().getStringArray(valueId)[i];
	}
	
	private void setLocale(String lang) {
		Locale locale = new Locale(lang); 
	    Locale.setDefault(locale);
	    Configuration config = new Configuration();
	    config.locale = locale;
	    getApplicationContext().getResources().updateConfiguration(config,getApplicationContext().getResources().getDisplayMetrics());
	}

}
