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

package sk.boinc.androboinc.service;

import sk.boinc.androboinc.bridge.BridgeManager;
import sk.boinc.androboinc.clientconnection.ConnectionManager;
import sk.boinc.androboinc.debug.Logging;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;


public class ConnectionManagerService extends Service {
	private static final String TAG = "ConnectionManagerService";

	private static final int TERMINATE_GRACE_PERIOD_CONN = 45;
	private static final int TERMINATE_GRACE_PERIOD_IDLE = 3;

	public class LocalBinder extends Binder {
		public ConnectionManagerService getService() {
			return ConnectionManagerService.this;
		}
	}

	private final IBinder mBinder = new LocalBinder();
	private final Handler mHandler = new Handler();
	private Runnable mTerminateRunnable = null;

	private ConnectivityStatus mConnectivityStatus = null;
	private ConnectionStatusNotifier mStatusNotifier = null;
	private NetworkStatisticsHandler mNetStats = null;
	private BridgeManager mConnectionManager = null;


	@Override
	public IBinder onBind(Intent intent) {
		if (Logging.DEBUG) Log.d(TAG, "onBind()");
		// Just make sure the service is running:
		startService(new Intent(this, ConnectionManagerService.class));
		return mBinder;
	}

	@Override
	public void onRebind(Intent intent) {
		if (mTerminateRunnable != null) {
			// There is Runnable to stop the service
			// We cancel that request now
			mHandler.removeCallbacks(mTerminateRunnable);
			mTerminateRunnable = null;
			if (Logging.DEBUG) Log.d(TAG, "onRebind() - cancelled stopping of the service");
		}
		else {
			// This is not expected
			if (Logging.ERROR) Log.e(TAG, "onRebind() - mTerminateRunnable empty");
			// We just make sure the service is running
			// If service is still running, it's kept running anyway
			startService(new Intent(this, ConnectionManagerService.class));
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// Create runnable which will stop service after grace period
		mTerminateRunnable = new Runnable() {
			@Override
			public void run() {
				// We remove reference to self
				mTerminateRunnable = null;
				// Stop service
				stopSelf();
				if (Logging.DEBUG) Log.d(TAG, "Stopped service");
			}
		};
		// Post the runnable to self - delayed by grace period
		long gracePeriod;
		if (mConnectionManager.getClientId() != null) {
			gracePeriod = TERMINATE_GRACE_PERIOD_CONN * 1000;
		}
		else {
			gracePeriod = TERMINATE_GRACE_PERIOD_IDLE * 1000;
		}
		mHandler.postDelayed(mTerminateRunnable, gracePeriod);
		if (Logging.DEBUG) Log.d(TAG, "onUnbind() - Started grace period to terminate self");
		return true;
	}

	@Override
	public void onCreate() {
		if (Logging.DEBUG) Log.d(TAG, "onCreate()");
		// Notifications handler
		mStatusNotifier = new ConnectionStatusNotifier(getApplicationContext());
		// Create network statistics handler
		mNetStats = new NetworkStatisticsHandler(getApplicationContext());
		// Finally, create connection manager
		mConnectionManager = new BridgeManager(getApplicationContext(), mStatusNotifier, mNetStats);
		// Add connectivity monitoring (to be notified when connection is down)
		mConnectivityStatus = new ConnectivityStatus(this, mConnectionManager);
	}

	@Override
	public void onDestroy() {
		if (Logging.DEBUG) Log.d(TAG, "onDestroy()");
		// Clean-up connectivity monitoring
		mConnectivityStatus.cleanup();
		mConnectivityStatus = null;
		// Clean-up bridge
		mConnectionManager.cleanup();
		mConnectionManager = null;
		// Clean-up notifications handler
		mStatusNotifier.cleanup();
		mStatusNotifier = null;
		// Clean-up network statistics handler
		mNetStats.cleanup();
		mNetStats = null;
	}

	public final ConnectionManager getConnectionManager() {
		return mConnectionManager;
	}
}
