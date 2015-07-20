package org.ShinRH.android.mocklocation.utl;

import android.annotation.TargetApi;
import android.content.Context;
import android.location.Location;
import android.os.PowerManager;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Created by Shin on 7/5/15.
 */
@TargetApi(20)
public class Api20Adapter extends Api19Adapter{

    @Override
    public boolean isScreenOn(Context context) {
        boolean ret = true;
        PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        try {
            Method isInteractive = PowerManager.class
                    .getMethod("isInteractive");
            ret = (boolean)isInteractive.invoke(powerManager);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
