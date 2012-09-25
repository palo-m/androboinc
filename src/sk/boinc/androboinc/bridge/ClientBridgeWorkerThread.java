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
import sk.boinc.androboinc.clientconnection.HostInfo;
import sk.boinc.androboinc.clientconnection.MessageInfo;
import sk.boinc.androboinc.clientconnection.ModeInfo;
import sk.boinc.androboinc.clientconnection.ConnectionManagerCallback.DisconnectCause;
import sk.boinc.androboinc.clientconnection.ConnectionManagerCallback.ProgressInd;
import sk.boinc.androboinc.clientconnection.ProjectInfo;
import sk.boinc.androboinc.clientconnection.TaskInfo;
import sk.boinc.androboinc.clientconnection.TransferInfo;
import sk.boinc.androboinc.clientconnection.VersionInfo;
import sk.boinc.androboinc.debug.Logging;
import sk.boinc.androboinc.debug.NetStats;
import sk.boinc.androboinc.util.ClientId;
import android.content.Context;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.Vector;


/**
 * This class handles creation of worker thread as well as posting
 * of requests from requesting thread to the worker thread
 * @see ClientBridgeWorkerThread.ReplyHandler
 */
public class ClientBridgeWorkerThread extends Thread {
	private static final String TAG = "ClientBridgeWorkerThread";

	/**
	 * This class is used to switch from worker thread back to requesting thread
	 * so the data received from network by worker thread are posted
	 * back to UI thread
	 */
	public class ReplyHandler {
		private Handler mBridgeReplyHandler = new Handler();

		public void disconnecting() {
			mBridgeReplyHandler.post(new Runnable() {
				@Override
				public void run() {
					mBridgeReply.disconnecting();
				}
			});
		}

		public void disconnected(final DisconnectCause cause) {
			// This is final call - the cleanup() of ClientBridgeWorkerHandler finished
			final ClientBridge.BridgeReply moribund = mBridgeReply;
			mBridgeReply = null;
			mBridgeReplyHandler.post(new Runnable() {
				@Override
				public void run() {
					moribund.disconnected(cause);
				}
			});
		}

		public void delayedDisconnect(final DisconnectCause cause) {
			mBridgeReplyHandler.post(new Runnable() {
				@Override
				public void run() {
					mBridgeReply.delayedDisconnect(cause);
				}
			});
		}

		public void notifyProgress(final ProgressInd progress) {
			mBridgeReplyHandler.post(new Runnable() {
				@Override
				public void run() {
					mBridgeReply.notifyProgress(progress);
				}
			});
		}

		public void notifyConnected(final VersionInfo clientVersion) {
			mBridgeReplyHandler.post(new Runnable() {
				@Override
				public void run() {
					mBridgeReply.notifyConnected(clientVersion);
				}
			});
		}

		public void notifyDisconnected() {
			mBridgeReplyHandler.post(new Runnable() {
				@Override
				public void run() {
					mBridgeReply.notifyDisconnected();
				}
			});
		}

		public void updatedClientMode(final ClientReplyReceiver callback, final ModeInfo modeInfo) {
			mBridgeReplyHandler.post(new Runnable() {
				@Override
				public void run() {
					mBridgeReply.updatedClientMode(callback, modeInfo);
				}
			});
		}

		public void updatedHostInfo(final ClientReplyReceiver callback, final HostInfo hostInfo) {
			mBridgeReplyHandler.post(new Runnable() {
				@Override
				public void run() {
					mBridgeReply.updatedHostInfo(callback, hostInfo);
				}
			});
		}

		public void updatedProjects(final ClientReplyReceiver callback, final Vector <ProjectInfo> projects) {
			mBridgeReplyHandler.post(new Runnable() {
				@Override
				public void run() {
					mBridgeReply.updatedProjects(callback, projects);
				}
			});
		}

		public void updatedTasks(final ClientReplyReceiver callback, final Vector <TaskInfo> tasks) {
			mBridgeReplyHandler.post(new Runnable() {
				@Override
				public void run() {
					mBridgeReply.updatedTasks(callback, tasks);
				}
			});
		}

		public void updatedTransfers(final ClientReplyReceiver callback, final Vector <TransferInfo> transfers) {
			mBridgeReplyHandler.post(new Runnable() {
				@Override
				public void run() {
					mBridgeReply.updatedTransfers(callback, transfers);
				}
			});
		}

		public void updatedMessages(final ClientReplyReceiver callback, final Vector <MessageInfo> messages) {
			mBridgeReplyHandler.post(new Runnable() {
				@Override
				public void run() {
					mBridgeReply.updatedMessages(callback, messages);
				}
			});
		}
	}

