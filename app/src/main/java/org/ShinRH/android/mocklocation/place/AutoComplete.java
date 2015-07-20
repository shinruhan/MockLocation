package org.ShinRH.android.mocklocation.place;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
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

	
	public AutoComplete(String Input , String key ) {
		this.input = Input ; 
		this.key = key;
	}


    public ArrayList<SuggestPlace> query(){

        String autoCompleteRespondJson = null;
        autoCompleteRespondJson = getJsonRespond(makeUrl());
        if (Constants.VERBODE)
            Log.d(TAG, "getPlaceSuggestions getJsonRespond" + autoCompleteRespondJson);
        return jsonRespondToSuggestPlace(autoCompleteRespondJson);

    }

	
	private String makeUrl() {
		
		StringBuilder stringBuilder = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
		try {
			stringBuilder.append("?input=" + URLEncoder.encode(input, "utf8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        stringBuilder.append("&sensor=false&key=" + key);
		return  stringBuilder.toString();
	}


	private String getJsonRespond(String urlsting) {

		HttpURLConnection conn = null;
		StringBuilder jsonResults = new StringBuilder();
		java.net.URL url;

		try {
			url = new URL(urlsting);
			conn = (HttpURLConnection) url.openConnection();

			InputStreamReader in = new InputStreamReader(conn.getInputStream());

			// Load the results into a StringBuilder
			int read;
			char[] buff = new char[1024];
			while ((read = in.read(buff)) != -1) {

				jsonResults.append(buff, 0, read);
			}

		} catch (IOException e) {

			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		return jsonResults.toString();

	}

	private ArrayList<SuggestPlace> jsonRespondToSuggestPlace(String jsonResults) {
		
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
	
}
