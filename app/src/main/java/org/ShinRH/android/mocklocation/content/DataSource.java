package org.ShinRH.android.mocklocation.content;


import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.ContentObserver;
import android.net.Uri;

import org.ShinRH.android.mocklocation.utl.Constants;
/**
 * Data source on the phone.
 * 
 * @author 
 */
public class DataSource {

  private final ContentResolver contentResolver;
  private final SharedPreferences sharedPreferences;
  
  public DataSource(Context context) {
    contentResolver = context.getContentResolver();
    sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_MULTI_PROCESS);
  }

  /**
   * Registers a content observer.
   * 
   * @param uri the uri
   * @param observer the observer
   */
  public void registerContentObserver(Uri uri, ContentObserver observer) {
    contentResolver.registerContentObserver(uri, false, observer);
  }

  /**
   * Unregisters a content observer.
   * 
   * @param observer the observer
   */
  public void unregisterContentObserver(ContentObserver observer) {
    contentResolver.unregisterContentObserver(observer);
  }

  /**
   * Registers a shared preference change listener.
   * 
   * @param listener the listener
   */
  public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
    sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
  }

  /**
   * Unregisters a shared preference change listener.
   * 
   * @param listener the listener
   */
  public void unregisterOnSharedPreferenceChangeListener(
      OnSharedPreferenceChangeListener listener) {
    sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
  }
}
