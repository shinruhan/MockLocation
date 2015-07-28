package org.ShinRH.android.mocklocation;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.Status;

import org.ShinRH.android.mocklocation.utl.ApiAdapterFactory;
import org.ShinRH.android.mocklocation.utl.PreferencesUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.os.AsyncTask.*;
public class MockLocationService extends Service implements
		ConnectionCallbacks, OnConnectionFailedListener {
	
	private static final String TAG = MockLocationService.class.getName();
	
	// Message for this service
	public static final int MSG_START_BROADCAST = 1;
	public static final int MSG_STOP_BROADCAST = 2;
	public static final int MSG_SET_LOCATION = 3;
	public static final int MSG_GET_STATUS = 4;
	public static final int MSG_RET_STATUS = 5;
	public static final int MSG_BROADCAST_LOCATION = 6;
	
	// Fuse provider string name , android system does not define this 
	public static final String FUSE_PROVIDER = "fused";
	
	// The notification id we use to show notification
	private static final int NOTIFICATIONID = 1;
	
	
	// Location report interval ms
    private int report_interval = 1000;
    
    // NotificationManager to show notification
	private NotificationManager mNM;
	
	
	// The location to report 
	private Location mlocation = null;
	
	// LocationManager to report mock location
	private LocationManager mLocationManager;
	
	// HandlerThread run in background
	private HandlerThread mBackgroundThread;


	public enum  MockLocationServiceStatus {
		BROADCASTING,
		IDEL,
		PAUSE_SCREEN_OFF,
	}

    public enum ScreenState {
        ON,
        OFF,
    }

	private volatile ScreenState mScreenState;
	private EnumMap<MockLocationServiceStatus, MockLocationServiceState> mStateEnumMap;
	private MockLocationServiceState mState;
	private MockLocationServiceStatus mMockLocationServiceStatus;
	
	// 
	private BackgroundHandler mBackgroundHandler;
	private UIThreadHandler mUIThreadHandler;
	private List<String> mMockProviders ; 
	private Messenger mMessenger ;
	private GoogleApiClient mGoogleApiClient;
	private PowerManager mPowerManager;
	
	
	private interface MockLocationServiceState {
		
		 void onCreate();
		 void onDestroy();
		 void onScreenOn();
		 void onScreenoff();
		 void handleStartBroadcast();
		 void handleStopBroadcast();
		 void updateNotificationLocation();
	}
	
	
	private class IdleState implements MockLocationServiceState {
		
		private final String TAG = IdleState.class.getName();
		MockLocationService mockLocationService;
		
		
		IdleState(MockLocationService mockLocationService) {
			this.mockLocationService = mockLocationService;
		}


		@Override
		public void onCreate() {
			Log.d(TAG,"onCreate do nothing ");
			
		}
		
		@Override
		public void onDestroy() {
			Log.d(TAG,"onDestroy do nothing ");
		}

		@Override
		public void onScreenOn() {
			Log.d(TAG,"onScreenOn do nothing ");
		}

		@Override
		public void onScreenoff() {
			Log.d(TAG,"onScreenoff do nothing ");
			
		}

		@Override
		public void handleStartBroadcast() {			
			mockLocationService.handleStartBroadcast();
			mockLocationService.setState(MockLocationServiceStatus.BROADCASTING);
		}


		@Override
		public void handleStopBroadcast() {
			Log.d(TAG,"handleStopBroadcast do nothing ");
		}


		@Override
		public void updateNotificationLocation() {
			Log.d(TAG,"updateNotificationLocation do nothing");
		}


	}
	
	private class BroadCastingState implements MockLocationServiceState {
		
		private final String TAG = BroadCastingState.class.getName();
		MockLocationService mockLocationService;
		
		
		BroadCastingState(MockLocationService mockLocationService) {
			this.mockLocationService = mockLocationService;
		}
		
		@Override
		public void onCreate() {
			Log.d(TAG,"onCreate StartBroadcast ");
			mockLocationService.handleStartBroadcast();
		}
				
		@Override
		public void onDestroy() {
			Log.d(TAG,"onDestroy ");
			mockLocationService.mBackgroundHandler.removeMessages(MSG_BROADCAST_LOCATION);
			mockLocationService.setState(MockLocationServiceStatus.IDEL);	
		}

		@Override
		public void onScreenOn() {
			Log.d(TAG,"onScreenOn do nothing ");
		}

		@Override
		public void onScreenoff() {
			Log.d(TAG,"onScreenoff ");
			mockLocationService.mBackgroundHandler.removeMessages(MSG_BROADCAST_LOCATION);
			mockLocationService.setState(MockLocationServiceStatus.PAUSE_SCREEN_OFF);		
		}

		@Override
		public void handleStartBroadcast() {
			Log.d(TAG,"handleStartBroadcast do nothing ");		
		}

		@Override
		public void handleStopBroadcast() {
			Log.d(TAG,"handleStopBroadcast ");	
			mockLocationService.handleStopBroadcast();
			mockLocationService.setState(MockLocationServiceStatus.IDEL);
			
		}

		@Override
		public void updateNotificationLocation() {
			
			Log.d(TAG,"updateNotificationLocation ");
			mockLocationService.updateNotificationLocation();
		}
		
	}
	
	private class PauseState implements MockLocationServiceState {
		
		private final String TAG = PauseState.class.getName();
		
		MockLocationService mockLocationService;

		PauseState(MockLocationService mockLocationService) {
			this.mockLocationService = mockLocationService;
		}

		@Override
		public void onCreate() {
			Log.d(TAG,"onCreate ");
			// Start broadcast first to resume the icon and mock providers 
			mockLocationService.handleStartBroadcast();
			if (mScreenState == ScreenState.ON){
				mockLocationService.setState(MockLocationServiceStatus.BROADCASTING);
			} else {
				mockLocationService.setState(MockLocationServiceStatus.PAUSE_SCREEN_OFF);
			}
		}
		
		@Override
		public void onDestroy() {
			Log.d(TAG,"onDestroy do nothing ");	
			
		}

		@Override
		public void onScreenOn() {
			Log.d(TAG,"onScreenOn ");
			Message m = Message.obtain(mockLocationService.mBackgroundHandler,
					MockLocationService.MSG_BROADCAST_LOCATION);
			mockLocationService.mBackgroundHandler.sendMessage(m);
			mockLocationService.setState(MockLocationServiceStatus.BROADCASTING);
			
		}

		@Override
		public void onScreenoff() {
			Log.d(TAG,"onScreenoff do nothing ");	
			
		}


		@Override
		public void handleStartBroadcast() {
			Log.d(TAG,"handleStartBroadcast do nothing ");
		}


		@Override
		public void handleStopBroadcast() {
			Log.d(TAG, "handleStopBroadcast");
		}


		@Override
		public void updateNotificationLocation() {
			Log.d(TAG, "updateNotificationLocation do nothing");
			
		}
		
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.d(TAG, "onStartCommand" );
		// check mock location button status , if it is enabled , which mean we are kill by system and restart again , and we need to keep broadcasting location
		if(intent == null) {
			Log.d(TAG, "onStartCommand intent null " );
			if(PreferencesUtils.getBoolean(this, R.string.mock_btn_enabled_key, false)) {
				Log.d(TAG, "onStartCommand resume broadcasting status");
				Message m = Message.obtain(mUIThreadHandler, MSG_START_BROADCAST);
				mUIThreadHandler.sendMessage(m);
			}
		} else {
			// New report interval from settings 
			report_interval = intent.getIntExtra(getString(R.string.pref_location_report_interval_key), report_interval);
			Log.d(TAG, " new report_interval  " + report_interval + " ms");
		}
		
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");

		
		mState.onDestroy();
		PreferencesUtils.setInt(this, 
				R.string.mocklocationservice_state_key, getStat());
		
		// stop the background thread looper
		mBackgroundThread.getLooper().quit();
		mBackgroundThread = null;
		mBackgroundHandler = null;
		
		// unBind Google play service
		if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
		
		
	}

	@Override
	public void onCreate() {
		
		Log.d(TAG, "onCreate");
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);	
		
		mMockProviders = new ArrayList<String>();
		mMockProviders.add(LocationManager.GPS_PROVIDER);
		mMockProviders.add(LocationManager.NETWORK_PROVIDER);
		mMockProviders.add(FUSE_PROVIDER);
		
		mStateEnumMap = new EnumMap<MockLocationServiceStatus,MockLocationServiceState>(MockLocationServiceStatus.class);
		mStateEnumMap.put(MockLocationServiceStatus.IDEL, new IdleState(this));
		mStateEnumMap.put(MockLocationServiceStatus.BROADCASTING, new BroadCastingState(this));
		mStateEnumMap.put(MockLocationServiceStatus.PAUSE_SCREEN_OFF, new PauseState(this));
		
		// Create state
		int state = PreferencesUtils.getInt(this, 
				R.string.mocklocationservice_state_key, MockLocationServiceStatus.IDEL.ordinal());
        Log.d(TAG, "mocklocationservice_state" + state);
		setState(MockLocationServiceStatus.values()[state]);
		synchronized (mMockLocationServiceStatus) {
			mState.onCreate();
		}

        mScreenState = ScreenState.ON;
		
		// Create a location client for Google Play services
		GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
		mGoogleApiClient.connect();
		
		mBackgroundThread = new HandlerThread("location broadcast thread",Process.THREAD_PRIORITY_BACKGROUND);
		mBackgroundThread.start();
		mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());
		
		mUIThreadHandler = new UIThreadHandler(this);
		mMessenger = new Messenger(mUIThreadHandler);
		
		//Listen for screen on off state
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mBroadcastReciever, intentFilter, null, mUIThreadHandler);
		
		
	
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		Toast.makeText(this, "onBind", Toast.LENGTH_SHORT).show();
		return mMessenger.getBinder();
	}

	public boolean onUnbind(Intent intent) {

		// Tell the user we stopped.
		Toast.makeText(this, "onUnbind", Toast.LENGTH_SHORT).show();
		return false;

	}

	
	 /*
     * When the client is connected, Location Services calls this method, which in turn
     * starts the testing cycle by sending a message to the Handler that injects the test locations.
     */
	@Override
	public void onConnected(Bundle arg0) {
		
		Log.d(TAG, " GooleApiClient onConnected " + arg0);
		
	}

	@Override
	public void onConnectionSuspended(int i) {
		mGoogleApiClient = null;
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		mGoogleApiClient = null;
	}
	

	
	// Main thread handler 
	private static class UIThreadHandler extends Handler {

		WeakReference<MockLocationService> mservice;

		UIThreadHandler(MockLocationService service) {
			mservice = new WeakReference<MockLocationService>(service);
		}

		@Override
		public void handleMessage(Message msg) {

			MockLocationService theService = mservice.get();
			switch (msg.what) {
			
			case MSG_START_BROADCAST:
				Log.d(TAG, "UIThreadHandler MSG_START_BROADCAST ");
				if (theService != null) {	
					Message m = Message.obtain(theService.mBackgroundHandler, MSG_START_BROADCAST);
					theService.mBackgroundHandler.sendMessage(m);
				}
				
				break;
			case MSG_STOP_BROADCAST:
				Log.d(TAG, "UIThreadHandler MSG_STOP_BROADCAST ");
				if (theService != null) {
					Message m = Message.obtain(theService.mBackgroundHandler, MSG_STOP_BROADCAST);
					theService.mBackgroundHandler.sendMessage(m);			
				}				
				break;
				
			case MSG_SET_LOCATION:
				Log.d(TAG, "UIThreadHandler MSG_SET_LOCATION " + (Location) msg.obj);
				// Update Location
				Location location = (Location) msg.obj;
				if (location != null && theService != null) {
					
					theService.setLocation(location);
					synchronized (theService.mMockLocationServiceStatus) {
						theService.mState.updateNotificationLocation();
					}

				}
				
				break;

			case MSG_GET_STATUS:
				Log.d(TAG, "UIThreadHandler MSG_GET_STATUS ");
				Messenger callbackMessenger = msg.replyTo;
				if (callbackMessenger != null && theService != null) {
					Message m = Message.obtain(null,MSG_RET_STATUS,null);
					try {
						m.arg1 = theService.getStat();
						m.obj = theService.getLocation();
						callbackMessenger.send(m);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	

	// Background thread handler
	private class BackgroundHandler extends Handler {

		BackgroundHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			
			case MSG_START_BROADCAST:
				Log.d(TAG, "BackgroundHandler MSG_START_BROADCAST ");
				synchronized (mMockLocationServiceStatus) {
					mState.handleStartBroadcast();
				}
				
				break;
			case MSG_STOP_BROADCAST:
				Log.d(TAG, "BackgroundHandler MSG_STOP_BROADCAST ");
				synchronized (mMockLocationServiceStatus) {
					mState.handleStopBroadcast();
				}
				break;
			
			case MSG_BROADCAST_LOCATION:
				Log.d(TAG, "BackgroundHandler MSG_BROADCAST_LOCATION ");
				handleBroadcastLocation();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	
	/*
	 *  BroadcastReceiver to listen screen on off state , in screen off pause broadcasting 
	 *  and resume broadcasting when screen on 
	 */
	private final BroadcastReceiver mBroadcastReciever = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if (action.equals(Intent.ACTION_SCREEN_ON)) {
                mScreenState = ScreenState.ON;
				Log.d(TAG, "ACTION_SCREEN ON Resume Broadcasting");
				synchronized (mMockLocationServiceStatus) {
					mState.onScreenOn();
				}


			} else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                mScreenState = ScreenState.OFF;
				Log.d(TAG, "ACTION_SCREEN_OFF Pause Broadcasting");
				synchronized (mMockLocationServiceStatus) {
					mState.onScreenoff();
				}
			}
		}

	};
	
	private void handleStartBroadcast() {
		
		// Replace system GPS , Network , Fuse provider for LocationManager
		addMockProviders(mMockProviders);
		
		// Replace location provider for Google Location Service
		if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){
            PendingResult<Status> ret = LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, true);
            final Status status = ret.await(1, TimeUnit.SECONDS);
            if (!status.isSuccess()) {
                Log.d(TAG,"setMockMode fail");
            }
		}
		
		Message m = Message.obtain(mBackgroundHandler, MSG_BROADCAST_LOCATION);
		mBackgroundHandler.sendMessage(m);
		
		Location location = getLocation();
		// show notification
		startForeground(NOTIFICATIONID, getNotification(getString(R.string.notification_mocklocation_title),
				getString(R.string.notification_mocklocation_secondline , 
						location.getLatitude(), 
						location.getLongitude())));
		
	}

	private void handleStopBroadcast() {

		
		mBackgroundHandler.removeMessages(MSG_BROADCAST_LOCATION);
		// Clear location

		// Remove location provider for Google Location Service
		if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
			try {
                PendingResult<Status> ret = LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient,false);
                final Status status = ret.await(1, TimeUnit.SECONDS);
                if (!status.isSuccess()) {
                    Log.d(TAG,"setMockMode fail");
                }
			} catch (SecurityException e) {
				e.printStackTrace();
			}

		}

		// Remove system GPS , Network , Fuse provider for LocationManager
		removeMockProviders(mMockProviders);

		// cancel notification
		stopForeground(true);
		
	}

	private void handleBroadcastLocation() {

		Log.d(TAG, "handleBroadcastLocation");
		Location location = getLocation();

		for (String providerName : mMockProviders) {
			location.setProvider(providerName);
			Log.d(TAG, "broadcast location" + location);

			try {
				mLocationManager
						.setTestProviderLocation(providerName, location);

				if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    PendingResult<Status> ret = LocationServices.FusedLocationApi.setMockLocation(mGoogleApiClient,location);
                    final Status status = ret.await(1, TimeUnit.SECONDS);
                    if (!status.isSuccess()) {
                        Log.d(TAG," FusedLocationApi setMockLocation fail");
                    }
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		// if state is still need to broadcast , send a delay message to
		// ourself
		synchronized (mMockLocationServiceStatus) {
			Log.d(TAG, "handleBroadcastLocation sendMessageDelayed ");
			if (mMockLocationServiceStatus == MockLocationServiceStatus.BROADCASTING) {
				Message m = Message.obtain(mBackgroundHandler,
						MSG_BROADCAST_LOCATION);
				mBackgroundHandler.sendMessageDelayed(m, report_interval);
			}
		}

	}

	private void addMockProviders(List<String> providers) {

		if (providers == null || providers.isEmpty()) {
			return;
		}

		for (String providerName : providers) {

			try {
				mLocationManager.addTestProvider(providerName, false, false, false, false,
						true, true, true, android.location.Criteria.POWER_LOW,
						android.location.Criteria.ACCURACY_COARSE);
				mLocationManager.setTestProviderEnabled(providerName, true);
				mLocationManager.setTestProviderStatus(providerName,
						LocationProvider.AVAILABLE, null,
						System.currentTimeMillis());
			} catch (Exception e) {
				Log.d(TAG,
						"Failed setting up Mock Location Provider "
								+ e.toString());
			}
		}

	}

	private void removeMockProviders(List<String> providers) {

		if (providers == null || providers.isEmpty()) {
			return;
		}

		for (String providerName : providers) {

			try {
				mLocationManager.clearTestProviderEnabled(providerName);
				mLocationManager.clearTestProviderStatus(providerName);
				mLocationManager.removeTestProvider(providerName);

			} catch (Exception e) {
				Log.d(TAG,
						"Failed clearing up Mock Location Provider "
								+ e.toString());
			}
		}

	}

	private void setLocation(Location location) {
		Log.d(TAG , "setLocation " + location);
		this.mlocation = location;
	}
	
	/**
	 * get current location , if it is null , return a default location at (0,0)
	 */
	private Location getLocation() {
		if(mlocation == null) {
			Log.d(TAG , " mlocation == null " );
			Location location = new Location("");
			ApiAdapterFactory.getApiAdapter().makeLocationComplete(location);
			return location;
		} else {
			return mlocation;
		}
		
	}


	private Notification getNotification(String title , String content ) {
		
		Intent showTaskIntent = new Intent(getApplicationContext(), MapActivity.class);
		//showTaskIntent.setAction(Intent.ACTION_MAIN);
		//showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 
		PendingIntent contentIntent = PendingIntent.getActivity(
				getApplicationContext(),
				0,
				showTaskIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		
		Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.maps_xhdpi);
		
		Notification notification = new NotificationCompat.Builder(getApplicationContext())
				.setContentTitle(title)
				.setContentText(content)
				.setSmallIcon(R.drawable.maps_hdpi)
				.setLargeIcon(bm)
				.setWhen(System.currentTimeMillis())
				.setContentIntent(contentIntent)
				.build();
		
       
        return notification;
	}

	/*
	 * Update the  notification title and content
	 * @param title notification title
	 * @param content  notification content
	 */
	
	private void updateNotification(String title, String content ) {
		Log.d(TAG, "UpdateNotification ++ ");
		mNM.notify(NOTIFICATIONID, getNotification(title,content));
		Log.d(TAG, "UpdateNotification -- ");
	}
	
	/*
	 * Update the location of notification
	 */
	private void updateNotificationLocation() {
		Location location = getLocation();
		updateNotification(getString(R.string.notification_mocklocation_title),
				getString(R.string.notification_mocklocation_secondline , location.getLatitude(), location.getLongitude()));
	}


	private int getStat() {
		return mMockLocationServiceStatus.ordinal();
	}

	private synchronized void setState(MockLocationServiceStatus state) {
		mMockLocationServiceStatus = state;
		mState = mStateEnumMap.get(state);
	}

}



