package net.fajarachmad.prayer.fragment;

import net.fajarachmad.prayer.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PrayerInfoFragment extends Fragment {
	
	private String nextPrayerName;
	private String nextPrayerTime;
	private String remainigTime;
	private String upcomingPray;
	
	private View view;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		nextPrayerName = getArguments().getString("nextPrayerName");
		nextPrayerTime = getArguments().getString("nextPrayerTime");
		remainigTime = getArguments().getString("remainingTime");
		upcomingPray = getArguments().getString("upcomingPray");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.prayer_info_fragment, container, false);
		
		((TextView) view.findViewById(R.id.next_prayer)).setText(nextPrayerName);
		((TextView) view.findViewById(R.id.next_pray_time)).setText(nextPrayerTime);
		((TextView) view.findViewById(R.id.remaining_time)).setText(remainigTime);
		((TextView) view.findViewById(R.id.upcoming_prayer)).setText(upcomingPray);
		return view;
	}
	
	public void updateValue(String nextPrayerName, String nextPrayerTime,  String upcomingPray) {
		((TextView) view.findViewById(R.id.next_prayer)).setText(nextPrayerName);
		((TextView) view.findViewById(R.id.next_pray_time)).setText(nextPrayerTime);
		((TextView) view.findViewById(R.id.upcoming_prayer)).setText(upcomingPray);
	}
	
	public void updateRemainingTime(String value) {
		((TextView) view.findViewById(R.id.remaining_time)).setText(value);
	}
	
}
