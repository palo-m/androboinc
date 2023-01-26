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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Vector;

import sk.boinc.androboinc.clientconnection.ClientReplyReceiver;
import sk.boinc.androboinc.clientconnection.ClientRequestHandler;
import sk.boinc.androboinc.clientconnection.ConnectionManagerCallback;
import sk.boinc.androboinc.clientconnection.HostInfo;
import sk.boinc.androboinc.clientconnection.MessageInfo;
import sk.boinc.androboinc.clientconnection.ModeInfo;
import sk.boinc.androboinc.clientconnection.ProjectInfo;
import sk.boinc.androboinc.clientconnection.TaskInfo;
import sk.boinc.androboinc.clientconnection.TransferInfo;
import sk.boinc.androboinc.clientconnection.VersionInfo;
import sk.boinc.androboinc.service.ConnectionManagerService;
import sk.boinc.androboinc.util.ClientId;
import sk.boinc.androboinc.util.ScreenOrientationHandler;


public class ManageClientActivity extends PreferenceActivity implements ConnectionManagerCallback, ClientReplyReceiver {
	private static final String TAG = "ManageClientActivity";

	private static final int DIALOG_CONNECT_PROGRESS = 1;
	private static final int DIALOG_NETWORK_DOWN     = 2;
	private static final int DIALOG_WARN_SHUTDOWN    = 3;
	private static final int DIALOG_HOST_INFO 		 = 4;
	private static final int DIALOG_CONNECT_FAILED   = 8;
	private static final int DIALOG_AUTHFAIL_NO_PWD  = 9;
	private static final int DIALOG_AUTHFAIL_BAD_PWD = 10;
	private static final int DIALOG_CONNECTION_DROP  = 11;

	private static final int ACTIVITY_SELECT_HOST = 1;

	private ScreenOrientationHandler mScreenOrientation;

	private ProgressInd mConnectProgressIndicator = ProgressInd.NONE;
	private boolean mDialogsAllowed = false;
	private ModeInfo mClientMode = null;
	private HostInfo mHostInfo = null;
	private boolean mPeriodicModeRetrievalAllowed = false;

	private ClientRequestHandler mConnectedClientHandler = null;
	private boolean mDelayedObserverRegistration = false;
	private ClientId mConnectedClient = null;
	private ClientId mSelectedClient = null;
	private ClientId mLastAttemptedClient = null;

	private class SavedState {
		public final HostInfo hostInfo;

		public SavedState() {
			hostInfo = mHostInfo;
			if (BuildConfig.DEBUG) Log.d(TAG, "saved: hostInfo=" + hostInfo);
		}
		public void restoreState(ManageClientActivity activity) {
			activity.mHostInfo = hostInfo;
			if (BuildConfig.DEBUG) Log.d(TAG, "restored: mHostInfo=" + activity.mHostInfo);
		}
	}

