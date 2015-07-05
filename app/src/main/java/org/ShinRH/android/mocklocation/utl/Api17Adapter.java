package org.ShinRH.android.mocklocation.utl;

import java.lang.reflect.Method;

import android.annotation.TargetApi;
import android.location.Location;
@TargetApi(17)
public class Api17Adapter extends Api16Adapter {
	@Override
	public void makeLocationComplete(Location location) {
		try {
			Method makeComplete = Location.class
					.getMethod("makeComplete");
			makeComplete.invoke(location);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
