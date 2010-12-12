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

package sk.boinc.androboinc;

import sk.boinc.androboinc.debug.Logging;
import android.app.Application;
import android.preference.PreferenceManager;
import android.util.Log;


/**
 * Global application point which can be used by any activity.<p>
 * It handles some common stuff:
 * <ul>
 * <li>sets the default values on preferences
 * <li>provides application-wide constants
 * </ul>
 */
public class BoincManagerApplication extends Application {
	private static final String TAG = "BoincManagerApplication";

	public static final String GLOBAL_ID = "sk.boinc.androboinc";
	public static final int DEFAULT_PORT = 31416;

	@Override
	public void onCreate() {
		super.onCreate();
		if (Logging.DEBUG) Log.d(TAG, "onCreate()");
		PreferenceManager.setDefaultValues(this, R.xml.manage_client, false);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		if (Logging.DEBUG) Log.d(TAG, "onTerminate() - finished");
	}
}
