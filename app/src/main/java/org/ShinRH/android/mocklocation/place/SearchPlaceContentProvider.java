package org.ShinRH.android.mocklocation.place;


import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

import org.ShinRH.android.mocklocation.MyContext;
import org.ShinRH.android.mocklocation.R;

import java.util.ArrayList;
import java.util.List;

public class SearchPlaceContentProvider extends ContentProvider implements SharedPreferences.OnSharedPreferenceChangeListener,
		GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
	
	private static final String TAG =  SearchPlaceContentProvider.class.getName();
	private UriMatcher mUriMatcher;
	private GooglePlacesApi mGooglePlacesApi;
	private GoogleApiClient mGoogleApiClient;
	private SearchPlacesDB mDB;
	private GeocoderAPI mGeocoderAPI;
	private Context mContext;
	private boolean mIsNetworkAvaialable;
	
	public SearchPlaceContentProvider() {
		


	}

	@Override
	public boolean onCreate() {
		
		mContext 	 	 = getContext();

		// TODO Auto-generated method stub
		Log.d(TAG,"onCreate" + mContext.databaseList() + mContext.getPackageName());
		if(Binder.getCallingPid() == Process.myPid()){
			Log.d(TAG,"calling process is myPid");
		}
		mDB      		 = new SearchPlacesDB(mContext);
		mGeocoderAPI     = new GeocoderAPI(mContext);
		// Create a GoogleApiClient instance

		mGoogleApiClient = new GoogleApiClient.Builder(mContext)
				.addApi(Places.GEO_DATA_API)
				.addApi(Places.PLACE_DETECTION_API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();

		mIsNetworkAvaialable = isDeviceOnline();

		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);        
		mContext.registerReceiver(networkStateReceiver, filter);

		mGooglePlacesApi = new GooglePlacesApi(mGoogleApiClient);

        //the code returned for URI match to components
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(Constants.SearchSuggestion.AUTHORITY_SEARCHPLACE,
                Constants.SearchSuggestion.SUGGEST_URI_PATH_QUERY+"/*" ,
                Constants.SearchSuggestion.SEARCHPLACETABLE_URI_CODE_KEYWORD_QUERY);
        mUriMatcher.addURI(Constants.SearchSuggestion.AUTHORITY_SEARCHPLACE,
                Constants.SearchSuggestion.SUGGEST_URI_PATH_DELTEALL ,
                Constants.SearchSuggestion.SEARCHPLACETABLE_URI_CODE_DELTE_ALL);
        // SUGGEST_URI_PATH_UPDATE_RECENTPLACE/123.2222,34.222
        mUriMatcher.addURI(Constants.SearchSuggestion.AUTHORITY_SEARCHPLACE,
                Constants.SearchSuggestion.SUGGEST_URI_PATH_UPDATE_RECENTPLACE + "/*/*",
                Constants.SearchSuggestion.SEARCHPLACETABLE_URI_CODE_UPDATE_RECENTPLACE);
        mUriMatcher.addURI(Constants.SearchSuggestion.AUTHORITY_SEARCHPLACE,
                Constants.SearchSuggestion.SUGGEST_URI_PATH_QUERY+"/" ,
                Constants.SearchSuggestion.SEARCHPLACETABLE_URI_CODE_RECENTPLACE_QUERY);

        return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		Cursor ret = null ;
		/*parse user query*/
		String query = uri.getLastPathSegment();

		Log.d(TAG, "query  url " + uri.toString() + "\n" +
				    "projection " + projection + "\n" +
				    "selection " + selection + "\n" +
				    "selectionArgs " + selectionArgs + "\n" +
				    "sortOrder " + sortOrder);
		
		switch (mUriMatcher.match(uri)) {
		case Constants.SearchSuggestion.SEARCHPLACETABLE_URI_CODE_KEYWORD_QUERY:
			/* if device is online query suggest place from Geocoder */
			/* 
			 * */
			if(mIsNetworkAvaialable && mDB.ifNeedUpdate(query)){
				Log.d(TAG,"query:" + query);
				
				ArrayList<SuggestPlace> suggestPlacesList = mGeocoderAPI.getPlaceSuggestions(query);
				//if (suggestPlacesList == null ) {
				//	Log.d(TAG,"query from Geocoder fail try GooglePlaceAPI");
					// TO DO  need to use google place api or not ?? 
				//ArrayList<SuggestPlace> suggestPlacesList = mGooglePlacesApi.getPlaceSuggestions(query);
				//}
				
				mDB.insertGooglePlaces(suggestPlacesList);
			}
			
			ret =  mDB.getSuggestGooglePlaces(query); 
			break;
		case Constants.SearchSuggestion.SEARCHPLACETABLE_URI_CODE_RECENTPLACE_QUERY:
			ret =  mDB.getRecentPlaces();
			break;
			default:
			Log.d(TAG, "no match for this uri");
			break;
		}
		
		return ret;
	}

	@Override
	public String getType(Uri uri) {
		Uri url = uri;
		// TODO Auto-generated method stub
		Log.d(TAG, "getType" + url );
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		Uri url = uri;
		Log.d(TAG, "insert url = " + url + "values " + values.toString() );
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		Uri url = uri;
		int rows_deleted = 0;
		Log.d(TAG, "delete url = " + url + " selection " + selection
				+ "selectionArgs " + selectionArgs);
		switch (mUriMatcher.match(uri)) {
		case Constants.SearchSuggestion.SEARCHPLACETABLE_URI_CODE_DELTE_ALL:
			Log.d(TAG, "delete " + Constants.SearchSuggestion.RECENTPLACE_TABLENAME);
			rows_deleted += mDB
					.deleteAllRow(Constants.SearchSuggestion.RECENTPLACE_TABLENAME);
			Log.d(TAG, "rows_deleted " + rows_deleted);
			break;

		}
		return rows_deleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		
		// TODO Auto-generated method stub
		
		Log.d(TAG, "update url = " + uri + " selection " + selection
				+ "selectionArgs " + selectionArgs);
		switch (mUriMatcher.match(uri)) {
		case Constants.SearchSuggestion.SEARCHPLACETABLE_URI_CODE_UPDATE_RECENTPLACE:
			List<String> list = uri.getPathSegments();
			int size = list.size();
			if (size > 2) {
				String location = list.get(size - 1);
				String query = list.get(size - 2);
				Log.d(TAG, "location " + location);
				Log.d(TAG, "query " + query);
				
				handleUpdateRecentPlace(query,location);
				
			}
			break;
		default:
			Log.d(TAG, "no match for this uri");
		}
		return 0;
	}
	

	/** Checks whether the device currently has a network connection */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
        		mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
    
    BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w("Network Listener", "Network Type Changed" + intent);
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {       
                if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
                	mIsNetworkAvaialable = false;
                } else {
                	mIsNetworkAvaialable = true;
                }
            }
        }
    };
    
    /**
     * Query google places database by query and locationString , and update recent place talbles 
     * @param query
     * @param locationString
     */
    void handleUpdateRecentPlace(String query, String locationString) {
    	
		Cursor cursor = mDB.getSuggestGooglePlaces(query);
		SuggestPlace suggestPlace = null;
		while (cursor.moveToNext()) {
			int index;
			index = cursor
					.getColumnIndex(Constants.SearchSuggestion.INTENT_EXTRA_LOCATION);
			if (index != -1) {
				String goolelocationString = cursor.getString(index);
				Log.d(TAG, "goolelocationString = " + goolelocationString);
				
				if (locationString.equals(goolelocationString)) {

					index = cursor
							.getColumnIndex(Constants.SearchSuggestion.SEARCHPLACENAME);
					if (index != -1) {
						String placeName1 = cursor.getString(index);
						Log.d(TAG, "placeName1 = " + placeName1);
						suggestPlace = new SuggestPlace(placeName1, query);
	
					}

					if (suggestPlace != null) {
						index = cursor
								.getColumnIndex(Constants.SearchSuggestion.SEARCHPLACENAME2);
						if (index != -1) {
							String placeName2 = cursor.getString(index);

							Log.d(TAG, "placeName2 = " + placeName2);
							suggestPlace.setSub_description(placeName2);

						}

						suggestPlace.setLocationString(locationString);
						mDB.insertRecentPlaces(suggestPlace);
					}
					
					break;
				}
			}
		}
		cursor.close();
    }


	@Override
	public void onConnected(Bundle bundle) {

	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

	}
}
