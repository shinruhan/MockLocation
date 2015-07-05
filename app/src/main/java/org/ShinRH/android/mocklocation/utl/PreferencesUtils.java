package org.ShinRH.android.mocklocation.utl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Utilities to access preferences stored in {@link SharedPreferences}.
 * 
 * @author 
 */
public class PreferencesUtils {
	
	public static final int MAP_TYPE_DEFAUlT = 1;

	private PreferencesUtils() {}

	  /**
	   * Gets a preference key
	   * 
	   * @param context the context
	   * @param keyId the key id
	   */
	  public static String getKey(Context context, int keyId) {
	    return context.getString(keyId);
	  }
	  

	  /**
	   * Gets a boolean preference value.
	   * 
	   * @param context the context
	   * @param keyId the key id
	   * @param defaultValue the default value
	   */
	  public static boolean getBoolean(Context context, int keyId, boolean defaultValue) {
	    SharedPreferences sharedPreferences = context.getSharedPreferences(
	        Constants.SETTINGS_NAME, Context.MODE_MULTI_PROCESS);
	    return sharedPreferences.getBoolean(getKey(context, keyId), defaultValue);
	  }
	  

	  /**
	   * Sets a boolean preference value.
	   * 
	   * @param context the context
	   * @param keyId the key id
	   * @param value the value
	   */
	  @SuppressLint("CommitPrefEdits")
	  public static void setBoolean(Context context, int keyId, boolean value) {
	    SharedPreferences sharedPreferences = context.getSharedPreferences(
	        Constants.SETTINGS_NAME, Context.MODE_MULTI_PROCESS);
	    Editor editor = sharedPreferences.edit();
	    editor.putBoolean(getKey(context, keyId), value);
	    ApiAdapterFactory.getApiAdapter().applyPreferenceChanges(editor);
	  }


	  /**
	   * Gets an integer preference value.
	   * 
	   * @param context the context
	   * @param keyId the key id
	   * @param defaultValue the default value
	   */
	  public static int getInt(Context context, int keyId, int defaultValue) {
	    SharedPreferences sharedPreferences = context.getSharedPreferences(
	        Constants.SETTINGS_NAME, Context.MODE_MULTI_PROCESS);
	    return sharedPreferences.getInt(getKey(context, keyId), defaultValue);
	  }

	  /**
	   * Sets an integer preference value.
	   * 
	   * @param context the context
	   * @param keyId the key id
	   * @param value the value
	   */
	  @SuppressLint("CommitPrefEdits")
	  public static void setInt(Context context, int keyId, int value) {
	    SharedPreferences sharedPreferences = context.getSharedPreferences(
	        Constants.SETTINGS_NAME, Context.MODE_MULTI_PROCESS);
	    Editor editor = sharedPreferences.edit();
	    editor.putInt(getKey(context, keyId), value);
	    ApiAdapterFactory.getApiAdapter().applyPreferenceChanges(editor);
	  }
	  
	  /**
	   * Gets a float preference value.
	   * 
	   * @param context the context
	   * @param keyId the key id
	   * @param defaultValue the default value
	   */
	  public static float getFloat(Context context, int keyId, float defaultValue) {
	    SharedPreferences sharedPreferences = context.getSharedPreferences(
	        Constants.SETTINGS_NAME, Context.MODE_MULTI_PROCESS);
	    return sharedPreferences.getFloat(getKey(context, keyId), defaultValue);
	  }

	  /**
	   * Sets a float preference value.
	   * 
	   * @param context the context
	   * @param keyId the key id
	   * @param value the value
	   */
	  @SuppressLint("CommitPrefEdits")
	  public static void setFloat(Context context, int keyId, float value) {
	    SharedPreferences sharedPreferences = context.getSharedPreferences(
	        Constants.SETTINGS_NAME, Context.MODE_MULTI_PROCESS);
	    Editor editor = sharedPreferences.edit();
	    editor.putFloat(getKey(context, keyId), value);
	    ApiAdapterFactory.getApiAdapter().applyPreferenceChanges(editor);
	  }

	  /**
	   * Gets a long preference value.
	   * 
	   * @param context the context
	   * @param keyId the key id
	   */
	  public static long getLong(Context context, int keyId) {
	    SharedPreferences sharedPreferences = context.getSharedPreferences(
	        Constants.SETTINGS_NAME, Context.MODE_MULTI_PROCESS);
	    return sharedPreferences.getLong(getKey(context, keyId), -1L);
	  }

	  /**
	   * Sets a long preference value.
	   * 
	   * @param context the context
	   * @param keyId the key id
	   * @param value the value
	   */
	  @SuppressLint("CommitPrefEdits")
	  public static void setLong(Context context, int keyId, long value) {
	    SharedPreferences sharedPreferences = context.getSharedPreferences(
	        Constants.SETTINGS_NAME, Context.MODE_MULTI_PROCESS);
	    Editor editor = sharedPreferences.edit();
	    editor.putLong(getKey(context, keyId), value);
	    ApiAdapterFactory.getApiAdapter().applyPreferenceChanges(editor);
	  }

	  /**
	   * Gets a string preference value.
	   * 
	   * @param context the context
	   * @param keyId the key id
	   * @param defaultValue default value
	   */
	  public static String getString(Context context, int keyId, String defaultValue) {
	    SharedPreferences sharedPreferences = context.getSharedPreferences(
	        Constants.SETTINGS_NAME, Context.MODE_MULTI_PROCESS);
	    return sharedPreferences.getString(getKey(context, keyId), defaultValue);
	  }

	  /**
	   * Sets a string preference value.
	   * 
	   * @param context the context
	   * @param keyId the key id
	   * @param value the value
	   */
	  @SuppressLint("CommitPrefEdits")
	  public static void setString(Context context, int keyId, String value) {
	    SharedPreferences sharedPreferences = context.getSharedPreferences(
	        Constants.SETTINGS_NAME, Context.MODE_MULTI_PROCESS);
	    Editor editor = sharedPreferences.edit();
	    editor.putString(getKey(context, keyId), value);
	    ApiAdapterFactory.getApiAdapter().applyPreferenceChanges(editor);
	  }
	  

}
