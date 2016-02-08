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

package sk.boinc.androboinc;

import sk.boinc.androboinc.debug.Logging;
import sk.boinc.androboinc.util.PreferenceName;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.util.Linkify;
import android.text.util.Linkify.TransformFilter;
import android.util.Log;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Global application point which can be used by any activity.<p>
 * It handles some common stuff:
 * <ul>
 * <li>sets the default values on preferences
 * <li>provides application-wide constants
 * <li>provides basic information about application
 * </ul>
 */
public class BoincManagerApplication extends Application {
	private static final String TAG = "BoincManagerApplication";

	public static final int DEFAULT_PORT = 31416;

	public static enum AppStatus {
		NORMAL,
		NEW_INSTALLED,
		UPGRADED
	}

	private static final int READ_BUF_SIZE = 2048;
	private static final int LICENSE_TEXT_SIZE = 37351;

	private char[] mReadBuffer = new char[READ_BUF_SIZE];
	private StringBuilder mStringBuilder = null;
	private AppStatus mAppStatus = AppStatus.NORMAL;

	@Override
	public void onCreate() {
		super.onCreate();
		if (Logging.DEBUG) Log.d(TAG, "onCreate()");
		PreferenceManager.setDefaultValues(this, R.xml.manage_client, false);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		retrieveAppStatus();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		if (Logging.DEBUG) Log.d(TAG, "onTerminate() - finished");
	}

	@Override
	public void onLowMemory() {
		if (Logging.DEBUG) Log.d(TAG, "onLowMemory()");
		// Let's free what we do not need essentially
		mStringBuilder = null; // So garbage collector will free the memory
		mReadBuffer = null;
		super.onLowMemory();
	}

	private void retrieveAppStatus() {
		SharedPreferences globalPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		int upgradeInfoShownVersion = globalPrefs.getInt(PreferenceName.UPGRADE_INFO_SHOWN_VERSION, 0);
		int currentVersion = 0;
		try {
			currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		}
		catch (NameNotFoundException e) {
			if (Logging.ERROR) Log.e(TAG, "Cannot retrieve application version");
			return;
		}
		if (Logging.DEBUG) Log.d(TAG, "currentVersion=" + currentVersion + ", upgradeInfoShownVersion=" + upgradeInfoShownVersion);
		if (upgradeInfoShownVersion == 0) {
			mAppStatus = AppStatus.NEW_INSTALLED;
		}
		else if (currentVersion > upgradeInfoShownVersion) {
			mAppStatus = AppStatus.UPGRADED;
		}
		else {
			mAppStatus = AppStatus.NORMAL;
		}
	}

	public final AppStatus getAppStatus() {
		return mAppStatus;
	}

	public void upgradeInfoShown() {
		int currentVersion = 0;
		try {
			currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		}
		catch (NameNotFoundException e) {
			if (Logging.ERROR) Log.e(TAG, "Cannot retrieve application version");
			return;
		}
		SharedPreferences globalPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = globalPrefs.edit();
		editor.putInt(PreferenceName.UPGRADE_INFO_SHOWN_VERSION, currentVersion).commit();
		mAppStatus = AppStatus.NORMAL;
	}

