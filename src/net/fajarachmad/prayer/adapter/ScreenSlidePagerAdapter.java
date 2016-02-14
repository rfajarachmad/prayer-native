package net.fajarachmad.prayer.adapter;

import java.util.HashMap;
import java.util.Map;

import net.fajarachmad.prayer.fragment.PrayerInfoFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter{
	
	private static final int NUM_PAGES = 2;
	
	private String nextPrayerName;
	private String nextPrayerTime;
	private String remainigTime;
	private String upcomingPray;
	
	private Map<Integer, Fragment> fragmentMap = new HashMap<Integer, Fragment>();
	
	public ScreenSlidePagerAdapter(FragmentManager fm) {
		super(fm);
	}
	
	@Override
    public Fragment getItem(int position) {
        PrayerInfoFragment fragment = new PrayerInfoFragment();
        Bundle args = new Bundle();
        args.putString("nextPrayerName", nextPrayerName);
        args.putString("nextPrayerTime", nextPrayerTime);
        args.putString("remainingTime", remainigTime);
        args.putString("upcomingPray", upcomingPray);
        fragment.setArguments(args);
        fragmentMap.put(position, fragment);
        return fragment;
	}

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
    
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    	super.destroyItem(container, position, object);
    	fragmentMap.remove(position);
    }
    
    @Override
    public Object instantiateItem(ViewGroup content, int position) {
    	Fragment fragment = (PrayerInfoFragment)super.instantiateItem(content, position);
    	fragmentMap.put(position, fragment);
    	return fragment;
    }
    
    public Fragment getFragment(int position){
    	return fragmentMap.get(position);
    }
    
	public void setNextPrayerName(String nextPrayerName) {
		this.nextPrayerName = nextPrayerName;
	}

	public void setNextPrayerTime(String nextPrayerTime) {
		this.nextPrayerTime = nextPrayerTime;
	}

	public void setRemainigTime(String remainigTime) {
		this.remainigTime = remainigTime;
	}

	public void setUpcomingPray(String upcomingPray) {
		this.upcomingPray = upcomingPray;
	}
    
}
