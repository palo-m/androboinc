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

import sk.boinc.androboinc.clientconnection.ClientReplyReceiver;
import sk.boinc.androboinc.clientconnection.ClientRequestHandler;
import sk.boinc.androboinc.clientconnection.HostInfo;
import sk.boinc.androboinc.clientconnection.MessageInfo;
import sk.boinc.androboinc.clientconnection.ModeInfo;
import sk.boinc.androboinc.clientconnection.ProjectInfo;
import sk.boinc.androboinc.clientconnection.TaskInfo;
import sk.boinc.androboinc.clientconnection.TransferInfo;
import sk.boinc.androboinc.debug.Logging;
import sk.boinc.androboinc.service.ConnectionManagerService;
import sk.boinc.androboinc.util.ScreenOrientationHandler;
import android.app.Activity;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;


public class MessagesActivity extends ListActivity implements ClientReplyReceiver {
	private static final String TAG = "MessagesActivity";

	private ScreenOrientationHandler mScreenOrientation;

	private ClientRequestHandler mConnectedClientHandler = null;
	private boolean mRequestUpdates = false;
	private boolean mViewUpdatesAllowed = false;
	private boolean mViewDirty = false;

	private Vector<MessageInfo> mMessages = new Vector<MessageInfo>();


	private class SavedState {
		private final Vector<MessageInfo> messages;

		public SavedState() {
			messages = mMessages;
			if (Logging.DEBUG) Log.d(TAG, "saved: messages.size()=" + messages.size());
		}
		public void restoreState(MessagesActivity activity) {
			activity.mMessages = messages;
			if (Logging.DEBUG) Log.d(TAG, "restored: mMessages.size()=" + activity.mMessages.size());
		}
	}

	private class MessageListAdapter extends BaseAdapter {
		private Context mContext;

		public MessageListAdapter(Context context) {
			mContext = context;
		}

		@Override
		public int getCount() {
            return mMessages.size();
        }

		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public boolean isEnabled(int position) {
			return true;
		}

		@Override
		public Object getItem(int position) {
			return mMessages.elementAt(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View layout;
			if (convertView == null) {
				layout = LayoutInflater.from(mContext).inflate(
						R.layout.messages_list_item, parent, false);
			}
			else {
				layout = convertView;
			}
			int color = mContext.getResources().getColorStateList(android.R.color.secondary_text_dark).getDefaultColor();
			if (mMessages.elementAt(position).priority == 2) {
				color = mContext.getResources().getColor(R.color.orange);
			}
			TextView tv;
			tv = (TextView)layout.findViewById(R.id.messageTimestamp);
			tv.setText(mMessages.elementAt(position).time);
			tv.setTextColor(color);
			tv = (TextView)layout.findViewById(R.id.messageProject);
			tv.setText(mMessages.elementAt(position).project);
			tv.setTextColor(color);
			tv = (TextView)layout.findViewById(R.id.messageBody);
			tv.setText(mMessages.elementAt(position).body);
			tv.setTextColor(color);
			return layout;
		}
	}

