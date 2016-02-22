/* 
 * AndroBOINC - BOINC Manager for Android
 * Copyright (C) 2010, Pavol Michalec
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package sk.boinc.androboinc.util;

import sk.boinc.androboinc.BuildConfig;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;
import android.content.pm.ActivityInfo;


public class ScreenOrientationHandler implements OnSharedPreferenceChangeListener {
	private static final String TAG = "ScreenOrientationHandle";

	private final Activity mActivity;
	private int mChosenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

	public ScreenOrientationHandler(Activity activity) {
		mActivity = activity;
		SharedPreferences globalPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
		globalPrefs.registerOnSharedPreferenceChangeListener(this);
		String orientation = globalPrefs.getString(PreferenceName.SCREEN_ORIENTATION, "-1");
		int savedOrientation = Integer.parseInt(orientation);
		if ( (savedOrientation < ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) ||
				(savedOrientation > ActivityInfo.SCREEN_ORIENTATION_SENSOR) ) {
			savedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
		}
		mChosenOrientation = savedOrientation;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(PreferenceName.SCREEN_ORIENTATION)) {
			String orientation = sharedPreferences.getString(PreferenceName.SCREEN_ORIENTATION, "-1");
			int newOrientation = Integer.parseInt(orientation);
			if ( (newOrientation < ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) ||
					(newOrientation > ActivityInfo.SCREEN_ORIENTATION_SENSOR) ) {
				return;
			}
			if (newOrientation == mChosenOrientation) return; // unchanged
			if (BuildConfig.DEBUG) Log.d(TAG, "Orientation setting changed from " + mChosenOrientation + " to " + newOrientation);
			mChosenOrientation = newOrientation;
		}
	}

	public void setOrientation() {
		if (mChosenOrientation != mActivity.getRequestedOrientation()) {
			if (BuildConfig.DEBUG) Log.d(TAG, "Changing orientation for " + mActivity.toString());
			// mChosenOrientation can have only allowed values (see above)
			//noinspection AndroidLintWrongConstant
			mActivity.setRequestedOrientation(mChosenOrientation);
		}
	}
}
