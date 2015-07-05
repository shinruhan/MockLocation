package org.ShinRH.android.mocklocation.fragments;

import org.ShinRH.android.mocklocation.R;

import android.app.AlertDialog;
import android.app.Dialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentManager;

public class MockLocationSettingsDialogFragment extends AbstractDialogFragment{

	public static final String MOCK_LOCATION_SETTINGS_DIALOG_TAG = "mock_location_settings_diaglog";
	
	DialogInterface.OnClickListener SettingsClick = new DialogInterface.OnClickListener()
	{
		public void onClick(DialogInterface dialog, int which) {
			Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
			//intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings"));
			startActivity(intent);
		}
	};
	
	DialogInterface.OnClickListener CancelClick = new DialogInterface.OnClickListener()
	{
		public void onClick(DialogInterface dialog, int which) {
		//Do nothing
		}
	};
	
	@Override
	protected Dialog createDialog() {
		// TODO Auto-generated method stub
		AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
		.setCancelable(true)
        .setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(R.string.dialog_enable_mocklocation)
		.setPositiveButton(R.string.generic_settings, SettingsClick)
		.setNegativeButton(R.string.generic_cancel,CancelClick).create();
		
		return alertDialog;
	}



}
