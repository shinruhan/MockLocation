package org.ShinRH.android.mocklocation;


import java.io.FileDescriptor;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ShinRH.android.mocklocation.content.DataSource;
import org.ShinRH.android.mocklocation.fragments.MapLayerDialogFragment;
import org.ShinRH.android.mocklocation.fragments.MockLocationSettingsDialogFragment;
import org.ShinRH.android.mocklocation.place.Constants;
import org.ShinRH.android.mocklocation.utl.ApiAdapterFactory;
import org.ShinRH.android.mocklocation.utl.HandlerThreadHelper;
import org.ShinRH.android.mocklocation.utl.PreferencesUtils;
import org.ShinRH.android.mocklocation.utl.LayoutUtils;
import org.ShinRH.android.mocklocation.settings.*;
import org.ShinRH.android.mocklocation.ad.*;


import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;



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


import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.drawable.Drawable;

import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ToggleButton;
public class MapActivity extends ActionBarActivity implements OnSharedPreferenceChangeListener {

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
	
	private List<OnCheckChangedListener> mCheckChangedListener = new ArrayList<OnCheckChangedListener>();
	private List<OnMapClickListener> mMapClickListener = new ArrayList<OnMapClickListener>();
	
	public interface OnCheckChangedListener {
	    public boolean onCheckChanged(CompoundButton buttonView, boolean isChecked);
	}
	
	public interface OnMapClickListener {
		public boolean onMapClick(LatLng latLng) ;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		mContext = this;

		mMockLocationServiceController = new MockLocationServiceController(this);
        mLocationManager= (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mActionBar = getSupportActionBar();
        mActionBar.show();
        Log.d(TAG, "ActionBar is showing " + mActionBar.isShowing());
        mDataSource = new DataSource(this);
        
        mAdHelper = new AdHelper(this,
        		(AdView)findViewById(R.id.adView),
        		getString(R.string.admod_testdevice_id_m8));

        initMap(savedInstanceState);
        initBottom();
        LayoutUtils.dumpLayout((ViewGroup)this.getWindow().getDecorView(), 0);
              
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
	protected void onPostResume () {
		Log.d(TAG, "onPostResume");
		mDataSource.registerOnSharedPreferenceChangeListener(this);
		mMockLocationServiceController.onPostResume();
		super.onPostResume();
	}
	
	@Override
	public void onPause() {
		mAdHelper.onPause();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		mAdHelper.onResume();
	}

	@Override
	public void onDestroy() {
		mAdHelper.onDestroy();
		super.onDestroy();
	}
	
	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		mDataSource.unregisterOnSharedPreferenceChangeListener(this);
		mMarker.onStop();
		mMockLocationServiceController.onStop();
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
						Log.d(TAG, "onQueryTextSubmit query" + query);
						return false;
					}

