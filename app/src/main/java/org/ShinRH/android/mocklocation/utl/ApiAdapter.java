package org.ShinRH.android.mocklocation.utl;

import android.app.Activity;
import android.app.Notification;
import android.content.SharedPreferences;
import android.location.Location;


/**
 * A set of methods that may be implemented differently depending on the Android
 * API level.
 */
public interface ApiAdapter {
	
	/**
	 * Applies all the changes done to a given preferences editor. Changes may
	 * or may not be applied immediately.
	 * <p>
	 * Due to changes in API level 9.
	 * 
	 * @param editor
	 *            the editor
	 */
	public void applyPreferenceChanges(SharedPreferences.Editor editor);
	
	
	/**
	 * Returns true if GeoCoder is present.
	 * <p>
	 * Due to changes in API level 9.
	 */
	public boolean isGeoCoderPresent();

	/**
	 * Returns true if has dialog title divider.
	 * <p>
	 * Due to changes in API level 11.
	 */
	public boolean hasDialogTitleDivider();
	
	/**
	   * Configures the action bar with the Home button as an Up button. If the
	   * platform doesn't support the action bar, do nothing.
	   * <p>
	   * Due to changes in API level 11.
	   *
	   * @param activity the activity
	   */
	  public void configureActionBarHomeAsUp(Activity activity);
	  
	/**
	 * Make Location complete .
	 * <p>
	 * Due to changes in API level 17.
	 */
	public void makeLocationComplete(Location location);

	 
}
