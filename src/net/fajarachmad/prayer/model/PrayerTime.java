package net.fajarachmad.prayer.model;

import java.util.Date;

public class PrayerTime {
	
	private String prayName;
	private String prayTime;
	private Date prayDate;
	
	public PrayerTime() {};
	
	public PrayerTime(String prayName, String prayTime) {
		this.prayName = prayName;
		this.prayTime = prayTime;
	}
	
	public PrayerTime(String prayName, String prayTime, Date prayDate) {
		this.prayName = prayName;
		this.prayTime = prayTime;
		this.prayDate = prayDate;
	}
	
	public String getPrayName() {
		return prayName;
	}
	public void setPrayName(String prayName) {
		this.prayName = prayName;
	}
	public String getPrayTime() {
		return prayTime;
	}
	public void setPrayTime(String prayTime) {
		this.prayTime = prayTime;
	}
	public Date getPrayDate() {
		return prayDate;
	}
	public void setPrayDate(Date prayDate) {
		this.prayDate = prayDate;
	}
	
	
	

}
