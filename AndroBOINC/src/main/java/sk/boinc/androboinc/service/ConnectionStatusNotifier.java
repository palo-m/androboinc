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

import sk.boinc.androboinc.BuildConfig;
import sk.boinc.androboinc.BoincManagerActivity;
import sk.boinc.androboinc.R;
import sk.boinc.androboinc.clientconnection.ConnectionManagerCallback.DisconnectCause;
import sk.boinc.androboinc.clientconnection.StatusNotifier;
import sk.boinc.androboinc.util.ClientId;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;


/**
 * Class to show notifications about connection state
 *
 */
public class ConnectionStatusNotifier implements StatusNotifier {
	private static final String TAG = "ConnectionStatusNotifie";

	private static final int CONNECTED_ID = 1;
	private static final int DISCONNECTED_ID = 2;

	private final Context mContext;
	private NotificationManager mNotificationManager;
	private boolean mConnected = false;
	private boolean mDisconnected = false;
	private Handler mHandler = new Handler();
	private Runnable mDelayedTrigger = null;


	public ConnectionStatusNotifier(Context context) {
		if (context == null) throw new NullPointerException();
		if (BuildConfig.DEBUG) Log.d(TAG, "ConnectionStatusNotifier(context=" + context.toString() + ")");
		mContext = context;
		mNotificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		// Cancel possible old notifications
		mNotificationManager.cancel(CONNECTED_ID);
		mNotificationManager.cancel(DISCONNECTED_ID);
	}

	public void cleanup() {
		if (BuildConfig.DEBUG) Log.d(TAG, "cleanup()");
		cancelConnected();
		// We will keep disconnected notification
	}

	private void notifyConnected(String contentText, String tickerText) {
		Intent intent = new Intent(mContext, BoincManagerActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		Notification notification = new Notification(R.drawable.ic_stat_connected, tickerText, System.currentTimeMillis());
		notification.setLatestEventInfo(mContext, mContext.getString(R.string.app_name), contentText, contentIntent);
		notification.flags |= (Notification.FLAG_NO_CLEAR|Notification.FLAG_ONGOING_EVENT);
		cancelDisconnected();
		cancelConnected();
		mNotificationManager.notify(CONNECTED_ID, notification);
		mConnected = true;
	}

	@Override
	public void connected(ClientId host) {
		String contentText = String.format(mContext.getString(R.string.notifyConnected), host.getNickname());
		notifyConnected(contentText, null);
		if (BuildConfig.DEBUG) Log.d(TAG, "Shown connected notification");
	}

	@Override
	public void connectionNoFrontend(ClientId host) {
		String contentText = String.format(mContext.getString(R.string.notifyConnected), host.getNickname());
		String tickerText = String.format(mContext.getString(R.string.notifyConnNoFrontend), host.getNickname());
		notifyConnected(contentText, tickerText);
		if (BuildConfig.DEBUG) Log.d(TAG, "Shown connectedNoFrontend notification");
	}

	private void notifyDisconnected(ClientId host, String contentText, boolean autoCancel) {
		String tickerText = String.format(mContext.getString(R.string.notifyDisconnected), host.getNickname());
		Intent intent = new Intent(mContext, BoincManagerActivity.class).putExtra(ClientId.TAG, host);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		Notification notification = new Notification(R.drawable.ic_stat_disconnected, tickerText, System.currentTimeMillis());
		notification.setLatestEventInfo(mContext, mContext.getString(R.string.app_name), contentText, contentIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		cancelDisconnected();
		cancelConnected();
		mNotificationManager.notify(DISCONNECTED_ID, notification);
		mDisconnected = true;
		if (autoCancel) {
			mDelayedTrigger = new Runnable() {
				@Override
				public void run() {
					if (BuildConfig.DEBUG) Log.d(TAG, "Auto-cancelling disconnected notification (normal disconnect)");
					mDelayedTrigger = null;
					cancelDisconnected();
				}
			};
			mHandler.postDelayed(mDelayedTrigger, 10000);
		}
	}

	@Override
	public void disconnected(ClientId host, DisconnectCause cause) {
		String contentText;
		switch (cause) {
		case NORMAL:
		case CONNECTION_DROP:
			contentText = String.format(mContext.getString(R.string.notifyDiscReconnect), host.getNickname());
			break;
		default:
			contentText = String.format(mContext.getString(R.string.notifyDisconnected), host.getNickname());
			break;
		}
		notifyDisconnected(host, contentText, (cause == DisconnectCause.NORMAL));
		if (BuildConfig.DEBUG) Log.d(TAG, "Shown disconnected notification");
	}

	@Override
	public void disconnectedNoFrontend(ClientId host) {
		String contentText = String.format(mContext.getString(R.string.notifyDiscNoFrontend), host.getNickname());
		notifyDisconnected(host, contentText, false);
		if (BuildConfig.DEBUG) Log.d(TAG, "Shown disconnectedNoFrontend notification");
	}

	private void cancelConnected() {
		if (mConnected) {
			// Discard previous disconnected notification
			mNotificationManager.cancel(CONNECTED_ID);
			mConnected = false;
		}
	}

	@Override
	public void cancelDisconnected() {
		if (mDisconnected) {
			// Discard previous disconnected notification
			mNotificationManager.cancel(DISCONNECTED_ID);
			mDisconnected = false;
		}
		if (mDelayedTrigger != null) {
			mHandler.removeCallbacks(mDelayedTrigger);
			mDelayedTrigger = null;
		}
	}
}
