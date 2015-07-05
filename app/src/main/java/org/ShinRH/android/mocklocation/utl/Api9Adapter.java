package org.ShinRH.android.mocklocation.utl;

import android.annotation.TargetApi;
import android.content.SharedPreferences.Editor;

@TargetApi(9)
public class Api9Adapter extends Api8Adapter{

	 @Override
	  public void applyPreferenceChanges(Editor editor) {
	    // Apply asynchronously
	    editor.apply();
	  }
}
