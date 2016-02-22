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
import sk.boinc.androboinc.bridge.AutoRefresh.RequestType;
import sk.boinc.androboinc.clientconnection.ClientReplyReceiver;
import sk.boinc.androboinc.clientconnection.ClientRequestHandler;
import sk.boinc.androboinc.clientconnection.ConnectionManagerCallback.DisconnectCause;
import sk.boinc.androboinc.clientconnection.ConnectionManagerCallback.ProgressInd;
import sk.boinc.androboinc.clientconnection.HostInfo;
import sk.boinc.androboinc.clientconnection.MessageInfo;
import sk.boinc.androboinc.clientconnection.ModeInfo;
import sk.boinc.androboinc.clientconnection.ProjectInfo;
import sk.boinc.androboinc.clientconnection.TaskInfo;
import sk.boinc.androboinc.clientconnection.TransferInfo;
import sk.boinc.androboinc.clientconnection.VersionInfo;
import edu.berkeley.boinc.lite.NetStats;
import sk.boinc.androboinc.util.ClientId;
import android.content.Context;
import android.os.ConditionVariable;
import android.util.Log;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;


/**
 * The <code>ClientBridge</code> runs in UI thread. 
 * Right on its own creation it creates another thread, the worker thread.
 * When request is received at this class, it is posted to worker thread.
 */
public class ClientBridge implements ClientRequestHandler {
	private static final String TAG = "ClientBridge";

	public class BridgeReply {
		private static final String TAG = "ClientBridge.BridgeRepl";
		
		private ClientId mClientId = null;
		
		public void setClientId(ClientId clientId) {
			if (mClientId == null) {
				mClientId = clientId;
			}
		}

		public void disconnecting() {
			// The worker thread started disconnecting
			// This means, that all actions needed to close connection are either
			// processed already, or waiting in queue to be processed
			// We can start clearing of worker thread (will be also done as post,
			// so it will be executed afterwards)
			if (BuildConfig.DEBUG) Log.d(TAG, "disconnecting(), stopping ClientBridgeWorkerThread");
			mWorker.stopThread(null);
			// Clean up periodic updater as well, because no more periodic updates will be needed
			mAutoRefresh.cleanup();
		}

		private void detachCallback(final DisconnectCause cause) {
			if (mCallback != null) {
				// The callback can now delete reference to us
				mCallback.bridgeDisconnected(mClientId, cause);
				mCallback = null;
			}
		}

		public void disconnected(final DisconnectCause cause) {
			if (BuildConfig.DEBUG) Log.d(TAG, "disconnected(cause=" + cause.toString() + ")");
			// The worker thread was cleared completely 
			mWorker = null;
			mRemoteClient = null; // Should already be - here it is again for security
			// We detach the callback, so it can delete reference to this object
			// That should be the last reference, so this object could be garbage collected
			detachCallback(cause);
		}

		public void delayedDisconnect(final DisconnectCause cause) {
			if (BuildConfig.DEBUG) Log.d(TAG, "delayedDisconnect(cause=" + cause.toString() + ")");
			// The disconnection will continue autonomously;
			// We must make sure nothing else is sent out
			mReceivers.clear();
			// The callback can now delete reference to us, but
			// the worker thread continues running until disconnect is finished
			// Worker thread still has the reference to us until disconnected() is called,
			// so the garbage collection can be done afterwards
			detachCallback(cause);
		}

		public void notifyProgress(ProgressInd progress) {
			if (mCallback != null) {
				mCallback.bridgeConnectionProgress(progress);
			}
		}

		public void notifyConnected(VersionInfo clientVersion) {
			mConnected = true;
			if (mCallback != null) {
				mCallback.bridgeConnected(mClientId, clientVersion);
			}
			Iterator<ClientReplyReceiver> it = mReceivers.iterator();
			while (it.hasNext()) {
				ClientReplyReceiver receiver = it.next();
				receiver.clientConnected(ClientBridge.this);
			}
		}

