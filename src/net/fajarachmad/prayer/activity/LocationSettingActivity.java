package net.fajarachmad.prayer.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import net.fajarachmad.prayer.R;
import net.fajarachmad.prayer.model.Location;
import net.fajarachmad.prayer.util.GPSTracker;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class LocationSettingActivity extends Activity {
	
	// List view
    private ListView lv;
     
    // Listview Adapter
    LocationAdapter adapter;
     
    // Search EditText
    EditText inputSearch;
    
    Geocoder geocoder;
     
     
    // ArrayList for Listview
    ArrayList<HashMap<String, String>> productList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_setting_layout);
		
		// Listview Data
         
        lv = (ListView) findViewById(R.id.list_view);
        inputSearch = (EditText) findViewById(R.id.inputSearch);
        
        geocoder = new Geocoder(this, new Locale("id"));
        // Adding items to listview
        /*adapter = new ArrayAdapter<String>(this, R.layout.list_location_layout, R.id.product_name, products);
        lv.setAdapter(adapter);*/   
        
        /**
         * Enabling Search Filter
         * */
        inputSearch.addTextChangedListener(new TextWatcher() {
             
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
            	if (cs.length() > 3) {
            		try {
    					List<Address> addresses = geocoder.getFromLocationName(cs.toString(), 50);
    					List<Location> locations = new ArrayList<Location>();
    					for (Address address : addresses) {
    						Location location = new Location();
    						location.setAddressLine(address.getAddressLine(0));
    						location.setCity(address.getFeatureName());
    						location.setCountry(address.getCountryName());
    						location.setPostalCode(address.getPostalCode());
    						location.setLatitude(address.getLatitude());
    						location.setLongitude(address.getLongitude());
    						locations.add(location);
    					}
    					
    					adapter = new LocationAdapter(LocationSettingActivity.this, R.layout.list_location_layout, R.id.location_name, locations);
    		            lv.setAdapter(adapter);
    				} catch (IOException e) {
    					Log.e("Prayer", e.getMessage());
    				}
            	}
                
            }
             
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                    int arg3) {
                // TODO Auto-generated method stub
                 
            }
             
            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub                          
            }
        });
        
        lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Location selected = (Location) parent.getItemAtPosition(position);
				Intent intent = new Intent(LocationSettingActivity.this, PrayerTimeActivity.class);
				intent.putExtra(Location.class.getName(), selected);
                startActivity(intent);
			}
        	
        });
        
        
        findViewById(R.id.btn_current_location).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				GPSTracker gpsTracker = new GPSTracker(LocationSettingActivity.this);
		        if (gpsTracker.getIsGPSTrackingEnabled())
		        {
		            double latitude = gpsTracker.getLatitude();
		            double longitude = gpsTracker.getLongitude();
		            String country = gpsTracker.getCountryName(LocationSettingActivity.this);
		            String city = gpsTracker.getLocality(LocationSettingActivity.this);
		            String postalCode = gpsTracker.getPostalCode(LocationSettingActivity.this);
		            String addressLine = gpsTracker.getAddressLine(LocationSettingActivity.this);
		            
		            Location location = new Location();
		            location.setLatitude(latitude);
		            location.setLongitude(longitude);
		            location.setCountry(country);
		            location.setCity(city);
		            location.setAddressLine(addressLine);
		            location.setPostalCode(postalCode);
		            
		            Log.i("Prayer", "Latitude: "+latitude);
		            Log.i("Prayer", "Longitude: "+ longitude);
		            Log.i("Prayer", "Country: "+ country);
		            Log.i("Prayer", "City: "+ city);		            
		            
		            List<Location> locations = new ArrayList<Location>();
		            locations.add(location);
		            
		            adapter = new LocationAdapter(LocationSettingActivity.this, R.layout.list_location_layout, R.id.location_name, locations);
		            lv.setAdapter(adapter);
		            
		        }
		        else
		        {
		            // can't get location
		            // GPS or Network is not enabled
		            // Ask user to enable GPS/network in settings
		            gpsTracker.showSettingsAlert();
		        }
				
			}
		});
	}
	
	public class LocationAdapter extends ArrayAdapter<Location> {
		
		private List<Location> locations;
		
		public LocationAdapter(Context context, int resource,
				int textViewResourceId, List<Location> objects) {
			super(context, resource, textViewResourceId, objects);
			locations = objects;
		}
		
		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.list_location_layout, null);
            }
			Location location = locations.get(position);
			if (location != null) {
				((TextView)view.findViewById(R.id.location_name)).setText(location.getCity()+", "+location.getCountry());
			}
			return view;
		}
	}
	
}
	