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

import android.os.ConditionVariable;
import android.util.Log;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


public class BoincClientStub extends Thread {
    private static final String TAG = "BoincClientStub";

    public enum Behavior {
        DEFAULT,
        NO_REPLY,
        FAILURE,
        TRUNCATED_DATA
    }

    private int mPort;
    private Behavior mBehavior = Behavior.DEFAULT;
    private String mPassword = "";
    private boolean mListening = false;
    private ConditionVariable mLock = new ConditionVariable(false);
    private BoincClientStubWorker mWorker = null;

    public BoincClientStub() {
        this(31416);
    }

    public BoincClientStub(int bindPort) {
        Log.d(TAG, "BoincClientStub(" + bindPort + ")");
        mPort = bindPort;
    }

    public boolean startListener() {
        mLock.close();
        start();
        return mLock.block(2000);
    }

    @Override
    public void run() {
        Log.d(TAG, "run() - Started " + Thread.currentThread().toString());

        ServerSocket listenSocket = null;
        try {
            listenSocket = new ServerSocket(mPort, 3, InetAddress.getByName("127.0.0.1"));
            listenSocket.setSoTimeout(1000);
            mListening = true;
            Log.d(TAG, "run() - Created listener on port " + mPort);

            mLock.open();

            while (mListening) {
                try {
                    Socket handlerSocket = listenSocket.accept();
                    if (mListening) {

                        mWorker = new BoincClientStubWorker(handlerSocket, mBehavior);
                        if (!mPassword.equals("")) {
                            mWorker.setPassword(mPassword);
                        }

                        // Looping here:
                        mWorker.run();

                        // Looping finished
                        mWorker.cleanup();
                        mWorker = null;
                    }
                    else {
                        // Stopped meanwhile - we close socket as there is no handler
                        handlerSocket.close();
                    }
                }
                catch (InterruptedIOException e) {
                    // SoTimeout. Do nothing here, go to next loop iteration if we should still listen
                    Log.d(TAG, "run() - timeout for listenSocket.accept()");
                }
                catch (IOException e) {
                    Log.e(TAG, "IOException when calling listenSocket.accept() in " + Thread.currentThread().toString());
                    // Error just during accepting new connection, continue listening
                }
            }
        }
        catch (SocketException e) {
            // setSoTimeout() failed
            Log.e(TAG, "SocketException in " + Thread.currentThread().toString());
        }
        catch (IOException e) {
            Log.e(TAG, "IOException in " + Thread.currentThread().toString());
        }
        finally {
            if (listenSocket != null) {
                try {
                    listenSocket.close();
                }
                catch (IOException e) {
                    Log.e(TAG, "IOException when calling listenSocket.close() in " + Thread.currentThread().toString());
                }
            }
        }

        Log.d(TAG, "run() - Finished " + Thread.currentThread().toString());
        mLock.open();
    }

    public boolean stopListener() {
        Log.d(TAG, "stopListener() - Shutting down the server on port " + mPort);
        if (!mListening) {
            // Already stopped
            return true;
        }
        mLock.close();
        // mLock will open at the end of run()
        mListening = false;
        boolean result = true;
        if (mWorker != null) {
            // Finish the worker gracefully now
            mWorker.stop();
        }
        result = mLock.block(15000);
        if (result) {
            Log.d(TAG, "stopListener() - worker finished gracefully");
        }
        return result;
    }

    public void setBehavior(Behavior behavior) {
        Log.d(TAG, "setBehavior(" + behavior + ")");
        mBehavior = behavior;
        if (mWorker != null) {
            mWorker.setBehavior(mBehavior);
        }
    }

    public void setPassword(String password) {
        Log.d(TAG, "setPassword(" + password + ")");
        mPassword = password;
        if (mWorker != null) {
            mWorker.setPassword(mPassword);
        }
    }
}
