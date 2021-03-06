package org.ShinRH.android.mocklocation;


import android.app.Dialog;
import android.app.DialogFragment;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.ShinRH.android.mocklocation.ad.AdHelper;
import org.ShinRH.android.mocklocation.content.DataSource;
import org.ShinRH.android.mocklocation.fragments.MapLayerDialogFragment;
import org.ShinRH.android.mocklocation.fragments.MockLocationSettingsDialogFragment;
import org.ShinRH.android.mocklocation.place.Constants;
import org.ShinRH.android.mocklocation.settings.SettingsActivity;
import org.ShinRH.android.mocklocation.utl.ApiAdapterFactory;
import org.ShinRH.android.mocklocation.utl.HandlerThreadHelper;
import org.ShinRH.android.mocklocation.utl.LayoutUtils;
import org.ShinRH.android.mocklocation.utl.PreferencesUtils;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


public class MapActivity extends ActionBarActivity implements OnSharedPreferenceChangeListener ,
        ConnectionCallbacks ,OnConnectionFailedListener {

    private static final String TAG = MapActivity.class.getName();
    private ActionBar mActionBar;
    private ToggleButton mToggleButton;
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private MockLocationServiceController mMockLocationServiceController;
    private SearchView mSearchView;
    private MapActivity mContext;
    private DataSource mDataSource;
    private MyMarker mMarker;
    private AdHelper mAdHelper;

    //Google Api client
    private GoogleApiClient mGoogleApiClient;
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;


    private List<OnCheckChangedListener> mCheckChangedListener = new ArrayList<OnCheckChangedListener>();
    private List<OnMapClickListener> mMapClickListener = new ArrayList<OnMapClickListener>();


    //Google Api client +++++++++
    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }

    }
    // The rest of this code is all about building the error dialog

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(this.getFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MapActivity)getActivity()).onDialogDismissed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }
    //Google Api client -------

    public interface OnCheckChangedListener {
        public boolean onCheckChanged(CompoundButton buttonView, boolean isChecked);
    }

    public interface OnMapClickListener {
        public boolean onMapClick(LatLng latLng);
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		mContext = this;

		mMockLocationServiceController = new MockLocationServiceController(this);
        mLocationManager= (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mDataSource = new DataSource(this);
        mAdHelper = new AdHelper(this,
        		(AdView)findViewById(R.id.adView),
        		getString(R.string.admod_testdevice_id_m8),
				getString(R.string.mocklocation_interstitial_id));

        // Create a GoogleApiClient instance
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        initMap(savedInstanceState);
        initBottom();
        LayoutUtils.dumpLayout((ViewGroup) this.getWindow().getDecorView(), 0);

    }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu");
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.activity_mapactivity, menu);
	    configSearchView(menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		int itemid = menuItem.getItemId();
		if (itemid == R.id.menu_map_layer) {
			
			showMapLayerDialog();
			return true;
		} else if (itemid == R.id.menu_settings){
			
			showSettings();
		}
		return super.onOptionsItemSelected(menuItem);
	}

	

	@Override
	protected void onNewIntent(final Intent intent) {
		Log.d(TAG, "onNewIntent");
		setIntent(intent);
		handleSearchIntent(intent);
		super.onNewIntent(intent);
	}

    @Override
    public void onResume() {
        super.onResume();
        mAdHelper.onResume();

    }

	@Override
	public void onPause() {
		mAdHelper.onPause();
		super.onPause();
	}

	@Override
	public void onDestroy() {
		mAdHelper.onDestroy();
		super.onDestroy();
	}

    @Override
    protected void onStart() {

        Log.d(TAG, "onStart");

        super.onStart();

        if(!mResolvingError)
        {  // more about this later
            mGoogleApiClient.connect();
        }
        mDataSource.registerOnSharedPreferenceChangeListener(this);
        mMockLocationServiceController.onStart();

        boolean isEnabled = PreferencesUtils.getBoolean(this,
                R.string.mock_btn_enabled_key, false);

        float last_cam_zoom = PreferencesUtils.getFloat(this,
                R.string.map_lastknow_cam_zoom_key, 10);

        if (isEnabled) {
            // Resume Bottom state
            mToggleButton.setChecked(isEnabled);

            // Resume Marker state
            mMarker.onStart();

            // Resume Map state
            moveToLocationZoom(mMarker.getMarkerLocation(), 500, last_cam_zoom);

            // Resume Service state
            mMockLocationServiceController.startBroadcast(mMarker.getMarkerLocation());

        } else {
            // Resume Map state
            float last_cam_lat = PreferencesUtils.getFloat(this,
                    R.string.map_lastknow_cam_lat_key, 0);
            float last_cam_lon = PreferencesUtils.getFloat(this,
                    R.string.map_lastknow_cam_lon_key, 0);

            if (last_cam_lat != 0 && last_cam_lon != 0) {

                LatLng last_cam_position = new LatLng(last_cam_lat, last_cam_lon);
                Log.d(TAG, "Restore Camera location " + last_cam_position);
                moveToLocationZoom(last_cam_position, 500, last_cam_zoom);

            } else {
                moveToLastKnownLocation();
            }
        }

        //Restore MapType
        int mapType = PreferencesUtils.getInt(mContext,
                R.string.map_type_key, PreferencesUtils.MAP_TYPE_DEFAUlT);
        mMap.setMapType(mapType);


    }

    @Override
	protected void onStop() {
		Log.d(TAG, "onStop");


        if (checkMap()) {
            CameraPosition cameraPosition = mMap.getCameraPosition();
            Log.d(TAG, "Save camera position " + cameraPosition.target);
            PreferencesUtils.setFloat(this,
                    R.string.map_lastknow_cam_lat_key,
                    (float) cameraPosition.target.latitude);
            PreferencesUtils.setFloat(this,
                    R.string.map_lastknow_cam_lon_key,
                    (float) cameraPosition.target.longitude);
            Log.d(TAG, "Save Camera Zoom Level " + cameraPosition.zoom);
            PreferencesUtils.setFloat(this,
                    R.string.map_lastknow_cam_zoom_key,
                    cameraPosition.zoom);

        }

		mDataSource.unregisterOnSharedPreferenceChangeListener(this);
        mMarker.onStop();
		mMockLocationServiceController.onStop();
        mGoogleApiClient.disconnect();
		super.onStop();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}
	

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.d(TAG, "onSharedPreferenceChanged key = " + key);
		if (key.equals(getString(R.string.map_type_key))) {
			if (checkMap()) {
				int maptype= PreferencesUtils.getInt(mContext,
						R.string.map_type_key,
						PreferencesUtils.MAP_TYPE_DEFAUlT);
				Log.d(TAG, "map_type change  = " + maptype);
				mMap.setMapType(maptype);
			}
		}
	}

	private void configSearchView(Menu menu) {
		
		// Get the SearchView and set the searchable configuration
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

		mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		// Assumes current activity is the searchable activity
		mSearchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		mSearchView.setQueryRefinementEnabled(true);

		mSearchView
				.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						// Hide and show trackController when search widget has
						// focus/no focus
						Log.d(TAG, "onFocusChange hasFocus" + hasFocus);
						if (!hasFocus) {
							//collapse search view
							mSearchView.setIconified(true);
						}
					}
				});

		mSearchView
				.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
					@Override
					public boolean onQueryTextSubmit(String query) {
						//clear query and collapse search view
						//mSearchView.setQuery("",false);
						//mSearchView.setIconified(true);
						Log.d(TAG, "onQueryTextSubmit query " + query);
						return false;
					}

					@Override
					public boolean onQueryTextChange(String newText) {
						Log.d(TAG, "onQueryTextChange newText " + newText);
						return false;
					}
				});

		mSearchView
				.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
					@Override
					public boolean onSuggestionSelect(int position) {
						Log.d(TAG, "onSuggestionSelect  position " + position);
						return false;
					}

					
					@Override
					public boolean onSuggestionClick(int position) {
						
						Log.d(TAG, "onSuggestionClick position " + position);
						//clear query and collapse search view
						mSearchView.setQuery("",false);
						mSearchView.setIconified(true);
						return false;
					}
				});

	}
	
	private void initBottom() {

		mToggleButton = (ToggleButton) findViewById(R.id.button_MockLocation);

        addOnCheckChangedListener(mMarker);

		addOnCheckChangedListener(new OnCheckChangedListener() {
			
			@Override
			public boolean onCheckChanged(CompoundButton buttonView, boolean isChecked) {
				Drawable Background = mToggleButton.getBackground();
				Log.d(TAG, "onCheckedChanged isChecked " + isChecked);

				if (isChecked) {
					if ( !isHTCMode() && !checkMockLocationSettings()) {
						mToggleButton.setChecked(false);
						return false;
					} else {
						Background.setLevel(MyContext.getInstance()
								.getResources()
								.getInteger(R.integer.btn_mocklocation_on));
						mMockLocationServiceController
								.startBroadcast(mMarker.getMarkerLocation());
						
					}
				} else {

					Background.setLevel(MyContext.getInstance().getResources()
							.getInteger(R.integer.btn_mocklocation_off));
					mMockLocationServiceController.stopBroadcast();
				}
				
				PreferencesUtils.setBoolean(getApplication(),
						R.string.mock_btn_enabled_key, isChecked);
				
				return true;
			}
		});
		

		
		mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				for (int i = 0; i < mCheckChangedListener.size(); i++) {					
		            if (!mCheckChangedListener.get(i).onCheckChanged(buttonView, isChecked))
		                break;
		        }
				
			}
		});

	}
	
	/*   
	 *  Map methods
	 * */
	private void initMap(Bundle savedInstanceState){
		
		// Init Map
		if (checkMap()) {
			Log.d(TAG, "checkMap ok ");
			// Create marker
			mMarker = new MyMarker(this);
            // Enable Zoom Control
            mMap.getUiSettings().setZoomControlsEnabled(true);

			addOnMapClickListener(new OnMapClickListener() {

                @Override
                public boolean onMapClick(LatLng latLng) {

                    // Collapse search view if user click map
                    Log.d(TAG, "onMapClick");
                    if (mSearchView != null && (!mSearchView.isIconified())) {
                        mSearchView.setIconified(true);
                    }
                    return true;
                }
            });
			
			// loop through mMapClickListener to let each listener handle this event 
			mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    for (int i = 0; i < mMapClickListener.size(); i++) {
                        if (!mMapClickListener.get(i).onMapClick(latLng))
                            break;
                    }
                }
            });

		}
	}
	
	
	private boolean checkMap() {
	    // Do a null check to confirm that we have not already instantiated the map.
	    if (mMap == null) {
	    	
	    	// use getSupportFragmentManager for supportMapFragment
	    	// use getFragmentManager for MapFragment
	    	FragmentManager fragmentManager = getSupportFragmentManager();
	        //Log.d(TAG, "fragmentManager from v4  " + fragmentManager);
	        SupportMapFragment fragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);
	        //Log.d(TAG, "fragment from v4  " + fragment);
	        mMap = fragment.getMap(); 
	        // Check if we were successful in obtaining the map.
	        if (mMap != null) {
	            // The Map is verified. It is now safe to manipulate the map.
	        	return true;
	        } else
	        	return false;
	    }
		return true;
	}
	
	/**
	 * Check mock location settings 
	 * @return  return true for set , false for not set
	 */
	private boolean isMockLocationOn() {

		if (Settings.Secure.getString(this.getContentResolver(),
				Settings.Secure.ALLOW_MOCK_LOCATION).contentEquals("1")) {
			return true;
		} else {
			return false;
		}

	}
	
	/**
	 * Check mock location settings , if not set show dialog 
	 * @return false for not set , true for set
	 *   
	 * 
	 */
	private boolean checkMockLocationSettings() {
		if(!isMockLocationOn()) {
			new MockLocationSettingsDialogFragment().show(this.getFragmentManager(),
					MockLocationSettingsDialogFragment.MOCK_LOCATION_SETTINGS_DIALOG_TAG);
			return false;
		}
		return true;
	}

	/**
	 * Shows the map layer dialog.
	 */
	public void showMapLayerDialog() {
		new MapLayerDialogFragment().show(this.getFragmentManager(),
				MapLayerDialogFragment.MAP_LAYER_DIALOG_TAG);
	}
	
	
	/**
	 * Show Settings fragment
	 */
	private void showSettings() {
		startActivity(new Intent(this,SettingsActivity.class));
	}

	
	/**  
	 *  Handle search intent 
	 **/
	private void handleSearchIntent(Intent intent) {
		
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			new HandleSearchIntentTask().execute(intent);
			
		}
	}
	
	
	private class UpdateRecentPlaceTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			String queryString = params[0];
			String latlong = params[1];
			
			Uri.Builder builder = new Uri.Builder();
			builder.scheme("content");
			builder.authority(getString(R.string.search_authority));
			builder.appendPath(Constants.SearchSuggestion.SUGGEST_URI_PATH_UPDATE_RECENTPLACE);
			builder.appendEncodedPath(queryString);
			builder.appendEncodedPath(latlong);
			Uri uri = builder.build();
			Log.d(TAG, "Update recent place uri "+ uri);
			getApplicationContext().getContentResolver().update(uri, null, null, null);
			
			return null;
		}	
	}
	
	
	private class HandleSearchIntentTask extends AsyncTask<Intent, Void, LatLng>{
		
		
		@Override
		protected LatLng doInBackground(Intent... params) {
			// TODO Auto-generated method stub
			String query = params[0].getStringExtra(SearchManager.QUERY);
			String locationString = params[0].
					getStringExtra(SearchManager.EXTRA_DATA_KEY);
			Log.d(TAG, "User query " + query + " location " + locationString);
			if (locationString != null) {
				new UpdateRecentPlaceTask().execute(query,locationString);
				return parseLatLng(locationString);
				
			} else {
				
				Uri.Builder builder = new Uri.Builder();
				builder.scheme("content");
				builder.authority(getString(R.string.search_authority));
				builder.appendPath(Constants.SearchSuggestion.SUGGEST_URI_PATH_QUERY);
				builder.appendPath(query);
				Uri uri = builder.build();
				Log.d(TAG, "query uri "+ uri);
				
				Cursor c = getApplicationContext().getContentResolver().query(
						uri, null, null, null, null);
				if (c != null) {
					if (c.moveToFirst()) {
						int index;
						index = c.getColumnIndex(Constants.SearchSuggestion.INTENT_EXTRA_LOCATION);
						if (index != -1) {
							locationString = c.getString(index);
							c.close();
							Log.d(TAG, "query resault locationString "+ locationString);
							new UpdateRecentPlaceTask().execute(query,locationString);
							return parseLatLng(locationString);
						}
					}
					c.close();
				} 
				
			}
			
			return null;
		}
		
		
		@Override
		protected void onPostExecute(LatLng latLng) {
			if(latLng != null){
				moveToLocation(latLng, 2000);
			}
		}
		
		
		private LatLng parseLatLng(String latlonString) {
			
			double latitude;
			double longitude;
			String[] latlon; 
			
			try {
				
				latlon = latlonString.split(",");
				latitude = Double.parseDouble(latlon[0]);
				longitude = Double.parseDouble(latlon[1]);
				
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return null;
			} catch (NullPointerException  e) {
				e.printStackTrace();
				return null;
			}
			
			LatLng location = new LatLng(latitude,longitude);
			Log.d(TAG,"Parsed location " +location.toString());
			return location;
			
		}
		
		
	}
	
	/**
	 * Move camera to given position
	 * @param cameraPosition The camera position 
	 * @param durationMs The duration of the animation in milliseconds.
	 */
	private void moveToCameraPosition(CameraPosition cameraPosition ,int durationMs) {
		
		if(checkMap()) {
			mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), durationMs, null);
		}
	}
	
	/**
	 * Move camera to given {@link Location}
	 * @param loc {@link Location} to move 
	 * @param durationMs durationMs The duration of the animation in milliseconds.
	 */
	private void moveToLocation(final Location loc , final int durationMs) {
		if(checkMap()) {
			runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(
                            loc.getLatitude(), loc.getLongitude())), durationMs, null);
                }
            });
			
		}
	}
	
	/**
	 * Move Camera to Latlng position
	 * @param latLng
	 * @param durationMs
	 */
	private void moveToLocation(final LatLng latLng , final int durationMs) {
		if(checkMap()) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng), durationMs, null);
                }
            });
		}	
	}

    /**
     * Move Camera to Latlng position
     * @param latLng
     * @param durationMs
     * @param zoomLevel zoomLevel
     */
    private void moveToLocationZoom (final LatLng latLng , final int durationMs , final float zoomLevel) {
        if(checkMap()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel), durationMs, null);
                }
            });
        }
    }

    /**
     * Move camera to given {@link Location}
     * @param loc {@link Location} to move
     * @param durationMs durationMs The duration of the animation in milliseconds.
     * @param zoomLevel zoomLevel
     */
    private void moveToLocationZoom (final Location loc , final int durationMs , final float zoomLevel) {
        if(checkMap()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                            loc.getLatitude(), loc.getLongitude()), zoomLevel), durationMs, null);
                }
            });
        }
    }


	private void moveToLastKnownLocation() {
		
		Location lastlocation = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		Log.d(TAG, "Got last know location " + lastlocation);
		if(lastlocation != null) {
            moveToLocationZoom(lastlocation, 2000, 10);
		}
		
	}

	private boolean isHTCMode(){
        SharedPreferences sharePreference = getSharedPreferences(org.ShinRH.android.mocklocation.utl.Constants.SETTINGS_NAME,Context.MODE_MULTI_PROCESS);
        return sharePreference.getBoolean(getString(R.string.pref_htc_mode_checkbox_key),false);
    }

	/**
	 * Class to represent marker on the map
	 * @author shinru_han
	 *
	 */
	private class MyMarker implements MapActivity.OnCheckChangedListener{
		
		MarkerOptions mMarker;
		MapActivity mMapActivity;
		GoogleMap mMap;
		boolean misEnabled;
		private final String TAG = MyMarker.class.getName();
		
		MyMarker(MapActivity context) {
			this.mMapActivity = context;
			this.mMap = mMapActivity.mMap;
			mMarker = new MarkerOptions();
			mMarker.draggable(false);

		}
		
		
		private void moveMarkerToLocation(LatLng latLng) {
			mMarker.position(latLng);
			mMap.clear();
			mMap.addMarker(mMarker);
		}
		

		@Override
		public boolean onCheckChanged(CompoundButton buttonView,
				boolean isChecked) {
			Log.d(TAG, "toggle botton change ischeck " + isChecked);
			misEnabled = isChecked;
			mMarker.draggable(!misEnabled);
			if(isChecked) {
				// put marker to the center of map 
				moveMarkerToLocation(mMap.getCameraPosition().target);
			} else {
				mMap.clear();
			}
			return true;
		}
		
		/**
		 * Get marker location
		 * @return Location
		 */
		public Location getMarkerLocation() {

			if (mMarker == null) {
                Log.d(TAG, " mMarker == null return null location");
                return null;
            }

			LatLng latLng = mMarker.getPosition();

			if (latLng != null) {
				Location location = new Location("");
				location.setLatitude(latLng.latitude);
				location.setLongitude(latLng.longitude);

				location.setTime(System.currentTimeMillis());
				// since 4.2.2 mock location need to fill
				// ElapsedRealtimeNanos , use helper method to do this
				ApiAdapterFactory.getApiAdapter()
						.makeLocationComplete(location);
				Log.d(TAG, " Marker location" + location);
				return location;
			}

            Log.d(TAG, " Marker location null ");
			return null;
		}
		
		public void onStop() {

			Log.d(TAG, "onStop");
			if (isEnabled()) {
                Location location = getMarkerLocation();
                if (location != null) {
                    Log.d(TAG, "save location" + location);
                    PreferencesUtils.setFloat(mMapActivity,
                            R.string.map_lastknow_marker_lat_key,
                            (float) location.getLatitude());
                    PreferencesUtils.setFloat(mMapActivity,
                            R.string.map_lastknow_marker_lon_key,
                            (float) location.getLongitude());
                }
            }
		}

		public void onStart() {

			Log.d(TAG, "onStart");

            double lat = (double) PreferencesUtils.getFloat(mMapActivity,
						R.string.map_lastknow_marker_lat_key, 0);
            double lon = (double) PreferencesUtils.getFloat(mMapActivity,
						R.string.map_lastknow_marker_lon_key, 0);
            LatLng latLng = new LatLng(lat, lon);
            Log.d(TAG, "Resume location" + latLng);
            moveMarkerToLocation(latLng);


		}

		/**
		 * Check for whether lock location service is enabled
		 * 
		 * @return true for service is enabled
		 */
		private boolean isEnabled() {
			return misEnabled;
		}

	}
	
	
	/**
	 *  Class help to handle all thing about MockLocationService
	 *  @author shinru_han
	 */
	private class MockLocationServiceController implements ServiceConnection {

		public int mRemoteMockLocationServiceStatus;
        private boolean mIsBound;
		private MapActivity mMapActivity;
		private Intent mIntent;
		private Messenger mMockLocationService;
		private Messenger mMessenger;
		private HandlerThread mthread;
		private MockLocationServiceHandler mMockLocationServiceHandler;

		private class MockLocationServiceHandler extends Handler {

			MockLocationServiceHandler(Looper looper) {
				super(looper);
			}

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MockLocationService.MSG_RET_STATUS:
					Log.d(TAG,  " MockLocationServiceHandler MSG_RET_STATUS");
					Log.d(TAG,  " MockLocationServiceHandler thread" + Thread.currentThread().getName());
					mRemoteMockLocationServiceStatus =  msg.arg1;
					break;
				}
			}
		}

		// Once this instance create , we will start and bind to remote service
		// we only control the broadcasting event of remote service , once it 
		// start broadcasting
		// no matter this activity is stop or kill , the remote service should
		// keep the broadcasting state
		// once it is binded , we should send a get state request to get remote
		// service state
		public MockLocationServiceController(MapActivity context) {
			Log.d(TAG, " MockLocationServiceController constrct ");
			mMapActivity = context;
			mIntent = new Intent(mMapActivity, MockLocationService.class);
			
		}
		
		public void onStart(){

			mthread = HandlerThreadHelper.createHandlerThread("MockLocationServiceProxy");
			try {
				mMockLocationServiceHandler = new MockLocationServiceHandler(
						mthread.getLooper());
				mMessenger = new Messenger(mMockLocationServiceHandler);
			} catch (IllegalThreadStateException e) {
				e.printStackTrace();
            }
            Log.d(TAG, "mthread state  " + mthread.getState());

			if (mIsBound == false) {
				bindService();
			}
		}

		public void onStop() {
			
			unBindService();
			
			mthread.getLooper().quit();

		}
	
		public void startMockService() {
            startService(mIntent);
		}

		public void setLocation(Location location) {

			sendMessageToService(MockLocationService.MSG_SET_LOCATION,location);
		}

		public void startBroadcast(Location location) {

			setLocation(location);
            sendMessageToService(MockLocationService.MSG_START_BROADCAST,null);
		}

		public void stopBroadcast() {

            sendMessageToService(MockLocationService.MSG_STOP_BROADCAST,null);
		}

		private void sendMessageToService(int message, Object obj) {
			
			final Message msg = Message.obtain(null, message, 0, 0);
			msg.replyTo = mMessenger;
			
			if(message == MockLocationService.MSG_SET_LOCATION){
				msg.obj = obj;			
			}
			
			try {
				if(mMockLocationService != null){
					Log.d(TAG, " sendMessageToService " + message);
					mMockLocationService.send(msg);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		private boolean bindService() {
			startMockService();
			return mMapActivity.bindService(mIntent, MockLocationServiceController.this, Context.BIND_AUTO_CREATE);
			
		}

		private void unBindService() {

			if (mIsBound) {
				mMapActivity.unbindService(MockLocationServiceController.this);
				mIsBound = false;
			}

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "onServiceConnected ");
			mMockLocationService = new Messenger(service);
			mIsBound = true;

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "onServiceDisconnected ");
			mMockLocationService = null;
			mIsBound = false;

		}

	}


	
	public void addOnCheckChangedListener(OnCheckChangedListener listener) {
	    this.mCheckChangedListener.add(listener);
	}

	public void removeOnCheckChangedListener(OnCheckChangedListener listener) {
	    this.mCheckChangedListener.remove(listener);
	}
	
	public void addOnMapClickListener(OnMapClickListener listener) {
	    this.mMapClickListener.add(listener);
	}

	public void removeOnMapClickListener(OnMapClickListener listener) {
	    this.mMapClickListener.remove(listener);
	}
	
	
	
	public void dump (String prefix, FileDescriptor fd, PrintWriter writer, String[] args){
		String newlinwString= "\n";
		StringBuilder s = new StringBuilder();
		s.append("mCheckChangedListener = ").append(mCheckChangedListener).append(newlinwString);
		s.append("mMapClickListener = ").append(mMapClickListener).append(newlinwString);
		writer.append(s);
	}



}
