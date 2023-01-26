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

package sk.boinc.androboinc.bridge;

import sk.boinc.androboinc.BuildConfig;
import sk.boinc.androboinc.clientconnection.ClientReplyReceiver;
import sk.boinc.androboinc.clientconnection.ClientRequestHandler;
import sk.boinc.androboinc.util.PreferenceName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;


public class AutoRefresh implements OnSharedPreferenceChangeListener {
	private static final String TAG = "AutoRefresh";

	public enum RequestType {
		CLIENT_MODE,
		PROJECTS,
		TASKS,
		TRANSFERS,
		MESSAGES
	}

	private final static int RUN_UPDATE = 1;

	private final static int NO_CONNECTIVITY = -1;

	private static class UpdateRequest {
		public final ClientReplyReceiver callback;
		public final RequestType requestType;

		public UpdateRequest(final ClientReplyReceiver callback, final RequestType requestType) {
			this.callback = callback;
			this.requestType = requestType;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof UpdateRequest)) return false;
			return (o.hashCode() == this.hashCode());
		}

		@Override
		public int hashCode() {
			int result = 17;
			result = 31 * result + callback.toString().hashCode();
			result = 31 * result + requestType.toString().hashCode();
			return result;
		}
	}

	/**
	 * This class manages the calls to the request handler based on 
	 * received messages
	 */
	public static class RefreshHandler extends Handler {
		private static final String TAG = "AutoRefresh.RefreshHand";
		private final WeakReference<AutoRefresh> mAutoRefresh;
		/**
		 * Constructor
		 * @param requestHandler - the handler to be called when message arrives
		 */
		RefreshHandler(final AutoRefresh requestHandler) {
			mAutoRefresh = new WeakReference<>(requestHandler);
		}

		@Override
		public void handleMessage(Message msg) {
			AutoRefresh autoRefresh = mAutoRefresh.get();
			if (autoRefresh != null) {
				// The request handler still exists - we can call it
				UpdateRequest request = (UpdateRequest)msg.obj;
				if (autoRefresh.mScheduledUpdates.remove(request)) {
					// So far so good, update was still scheduled
					ClientRequestHandler clientBridge = autoRefresh.mClientBridge;
					if (clientBridge == null) {
						// Cleared meanwhile in AutoRefresh class
						// This indicates cleanup phase, so we really do not need to
						// do the auto-refresh anymore
						// Nevertheless, this should never happen (cleanup() should make it sure)
						// so we emit warning here...
						Log.w(TAG, "handleMessage(): after cleanup(), message ignored");
						return;
					}
					if (BuildConfig.DEBUG) Log.d(TAG, "handleMessage(): triggering automatic update (" + request.callback.toString() + "," + request.requestType.toString() + ")");
					switch (request.requestType) {
					case CLIENT_MODE:
						clientBridge.updateClientMode(request.callback);
						break;
					case PROJECTS:
						clientBridge.updateProjects(request.callback);
						break;
					case TASKS:
						clientBridge.updateTasks(request.callback);
						break;
					case TRANSFERS:
						clientBridge.updateTransfers(request.callback);
						break;
					case MESSAGES:
						clientBridge.updateMessages(request.callback);
						break;						
					default:
						Log.e(TAG, "Unhandled request type: " + request.requestType.toString());
					}
				}
				else {
					// Request removed meanwhile, but message was not removed
					Log.w(TAG, "Orphaned message received - update already removed: (" + request.callback.toString() + "," + request.requestType.toString() + ")");
				}
			}
		}
	}
	

	private RefreshHandler mRefreshHandler;
	private Context mContext = null;
	private ClientRequestHandler mClientBridge;
	private Set<UpdateRequest> mScheduledUpdates = new HashSet<>();
	private int mConnectionType = ConnectivityManager.TYPE_MOBILE;
	private int mAutoRefresh = 0;


	public AutoRefresh(final Context context, final ClientRequestHandler clientRequestHandler) {
		if (BuildConfig.DEBUG) Log.d(TAG, "AutoRefresh()");
		mRefreshHandler = new RefreshHandler(this);
		mClientBridge = clientRequestHandler;
		final ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo ni = (cm == null) ? null : cm.getActiveNetworkInfo();
		mConnectionType = (ni == null) ? NO_CONNECTIVITY : ni.getType();
		if (mConnectionType == NO_CONNECTIVITY) {
			// This is the case when ni == null or cm == null
			mAutoRefresh = 0;      // auto-refresh is disabled
			Log.i(TAG, "Networking not active, disabled auto-refresh");
			// We will not register as preference change listener in this case
		}
		else {
			mContext = context;
			SharedPreferences globalPrefs = PreferenceManager.getDefaultSharedPreferences(context);
			globalPrefs.registerOnSharedPreferenceChangeListener(this);
			if (mConnectionType == ConnectivityManager.TYPE_WIFI) {
				// The current connection type is WiFi
				mAutoRefresh = Integer.parseInt(globalPrefs.getString(PreferenceName.AUTO_UPDATE_WIFI, "0"));
				if (BuildConfig.DEBUG) Log.d(TAG, "Auto-refresh interval is set to: " + mAutoRefresh + " seconds (WiFi)");
			}
			else if (mConnectionType == ConnectivityManager.TYPE_MOBILE) {
				mAutoRefresh = Integer.parseInt(globalPrefs.getString(PreferenceName.AUTO_UPDATE_MOBILE, "0"));
				if (BuildConfig.DEBUG) Log.d(TAG, "Auto-refresh interval is set to: " + mAutoRefresh + " seconds (Mobile)");
			}
			else {
				mConnectionType = ConnectivityManager.TYPE_MOBILE;
				mAutoRefresh = Integer.parseInt(globalPrefs.getString(PreferenceName.AUTO_UPDATE_MOBILE, "0"));
				if (BuildConfig.DEBUG) Log.d(TAG, "Auto-refresh interval is set to: " + mAutoRefresh + " seconds (other connection, default to Mobile)");			
			}
		}
	}

	public void cleanup() {
		if (BuildConfig.DEBUG) Log.d(TAG, "cleanup()");
		if (mContext != null) {
			// We are registered as preference change listener;
			// Let's unregister now
			SharedPreferences globalPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
			globalPrefs.unregisterOnSharedPreferenceChangeListener(this);
			mContext = null;
		}
		if (mRefreshHandler.hasMessages(RUN_UPDATE)) {
			if (BuildConfig.DEBUG) Log.d(TAG, "cleanup(): Removing messages from handler queue");
			mRefreshHandler.removeMessages(RUN_UPDATE);
		}
		mScheduledUpdates.clear();
		mClientBridge = null;
		mAutoRefresh = 0;
		// Since we unregistered preference change listener, 
		// the mAutoRefresh will not change anymore.
		// So further calls of scheduleAutomaticRefresh() will have no effect on this class
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(PreferenceName.AUTO_UPDATE_WIFI) && (mConnectionType == ConnectivityManager.TYPE_WIFI)) {
			mAutoRefresh = Integer.parseInt(sharedPreferences.getString(PreferenceName.AUTO_UPDATE_WIFI, "0"));
			if (BuildConfig.DEBUG) Log.d(TAG, "Auto-refresh interval for WiFi changed to: " + mAutoRefresh + " seconds");
		}
		else if (key.equals(PreferenceName.AUTO_UPDATE_MOBILE) && (mConnectionType == ConnectivityManager.TYPE_MOBILE)) {
			mAutoRefresh = Integer.parseInt(sharedPreferences.getString(PreferenceName.AUTO_UPDATE_MOBILE, "0"));
			if (BuildConfig.DEBUG) Log.d(TAG, "Auto-refresh interval for Mobile changed to: " + mAutoRefresh + " seconds");
		}
	}

	public void scheduleAutomaticRefresh(final ClientReplyReceiver callback, final RequestType requestType) {
		if (mAutoRefresh == 0) return;
		UpdateRequest request = new UpdateRequest(callback, requestType);
		if (mScheduledUpdates.contains(request)) {
			if (BuildConfig.DEBUG) Log.d(TAG, "Entry (" + request.callback.toString() + "," + request.requestType.toString() + ") already scheduled, removing the old schedule");
			removeAutomaticRefresh(request);
		}
		mScheduledUpdates.add(request);
		mRefreshHandler.sendMessageDelayed(mRefreshHandler.obtainMessage(RUN_UPDATE, request), (mAutoRefresh * 1000));
		if (BuildConfig.DEBUG) Log.d(TAG, "Scheduled automatic refresh for (" + request.callback.toString() + "," + request.requestType.toString() + ")");
	}

	public void unscheduleAutomaticRefresh(final ClientReplyReceiver callback) {
		for (UpdateRequest req : mScheduledUpdates) {
			// Found pending auto-update; remove its schedule now
			if (req.callback == callback) {
				mRefreshHandler.removeMessages(RUN_UPDATE, req);
				mScheduledUpdates.remove(req);
				if (BuildConfig.DEBUG)
					Log.d(TAG, "unscheduleAutomaticRefresh(): Removed schedule for entry (" + req.callback.toString() + "," + req.requestType.toString() + ")");
			}
		}
	}

	private void removeAutomaticRefresh(UpdateRequest request) {
		for (UpdateRequest req : mScheduledUpdates) {
			if (req.equals(request)) {
				// The same request - retrieve the original object, as it was the one
				// which was used for posting the delayed message
				if (BuildConfig.DEBUG)
					Log.d(TAG, "mRefreshHandler.hasMessages(RUN_UPDATE, req)=" + mRefreshHandler.hasMessages(RUN_UPDATE, req));
				mRefreshHandler.removeMessages(RUN_UPDATE, req);
				mScheduledUpdates.remove(req);
				break;
			}
		}
	}
}
