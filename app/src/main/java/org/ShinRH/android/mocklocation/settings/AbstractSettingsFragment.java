package org.ShinRH.android.mocklocation.settings;

import org.ShinRH.android.mocklocation.utl.Constants;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;


public class AbstractSettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		PreferenceManager preferenceManager = getPreferenceManager();
		preferenceManager.setSharedPreferencesName(Constants.SETTINGS_NAME);
		preferenceManager.setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);

		// Set up automatic preferences backup
		// backupPreferencesListener = new BackupPreferencesListener(this);

		// preferenceManager.getSharedPreferences()
		// .registerOnSharedPreferenceChangeListener(backupPreferencesListener);
	}
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getView().setBackgroundColor(Color.BLACK);
        getView().setClickable(true);
    }
    
	/**
	 * Helper class to show this fragment
	 * 
	 * @param fragmentManager
	 *            The fragmentManager of the activity
	 * @param abstractSettingsFragment
	 *            The instance of the SettingsFragment
	 */
	public void show(FragmentManager fragmentManager,
			Fragment abstractSettingsFragment) {
		// Display the fragment as the main content.
		fragmentManager.beginTransaction()
				.replace(android.R.id.content, abstractSettingsFragment)
				.commit();

	}

}
