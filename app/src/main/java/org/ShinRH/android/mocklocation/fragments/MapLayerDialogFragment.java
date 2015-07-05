package org.ShinRH.android.mocklocation.fragments;


import org.ShinRH.android.mocklocation.R;
import org.ShinRH.android.mocklocation.utl.PreferencesUtils;
import com.google.android.gms.maps.GoogleMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
/**
 * A DialogFragment to select a map layer.
 * 
 * 
 */
public class MapLayerDialogFragment extends AbstractDialogFragment {

  public static final String MAP_LAYER_DIALOG_TAG = "mapLayer";

  private static final int[] LAYERS = { R.string.menu_map, R.string.menu_satellite,
      R.string.menu_satellite_with_streets, R.string.menu_terrain };
  private static final int[] MAP_TYPES = { GoogleMap.MAP_TYPE_NORMAL, GoogleMap.MAP_TYPE_SATELLITE,
      GoogleMap.MAP_TYPE_HYBRID, GoogleMap.MAP_TYPE_TERRAIN };

  @Override
  protected Dialog createDialog() {
    String[] choices = new String[LAYERS.length];
    for (int i = 0; i < LAYERS.length; i++) {
      choices[i] = getString(LAYERS[i]);
    }

    int mapType = PreferencesUtils.getInt(
        getActivity(), R.string.map_type_key, PreferencesUtils.MAP_TYPE_DEFAUlT);

    return new AlertDialog.Builder(getActivity()).setNegativeButton(R.string.generic_cancel, null)
        .setPositiveButton(R.string.generic_ok, new OnClickListener() {
            @Override
          public void onClick(DialogInterface dialog, int which) {
            Log.d(MAP_LAYER_DIALOG_TAG, "onClick " + which );
            int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
            PreferencesUtils.setInt(getActivity(), R.string.map_type_key, MAP_TYPES[position]);
          }
        }).setSingleChoiceItems(choices, getPositionFromMapType(mapType), null)
        .setTitle(R.string.menu_map_layer).create();
  }

  private int getPositionFromMapType(int mapType) {
    for (int i = 0; i < MAP_TYPES.length; i++) {
      if (MAP_TYPES[i] == mapType) {
        return i;
      }
    }
    return 0;
  }
}
