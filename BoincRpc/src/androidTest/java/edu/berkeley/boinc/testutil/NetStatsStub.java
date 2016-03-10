/*
 * AndroBOINC - BOINC Manager for Android
 * Copyright (C) 2010 - 2016, Pavol Michalec
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

package edu.berkeley.boinc.testutil;

import edu.berkeley.boinc.NetStats;


public class NetStatsStub implements NetStats {
    private static final String TAG = "NetStatsStub";

    private boolean mIsConnected = false;
    private long mBytesReceived = 0;
    private long mBytesSent = 0;

    @Override
    public void connectionOpened() {
        mIsConnected = true;
    }

    @Override
    public void bytesReceived(int numBytes) {
        mBytesReceived += numBytes;
    }

    @Override
    public void bytesTransferred(int numBytes) {
        mBytesSent += numBytes;
    }

    @Override
    public void connectionClosed() {
        mIsConnected = false;
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    public long getBytesReceived() {
        return mBytesReceived;
    }

    public long getBytesSent() {
        return mBytesSent;
    }
}
