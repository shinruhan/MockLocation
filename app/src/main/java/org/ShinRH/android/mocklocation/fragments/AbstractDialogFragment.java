package org.ShinRH.android.mocklocation.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import org.ShinRH.android.mocklocation.utl.DialogUtils;
public abstract class AbstractDialogFragment extends DialogFragment{

	  @Override
	  public Dialog onCreateDialog(Bundle savedInstanceState) {
	    final Dialog dialog = createDialog();
	    dialog.setOnShowListener(new DialogInterface.OnShowListener() {

	        @Override
	      public void onShow(DialogInterface dialogInterface) {
	        DialogUtils.setDialogTitleColor(getActivity(), dialog);
	        DialogUtils.setDialogTitleDivider(getActivity(), dialog);
	        DialogUtils.dumpLayout(getActivity(), dialog);
	      }
	    });
	    return dialog;
	  }
	  
	  

	  protected abstract Dialog createDialog();
}
