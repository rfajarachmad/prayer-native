package net.fajarachmad.prayer.model;

public class Setting {

	private String language = "en";
	private int method = 3;
	private int asrMethod = 0;
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

}
