package net.fajarachmad.prayer.activity;

import java.util.ArrayList;
import java.util.List;

import net.fajarachmad.prayer.R;
import net.fajarachmad.prayer.preference.SliderPreference;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public class SettingActivity extends PreferenceActivity {
	
	SharedPreferences sharedPrefs;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		addPreferencesFromResource(R.xml.setting_layout);
		
		sharedPrefs =  PreferenceManager.getDefaultSharedPreferences(this);
		
		List<String> values = new ArrayList<String>();
		
		for (int i = -60; i <= 60; i++) {
			values.add(String.valueOf(i));
		}
		
		((SliderPreference)findPreference("tune_fajr")).setSummary(values.toArray(new String[0]));;
		((SliderPreference)findPreference("tune_dhuhr")).setSummary(values.toArray(new String[0]));;
		((SliderPreference)findPreference("tune_asr")).setSummary(values.toArray(new String[0]));;
		((SliderPreference)findPreference("tune_maghrib")).setSummary(values.toArray(new String[0]));;
		((SliderPreference)findPreference("tune_isha")).setSummary(values.toArray(new String[0]));;
		
		((SliderPreference)findPreference("tune_fajr")).setDialogMessage(((SliderPreference)findPreference("tune_fajr")).getSummary());
		((SliderPreference)findPreference("tune_dhuhr")).setDialogMessage(((SliderPreference)findPreference("tune_dhuhr")).getSummary());
		((SliderPreference)findPreference("tune_asr")).setDialogMessage(((SliderPreference)findPreference("tune_asr")).getSummary());
		((SliderPreference)findPreference("tune_maghrib")).setDialogMessage(((SliderPreference)findPreference("tune_maghrib")).getSummary());
		((SliderPreference)findPreference("tune_isha")).setDialogMessage(((SliderPreference)findPreference("tune_isha")).getSummary());
		
		Preference resetPref = findPreference("tune_reset");
		resetPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@SuppressLint("UseValueOf")
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				((SliderPreference)findPreference("tune_fajr")).setValue(new Float(0.5));
				((SliderPreference)findPreference("tune_dhuhr")).setValue(new Float(0.5));
				((SliderPreference)findPreference("tune_asr")).setValue(new Float(0.5));
				((SliderPreference)findPreference("tune_maghrib")).setValue(new Float(0.5));
				((SliderPreference)findPreference("tune_isha")).setValue(new Float(0.5));
				return false;
			}
		});
		
		findPreference("prefLanguage").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object value) {
				findPreference("prefLanguage").setSummary(getValueByKey(R.array.languageValues, R.array.language, value.toString()));
				((ListPreference)findPreference("prefLanguage")).setValue(value.toString());
				return false;
			}
		});
		
		findPreference("prefCalculationMethod").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object value) {
				findPreference("prefCalculationMethod").setSummary(getValueByKey(R.array.calulationMethodValues, R.array.calulationMethod, value.toString()));
				((ListPreference)findPreference("prefCalculationMethod")).setValue(value.toString());
				return false;
			}
		});
		
		findPreference("prefAsrMethod").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object value) {
				findPreference("prefAsrMethod").setSummary(getValueByKey(R.array.ashMethodValues, R.array.asrMethod, value.toString()));
				((ListPreference)findPreference("prefAsrMethod")).setValue(value.toString());
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

}
