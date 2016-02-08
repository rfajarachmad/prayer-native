package net.fajarachmad.prayer.model;

public class Setting {

	private String language = "en";
	private int method = 3;
	private int asrMethod = 0;
	private int fajrtuning = 0;
	private int dhuhrtuning = 0;
	private int asrtuning = 0;
	private int maghribtuning = 0;
	private int ishatuning = 0;
	private boolean disablednotification = false;

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public int getMethod() {
		return method;
	}

	public void setMethod(int method) {
		this.method = method;
	}

	public int getAsrMethod() {
		return asrMethod;
	}

	public void setAsrMethod(int asrMethod) {
		this.asrMethod = asrMethod;
	}

	public boolean isDisablednotification() {
		return disablednotification;
	}

	public void setDisablednotification(boolean disablednotification) {
		this.disablednotification = disablednotification;
	}

	public int getFajrtuning() {
		return fajrtuning;
	}

	public void setFajrtuning(int fajrtuning) {
		this.fajrtuning = fajrtuning;
	}

	public int getDhuhrtuning() {
		return dhuhrtuning;
	}

	public void setDhuhrtuning(int dhuhrtuning) {
		this.dhuhrtuning = dhuhrtuning;
	}

	public int getAsrtuning() {
		return asrtuning;
	}

	public void setAsrtuning(int asrtuning) {
		this.asrtuning = asrtuning;
	}

	public int getMaghribtuning() {
		return maghribtuning;
	}

	public void setMaghribtuning(int maghribtuning) {
		this.maghribtuning = maghribtuning;
	}

	public int getIshatuning() {
		return ishatuning;
	}

	public void setIshatuning(int ishatuning) {
		this.ishatuning = ishatuning;
	}
	
}
