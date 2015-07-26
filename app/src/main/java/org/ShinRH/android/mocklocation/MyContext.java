package org.ShinRH.android.mocklocation;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

public class MyContext extends Application implements ConnectionCallbacks, OnConnectionFailedListener  {
	private static final String TAG = MyContext.class.getName();
	private static MyContext singleton;
    private static GoogleApiClient mGoogleApiClient;
    public static MyContext getInstance() {
        return singleton;
    }
    public static GoogleApiClient getGoogleApiClient() { return mGoogleApiClient ;}

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;

        // Create a GoogleApiClient instance
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        Log.d(TAG, "onCreate");
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
}
