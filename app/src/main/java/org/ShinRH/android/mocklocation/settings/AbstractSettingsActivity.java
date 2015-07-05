package org.ShinRH.android.mocklocation.settings;


import org.ShinRH.android.mocklocation.utl.*;
import org.ShinRH.android.mocklocation.R;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;

/**
 * An abstract Activity for all the settings activities.
 */
public class AbstractSettingsActivity extends PreferenceActivity  {

  //private BackupPreferencesListener backupPreferencesListener;

  
  @Override
public void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    setVolumeControlStream(TextToSpeech.Engine.DEFAULT_STREAM);
    ApiAdapterFactory.getApiAdapter().configureActionBarHomeAsUp(this);

    PreferenceManager preferenceManager = getPreferenceManager();
    preferenceManager.setSharedPreferencesName(Constants.SETTINGS_NAME);
    preferenceManager.setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);

    // Set up automatic preferences backup
    //backupPreferencesListener = new BackupPreferencesListener(this);

    //preferenceManager.getSharedPreferences()
    //    .registerOnSharedPreferenceChangeListener(backupPreferencesListener);
  }
  
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() != android.R.id.home) {
      return super.onOptionsItemSelected(item);
    }
    finish();
    return true;
  }

  @Override
public void onDestroy() {
    super.onDestroy();
    //PreferenceManager preferenceManager = getPreferenceManager();
    //preferenceManager.getSharedPreferences()
    //    .unregisterOnSharedPreferenceChangeListener(backupPreferencesListener);
  }
  /**
   * Helper class to show this fragment 
   * @param fragmentManager  The fragmentManager of the activity
   * @param abstractSettingsFragment The instance of the SettingsFragment
   */
public void show(FragmentManager fragmentManager, Fragment abstractSettingsFragment ){
	// Display the fragment as the main content.
	fragmentManager.beginTransaction().replace(android.R.id.content,
			abstractSettingsFragment).commit();
	
}

  /**
   * Configures a list preference.
   * 
   * @param listPreference the list preference
   * @param summary the summary array
   * @param options the options array
   * @param values the values array
   * @param value the value
   * @param listener optional listener
   */
  protected void configureListPreference(ListPreference listPreference, final String[] summary,
      final String[] options, final String[] values, String value,
      final OnPreferenceChangeListener listener) {
    listPreference.setEntryValues(values);
    listPreference.setEntries(options);
    listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        @Override
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        updatePreferenceSummary(pref, summary, values, (String) newValue);
        if (listener != null) {
          listener.onPreferenceChange(pref, newValue);
        }
        return true;
      }
    });
    updatePreferenceSummary(listPreference, summary, values, value);
    if (listener != null) {
      listener.onPreferenceChange(listPreference, value);
    }
  }

  /**
   * Update the preference summary.
   * 
   * @param preference the preference
   * @param summary the summary array
   * @param values the values array
   * @param value the value
   */
  private void updatePreferenceSummary(
      Preference preference, String[] summary, String[] values, String value) {
    int index = getIndex(values, value);
    if (index == -1) {
      preference.setSummary(R.string.value_unknown);
    } else {
      preference.setSummary(summary[index]);
    }
  }

  /**
   * Get the array index for a value.
   * 
   * @param values the array
   * @param value the value
   */
  private int getIndex(String[] values, String value) {
    for (int i = 0; i < values.length; i++) {
      if (value.equals(values[i])) {
        return i;
      }
    }
    return -1;
  }
}

