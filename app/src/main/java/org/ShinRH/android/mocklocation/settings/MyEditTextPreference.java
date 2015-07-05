package org.ShinRH.android.mocklocation.settings;
import org.ShinRH.android.mocklocation.utl.*;
import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

public class MyEditTextPreference extends EditTextPreference {

	  public MyEditTextPreference(Context context) {
	    super(context);
	    
	  }
	  
	  public MyEditTextPreference(Context context, AttributeSet attrs) {
	    super(context, attrs);
	  }

	  @Override
	  protected void showDialog(Bundle state) {
	    super.showDialog(state);
	    DialogUtils.setDialogTitleColor(getContext(), getDialog());
	    DialogUtils.setDialogTitleDivider(getContext(), getDialog());
	    DialogUtils.dumpLayout(getContext(), getDialog());
	  }
}


