package org.ShinRH.android.mocklocation.googlePlace;


import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;


public class GeocoderAPI {
	
	private static final String TAG = GeocoderAPI.class.getName();
	private static Geocoder mgeocoder;
	private Context mContext;
	private int MAXRESULT = 10;
	private static Map<Locale, Geocoder> mGeocoders = new HashMap<Locale, Geocoder>();
	

	public GeocoderAPI(Context context) {
		this.mContext = context;
		mgeocoder = new Geocoder(mContext, Locale.getDefault());
	}


	void switchToLocaleGeocoder(Locale locale) {

		if (Constants.DEBUG)
			Log.d(TAG, "mGeocoder Locale " + locale.toString());
		if (mGeocoders.containsKey(locale)) {

			mgeocoder = mGeocoders.get(locale);
		} else {
			mgeocoder = new Geocoder(mContext, locale);
			mGeocoders.put(locale, mgeocoder);
		}

		if (Constants.DEBUG) {
			Set<Locale> sets = mGeocoders.keySet();
			for (Locale l : sets) {
				Log.d(TAG, "mGeocoders Locales " + l.toString());
			}
			Log.d(TAG, "mGeocoders size = " + mGeocoders.size());
		}

	}

	private Locale getInputLocale(String input) {
		
		boolean isEnglish = true;
		Locale locale = new Locale("en" , "US");
		for ( char c : input.toCharArray() ) {
		
			Log.d(TAG, Character.UnicodeBlock.of(c).toString());
			if ( Character.UnicodeBlock.of(c) != Character.UnicodeBlock.BASIC_LATIN ) {
			    isEnglish = false;
			    break;
			}
		}
		
		if(isEnglish) 
			return locale;
		else {	
			return new Locale("zh", "TW");
		}
		
	}
	
	public List<Address> getFromLocation(Location location) {
		
		
		List<Address> addresses = null;
		switchToLocaleGeocoder(Locale.getDefault());
		try {
	        addresses = mgeocoder.getFromLocation(location.getLatitude(), location.getAltitude(), MAXRESULT);
	    } catch (IOException e) {
	    	Log.d(TAG,"getFromLocation error");  
	        e.printStackTrace();
	    }
		return addresses;
	}
	
	public List<Address> getFromLocationName(String locationName) {

		List<Address> addresses = null;
		// Switch to current Locale Geocoder 
		switchToLocaleGeocoder(Locale.getDefault());
		try {	
			addresses = mgeocoder.getFromLocationName(locationName, MAXRESULT);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return addresses;

	}

	public ArrayList<SuggestPlace> getPlaceSuggestions(String query) {

		ArrayList<SuggestPlace> suggestPlaceList = null;

		List<Address> addresses = getFromLocationName(query);

		if (addresses != null && !addresses.isEmpty()) {

			suggestPlaceList = new ArrayList<SuggestPlace>(addresses.size());
			ListIterator<Address> iterator = addresses.listIterator();
			while (iterator.hasNext()) {
				StringBuilder addressText = new StringBuilder();
				Address address = iterator.next();
				int maxAddressLinesIndex = address.getMaxAddressLineIndex();
				if (Constants.DEBUG) {
					Log.d(TAG, address.toString() + "address lines " + address.getMaxAddressLineIndex());
				}
				
				
				for (int i = 0; i <= maxAddressLinesIndex; i++) {
					addressText.append(address.getAddressLine(i));
					if(i != maxAddressLinesIndex) addressText.append(",");
				}


				SuggestPlace suggestPlace = new SuggestPlace(
						addressText.toString(), query);

				String CountryName = address.getCountryName();
				if (CountryName != null)
					suggestPlace.setSub_description(CountryName);

				if (address.hasLatitude()) {
					suggestPlace.setLatitude(address.getLatitude());
				}
				if (address.hasLongitude()) {
					suggestPlace.setLongitude(address.getLongitude());
				}
				suggestPlaceList.add(suggestPlace);

			}
		}

		return suggestPlaceList;

	}

}
