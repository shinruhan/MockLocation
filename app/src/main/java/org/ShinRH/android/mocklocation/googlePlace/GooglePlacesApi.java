package org.ShinRH.android.mocklocation.googlePlace;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.util.Log;


public class GooglePlacesApi {
	
	private String API_KEY;
	private static final String TAG =  GooglePlacesApi.class.getName();
	
	public GooglePlacesApi(String key) {
		this.API_KEY = key;
	}
	
	public void setApiKey(String apikey) {
		  this.API_KEY = apikey;
	}
	
	public ArrayList<SuggestPlace> getPlaceSuggestions(String inputString) {
		ArrayList<SuggestPlace> suggestPlace = null;
		String autoCompleteRequestURL= null;
		String autoCompleteRespondJson= null;
		
		AutoComplete autoCompleteRequest = new AutoComplete(inputString, API_KEY);
		autoCompleteRequestURL = autoCompleteRequest.getURL();
		autoCompleteRespondJson = getJsonRespond(autoCompleteRequestURL);
		if (Constants.VERBODE) Log.d(TAG, "getPlaceSuggestions getJsonRespond" + autoCompleteRespondJson);
		suggestPlace = autoCompleteRequest.jsonRespondToSuggestPlace(autoCompleteRespondJson);
		return suggestPlace;
	}
	
	
	private String getJsonRespond(String urlsting) {
		
			HttpURLConnection conn = null;
			StringBuilder jsonResults = new StringBuilder();
		  	URL url;
		  	
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
	        	        
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
		        if (conn != null) {
		            conn.disconnect();
		        }
		    }
			
			return jsonResults.toString();
	        
	}
	
	
	
	

}