	private ConnectionManagerService mConnectionManager = null;

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mConnectionManager = ((ConnectionManagerService.LocalBinder)service).getService();
			if (BuildConfig.DEBUG) Log.d(TAG, "onServiceConnected()");
			if (mDelayedObserverRegistration) {
				mConnectionManager.getConnectionManager().registerStatusObserver(ManageClientActivity.this);
				mConnectionManager.getConnectionManager().registerDataReceiver(ManageClientActivity.this);
				mDelayedObserverRegistration = false;
			}
			if (mSelectedClient != null) {
				// Some client was selected at the time when service was not bound yet
				// Now the service is available, so connection can proceed
				connectOrReconnect();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mConnectionManager = null;
			mConnectedClientHandler = null;
			// This should not happen normally, because it's local service 
			// running in the same process...
			Log.w(TAG, "onServiceDisconnected()");
			// We also reset client reference to prevent mess
			mConnectedClient = null;
			mSelectedClient = null;
		}
	};

	private void doBindService() {
		if (BuildConfig.DEBUG) Log.d(TAG, "doBindService()");
		bindService(new Intent(ManageClientActivity.this, ConnectionManagerService.class),
				mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	private void doUnbindService() {
		if (BuildConfig.DEBUG) Log.d(TAG, "doUnbindService()");
		unbindService(mServiceConnection);
		mConnectionManager = null;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// The super-class PreferenceActivity calls setContentView()
		// in its onCreate(). So we must request window features here.
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		super.onCreate(savedInstanceState);

		mScreenOrientation = new ScreenOrientationHandler(this);

		doBindService();

		// Initializes the preference activity.
		addPreferencesFromResource(R.xml.manage_client);

		Preference pref;
		ListPreference listPref;

		// Currently selected client
		pref = findPreference("selectedHost");
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				// We use other activity for selection of the host
				startActivityForResult(new Intent(ManageClientActivity.this, HostListActivity.class), ACTIVITY_SELECT_HOST);
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

		// GPU mode
		listPref = (ListPreference)findPreference("actGpuMode");
		listPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				ListPreference listPref = (ListPreference)preference;
				CharSequence[] actGpuDesc = listPref.getEntries();
				int idx = listPref.findIndexOfValue((String)newValue);
				listPref.setSummary(actGpuDesc[idx]);
				boincChangeGpuMode(Integer.parseInt((String)newValue));
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
				showDialog(DIALOG_WARN_SHUTDOWN);
				return true;
			}
		});

		// Restore state on configuration change (if applicable)
		final SavedState savedState = (SavedState)getLastNonConfigurationInstance();
		if (savedState != null) {
			// Yes, we have the saved state, this is activity re-creation after configuration change
			savedState.restoreState(this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mScreenOrientation.setOrientation();
		// We are in foreground, so we want to receive notification when data are updated
		if (mConnectionManager != null) {
			// register right now
			mConnectionManager.getConnectionManager().registerStatusObserver(this);
			mConnectionManager.getConnectionManager().registerDataReceiver(this);
			// Check, whether we are connected now or not
			mConnectedClient = mConnectionManager.getConnectionManager().getClientId();
		}
		else {
			// During creation of activity, we'll receive onServiceConnected() callback afterwards
			mDelayedObserverRegistration = true;
			// We force the "No host connected" display for now (can change as soon as we register as observer)
			mConnectedClient = null;
		}
		// Display currently connected host (or "No host connected")
		refreshClientName();

		// Progress dialog is allowed since now
		mDialogsAllowed = true;

		if (mSelectedClient != null) {
			// We just returned from activity which selected client to connect to
			if (mConnectionManager != null) {
				// Service is bound, we can use it
				connectOrReconnect();
			}
			else {
				// Service not bound at the moment (too slow start? or disconnected itself?)
				Log.i(TAG, "onResume() - Client selected, but service not yet available => binding again");
				doBindService();
			}
		}
		else {
			// Connection to another client is NOT to be started
			// If we are connected, we should also display current run/network mode
			if (mConnectedClient != null) {
				// We are connected - retrieve the current mode
				// Maybe the old one we have is correct, but we are not sure - so we disable it for now
				// The values are still visible, but they are grayed out
				refreshClientModePending();
				// We just registered as data receiver above and we still do not have reference
				// to ClientRequestHandler; mConnectedClientHandler was set to null in onPause()
				// For connected client we will receive clientConnected(ClientRequestHandler)
				// soon and then we will be able to request fresh client mode
			}
			else {
				// We are not connected - update display accordingly
				mClientMode = null;
				refreshClientMode();
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// No more repeated displays
		mPeriodicModeRetrievalAllowed = false;
		mDialogsAllowed = false;
		dismissProgressDialog();
		// Do not receive notifications about state and data availability, as we are not front activity now
		// We will change that when we resume again
		if (mConnectionManager != null) {
			mConnectionManager.getConnectionManager().unregisterDataReceiver(this);
			mConnectionManager.getConnectionManager().unregisterStatusObserver(this);
			mConnectedClientHandler = null; 
			mConnectedClient = null; // will be again retrieved in onResume();
		}
		// In case of service binding is unfinished (i.e. this activity is created and 
		// service bind is triggered, but callback onServiceConnected() not received yet),
		// we will NOT register observer at the time of onServiceConnected(), as we are in
		// background now and we do not want to observe connection
		mDelayedObserverRegistration = false;
		// If we did not perform deferred connect so far, we needn't do that anymore
		mSelectedClient = null;
	}

	@Override
	protected void onDestroy() {
		if (BuildConfig.DEBUG) Log.d(TAG, "onDestroy()");
		doUnbindService();
		mScreenOrientation = null;
		super.onDestroy();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return new SavedState();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		ProgressDialog progressDialog;
		AlertDialog.Builder adBuilder;
		switch (id) {
		case DIALOG_CONNECT_PROGRESS:
			if ( (mConnectProgressIndicator == ProgressInd.NONE) || !mDialogsAllowed ) {
				return null;
			}
			progressDialog = new ProgressDialog(this);
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(true);
			progressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					// Connecting canceled
					mConnectProgressIndicator = ProgressInd.NONE;
					// Disconnect & finish the thread
					boincDisconnect();
				}
			});
			return progressDialog;
		case DIALOG_NETWORK_DOWN:
        	return new AlertDialog.Builder(this)
        		.setIcon(android.R.drawable.ic_dialog_alert)
        		.setTitle(R.string.error)
				.setMessage(R.string.networkUnavailable)
        		.setNegativeButton(R.string.close, null)
        		.create();
		case DIALOG_WARN_SHUTDOWN:
        	return new AlertDialog.Builder(this)
        		.setIcon(android.R.drawable.ic_dialog_alert)
        		.setTitle(R.string.warning)
        		.setMessage(R.string.warnShutdownText)
        		.setPositiveButton(R.string.shutdown,
        			new DialogInterface.OnClickListener() {
        				public void onClick(DialogInterface dialog, int whichButton) {
        					boincShutdownClient();
        				}
        			})
        		.setNegativeButton(R.string.cancel, null)
        		.create();
		case DIALOG_HOST_INFO:
			return new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(R.string.menuHostInfo)
				.setView(LayoutInflater.from(this).inflate(R.layout.dialog, null))
				.setNegativeButton(R.string.ok, null)
				.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						// We don't need data anymore - allow them to be garbage collected
						mHostInfo = null;
					}
				})
				.create();
		case DIALOG_CONNECT_FAILED:
			adBuilder = new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.error)
				.setMessage(R.string.connectFailed)
        		.setNegativeButton(R.string.dismiss, null);
			if (mLastAttemptedClient != null) {
				adBuilder.setPositiveButton(R.string.reconnect, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mSelectedClient = mLastAttemptedClient;
						boincConnect();
					}
				});
			}
        	return adBuilder.create();
		case DIALOG_AUTHFAIL_NO_PWD:
			return new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.error)
				.setMessage(getString(R.string.authFailedNoPwd, getString(R.string.app_name)))
				.setNegativeButton(R.string.dismiss, null)
        		.create();
		case DIALOG_AUTHFAIL_BAD_PWD:
			return new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.error)
				.setMessage(getString(R.string.authFailedWrongPwd, getString(R.string.app_name)))
				.setNegativeButton(R.string.dismiss, null)
        		.create();
		case DIALOG_CONNECTION_DROP:
			adBuilder = new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.error)
				.setMessage(R.string.connectionDropped)
        		.setNegativeButton(R.string.dismiss, null);
			if (mLastAttemptedClient != null) {
				adBuilder.setPositiveButton(R.string.reconnect, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mSelectedClient = mLastAttemptedClient;
						boincConnect();
					}
				});
			}
        	return adBuilder.create();
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_CONNECT_PROGRESS:
			ProgressDialog pd = (ProgressDialog)dialog;
			switch (mConnectProgressIndicator) {
			case CONNECTING:
				pd.setMessage(getString(R.string.connecting));
				break;
			case AUTHORIZATION_PENDING:
				pd.setMessage(getString(R.string.authorization));
				break;
			case INITIAL_DATA:
				pd.setMessage(getString(R.string.retrievingData));				
				break;
			default:
				Log.e(TAG, "Unhandled progress indicator: " + mConnectProgressIndicator.toString());
			}
			break;
		case DIALOG_HOST_INFO:
			TextView text = (TextView)dialog.findViewById(R.id.dialogText);
			text.setText(Html.fromHtml(mHostInfo.htmlText));
			break;
		}
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
		return (mConnectedClient != null);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuHostInfo:
			// Request fresh host-info from client in any case
			if (mConnectedClientHandler != null) {
				showProgressDialog(ProgressInd.INITIAL_DATA);
				mConnectedClientHandler.updateHostInfo(this);
			}
			return true;
		case R.id.menuDisconnect:
			// Disconnect from currently connected client
			boincDisconnect();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (BuildConfig.DEBUG) Log.d(TAG, "onActivityResult()");
		switch (requestCode) {
		case ACTIVITY_SELECT_HOST:
			if (resultCode == RESULT_OK) {
				// Finished successfully - selected the host to which we should connect
				mSelectedClient = data.getParcelableExtra(ClientId.TAG);
			} 
			break;
		default:
			break;
		}
	}


	@Override
	public void clientConnectionProgress(ProgressInd progress) {
		switch (progress) {
		case INITIAL_DATA:
			// We are already connected, so hopefully we can display client ID in title bar
			// as well as progress spinner
			if (mLastAttemptedClient != null) {
				setTitle(mLastAttemptedClient.getNickname());
			}
			setProgressBarIndeterminateVisibility(true);
			// No break here, we drop to next case (dialog update)
		case CONNECTING:
		case AUTHORIZATION_PENDING:
			// Update dialog to display corresponding status, i.e.
			// "Connecting", "Authorization", "Retrieving" 
			showProgressDialog(progress);
			break;
		case XFER_STARTED:
			setProgressBarIndeterminateVisibility(true);
			break;
		case XFER_FINISHED:
			setProgressBarIndeterminateVisibility(false);
			break;
		default:
			Log.e(TAG, "Unhandled progress indicator: " + progress.toString());
		}
	}

	@Override
	public void clientConnected(ClientId clientId, VersionInfo clientVersion) {
		if (BuildConfig.DEBUG) Log.d(TAG, "clientConnected(clientId=\"" + clientId.getNickname() + "\", clientVersion=\"" + clientVersion.version + "\")");
		setProgressBarIndeterminateVisibility(false);
		mConnectedClient = clientId;
		refreshClientName();
		mLastAttemptedClient = mConnectedClient; // For re-connect
		// Trigger retrieval of client run/network modes
		mPeriodicModeRetrievalAllowed = true;
		// Now we wait for clientConnected(ClientRequestHandler) to continue in handling
	}

	@Override
	public void clientDisconnected(ClientId clientId, DisconnectCause cause) {
		if (BuildConfig.DEBUG) Log.d(TAG, "clientDisconnected(clientId=\"" + clientId.getNickname() + "\", cause=" + cause.toString() + ")");
		mConnectedClientHandler = null;
		mConnectedClient = null;
		refreshClientName();
		mClientMode = null;
		refreshClientMode();
		setProgressBarIndeterminateVisibility(false);
		dismissProgressDialog();
		if (mSelectedClient != null) {
			// Connection to another client is deferred, we proceed with it now
			boincConnect();
		}
		else if (mDialogsAllowed) {
			switch (cause) {
			case NO_CONNECTIVITY:
				showDialog(DIALOG_NETWORK_DOWN);
				break;
			case CONNECT_FAILURE:
				showDialog(DIALOG_CONNECT_FAILED);
				break;
			case AUTH_FAIL_NO_PWD:
				showDialog(DIALOG_AUTHFAIL_NO_PWD);
				break;
			case AUTH_FAIL_WRONG_PWD:
				showDialog(DIALOG_AUTHFAIL_BAD_PWD);
				break;
			case CONNECTION_DROP:
				showDialog(DIALOG_CONNECTION_DROP);
				break;
			default:
				// Normal event, e.g. requested disconnect - nothing to do
				break;
			}
		}
		
	}

	@Override
	public void clientConnected(ClientRequestHandler requestHandler) {
		if (BuildConfig.DEBUG) Log.d(TAG, "clientConnected(requestHandler=" + requestHandler.toString() + ")");
		mConnectedClientHandler = requestHandler;
		mConnectedClient = mConnectedClientHandler.getClientId();
		refreshClientName();
		if (mConnectedClientHandler != null) {
			// Trigger retrieval of client run/network modes
			mPeriodicModeRetrievalAllowed = true;
			if (mConnectProgressIndicator != ProgressInd.NONE) {
				// We are still showing dialog about connection progress
				// Connect was just initiated by us (not reported by registering as observer)
				// We will update the dialog text now
				showProgressDialog(ProgressInd.INITIAL_DATA);
			}
			mConnectedClientHandler.updateClientMode(this);
		}
		else {
			// Received connected notification, but client is unknown!
			Log.e(TAG, "Client not connected despite notification");
		}
	}

	@Override
	public void clientDisconnected() {
		if (BuildConfig.DEBUG) Log.d(TAG, "clientDisconnected()");
		mConnectedClientHandler = null;
		mConnectedClient = null;
		refreshClientName();
		mClientMode = null;
		refreshClientMode();
	}

	@Override
	public boolean updatedClientMode(ModeInfo modeInfo) {
		if (BuildConfig.DEBUG) Log.d(TAG, "Client run/network mode info updated, refreshing view");
		mClientMode = modeInfo;
		refreshClientMode();
		dismissProgressDialog();
		return mPeriodicModeRetrievalAllowed;
	}

	@Override
	public boolean updatedHostInfo(HostInfo hostInfo) {
		if (BuildConfig.DEBUG) Log.d(TAG, "Host info received, displaying");
		mHostInfo = hostInfo;
		dismissProgressDialog();
		if (mHostInfo != null) {
			showDialog(DIALOG_HOST_INFO);
		}
		return false;
	}

	@Override
	public boolean updatedMessages(Vector<MessageInfo> messages) {
		// Never requested, nothing to do
		return false;
	}

	@Override
	public boolean updatedProjects(Vector<ProjectInfo> projects) {
		// Never requested, nothing to do
		return false;
	}

	@Override
	public boolean updatedTasks(Vector<TaskInfo> tasks) {
		// Never requested, nothing to do
		return false;
	}

	@Override
	public boolean updatedTransfers(Vector<TransferInfo> transfers) {
		// Never requested, nothing to do
		return false;
	}


	private void refreshClientName() {
		Preference pref = findPreference("selectedHost");
		if (mConnectedClient != null) {
			// We have data about client, so we set the info "nickname (address:port)"
			pref.setSummary(String.format("%s (%s:%d)", mConnectedClient.getNickname(), mConnectedClient.getAddress(), mConnectedClient.getPort()));
		}
		else {
			// Not connected to client
			pref.setSummary(R.string.noHostConnected);
		}
	}

	private void refreshClientMode() {
		if ( (mConnectedClient != null) && (mClientMode != null) ) {
			// 1. The run-mode of currently connected client
			ListPreference listPref = (ListPreference)findPreference("actRunMode");
			CharSequence[] runDesc = listPref.getEntries();
			int idx = listPref.findIndexOfValue(Integer.toString(mClientMode.task_mode));
			listPref.setValueIndex(idx);
			listPref.setSummary(runDesc[idx]);
			// All operations depend on actRunMode
			// When this one is enabled, all others are enabled as well...
			listPref.setEnabled(true);
			// 2. The network mode of currently connected client
			listPref = (ListPreference)findPreference("actNetworkMode");
			runDesc = listPref.getEntries();
			idx = listPref.findIndexOfValue(Integer.toString(mClientMode.network_mode));
			listPref.setValueIndex(idx);
			listPref.setSummary(runDesc[idx]);
			// 3. The GPU mode (if applicable)
			listPref = (ListPreference)findPreference("actGpuMode");
			runDesc = listPref.getEntries();
			idx = listPref.findIndexOfValue(Integer.toString(mClientMode.gpu_mode));
			if (idx != -1) {
				// GPU mode set
				listPref.setValueIndex(idx);
				listPref.setSummary(runDesc[idx]);
				listPref.setEnabled(true);
			}
			else {
				// No GPU info (i.e. client does not support GPU)
				listPref.setEnabled(false);
				listPref.setSummary(R.string.noGpu);
			}
		}
		else {
			// Mode was not retrieved yet
			// actRunMode preference
			Preference pref = findPreference("actRunMode");
			pref.setSummary(R.string.noHostConnected);
			// All operations depend on actRunMode
			// When this one is disabled, all others are disabled as well...
			pref.setEnabled(false);
			// actNetworkMode preference
			pref = findPreference("actNetworkMode");
			pref.setSummary(R.string.noHostConnected);
			// actGpuMode preference
			pref = findPreference("actGpuMode");
			pref.setEnabled(false);
			pref.setSummary(R.string.noHostConnected);
		}
	}

	private void refreshClientModePending() {
		// All operations depend on actRunMode
		// We disable it, so all operations are grayed out and not accessible until
		// pending run/network mode retrieval is finished
		// This is used e.g. in onResume() when we are not sure whether last known modes
		// belong to the same client
		Preference pref = findPreference("actRunMode");
		pref.setEnabled(false);
		if (mClientMode != null) {
			// We have "some" info about modes, but it could be very obsolete
			// actRunMode preference
			ListPreference listPref = (ListPreference)pref;
			CharSequence[] runDesc = listPref.getEntries();
			int idx = listPref.findIndexOfValue(Integer.toString(mClientMode.task_mode));
			listPref.setValueIndex(idx);
			listPref.setSummary(runDesc[idx]);
			// actNetworkMode preference
			listPref = (ListPreference)findPreference("actNetworkMode");
			runDesc = listPref.getEntries();
			idx = listPref.findIndexOfValue(Integer.toString(mClientMode.network_mode));
			listPref.setValueIndex(idx);
			listPref.setSummary(runDesc[idx]);
			// actGpuMode preference
			listPref = (ListPreference)findPreference("actGpuMode");
			listPref.setEnabled(false);
			runDesc = listPref.getEntries();
			idx = listPref.findIndexOfValue(Integer.toString(mClientMode.gpu_mode));
			if (idx != -1) {
				listPref.setValueIndex(idx);
				listPref.setSummary(runDesc[idx]);
			}
			else {
				listPref.setSummary(R.string.noGpu);
			}
		}
		else {
			// No info available (i.e. client never connected or disconnected)
			// actRunMode preference
			pref.setSummary(R.string.retrievingData);
			// actNetworkMode preference
			pref = findPreference("actNetworkMode");
			pref.setSummary(R.string.retrievingData);
			// actGpuMode preference
			pref = findPreference("actGpuMode");
			pref.setEnabled(false);
			pref.setSummary(R.string.retrievingData);
		}
	}

	private void showProgressDialog(final ProgressInd progress) {
		if (mDialogsAllowed) {
			mConnectProgressIndicator = progress;
			showDialog(DIALOG_CONNECT_PROGRESS);
		}
		else if (mConnectProgressIndicator != ProgressInd.NONE) {
			// Not allowed to show progress dialog (i.e. activity restarting/terminating),
			// but we are showing previous progress dialog - dismiss it
			dismissDialog(DIALOG_CONNECT_PROGRESS);
			mConnectProgressIndicator = ProgressInd.NONE;
		}
	}

	private void dismissProgressDialog() {
		if (mConnectProgressIndicator != ProgressInd.NONE) {
			dismissDialog(DIALOG_CONNECT_PROGRESS);
			mConnectProgressIndicator = ProgressInd.NONE;
		}
	}

	private void boincConnect() {
		mClientMode = null;
		refreshClientModePending();
		mConnectionManager.getConnectionManager().connect(this, mSelectedClient, false);
		mLastAttemptedClient = mSelectedClient;
		mSelectedClient = null;
	}

	private void boincDisconnect() {
		mConnectionManager.getConnectionManager().disconnect(this);
		mLastAttemptedClient = null;
	}

	private void connectOrReconnect() {
		if (mConnectedClient == null) {
			// No client connected now, we can safely connect to new one
			boincConnect();
		}
		else {
			// We are currently connected and some client was selected to connect
			// We must check whether it is not the same
			if (mSelectedClient.equals(mConnectedClient)) {
				// The same client was selected, as the one already connected
				// We will not change connection - reset mSelectedClient
				if (BuildConfig.DEBUG) Log.d(TAG, "Selected the same client as already connected: " + mSelectedClient.getNickname() + ", keeping existing connection");
				mSelectedClient = null;
			}
			else {
				if (BuildConfig.DEBUG) Log.d(TAG, "Selected new client: " + mSelectedClient.getNickname() + ", while already connected to: " + mConnectedClient.getNickname() + ", disconnecting it first");
				boincDisconnect();
				// The boincConnect() will be triggered after the clientDisconnected() notification
			}
		}
	}

	private void boincChangeRunMode(int mode) {
		if (mConnectedClientHandler != null) {
			mConnectedClientHandler.setRunMode(this, mode);
		}
	}

	private void boincChangeNetworkMode(int mode) {
		if (mConnectedClientHandler != null) {
			mConnectedClientHandler.setNetworkMode(this, mode);
		}
	}

	private void boincChangeGpuMode(int mode) {
		if (mConnectedClientHandler != null) {
			mConnectedClientHandler.setGpuMode(this, mode);
		}
	}

	private void boincRunCpuBenchmarks() {
		if (mConnectedClientHandler != null) {
			mConnectedClientHandler.runBenchmarks();
			Toast.makeText(this, R.string.clientRunBenchNotify, Toast.LENGTH_LONG).show();
		}
	}

	private void boincDoNetworkCommunication() {
		if (mConnectedClientHandler != null) {
			mConnectedClientHandler.doNetworkCommunication();
			Toast.makeText(this, R.string.clientDoNetCommNotify, Toast.LENGTH_LONG).show();
		}
	}

	private void boincShutdownClient() {
		if (mConnectedClientHandler != null) {
			mConnectedClientHandler.shutdownCore();
			Toast.makeText(this, R.string.clientShutdownNotify, Toast.LENGTH_LONG).show();
		}
	}
}
