/* 
 * AndroBOINC - BOINC Manager for Android
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

package sk.boinc.androboinc.bridge;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.res.Resources;


/**
 * The <code>Formatter</code> holds the objects which will be reused in single thread.
 * They are usually not thread-safe, but since each instance of this class
 * will be accessed by single thread only, they can be reused then.
 * <p>
 * For formatting a lot of objects is needed only for short time, so
 * reusing of objects could prevent some garbage collection.
 */
public class Formatter {
//	private static final String TAG = "Formatter";
//	private Thread mThread = null;
	private Context mContext = null;
	private Resources mResources = null;
	private StringBuilder mSb = new StringBuilder();
	private SimpleDateFormat mDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public Formatter(Context context) {
//		if (Logging.ON) {
//			mThread = Thread.currentThread();
//			Log.d(TAG, "Created for " + mThread.toString());
//		}
		mContext = context;
		mResources = mContext.getResources();
	}

	public void cleanup() {
//		if (Logging.ON) {
//			Thread currentThread = Thread.currentThread();
//			Log.d(TAG, "cleanup() for " + mThread.toString() + ", triggered by " + currentThread.toString());
//		}
//		mThread = null;
		mContext = null;
		mResources = null;
		mSb = null;
		mDateFormatter = null;
	}

	public final Resources getResources() {
		return mResources;
	}

	public final StringBuilder getStringBuilder() {
//		if (Logging.ON) {
//			Thread currentThread = Thread.currentThread();
//			if (currentThread != mThread)
//				Log.e(TAG, "Created for " + mThread.toString() + ", but used by " + currentThread.toString());
//		}
		mSb.setLength(0);
		return mSb;
	}

	public final String formatDate(long time) {
//		if (Logging.ON) {
//			Thread currentThread = Thread.currentThread();
//			if (currentThread != mThread)
//				Log.e(TAG, "Created for " + mThread.toString() + ", but used by " + currentThread.toString());
//		}
		Date date = new Date((long)(time*1000));
		return mDateFormatter.format(date).toString();
	}

	public static final String formatElapsedTime(long seconds) {
		long hours = seconds / 3600;
		int remain = (int)(seconds % 3600);
		int minutes = remain / 60;
		remain = remain % 60;
		return String.format("%02d:%02d:%02d", hours, minutes, remain);
	}

	public static final String formatSize(long size) {
		if (size > 1000000L) {
			// Megabytes
			return String.format("%.1f MB", ((float)size/1000000.0F));
		}
		else if (size > 1000) {
			// Kilobytes
			return String.format("%.1f KB", ((float)size/1000.0F));
		}
		else {
			// Bytes
			return String.format("%d B", size);
		}
	}

	public static final String formatSpeed(float speed) {
		if (speed > 1000000.0F) {
			// Megabytes per second
			return String.format("%.1f MB/s", (speed/1000000.0F));
		}
		else if (speed > 1000.0F) {
			// Kilobytes per second
			return String.format("%.1f KB/s", (speed/1000.0F));
		}
		else {
			// Bytes
			return String.format("%.1f B/s", speed);
		}
	}
}
