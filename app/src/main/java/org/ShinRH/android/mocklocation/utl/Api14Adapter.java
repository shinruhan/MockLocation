package org.ShinRH.android.mocklocation.utl;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;

@TargetApi(14)
public class Api14Adapter extends Api11Adapter {

	  @Override
	  public void configureActionBarHomeAsUp(Activity activity) {
	    ActionBar actionBar = activity.getActionBar();
	    if (actionBar != null) {
	      actionBar.setHomeButtonEnabled(true);
	      actionBar.setDisplayHomeAsUpEnabled(true);
	    }
	  }
}