		public void notifyDisconnected() {
			mConnected = false;
			Iterator<ClientReplyReceiver> it = mReceivers.iterator();
			while (it.hasNext()) {
				ClientReplyReceiver receiver = it.next();
				receiver.clientDisconnected();
				if (BuildConfig.DEBUG) Log.d(TAG, "Detached receiver: " + receiver.toString()); // see below clearing of all receivers
			}
			mReceivers.clear();
		}

		public void updatedClientMode(final ClientReplyReceiver callback, final ModeInfo modeInfo) {
			if (callback == null) {
				// No specific callback - broadcast to all receivers
				// This is used for early notification after connect
				Iterator<ClientReplyReceiver> it = mReceivers.iterator();
				while (it.hasNext()) {
					ClientReplyReceiver receiver = it.next();
					receiver.updatedClientMode(modeInfo);
				}
				return;
			}
			// Check whether callback is still present in receivers
			if (mReceivers.contains(callback)) {
				// Observer is still present, so we can call it back with data
				boolean periodicAllowed = callback.updatedClientMode(modeInfo);
				if (periodicAllowed) {
					mAutoRefresh.scheduleAutomaticRefresh(callback, RequestType.CLIENT_MODE);
				}
			}
		}

		public void updatedHostInfo(final ClientReplyReceiver callback, final HostInfo hostInfo) {
			// First, check whether callback is still present in receivers
			if (mReceivers.contains(callback)) {
				// Yes, receiver is still present, so we can call it back with data
				callback.updatedHostInfo(hostInfo);
			}
		}

		public void updatedProjects(final ClientReplyReceiver callback, final Vector <ProjectInfo> projects) {
			if (callback == null) {
				// No specific callback - broadcast to all receivers
				// This is used for early notification after connect
				Iterator<ClientReplyReceiver> it = mReceivers.iterator();
				while (it.hasNext()) {
					ClientReplyReceiver receiver = it.next();
					receiver.updatedProjects(projects);
				}
				return;
			}
			// Check whether callback is still present in receivers
			if (mReceivers.contains(callback)) {
				// Yes, receiver is still present, so we can call it back with data
				boolean periodicAllowed = callback.updatedProjects(projects);
				if (periodicAllowed) {
					mAutoRefresh.scheduleAutomaticRefresh(callback, RequestType.PROJECTS);
				}
			}
		}

		public void updatedTasks(final ClientReplyReceiver callback, final Vector <TaskInfo> tasks) {
			if (callback == null) {
				// No specific callback - broadcast to all receivers
				// This is used for early notification after connect
				Iterator<ClientReplyReceiver> it = mReceivers.iterator();
				while (it.hasNext()) {
					ClientReplyReceiver receiver = it.next();
					receiver.updatedTasks(tasks);
				}
				return;
			}
			// Check whether callback is still present in receivers
			if (mReceivers.contains(callback)) {
				// Yes, receiver is still present, so we can call it back with data
				boolean periodicAllowed = callback.updatedTasks(tasks);
				if (periodicAllowed) {
					mAutoRefresh.scheduleAutomaticRefresh(callback, RequestType.TASKS);
				}
			}
		}

		public void updatedTransfers(final ClientReplyReceiver callback, final Vector <TransferInfo> transfers) {
			if (callback == null) {
				// No specific callback - broadcast to all receivers
				// This is used for early notification after connect
				Iterator<ClientReplyReceiver> it = mReceivers.iterator();
				while (it.hasNext()) {
					ClientReplyReceiver receiver = it.next();
					receiver.updatedTransfers(transfers);
				}
				return;
			}
			// Check whether callback is still present in receivers
			if (mReceivers.contains(callback)) {
				// Yes, receiver is still present, so we can call it back with data
				boolean periodicAllowed = callback.updatedTransfers(transfers);
				if (periodicAllowed) {
					mAutoRefresh.scheduleAutomaticRefresh(callback, RequestType.TRANSFERS);
				}
			}
		}