	private ConnectionManagerService mConnectionManager = null;

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mConnectionManager = ((ConnectionManagerService.LocalBinder)service).getService();
			if (Logging.DEBUG) Log.d(TAG, "onServiceConnected()");
			mConnectionManager.getConnectionManager().registerDataReceiver(MessagesActivity.this);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mConnectionManager = null;
			mConnectedClientHandler = null;
			// This should not happen normally, because it's local service 
			// running in the same process...
			if (Logging.WARNING) Log.w(TAG, "onServiceDisconnected()");
		}
	};

	private void doBindService() {
		if (Logging.DEBUG) Log.d(TAG, "doBindService()");
		getApplicationContext().bindService(new Intent(MessagesActivity.this, ConnectionManagerService.class),
				mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	private void doUnbindService() {
		if (Logging.DEBUG) Log.d(TAG, "doUnbindService()");
		getApplicationContext().unbindService(mServiceConnection);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setListAdapter(new MessageListAdapter(this));
		mScreenOrientation = new ScreenOrientationHandler(this);
		doBindService();
		// Restore state on configuration change (if applicable)
		final SavedState savedState = (SavedState)getLastNonConfigurationInstance();
		if (savedState != null) {
			// Yes, we have the saved state, this is activity re-creation after configuration change
			savedState.restoreState(this);
			if (!mMessages.isEmpty()) {
				// We restored messages - view will be updated on resume (before we will get refresh)
				mViewDirty = true;
			}
		}
		ListView lv = getListView();
		lv.setStackFromBottom(true);
		lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mScreenOrientation.setOrientation();
		mRequestUpdates = true;
		if (mConnectedClientHandler != null) {
			// We are connected right now, request fresh data
			if (Logging.DEBUG) Log.d(TAG, "onResume() - Starting refresh of data");
			mConnectedClientHandler.updateMessages(this);
		}
		mViewUpdatesAllowed = true;
		if (mViewDirty) {
			// There were some updates received while we were not visible
			// The data are stored, but view is not updated yet; Do it now
			sortMessages();
			((BaseAdapter)getListAdapter()).notifyDataSetChanged();
			mViewDirty = false;
			if (Logging.DEBUG) Log.d(TAG, "Delayed refresh of view was done now");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// We shall not request data updates
		mRequestUpdates = false;
		mViewUpdatesAllowed = false;
		// Also remove possibly scheduled automatic updates
		if (mConnectedClientHandler != null) {
			mConnectedClientHandler.cancelScheduledUpdates(this);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mConnectionManager != null) {
			mConnectionManager.getConnectionManager().unregisterDataReceiver(this);
			mConnectedClientHandler = null;
		}
		doUnbindService();
		mScreenOrientation = null;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		final SavedState savedState = new SavedState();
		return savedState;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Activity parent = getParent();
		if (parent != null) {
			return parent.onKeyDown(keyCode, event);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.refresh_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		MenuItem item = menu.findItem(R.id.menuRefresh);
		item.setVisible(mConnectedClientHandler != null);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuRefresh:
			if (mConnectedClientHandler != null) {
				mConnectedClientHandler.updateMessages(this);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void clientConnected(ClientRequestHandler requestHandler) {
		if (Logging.DEBUG) Log.d(TAG, "clientConnected(requestHandler=" + requestHandler.toString() + ")");
		mConnectedClientHandler = requestHandler;
		if (mConnectedClientHandler != null) {
			// Connected client is retrieved
			if (mRequestUpdates) {
				mConnectedClientHandler.updateMessages(this);
			}
		}
	}

	@Override
	public void clientDisconnected() {
		if (Logging.DEBUG) Log.d(TAG, "clientDisconnected()");
		mConnectedClientHandler = null;
		mMessages.clear();
		((BaseAdapter)getListAdapter()).notifyDataSetChanged();
		mViewDirty = false;
	}

	@Override
	public boolean updatedClientMode(ModeInfo modeInfo) {
		// Just ignore
		return false;
	}

	@Override
	public boolean updatedHostInfo(HostInfo hostInfo) {
		// Just ignore
		return false;
	}

	@Override
	public boolean updatedProjects(Vector<ProjectInfo> projects) {
		// Just ignore
		return false;
	}

	@Override
	public boolean updatedTasks(Vector<TaskInfo> tasks) {
		// Just ignore
		return false;
	}

	@Override
	public boolean updatedTransfers(Vector<TransferInfo> transfers) {
		// Just ignore
		return false;
	}

	@Override
	public boolean updatedMessages(Vector<MessageInfo> messages) {
		if (mMessages.size() != messages.size()) {
			// Number of messages has changed (increased)
			// This is the only case when we need an update, because content of messages
			// never changes, only fresh arrived messages are added to list
			mMessages = messages;
			if (mViewUpdatesAllowed) {
				// We are visible, update the view with fresh data
				if (Logging.DEBUG) Log.d(TAG, "Messages are updated, refreshing view");
				sortMessages();
				((BaseAdapter)getListAdapter()).notifyDataSetChanged();
			}
			else {
				// We are not visible, do not perform costly tasks now
				if (Logging.DEBUG) Log.d(TAG, "Messages are updated, but view refresh is delayed");
				mViewDirty = true;
			}
		}
		return mRequestUpdates;
	}


	private void sortMessages() {
		Comparator<MessageInfo> comparator = new Comparator<MessageInfo>() {
			@Override
			public int compare(MessageInfo object1, MessageInfo object2) {
				return object1.seqNo - object2.seqNo;
			}
		};
		Collections.sort(mMessages, comparator);
	}

}