					@Override
					public boolean onQueryTextChange(String newText) {
						Log.d(TAG, "onQueryTextChange newText" + newText);
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
		
		addOnCheckChangedListener(new OnCheckChangedListener() {
			
			@Override
			public boolean onCheckChanged(CompoundButton buttonView, boolean isChecked) {
				Drawable Background = mToggleButton.getBackground();
				Log.d(TAG, "onCheckedChanged isChecked " + isChecked);

				if (isChecked) {
					if (!checkMockLocationSettings()) {
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
		
		addOnCheckChangedListener(mMarker);
		
		mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				for (int i = 0; i < mCheckChangedListener.size(); i++) {					
		            if (!mCheckChangedListener.get(i).onCheckChanged(buttonView, isChecked))
		                break;
		        }
				
			}
		});
		

		boolean buttomStatus = PreferencesUtils.getBoolean(this,
				R.string.mock_btn_enabled_key, false);
		if (buttomStatus) {
			mToggleButton.setChecked(buttomStatus);
		}
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
			    public  void onMapClick(LatLng latLng) {
			        for (int i = 0; i < mMapClickListener.size(); i++) {			        	
			            if (!mMapClickListener.get(i).onMapClick(latLng))
			                break;
			        }
			    }
			});
			

			//Restore MapType 
			int mapType = PreferencesUtils.getInt(mContext,
					R.string.map_type_key, PreferencesUtils.MAP_TYPE_DEFAUlT);
			mMap.setMapType(mapType);
				

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
    private void moveToLocationZoom (final LatLng latLng , final int durationMs , final int zoomLevel) {
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
    private void moveToLocationZoom (final Location loc , final int durationMs , final int zoomLevel) {
        if(checkMap()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
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
	
	/*   
	 *  Listeners
	 * 

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        	
        	mDrawerLayout.post(new Runnable() {
	            @Override
	            public void run() {
	            	if (checkMap())
	            	mMap.setMapType(mMapStyle.get(mMapStyleTitles[position]));
	            }
	        });
        }
    }
	*/
	
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
			misEnabled = PreferencesUtils.getBoolean(mMapActivity,
					R.string.mock_btn_enabled_key, false);
			Log.d(TAG, "misEnabled " + misEnabled);

			mMarker.draggable(false);
			
			// Restore camera or marker position in on create 
			onCreate();
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

			if (mMarker == null)
				return null;
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
				Log.d(TAG, "Marker location" + location);
				return location;
			}

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

			} else {

				if (checkMap()) {
					CameraPosition cameraPosition = mMap.getCameraPosition();
					
					Log.d(TAG, "Save camera position " + cameraPosition.target);
					PreferencesUtils.setFloat(mMapActivity,
							R.string.map_lastknow_cam_lat_key,
							(float) cameraPosition.target.latitude);
					PreferencesUtils.setFloat(mMapActivity,
							R.string.map_lastknow_cam_lon_key,
							(float) cameraPosition.target.longitude);

				}

			}
		}

		public void onCreate() {
			Log.d(TAG, "onCreate");
			if (isEnabled()) {
				
				double lat = (double) PreferencesUtils.getFloat(mMapActivity,
						R.string.map_lastknow_marker_lat_key, 0);
				double lon = (double) PreferencesUtils.getFloat(mMapActivity,
						R.string.map_lastknow_marker_lon_key, 0);
				LatLng latLng = new LatLng(lat, lon);
				Log.d(TAG, "Resume location" + latLng);
				moveMarkerToLocation(latLng);
				mMapActivity.moveToLocation(latLng, 500);

			} else {

				float last_cam_lat = PreferencesUtils.getFloat(mMapActivity,
						R.string.map_lastknow_cam_lat_key, 0);
				float last_cam_lon = PreferencesUtils.getFloat(mMapActivity,
						R.string.map_lastknow_cam_lon_key, 0);

				if (last_cam_lat != 0 && last_cam_lon != 0) {
					LatLng last_cam_position = new  LatLng(last_cam_lat, last_cam_lon);
					Log.d(TAG, "Restore Camera location " + last_cam_position);
					moveToLocation(last_cam_position, 500);

				} else {

					moveToLastKnownLocation();

				}

			}

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
		private Location mserviceLocation;

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
					mserviceLocation = (Location)msg.obj;
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
		
		public void onPostResume() {

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
			Log.d(TAG, "sendMessageToService" + message);
			
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
				// TODO Auto-generated catch block
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
			// TODO Auto-generated method stub
			Log.d(TAG, "onServiceConnected ");

			mMockLocationService = new Messenger(service);
			mIsBound = true;
			sendMessageToService(MockLocationService.MSG_GET_STATUS , null);
			//Toast.makeText(mContext, "MockLocationService connected",
			//		Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onServiceDisconnected ");

			mMockLocationService = null;
			mIsBound = false;
			
			//Toast.makeText(mContext, "MockLocationService disconnected",
			//		Toast.LENGTH_SHORT).show();
		}

		public Location getServiceLocation() {
			return mserviceLocation;
		}
		
		public boolean isBinded() {
			return mIsBound;
		}


	}

	
	/**
	 * Get service current location
	 * @return
	 */
	public Location getServiceLocation(){
		if(mMockLocationServiceController != null && mMockLocationServiceController.isBinded()) {
			return mMockLocationServiceController.getServiceLocation();
		} else 
			return null;
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
