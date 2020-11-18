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

import edu.berkeley.boinc.Md5;
import edu.berkeley.boinc.testutil.BoincClientStub.Behavior;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class BoincClientStubWorker {
    private static final String TAG = "BoincClientStubWorker";

    private static final int READ_BUF_SIZE = 8192;

    private Socket mSocket;
    private final PrintWriter mOutput;
    private final BufferedReader mInput;
    private final StringBuilder mStringBuilder = new StringBuilder();
    private boolean mProcessing = false;
    private Behavior mBehavior = Behavior.DEFAULT;
    private String mPassword = "";
    private boolean mAuthorized = true;
    private String mNonceHash = "";

    public BoincClientStubWorker(Socket socket, Behavior behavior) throws IOException {
        Log.d(TAG, "BoincClientStubHandler(" + socket.toString() + ")");
        mSocket = socket;
        mSocket.setSoTimeout(1000);
        mInput = new BufferedReader(new InputStreamReader(mSocket.getInputStream()), READ_BUF_SIZE);
        mOutput = new PrintWriter(mSocket.getOutputStream(), false);
        mStringBuilder.setLength(0);
        mBehavior = behavior;
    }

    public void stop() {
        Log.d(TAG, "stop()");
        synchronized (this) {
            mProcessing = false;
        }
    }

    public void setBehavior(Behavior behavior) {
        synchronized (this) {
            Log.d(TAG, "setBehavior(" + behavior + ")");
            mBehavior = behavior;
        }
    }

    public void setPassword(String password) {
        synchronized (this) {
            Log.d(TAG, "setPassword(" + password + ")");
            if (!mPassword.equals(password)) {
                mPassword = password;
                mAuthorized = false;
            }
        }
    }

    public void cleanup() {
        Log.d(TAG, "cleanup()");

        try {
            mInput.close();
            Log.d(TAG, "cleanup() closed mInput");
        }
        catch (IOException e) {
            Log.e(TAG, "Error closing input " + mInput.toString());
        }

        mOutput.close();
        Log.d(TAG, "cleanup() closed mOutput");

        if (mSocket != null) {
            if (mSocket.isConnected()) {
                Log.w(TAG, "Closing still connected socket " + mSocket.toString());
            }
            try {
                mSocket.close();
                Log.d(TAG, "cleanup() closed mSocket");
            }
            catch (IOException e) {
                Log.e(TAG, "Error closing socket " + mSocket.toString());
            }
            finally {
                mSocket = null;
            }
        }
    }

    public void run() {
        Log.d(TAG, "run() - begin");
        boolean processing = mProcessing = true;

        while (processing) {
            try {
                int readChar = mInput.read();
                if (readChar == -1) {
                    break;
                }
                if (readChar == '\003') {
                    handleRequest(mStringBuilder.toString());
                    mStringBuilder.setLength(0);
                }
                else {
                    mStringBuilder.append((char) readChar);
                }
            }
            catch (IOException e) {
                // Log.d(TAG, "run() IOException");
                // Socket timeout - no data received for some time
                // Nothing to do - expected event (SO-timeout was set to 1000 ms)
            }
            synchronized (this) {
                processing = mProcessing;
            }
        }

        if (mStringBuilder.length() != 0) {
            Log.d(TAG, "run() incomplete data: \"" + mStringBuilder.toString() + "\"");
        }

        Log.d(TAG, "run() - end");
    }

    private void handleRequest(String request) {
        Log.d(TAG, "handleRequest(" + request + ")");
        Behavior behavior;
        synchronized (this) {
            behavior = mBehavior;
        }
        if (behavior == Behavior.NO_REPLY) {
            Log.d(TAG, "handleRequest(): behavior NO_REPLY, not doing anything");
            return;
        }
        else if (behavior == Behavior.TRUNCATED_DATA) {
            mOutput.println("<boinc_gui_rpc_reply>");
            mOutput.print("<");
            mOutput.flush();
            return;
        }
        else if (behavior == Behavior.FAILURE) {
            mOutput.println("<boinc_gui_rpc_reply>");
            mOutput.println("<failure/>");
            mOutput.print("</boinc_gui_rpc_reply>");
            mOutput.print('\003');
            mOutput.flush();
            return;
        }
        String detectedRequest = RpcRequestParser.parse(request);
        if (detectedRequest.equals("auth1")) {
            // Authorization part 1
            if (!mAuthorized) {
                final float nonce = System.currentTimeMillis() / 1000;
                mNonceHash = Md5.hash(nonce + mPassword);
                mOutput.println("<boinc_gui_rpc_reply>");
                mOutput.print("<nonce>");
                mOutput.print(nonce);
                mOutput.println("</nonce>");
            }
            else {
                mOutput.println("<boinc_gui_rpc_reply>");
                mOutput.println("<authorized/>");
            }
            mOutput.print("</boinc_gui_rpc_reply>");
            mOutput.print('\003');
            mOutput.flush();
            return;
        }
        else if (detectedRequest.startsWith("auth2:")) {
            // Authorization part 2
            final String nonceHash = detectedRequest.substring(6);
            if (nonceHash.equals(mNonceHash)) {
                mAuthorized = true;
                mOutput.println("<boinc_gui_rpc_reply>");
                mOutput.println("<authorized/>");
            }
            else {
                mOutput.println("<boinc_gui_rpc_reply>");
                mOutput.println("<unauthorized/>");
            }
            mOutput.print("</boinc_gui_rpc_reply>");
            mOutput.print('\003');
            mOutput.flush();
            return;
        }
        else if (!mAuthorized) {
            Log.d(TAG, "handleRequest(): when unauthorized");
            mOutput.println("<boinc_gui_rpc_reply>");
            mOutput.println("<unauthorized/>");
            mOutput.print("</boinc_gui_rpc_reply>");
            mOutput.print('\003');
            mOutput.flush();
            return;
        }
        // At this moment client is authorized or there is no password set
        switch (detectedRequest) {
            case "exchange_versions":
                mOutput.println("<boinc_gui_rpc_reply>");
                mOutput.println("<server_version>");
                mOutput.println("   <major>7</major>");
                mOutput.println("   <minor>4</minor>");
                mOutput.println("   <release>23</release>");
                mOutput.println("</server_version>");
                mOutput.print("</boinc_gui_rpc_reply>");
                mOutput.print('\003');
                mOutput.flush();
                break;
            case "get_cc_status":
                mOutput.print(TestSupport.readResource(edu.berkeley.boinc.test.R.raw.get_cc_status_reply));
                mOutput.print('\003');
                mOutput.flush();
                break;
            case "get_file_transfers":
                mOutput.print(TestSupport.readResource(edu.berkeley.boinc.test.R.raw.get_file_transfers_reply));
                mOutput.print('\003');
                mOutput.flush();
                break;
            case "get_host_info":
                mOutput.print(TestSupport.readResource(edu.berkeley.boinc.test.R.raw.get_host_info_reply_gpu));
                mOutput.print('\003');
                mOutput.flush();
                break;
            case "get_message_count":
                mOutput.println("<boinc_gui_rpc_reply>");
                mOutput.println("<seqno>12741</seqno>");
                mOutput.print("</boinc_gui_rpc_reply>");
                mOutput.print('\003');
                mOutput.flush();
                break;
            case "get_messages":
                mOutput.print(TestSupport.readResource(edu.berkeley.boinc.test.R.raw.get_messages_reply));
                mOutput.print('\003');
                mOutput.flush();
                break;
            case "get_project_status":
                mOutput.print(TestSupport.readResource(edu.berkeley.boinc.test.R.raw.get_project_status_reply));
                mOutput.print('\003');
                mOutput.flush();
                break;
            case "get_results":
                mOutput.print(TestSupport.readResource(edu.berkeley.boinc.test.R.raw.get_results_reply));
                mOutput.print('\003');
                mOutput.flush();
                break;
            case "get_state":
                mOutput.print(TestSupport.readResource(edu.berkeley.boinc.test.R.raw.get_state_reply));
                mOutput.print('\003');
                mOutput.flush();
                break;
            case "network_available":
            case "project_update":
            case "project_suspend":
            case "project_resume":
            case "project_nomorework":
            case "project_allowmorework":
            case "suspend_result":
            case "resume_result":
            case "abort_result":
            case "run_benchmarks":
            case "set_gpu_mode":
            case "set_network_mode":
            case "set_run_mode":
            case "retry_file_transfer":
            case "abort_file_transfer":
                mOutput.println("<boinc_gui_rpc_reply>");
                mOutput.println("<success/>");
                mOutput.print("</boinc_gui_rpc_reply>");
                mOutput.print('\003');
                mOutput.flush();
                break;
            case "quit":
                mOutput.println("<boinc_gui_rpc_reply>");
                mOutput.println("<success/>");
                mOutput.print("</boinc_gui_rpc_reply>");
                mOutput.print('\003');
                mOutput.flush();
                stop();
                break;
            default:
                Log.d(TAG, "unhandled request \"" + detectedRequest + "\"");
        }
    }
}
