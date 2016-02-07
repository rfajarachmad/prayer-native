package net.fajarachmad.prayer.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Location implements Parcelable {
	
	private double latitude = -6.2087634;
	private double longitude = 106.84559899999999;
	private String country = "Indonesia";
	private String city = "South Jakarta";
	private String postalCode;
	private String addressLine;
	
	public Location() {};
	
	public Location (Parcel source){
	      this.city = source.readString();
	      this.country = source.readString();
	      this.postalCode = source.readString();
	      this.addressLine = source.readString();
	      this.latitude = source.readDouble();
	      this.longitude = source.readDouble();
	} 
	
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	public String getAddressLine() {
		return addressLine;
	}
	public void setAddressLine(String addressLine) {
		this.addressLine = addressLine;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		 dest.writeString(this.city);
		 dest.writeString(this.country);
		 dest.writeString(this.postalCode);
		 dest.writeString(this.addressLine);
		 dest.writeDouble(this.latitude);
		 dest.writeDouble(this.longitude);
		
	}
	
	@SuppressWarnings("rawtypes")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
	    public Location createFromParcel(Parcel in) {
	        return new Location(in);
	    }

	    public Location[] newArray(int size) {
	        return new Location[size];
	    }
	};
	
}
