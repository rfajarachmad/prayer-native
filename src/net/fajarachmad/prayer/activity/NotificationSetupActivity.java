package net.fajarachmad.prayer.activity;

import net.fajarachmad.prayer.R;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class NotificationSetupActivity extends PreferenceActivity {
	
	SharedPreferences sharedPrefs;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.notification_setup_layout);
		
		String prayID = getIntent().getParcelableExtra("PrayID");
		
		this.setTitle(prayID+" Notification Setup");
		sharedPrefs =  PreferenceManager.getDefaultSharedPreferences(this);
		
		
	}
	

}
