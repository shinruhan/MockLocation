package org.ShinRH.android.mocklocation.place;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import android.util.Log;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;


public class GooglePlacesApi {
	
	private String API_KEY;
    private GoogleApiClient mGoogleApiClient;
	private static final String TAG =  GooglePlacesApi.class.getName();


    public GooglePlacesApi(GoogleApiClient apiClient) { this.mGoogleApiClient = apiClient; }
	public GooglePlacesApi(String key) {
		this.API_KEY = key;
	}


	public ArrayList<SuggestPlace> getPlaceSuggestions(String inputString) {
        ArrayList<SuggestPlace> suggestPlace = null;

        if (mGoogleApiClient != null) {

            final PendingResult<AutocompletePredictionBuffer> autocompletePredictionsResult = Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient,
                    inputString, null, null);
            AutocompletePredictionBuffer autocompletePredictions = autocompletePredictionsResult.await(5, TimeUnit.SECONDS);
            final Status status = autocompletePredictions.getStatus();

            if (!status.isSuccess()) {
                Log.e(TAG, "Error getting place predictions: " + status
                        .toString());
                autocompletePredictions.release();
                return null;
            }
            int placeCount = autocompletePredictions.getCount();
            Log.d(TAG, "Query completed. Received " + placeCount
                    + " predictions.");
            suggestPlace = new ArrayList<SuggestPlace>(placeCount);
            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
            while (iterator.hasNext()) {
                AutocompletePrediction prediction = iterator.next();
                suggestPlace.add(new SuggestPlace(prediction.getDescription(),
                        inputString));
            }

        } else if (API_KEY != null) {

            suggestPlace = new AutoComplete(inputString, API_KEY).query();

        }
		return suggestPlace;
	}
	


}
