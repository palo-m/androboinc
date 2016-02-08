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

import sk.boinc.androboinc.clientconnection.ClientReplyReceiver;
import sk.boinc.androboinc.clientconnection.ConnectionManager;
import sk.boinc.androboinc.clientconnection.ConnectionManagerCallback;
import sk.boinc.androboinc.clientconnection.ConnectivityListener;
import sk.boinc.androboinc.clientconnection.ConnectionManagerCallback.DisconnectCause;
import sk.boinc.androboinc.clientconnection.ConnectionManagerCallback.ProgressInd;
import sk.boinc.androboinc.clientconnection.StatusNotifier;
import sk.boinc.androboinc.clientconnection.VersionInfo;
import sk.boinc.androboinc.debug.Logging;
import sk.boinc.androboinc.debug.NetStats;
import sk.boinc.androboinc.util.ClientId;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Manager for client bridge connections.
 * The service which created this manager should call {@link #cleanup()} method
 * before the service is terminated to ensure that connection is closed properly.
 *
 * @see sk.boinc.androboinc.clientconnection.ConnectionManager
 */
public class BridgeManager implements ConnectionManager, ClientBridgeCallback, ConnectivityListener {
	private static final String TAG = "BridgeManager";

	private final Context mContext;
	private final StatusNotifier mNotifier;
	private final NetStats mNetStats;
	private ClientBridge mClientBridge = null;
	private Runnable mDeferredConnect = null;
	private VersionInfo mClientVersion = null;
	private ClientId mClientId = null;
	private Set<ClientReplyReceiver> mDataReceivers = new HashSet<ClientReplyReceiver>();
	private Set<ConnectionManagerCallback> mStatusObservers = new HashSet<ConnectionManagerCallback>();
	private boolean mConnectivityAvailable = true;

	/**
	 * Creates new bridge manager.
	 * 
	 * @param context - The context in which service is run
	 * @param notifier - Used for status notification
	 * @param netStats - Used for network statistics collection (can be {@code null})
	 */
	public BridgeManager(final Context context, final StatusNotifier notifier, final NetStats netStats) {
		if (context == null) throw new NullPointerException();
		if (Logging.DEBUG) Log.d(TAG, "BridgeManager()");
		mContext = context;
		mNotifier = notifier;
		mNetStats = netStats;
	}

	/**
	 * Cleans up all the data immediately. 
	 * This method is intended for the case when service is being destroyed.
	 * If a connection is still existing, it is triggered for detach its callbacks
	 * immediately and release autonomously.
	 */
	public void cleanup() {
		if (Logging.DEBUG) Log.d(TAG, "cleanup()");
		mDeferredConnect = null;
		mDataReceivers.clear();
		if (mClientBridge != null) {
			// Detach from bridge immediately.
			// We cannot wait for callback from bridge anymore,
			// so we will notify disconnection autonomously while real disconnect
			// is still in progress (no more notification afterwards)
			Iterator<ConnectionManagerCallback> it = mStatusObservers.iterator();
			while (it.hasNext()) {
				ConnectionManagerCallback observer = it.next();
				observer.clientDisconnected(mClientId, DisconnectCause.NORMAL);
			}
			mStatusObservers.clear();
			mClientBridge.cleanup();
			mClientBridge = null;
			if ( (mNotifier != null) && (mClientId != null) ) {
				mNotifier.disconnectedNoFrontend(mClientId);
			}
		}
		mStatusObservers.clear();
		mClientId = null;
		mClientVersion = null;
	}

	/**
	 * Locally stores reference to the data receiver.
	 * <p>
	 * <ul>
	 * <li>If bridge is connected, the reference is passed to bridge as well</li>
	 * <li>If bridge is not connected, the stored reference will be used later at connection time
	 *     and it will be passed to bridge then.</li>
	 * </ul>
	 * 
	 * @see sk.boinc.androboinc.clientconnection.ConnectionManager#registerDataReceiver(sk.boinc.androboinc.clientconnection.ClientReplyReceiver)
	 */
	@Override
	public void registerDataReceiver(final ClientReplyReceiver receiver) {
		mDataReceivers.add(receiver);
		if (mClientBridge != null) {
			mClientBridge.registerDataReceiver(receiver);
		}
		if (Logging.DEBUG) Log.d(TAG, "Attached new data receiver: " + receiver.toString());
	}

	/**
	 * Deletes locally stored reference to the data receiver.
	 * <p>
	 * If bridge is connected, the reference is also passed to bridge, so it can be deleted there as well.
	 * 
	 * @see sk.boinc.androboinc.clientconnection.ConnectionManager#unregisterDataReceiver(sk.boinc.androboinc.clientconnection.ClientReplyReceiver)
	 */
	@Override
	public void unregisterDataReceiver(final ClientReplyReceiver receiver) {
		mDataReceivers.remove(receiver);
		if (mClientBridge != null) {
			mClientBridge.unregisterDataReceiver(receiver);
		}
		if (Logging.DEBUG) Log.d(TAG, "Detached data receiver: " + receiver.toString());

	}

	/* (non-Javadoc)
	 * @see sk.boinc.androboinc.clientconnection.ConnectionManager#registerStatusObserver(sk.boinc.androboinc.clientconnection.ConnectionManagerCallback)
	 */
	@Override
	public void registerStatusObserver(final ConnectionManagerCallback observer) {
		mStatusObservers.add(observer);
		if (mClientBridge != null) {
			observer.clientConnected(mClientId, mClientVersion);
		}
		if (Logging.DEBUG) Log.d(TAG, "Attached new observer: " + observer.toString());
	}

	/* (non-Javadoc)
	 * @see sk.boinc.androboinc.clientconnection.ConnectionManager#unregisterStatusObserver(sk.boinc.androboinc.clientconnection.ConnectionManagerCallback)
	 */
	@Override
	public void unregisterStatusObserver(final ConnectionManagerCallback observer) {
		mStatusObservers.remove(observer);
		if (Logging.DEBUG) Log.d(TAG, "Detached observer: " + observer.toString());
	}

	/* (non-Javadoc)
	 * @see sk.boinc.androboinc.clientconnection.ConnectionManager#getClientId()
	 */
	@Override
	public final ClientId getClientId() {
		return mClientId;
	}

	/* (non-Javadoc)
	 * @see sk.boinc.androboinc.clientconnection.ConnectionManager#connect(sk.boinc.androboinc.clientconnection.ConnectionManagerCallback, sk.boinc.androboinc.util.ClientId, boolean)
	 */
	@Override
	public void connect(final ConnectionManagerCallback callback, final ClientId host, final boolean retrieveInitialData) {
		if (Logging.DEBUG) Log.d(TAG, "connect()");
		if (mClientBridge != null) {
			// We are already connected
			// First we will disconnect the current bridge.
			// After it will be disconnected, we will re-trigger this connect again
			mDeferredConnect = new Runnable() {
				@Override
				public void run() {
					mDeferredConnect = null;
					connect(callback, host, retrieveInitialData);
				}
			};
			disconnect(null);
			return;
		}
		if (!mConnectivityAvailable) {
			// No connectivity is available, it makes no sense to connect now
			// We will reply only to requester now (not to all observers)
			// Because connection is not attempted (bridge not created)
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					callback.clientDisconnected(host, DisconnectCause.NO_CONNECTIVITY);
				}
			};
			Handler handler = new Handler();
			handler.post(runnable);
			return;
		}
		// Cancel previous disconnected notification
		if (mNotifier != null) {
			mNotifier.cancelDisconnected();
		}
		// Create new bridge
		mClientBridge = new ClientBridge(this, mContext, mNetStats);
		// Propagate all current data receivers to bridge, so they will receive
		// connected status and data
		Iterator<ClientReplyReceiver> it = mDataReceivers.iterator();
		while (it.hasNext()) {
			ClientReplyReceiver receiver = it.next();
			mClientBridge.registerDataReceiver(receiver);
		}
		// Finally, initiate connection to remote client
		mClientBridge.connect(host, retrieveInitialData);
	}

	/* (non-Javadoc)
	 * @see sk.boinc.androboinc.clientconnection.ConnectionManager#disconnect(sk.boinc.androboinc.clientconnection.ConnectionManagerCallback)
	 */
	@Override
	public void disconnect(final ConnectionManagerCallback callback) {
		if (Logging.DEBUG) Log.d(TAG, "disconnect()");
		if (mClientBridge != null) {
			mClientBridge.disconnect();
		}
		else {
			if (Logging.DEBUG) Log.d(TAG, "disconnect() - not connected");
		}
	}

	/* (non-Javadoc)
	 * @see ClientBridgeCallback#bridgeConnectionProgress(ProgressInd)
	 */
	@Override
	public void bridgeConnectionProgress(ProgressInd progress) {
		// Just propagate progress indicator to observers
		if (Logging.DEBUG) Log.d(TAG, "bridgeConnectionProgress()");
		Iterator<ConnectionManagerCallback> it = mStatusObservers.iterator();
		while (it.hasNext()) {
			ConnectionManagerCallback observer = it.next();
			observer.clientConnectionProgress(progress);
		}		
	}

	/* (non-Javadoc)
	 * @see ClientBridgeCallback#bridgeConnected(ClientId, VersionInfo)
	 */
	@Override
	public void bridgeConnected(final ClientId clientId, final VersionInfo clientVersion) {
		if (Logging.DEBUG) Log.d(TAG, "bridgeConnected()");
		if (mClientBridge == null) {
			// cleanup done meanwhile
			return;
		}
		mClientId = clientId;
		mClientVersion = clientVersion;
		Iterator<ConnectionManagerCallback> it = mStatusObservers.iterator();
		while (it.hasNext()) {
			ConnectionManagerCallback observer = it.next();
			observer.clientConnected(mClientId, mClientVersion);
		}		
		if (mNotifier != null) {
			mNotifier.connected(mClientId);
		}
	}

	/* (non-Javadoc)
	 * @see ClientBridgeCallback#bridgeDisconnected(ClientId, DisconnectCause)
	 */
	@Override
	public void bridgeDisconnected(final ClientId clientId, final DisconnectCause cause) {
		if (Logging.DEBUG) Log.d(TAG, "bridgeDisconnected()");
		if (mClientBridge == null) {
			// cleanup done meanwhile
			return;
		}
		if (clientId != null) {
			// Notify observers only if there was connection attempt before
			Iterator<ConnectionManagerCallback> it = mStatusObservers.iterator();
			while (it.hasNext()) {
				ConnectionManagerCallback observer = it.next();
				observer.clientDisconnected(clientId, cause);
			}
			if (mNotifier != null) {
				mNotifier.disconnected(clientId, cause);
			}
		}
		mClientId = null;
		mClientVersion = null;
		mClientBridge = null;
		if (mDeferredConnect != null) {
			// There was new connect scheduled before disconnect
			Handler handler = new Handler();
			handler.post(mDeferredConnect);
		}
	}

	@Override
	public void onConnectivityAvailable(int connectivityType) {
		if (Logging.DEBUG) Log.d(TAG, "onConnectivityAvailable(), connectivity type: " + connectivityType);
		mConnectivityAvailable = true;
		if (mClientBridge != null) {
			if (Logging.DEBUG) Log.d(TAG, "onConnectivityAvailable() while connected to host " + mClientId.getNickname() + ", connectivity type: " + connectivityType);
			// TODO Handle connectivity restoration
		}
	}

	@Override
	public void onConnectivityUnavailable() {
		if (Logging.DEBUG) Log.d(TAG, "onConnectivityUnavailable()");
		mConnectivityAvailable = false;
		if (mClientBridge != null) {
			if (Logging.DEBUG) Log.d(TAG, "onConnectivityUnavailable() while connected to host " + mClientId.getNickname());
			// TODO Handle connectivity loss
		}
	}

	@Override
	public void onConnectivityChangedType(int connectivityType) {
		if (Logging.DEBUG) Log.d(TAG, "onConnectivityChangedType(), new connectivity type: " + connectivityType);
		if (mClientBridge != null) {
			if (Logging.DEBUG) Log.d(TAG, "onConnectivityChangedType() while connected to host " + mClientId.getNickname() + ", new connectivity type: " + connectivityType);
			// TODO Handle connectivity type change
		}
	}
}
