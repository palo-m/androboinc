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

import sk.boinc.androboinc.BoincManagerActivity;
import sk.boinc.androboinc.R;
import sk.boinc.androboinc.clientconnection.ClientReplyReceiver.DisconnectCause;
import sk.boinc.androboinc.debug.Logging;
import sk.boinc.androboinc.util.ClientId;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * Class to show notifications about connection state
 *
 */
public class ConnectionStatusNotifier {
	private static final String TAG = "ConnectionStatusNotifier";

	private static final int CONNECTED_ID = 1;
	private static final int DISCONNECTED_ID = 2;

	private Context mContext;
	private ClientId mHost;
	private NotificationManager mNotificationManager;
	private boolean mConnected = false;
	private boolean mDisconnected = false;


	public ConnectionStatusNotifier(Context context) {
		mContext = context;
		mNotificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		// TODO:
	}

	public void cleanup() {
		cancelConnected();
		// We will keep disconnected notification
	}

	public void connected(ClientId host) {
		mHost = host;
		// TODO: Make proper notification text
		Intent intent = new Intent(mContext, BoincManagerActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		Notification notification = new Notification(R.drawable.ic_stat_connected, "Connected", System.currentTimeMillis());
		notification.setLatestEventInfo(mContext, mContext.getString(R.string.app_name), mHost.getNickname() + " connected", contentIntent);
		notification.flags |= (Notification.FLAG_NO_CLEAR|Notification.FLAG_ONGOING_EVENT);
		cancelDisconnected();
		cancelConnected();
		mNotificationManager.notify(CONNECTED_ID, notification);
		mConnected = true;
		if (Logging.DEBUG) Log.d(TAG, "Shown connected notification");
	}

	public void disconnected(DisconnectCause cause) {
		// TODO: Make proper notification text
		Intent intent = new Intent(mContext, BoincManagerActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		Notification notification = new Notification(R.drawable.ic_stat_disconnected, "Disconnected", System.currentTimeMillis());
		notification.setLatestEventInfo(mContext, mContext.getString(R.string.app_name), mHost.getNickname() + " disconnected", contentIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		cancelDisconnected();
		cancelConnected();
		mNotificationManager.notify(DISCONNECTED_ID, notification);
		mDisconnected = true;
		if (Logging.DEBUG) Log.d(TAG, "Shown disconnected notification");		
	}

	private void cancelConnected() {
		if (mConnected) {
			// Discard previous disconnected notification
			mNotificationManager.cancel(CONNECTED_ID);
			mConnected = false;
		}
	}

	private void cancelDisconnected() {
		if (mDisconnected) {
			// Discard previous disconnected notification
			mNotificationManager.cancel(DISCONNECTED_ID);
			mDisconnected = false;
		}
	}
}
