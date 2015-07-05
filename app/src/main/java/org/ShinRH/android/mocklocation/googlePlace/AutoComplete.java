package org.ShinRH.android.mocklocation.googlePlace;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class AutoComplete {
	
	/*
	https://maps.googleapis.com/maps/api/place/autocomplete/json
		  ?input=bank
		  &sensor=false
		  &key=
	*/
	private static final String TAG =  SuggestPlace.class.getName();
	private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
	private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
	private static final String OUT_JSON = "/json";
	private String input;
	private String key;
	private String components;
	private String URL ;  
	
	public AutoComplete(String Input , String key ) {
		this.input = Input ; 
		this.key = key;
		makeUrl();
	}
	
	public AutoComplete(String Input , String key , String local ) {
		this.input = Input;
		this.key = key;
		this.components = local ;
		makeUrl();
	}
	
	private void makeUrl() {
		
		StringBuilder stringBuilder = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
		try {
			stringBuilder.append("?input=" + URLEncoder.encode(input, "utf8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stringBuilder.append("&sensor=false&key=" + key);
		URL = stringBuilder.toString();
	}
	
	public ArrayList<SuggestPlace> jsonRespondToSuggestPlace(String jsonResults) {
		
		ArrayList<SuggestPlace> resultList = null;
		
		try {
	        // Create a JSON object hierarchy from the results
	        JSONObject jsonObj = new JSONObject(jsonResults);
	        JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
	        int predsJsonArraySize = predsJsonArray.length();
	        Log.d(TAG,"predsJsonArraySize " + predsJsonArraySize);
	        // Extract the Place descriptions from the results
	        resultList = new ArrayList<SuggestPlace>(predsJsonArraySize);
	        for (int i = 0; i < predsJsonArraySize; i++) {
	        	
	        	String description = predsJsonArray.getJSONObject(i).getString("description");
	        	
	        	SuggestPlace suggestPlace = new SuggestPlace(description,input);
	        	resultList.add(suggestPlace); 

	        }
	    } catch (JSONException e) {
	        Log.e(TAG, "Cannot process JSON results", e);
	    }
		
		return resultList;
		
	}
	
	public String getURL() {return URL;};
	
	
}
