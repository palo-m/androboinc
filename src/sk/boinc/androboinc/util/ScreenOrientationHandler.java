/* 
 * AndroBOINC - BOINC Manager for Android
 * Copyright (C) 2010, Pavol Michalec
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package sk.boinc.androboinc.util;

import sk.boinc.androboinc.debug.Logging;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.util.Log;


public class ScreenOrientationHandler implements OnSharedPreferenceChangeListener {
	private static final String TAG = "ScreenOrientationHandler";

	private final Activity mActivity;
	private int mScreenOrientation = -1;

	public ScreenOrientationHandler(Activity activity) {
		mActivity = activity;
		SharedPreferences globalPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
		globalPrefs.registerOnSharedPreferenceChangeListener(this);
		String orientation = globalPrefs.getString(PreferenceName.SCREEN_ORIENTATION, "-1");
		mScreenOrientation = Integer.parseInt(orientation);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(PreferenceName.SCREEN_ORIENTATION)) {
			String orientation = sharedPreferences.getString(PreferenceName.SCREEN_ORIENTATION, "-1");
			int newOrientation = Integer.parseInt(orientation);
			if (newOrientation == mScreenOrientation) return; // unchanged
			if (Logging.DEBUG) Log.d(TAG, "Orientation setting changed from " + mScreenOrientation + " to " + newOrientation);
			mScreenOrientation = newOrientation;
		}
	}

	public void setOrientation() {
		if (Logging.DEBUG) Log.d(TAG, "setOrientation() for " + mActivity.toString());
		// TODO: Proper orientation handling based on settings and current rotation of device
		switch (mScreenOrientation) {
		case ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED:
			mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			break;
		case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
		case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
			// Force portrait or landscape
			mActivity.setRequestedOrientation(mScreenOrientation);
			break;
		case ActivityInfo.SCREEN_ORIENTATION_SENSOR:
			mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			break;
		}
	}
}
