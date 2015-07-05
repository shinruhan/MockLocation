package org.ShinRH.android.mocklocation;

import android.app.Application;
import android.util.Log;

public class MyContext extends Application {
	private static final String TAG = MyContext.class.getName();
	private static MyContext singleton;
	
    public static MyContext getInstance() {
        return singleton;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        Log.d(TAG, "onCreate");
    }
}