		public void updatedMessages(final ClientReplyReceiver callback, final Vector <MessageInfo> messages) {
			if (callback == null) {
				// No specific callback - broadcast to all receivers
				// This is used for early notification after connect
				Iterator<ClientReplyReceiver> it = mReceivers.iterator();
				while (it.hasNext()) {
					ClientReplyReceiver receiver = it.next();
					receiver.updatedMessages(messages);
				}
				return;
			}
			// Check whether callback is still present in receivers
			if (mReceivers.contains(callback)) {
				// Yes, receiver is still present, so we can call it back with data
				boolean periodicAllowed = callback.updatedMessages(messages);
				if (periodicAllowed) {
					mAutoRefresh.scheduleAutomaticRefresh(callback, RequestType.MESSAGES);
				}
			}
		}
	}

	private final BridgeReply mBridgeReply = new BridgeReply();

	private Set<ClientReplyReceiver> mReceivers = new HashSet<ClientReplyReceiver>();
	private boolean mConnected = false;

	private ClientBridgeCallback mCallback;
	private ClientBridgeWorkerThread mWorker;

	private ClientId mRemoteClient = null;

	private final AutoRefresh mAutoRefresh;

	/**
	 * Constructs a new <code>ClientBridge</code> and starts worker thread
	 * 
	 * @throws RuntimeException if worker thread cannot start in a timely fashion
	 */
	public ClientBridge(ClientBridgeCallback callback, Context context, NetStats netStats) throws RuntimeException {
		mCallback = callback;
		if (BuildConfig.DEBUG) Log.d(TAG, "Starting ClientBridgeWorkerThread");
		ConditionVariable lock = new ConditionVariable(false);
		mAutoRefresh = new AutoRefresh(context, this);
		mWorker = new ClientBridgeWorkerThread(lock, mBridgeReply, context, netStats);
		mWorker.start();
		boolean runningOk = lock.block(2000); // Locking until new thread fully runs
		if (!runningOk) {
			// Too long time waiting for worker thread to be on-line - cancel it
			Log.e(TAG, "ClientBridgeWorkerThread did not start in 2 seconds");
			throw new RuntimeException("Worker thread cannot start");
		}
		if (BuildConfig.DEBUG) Log.d(TAG, "ClientClientBridgeWorkerThread started successfully");
	}

	public void cleanup() {
		// We are cleaning up - no more callback should be done afterwards
		mCallback = null;
		// We also trigger notification now (before real disconnect) which will
		// also clear the data receivers. So after real disconnect is finished,
		// there will be no further notifications sent
		mBridgeReply.notifyDisconnected();
		disconnect();
	}

	public void registerDataReceiver(ClientReplyReceiver receiver) {
		// Another receiver wants to be notified - add him into collection of receivers
		mReceivers.add(receiver);
		if (BuildConfig.DEBUG) Log.d(TAG, "Attached new receiver: " + receiver.toString());
		if (mConnected) {
			// New receiver is attached while we are already connected
			// Notify new receiver that we are connected, so it can fetch data
			receiver.clientConnected(this);
		}
	}

	public void unregisterDataReceiver(ClientReplyReceiver receiver) {
		// Observer does not want to receive notifications anymore - remove him
		mReceivers.remove(receiver);
		if (mConnected) {
			// The receiver could have automatic refresh pending
			// Remove it now
			mAutoRefresh.unscheduleAutomaticRefresh(receiver);
		}
		if (BuildConfig.DEBUG) Log.d(TAG, "Detached receiver: " + receiver.toString());
	}

	public void connect(final ClientId remoteClient, final boolean retrieveInitialData) {
		if (mRemoteClient != null) {
			// already connected
			Log.e(TAG, "Request to connect to: " + remoteClient.getNickname() + " while already connected to: " + mRemoteClient.getNickname());
			return;
		}
		if (mWorker == null) {
			// After disconnect - it cannot be reused
			Log.e(TAG, "Request to connect to: " + remoteClient.getNickname() + " after being cleaned up");
			return;
		}
		mBridgeReply.setClientId(remoteClient); // For bridge callback
		mRemoteClient = remoteClient;
		mWorker.connect(remoteClient, retrieveInitialData);
	}

