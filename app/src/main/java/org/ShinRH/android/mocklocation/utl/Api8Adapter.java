package org.ShinRH.android.mocklocation.utl;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.location.Location;


@TargetApi(8)
public class Api8Adapter implements ApiAdapter{

	@Override
	public void applyPreferenceChanges(Editor editor) {
		editor.commit();
	}

	@Override
	public boolean isGeoCoderPresent() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean hasDialogTitleDivider() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void configureActionBarHomeAsUp(Activity activity) {
	    // Do nothing
	}

	
	@Override
	public void makeLocationComplete(Location location) {
		// TODO Auto-generated method stub
		// do nothing
		if(!location.hasAccuracy()) location.setAccuracy(100);
		if(location.getTime() == 0) location.setTime(System.currentTimeMillis());
	}


}
