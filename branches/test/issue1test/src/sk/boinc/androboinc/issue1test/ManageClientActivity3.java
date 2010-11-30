/* 
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

package sk.boinc.androboinc.issue1test;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;


public class ManageClientActivity3 extends PreferenceActivity {
	private static final String TAG = "ManageClientActivity3";

	private boolean mTest;

	private class SavedState {
		public final boolean test;

		public SavedState() {
			test = mTest;
			Log.d(TAG, "saved: test=" + test);
		}
		public void restoreState(ManageClientActivity3 activity) {
			activity.mTest = test;
			Log.d(TAG, "restored: mHostInfo=" + activity.mTest);
		}
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
//		// The super-class PreferenceActivity calls setContentView()
//		// in its onCreate(). So we must request window features here.
//		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		Log.d(TAG, "onCreate() 2");
		super.onCreate(savedInstanceState);

		Log.d(TAG, "onCreate() 3");
		// Initializes the preference activity.
		addPreferencesFromResource(R.xml.manage_client_3);

		Log.d(TAG, "onCreate() 4");
		Preference pref;
		ListPreference listPref;

		Log.d(TAG, "onCreate() 5");
		// Currently selected client
		pref = findPreference("selectedHost");
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				// Only dummy
				return true;
			}
		});

		// Run mode
		listPref = (ListPreference)findPreference("actRunMode");
		listPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				ListPreference listPref = (ListPreference)preference;
				CharSequence[] actRunDesc = listPref.getEntries();
				int idx = listPref.findIndexOfValue((String)newValue);
				listPref.setSummary(actRunDesc[idx]);
				boincChangeRunMode(Integer.parseInt((String)newValue));
				return true;
			}
		});

		// Network mode
		listPref = (ListPreference)findPreference("actNetworkMode");
		listPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				ListPreference listPref = (ListPreference)preference;
				CharSequence[] actNetworkDesc = listPref.getEntries();
				int idx = listPref.findIndexOfValue((String)newValue);
				listPref.setSummary(actNetworkDesc[idx]);
				boincChangeNetworkMode(Integer.parseInt((String)newValue));
				return true;
			}
		});

		// Run CPU benchmarks
		pref = findPreference("runBenchmark");
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				boincRunCpuBenchmarks();
				return true;
			}
		});

		// Do network communications
		pref = findPreference("doNetworkCommunication");
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				boincDoNetworkCommunication();
				return true;
			}
		});

		// Shut down connected client
		pref = findPreference("shutDownClient");
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				boincShutdownClient();
				return true;
			}
		});

		Log.d(TAG, "onCreate() 6");
		// Restore state on configuration change (if applicable)
		final SavedState savedState = (SavedState)getLastNonConfigurationInstance();
		if (savedState != null) {
			// Yes, we have the saved state, this is activity re-creation after configuration change
			savedState.restoreState(this);
		}
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
		// Display currently connected host (or "No host connected")
		refreshClientName();
		refreshClientMode();
		Log.d(TAG, "onResume() 2");
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause()");
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy()");
		super.onDestroy();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		final SavedState savedState = new SavedState();
		return savedState;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.manage_client_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuHostInfo:
			return true;
		case R.id.menuDisconnect:
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void refreshClientName() {
		Preference pref = findPreference("selectedHost");
		pref.setSummary(getString(R.string.noHostConnected));
	}

	private void refreshClientMode() {
		// Has not not retrieved mode yet
		// actRunMode preference
		Preference pref = findPreference("actRunMode");
		pref.setSummary(getString(R.string.noHostConnected));
		// All operations depend on actRunMode
		// When this one is disabled, all others are disabled as well...
		pref.setEnabled(false);
		// actNetworkMode preference
		pref = findPreference("actNetworkMode");
		pref.setSummary(getString(R.string.noHostConnected));
	}

	private void boincChangeRunMode(int mode) {
		Toast.makeText(this, "boincChangeRunMode(" + mode + ")", Toast.LENGTH_LONG).show();
	}

	private void boincChangeNetworkMode(int mode) {
		Toast.makeText(this, "boincChangeNetworkMode(" + mode + ")", Toast.LENGTH_LONG).show();
	}

	private void boincRunCpuBenchmarks() {
		Toast.makeText(this, "boincRunCpuBenchmarks()", Toast.LENGTH_LONG).show();
	}

	private void boincDoNetworkCommunication() {
		Toast.makeText(this, "boincDoNetworkCommunication()", Toast.LENGTH_LONG).show();
	}

	private void boincShutdownClient() {
		Toast.makeText(this, "boincShutdownClient()", Toast.LENGTH_LONG).show();
	}
}
