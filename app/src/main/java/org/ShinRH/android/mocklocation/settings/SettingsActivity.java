package org.ShinRH.android.mocklocation.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

import org.ShinRH.android.mocklocation.MockLocationService;
import org.ShinRH.android.mocklocation.R;
import org.ShinRH.android.mocklocation.place.Constants;
import org.ShinRH.android.mocklocation.utl.PreferencesUtils;

public class SettingsActivity extends AbstractSettingsActivity {
	
	private static final String TAG = SettingsActivity.class.getName();
	private Context mContext;
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle bundle) {
	    super.onCreate(bundle);
	    mContext = this;
	    addPreferencesFromResource(R.xml.preference);
	    Preference clearHistoryPreference = (Preference) findPreference(getString(R.string.pref_storage_clear_history_key));
	    
	    clearHistoryPreference.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Log.d(TAG, "onPreferenceClick");
				return clearSearchHistory();
			}
	    });
	    
	    EditTextPreference locationReportIntervalPreference = (EditTextPreference) findPreference(getString(R.string.pref_location_report_interval_key));
	    // Set current report interval to summary
	    locationReportIntervalPreference.setSummary(getString(R.string.pref_location_report_interval_summary
				, Integer.valueOf(PreferencesUtils.getString(mContext, R.string.pref_location_report_interval_key, "1"))));
  
	    locationReportIntervalPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onPreferenceChange " + newValue);
				if (newValue instanceof String) {
					Integer reportInterval = Integer.valueOf((String) newValue);
					Log.d(TAG, "reportInterval " + reportInterval);
					Intent intent = new Intent(mContext, MockLocationService.class);
					intent.putExtra(getString(R.string.pref_location_report_interval_key), reportInterval * 1000);
					startService(intent);
					preference.setSummary(getString(R.string.pref_location_report_interval_summary, reportInterval));
				}
				return true;
			}
		});



	}
	
	public boolean clearSearchHistory() {
		
		Uri.Builder builder = new Uri.Builder();
		builder.scheme("content");
		builder.authority(getString(R.string.search_authority));
		builder.appendPath(Constants.SearchSuggestion.SUGGEST_URI_PATH_DELTEALL);
		Uri uri = builder.build();
		Log.d(TAG, "clearSearchHistory uri "+ uri);
		getApplicationContext().getContentResolver().delete(builder.build(), null, null);
		return true;
		
	}

}
