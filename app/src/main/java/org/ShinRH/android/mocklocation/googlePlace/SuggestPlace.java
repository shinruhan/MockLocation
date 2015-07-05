package org.ShinRH.android.mocklocation.googlePlace;

import android.content.ContentValues;

public class SuggestPlace {

	private static final String TAG = SuggestPlace.class.getName();
	private String main_description = null;
	private String sub_description = null;
	private String gueryText = null;
	private String locationString = null;
	
	private double Latitude = 0 ;
	private double Longitude = 0;

	public SuggestPlace(String main_description,String gueryText) {
		this.main_description = main_description;
		this.gueryText = gueryText;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("\n" + "SuggestPlace :");
		stringBuilder.append("Main description = " + main_description + "\n");
		stringBuilder.append("Sub description  = " + sub_description + "\n");
		stringBuilder.append("Query Key Word   = " + gueryText + "\n");
		if(locationString== null) {
			stringBuilder.append("Location   = " + "( " + Latitude + "," + Longitude + " )" + "\n");
		} else {
			stringBuilder.append("Location   = " + locationString + "\n");
		}
		
		return stringBuilder.toString();
	}
	
	public ContentValues toContentValue() {
		
		ContentValues newTaskValue = new ContentValues();

		newTaskValue.put(Constants.SearchSuggestion.INTENT_QUERY, gueryText);

		newTaskValue.put(Constants.SearchSuggestion.SEARCHPLACENAME,
				main_description);

		newTaskValue.put(Constants.SearchSuggestion.SEARCHPLACENAME2,
				sub_description);

		if (locationString != null) {
			newTaskValue.put(Constants.SearchSuggestion.INTENT_EXTRA_LOCATION,
					locationString);

		} else {
			newTaskValue.put(Constants.SearchSuggestion.INTENT_EXTRA_LOCATION,
					String.format("%.7f,%.7f", Latitude, Longitude));

		}

		return newTaskValue;
		
	}

	public double getLongitude() {
		return Longitude;
	}

	public void setLongitude(double longitude) {
		Longitude = longitude;
	}

	public double getLatitude() {
		return Latitude;
	}

	public void setLatitude(double d) {
		Latitude = d;
	}

	public String getMainDescription() {
		return main_description;
	}

	public void setMainDescription(String description) {
		this.main_description = description;
	}


	public String getSub_description() {
		return sub_description;
	}

	public void setSub_description(String sub_description) {
		this.sub_description = sub_description;
	}

	public String getQueryText() {
		return gueryText;
	}

	public void setQueryText(String gueryText) {
		this.gueryText = gueryText;
	}

	public String getLocationString() {
		return locationString;
	}

	public void setLocationString(String locationString) {
		this.locationString = locationString;
	}

	
}
