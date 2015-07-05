package org.ShinRH.android.mocklocation.utl;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Utilities for dump Layout .
 * 
 * 
 */
public class LayoutUtils {
	private static String TAG = LayoutUtils.class.getName();

	public static void dumpLayout(ViewGroup viewGroup, int level) {
		try {
			int index = 0;
			while (true) {

				View v = viewGroup.getChildAt(index);
				if (v == null)
					break;
				LogTAB(level, v.toString());

				if (v instanceof ViewGroup) {
					dumpLayout((ViewGroup) v, level + 1);
				}
				index++;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void LogTAB(int spaces, String string) {
		String TAB = "   ";
		String space = "";
		while (spaces > 0) {
			space += TAB;
			spaces--;
		}

		Log.d(TAG, space + string);
	}
}