	public void disconnect() {
		if (BuildConfig.DEBUG) Log.d(TAG, "disconnect()");
		if (mRemoteClient == null) return; // not connected
		mWorker.disconnect();
		mRemoteClient = null; // This will prevent further triggers towards worker thread
	}

	@Override
	public final ClientId getClientId() {
		return mRemoteClient;
	}

	@Override
	public void updateClientMode(final ClientReplyReceiver callback) {
		if (mRemoteClient == null) return; // not connected
		mWorker.updateClientMode(callback);
	}

	@Override
	public void updateHostInfo(final ClientReplyReceiver callback) {
		if (mRemoteClient == null) return; // not connected
		mWorker.updateHostInfo(callback);
	}

	@Override
	public void updateProjects(final ClientReplyReceiver callback) {
		if (mRemoteClient == null) return; // not connected
		mWorker.updateProjects(callback);
	}

	@Override
	public void updateTasks(final ClientReplyReceiver callback) {
		if (mRemoteClient == null) return; // not connected
		mWorker.updateTasks(callback);
	}

	@Override
	public void updateTransfers(final ClientReplyReceiver callback) {
		if (mRemoteClient == null) return; // not connected
		mWorker.updateTransfers(callback);
	}

	@Override
	public void updateMessages(final ClientReplyReceiver callback) {
		if (mRemoteClient == null) return; // not connected
		mWorker.updateMessages(callback);
	}

	@Override
	public void cancelScheduledUpdates(ClientReplyReceiver callback) {
		if (mRemoteClient == null) return; // not connected
		// Cancel pending updates in worker thread
		mWorker.cancelPendingUpdates(callback);
		// Remove scheduled auto-refresh (if any)
		mAutoRefresh.unscheduleAutomaticRefresh(callback);
	}

	@Override
	public void runBenchmarks() {
		if (mRemoteClient == null) return; // not connected
		mWorker.runBenchmarks();
	}

	@Override
	public void setRunMode(final ClientReplyReceiver callback, final int mode) {
		if (mRemoteClient == null) return; // not connected
		mWorker.setRunMode(callback, mode);
	}

	@Override
	public void setNetworkMode(final ClientReplyReceiver callback, final int mode) {
		if (mRemoteClient == null) return; // not connected
		mWorker.setNetworkMode(callback, mode);
	}

	@Override
	public void setGpuMode(final ClientReplyReceiver callback, final int mode) {
		if (mRemoteClient == null) return; // not connected
		mWorker.setGpuMode(callback, mode);
	}

	@Override
	public void shutdownCore() {
		if (mRemoteClient == null) return; // not connected
		mWorker.shutdownCore();
	}

	@Override
	public void doNetworkCommunication() {
		if (mRemoteClient == null) return; // not connected
		mWorker.doNetworkCommunication();
	}

	@Override
	public void projectOperation(final ClientReplyReceiver callback, final ProjectOp operation, final String projectUrl) {
		if (mRemoteClient == null) return; // not connected
		mWorker.projectOperation(callback, operation.opCode(), projectUrl);
	}

	@Override
	public void taskOperation(final ClientReplyReceiver callback, final TaskOp operation, final String projectUrl, final String taskName) {
		if (mRemoteClient == null) return; // not connected
		mWorker.taskOperation(callback, operation.opCode(), projectUrl, taskName);
	}

	@Override
	public void transferOperation(final ClientReplyReceiver callback, final TransferOp operation, final String projectUrl, final String fileName) {
		if (mRemoteClient == null) return; // not connected
		mWorker.transferOperation(callback, operation.opCode(), projectUrl, fileName);
	}
}