	public String getApplicationVersion() {
		if (mStringBuilder == null) mStringBuilder = new StringBuilder(32);
		mStringBuilder.setLength(0);
		mStringBuilder.append(getString(R.string.app_name));
		mStringBuilder.append(" v");
		try {
			mStringBuilder.append(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		}
		catch (NameNotFoundException e) {
			if (Logging.ERROR) Log.e(TAG, "Cannot retrieve application version");
			mStringBuilder.setLength(mStringBuilder.length() - 2); // Truncate " v" set above
		}		
		return mStringBuilder.toString();
	}

	public void setAboutText(TextView text) {
		text.setText(getString(R.string.aboutText, getApplicationVersion(), getString(R.string.app_name)));
		String httpURL = "http://";
		// Link to wiki
		Pattern wikiText = Pattern.compile(getString(R.string.app_name) +" wiki");
		TransformFilter wikiTransformer = new TransformFilter() {
			@Override
			public String transformUrl(Matcher match, String url) {
				return getString(R.string.wikiHowtoAddress);
			}
		};
		Linkify.addLinks(text, wikiText, httpURL, null, wikiTransformer);
		// Link to BOINC.SK page
		Pattern boincskText = Pattern.compile("BOINC\\.SK");
		TransformFilter boincskTransformer = new TransformFilter() {
			@Override
			public String transformUrl(Matcher match, String url) {
				return url.toLowerCase() + "/";
			}
		};
		Linkify.addLinks(text, boincskText, httpURL, null, boincskTransformer);
		// Link to GPLv3 license
		Pattern gplText = Pattern.compile("LGPLv3");
		TransformFilter gplTransformer = new TransformFilter() {
			@Override
			public String transformUrl(Matcher match, String url) {
				return "www.gnu.org/licenses/lgpl-3.0.txt";
			}
		};
		Linkify.addLinks(text, gplText, httpURL, null, gplTransformer);
	}

	public void setNewInstallText(TextView text) {
		text.setText(getString(R.string.newInstall, getString(R.string.app_name), getString(R.string.menuAbout)));
		String httpURL = "http://";
		// Link to wiki
		Pattern wikiText = Pattern.compile(getString(R.string.app_name) +" wiki");
		TransformFilter wikiTransformer = new TransformFilter() {
			@Override
			public String transformUrl(Matcher match, String url) {
				return getString(R.string.wikiHowtoAddress);
			}
		};
		Linkify.addLinks(text, wikiText, httpURL, null, wikiTransformer);
	}

	public void setLicenseText(TextView text) {
		text.setText(Html.fromHtml(readRawText(R.raw.license_lgpl)));
		Linkify.addLinks(text, Linkify.ALL);
	}

	public void setLicenseText2(TextView text) {
		text.setText(Html.fromHtml(readRawText(R.raw.license_gpl)));
		Linkify.addLinks(text, Linkify.ALL);
	}

	public void setChangelogText(TextView text) {
		String changelog = readRawText(R.raw.changelog);
		// Transform plain-text ChangeLog to simple HTML format:
		// 1. Make line beginning with "Version" bold
		String trans1 = changelog.replaceAll("(?m)^([Vv]ersion.*)$", "<b>$1</b>");
		// 2. Append <br> at the end of each line
		String trans2 = trans1.replaceAll("(?m)^(.*)$", "$1<br/>");
		// 3. Add HTML tags
		if (mStringBuilder == null) mStringBuilder = new StringBuilder(32);
		mStringBuilder.setLength(0);
		mStringBuilder.append("<html>\n<body>\n");
		mStringBuilder.append(trans2);
		mStringBuilder.append("\n</body>\n</html>");
		text.setText(Html.fromHtml(mStringBuilder.toString()));
	}

	public String readRawText(final int resource) {
		InputStream inputStream = null;
		if (mReadBuffer == null) mReadBuffer = new char[READ_BUF_SIZE];
		if (mStringBuilder == null) mStringBuilder = new StringBuilder(LICENSE_TEXT_SIZE);
		mStringBuilder.ensureCapacity(LICENSE_TEXT_SIZE);
		mStringBuilder.setLength(0);
		try {
			inputStream = getResources().openRawResource(resource);
			Reader reader = new InputStreamReader(inputStream, "UTF-8");
			int bytesRead;
			do {
				bytesRead = reader.read(mReadBuffer);
				if (bytesRead == -1) break;
				mStringBuilder.append(mReadBuffer, 0, bytesRead);
			} while (true);
			inputStream.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			if (Logging.ERROR) Log.e(TAG, "Error when reading raw resource " + resource);
		}
		return mStringBuilder.toString();
	}
}