	private ReplyHandler mReplyHandler;
	private ClientBridgeWorkerHandler mHandler;
	private ConditionVariable mLock;
	private ClientBridge.BridgeReply mBridgeReply;
	private Context mContext;
	private NetStats mNetStats;

	public ClientBridgeWorkerThread(
			ConditionVariable lock, 
			final ClientBridge.BridgeReply bridgeReply, 
			final Context context, 
			final NetStats netStats) {
		mLock = lock;
		mBridgeReply = bridgeReply;
		mContext = context;
		mNetStats = netStats;
		mReplyHandler = new ReplyHandler(); // Create in UI thread
		setDaemon(true);
	}

	@Override
	public void run() {
		if (Logging.DEBUG) Log.d(TAG, "run() - Started " + Thread.currentThread().toString());

		// Prepare Looper in this thread, to receive messages
		// "forever"
		Looper.prepare();

		// Create Handler - we must create it within run() method,
		// so it will be associated with this thread
		mHandler = new ClientBridgeWorkerHandler(mReplyHandler, mContext, mNetStats);

		// We have handler, we are ready to receive messages :-)
		if (mLock != null) {
			mLock.open();
			mLock = null;
		}

		// We passed the references to handler, we don't need them here anymore
		mContext = null;
		mNetStats = null;

		// Now, start looping
		Looper.loop();

		// We finished Looper and thread is going to stop 
		if (mLock != null) {
			mLock.open();
			mLock = null;
		}

		mHandler.cleanup();
		mHandler = null;
		if (Logging.DEBUG) Log.d(TAG, "run() - Finished " + Thread.currentThread().toString());
	}

	public void stopThread(ConditionVariable lock) {
		mLock = lock;
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (Logging.DEBUG) Log.d(TAG, "Quit message received, stopping " + Thread.currentThread().toString());
				Looper.myLooper().quit();
			}
		});
	}

	public void connect(final ClientId remoteClient, final boolean retrieveInitialData) {
		// Execute in worker thread
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mHandler.connect(remoteClient, retrieveInitialData);
			}
		});
	}

	public void disconnect() {
		// Run immediately (from UI thread) - NOW!
		mHandler.disconnect();
	}

	public void cancelPendingUpdates(final ClientReplyReceiver callback) {
		// Run immediately (from UI thread) - NOW!
		mHandler.cancelPendingUpdates(callback);
	}

	public void updateClientMode(final ClientReplyReceiver callback) {
		// Execute in worker thread
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mHandler.updateClientMode(callback);
			}
		});
	}

	public void updateHostInfo(final ClientReplyReceiver callback) {
		// Execute in worker thread
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mHandler.updateHostInfo(callback);
			}
		});
	}

	public void updateProjects(final ClientReplyReceiver callback) {
		// Execute in worker thread
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mHandler.updateProjects(callback);
			}
		});
	}

	public void updateTasks(final ClientReplyReceiver callback) {
		// Execute in worker thread
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mHandler.updateTasks(callback);
			}
		});
	}

	public void updateTransfers(final ClientReplyReceiver callback) {
		// Execute in worker thread
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mHandler.updateTransfers(callback);
			}
		});
	}

	public void updateMessages(final ClientReplyReceiver callback) {
		// Execute in worker thread
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mHandler.updateMessages(callback);
			}
		});
	}

	public void runBenchmarks() {
		// Execute in worker thread
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mHandler.runBenchmarks();
			}
		});
	}

	public void setRunMode(final ClientReplyReceiver callback, final int mode) {
		// Execute in worker thread
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mHandler.setRunMode(callback, mode);
			}
		});
	}

	public void setNetworkMode(final ClientReplyReceiver callback, final int mode) {
		// Execute in worker thread
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mHandler.setNetworkMode(callback, mode);
			}
		});
	}

	public void setGpuMode(final ClientReplyReceiver callback, final int mode) {
		// Execute in worker thread
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mHandler.setGpuMode(callback, mode);
			}
		});
	}

	public void shutdownCore() {
		// Execute in worker thread
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mHandler.shutdownCore();
			}
		});
	}

	public void doNetworkCommunication() {
		// Execute in worker thread
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mHandler.doNetworkCommunication();
			}
		});
	}

	public void projectOperation(final ClientReplyReceiver callback, final int operation, final String projectUrl) {
		// Execute in worker thread
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mHandler.projectOperation(callback, operation, projectUrl);
			}
		});
	}

	public void taskOperation(final ClientReplyReceiver callback, final int operation, final String projectUrl, final String taskName) {
		// Execute in worker thread
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mHandler.taskOperation(callback, operation, projectUrl, taskName);
			}
		});
	}

	public void transferOperation(final ClientReplyReceiver callback, final int operation, final String projectUrl, final String fileName) {
		// Execute in worker thread
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mHandler.transferOperation(callback, operation, projectUrl, fileName);
			}
		});
	}
}
