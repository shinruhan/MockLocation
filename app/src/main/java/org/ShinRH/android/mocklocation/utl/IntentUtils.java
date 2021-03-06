package org.ShinRH.android.mocklocation.utl;

import android.content.Context;
import android.content.Intent;

public class IntentUtils {

	  private IntentUtils() {}

	  /**
	   * Creates an intent with {@link Intent#FLAG_ACTIVITY_CLEAR_TOP} and
	   * {@link Intent#FLAG_ACTIVITY_NEW_TASK}.
	   * 
	   * @param context the context
	   * @param cls the class
	   */
	  public static final Intent newIntent(Context context, Class<?> cls) {
	    return new Intent(context, cls).addFlags(
	        Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	  }

}
