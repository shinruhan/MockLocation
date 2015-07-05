package org.ShinRH.android.mocklocation.utl;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;

@TargetApi(11)
public class Api11Adapter extends Api10Adapter {

	@Override
	public boolean hasDialogTitleDivider() {
		return true;
	}

	@Override
	public void configureActionBarHomeAsUp(Activity activity) {
		ActionBar actionBar = activity.getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}
}
