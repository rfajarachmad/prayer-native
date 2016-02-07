package net.fajarachmad.prayer.model;

import java.util.Date;
import java.util.List;

public class Prayer {
	private Date today = new Date();
	private Location location;
	private PrayerTime currentPrayer;
	private PrayerTime nextPrayer;
	private Setting setting = new Setting();
	private List<PrayerTime> prayerTimes;
	
	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public List<PrayerTime> getPrayerTimes() {
		return prayerTimes;
	}

	public void setPrayerTimes(List<PrayerTime> prayerTimes) {
		this.prayerTimes = prayerTimes;
	}

	public Date getToday() {
		return today;
	}

	public void setToday(Date today) {
		this.today = today;
	}

	public PrayerTime getCurrentPrayer() {
		return currentPrayer;
	}

	public void setCurrentPrayer(PrayerTime currentPrayer) {
		this.currentPrayer = currentPrayer;
	}

	public PrayerTime getNextPrayer() {
		return nextPrayer;
	}

	public void setNextPrayer(PrayerTime nextPrayer) {
		this.nextPrayer = nextPrayer;
	}

	public Setting getSetting() {
		return setting;
	}

	public void setSetting(Setting setting) {
		this.setting = setting;
	}

}
