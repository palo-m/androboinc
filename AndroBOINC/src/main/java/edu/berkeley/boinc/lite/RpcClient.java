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

package edu.berkeley.boinc.lite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import sk.boinc.androboinc.debug.Debugging;
import sk.boinc.androboinc.debug.Logging;
import sk.boinc.androboinc.debug.NetStats;
import android.util.Log;
import android.util.Xml;


/**
 * GUI RPC - the way how GUI can manage BOINC core client and retrieve the information
 * from the client.
 * This tries to be the same as original BOINC C++ GUI, but the names are rewritten
 * for the sake of naming convention. Therefore original RPC_CLIENT becomes RpcClient,
 * get_cc_status() becomes getCcStatus() etc.
 */
public class RpcClient {
	private static final String TAG = "RpcClient";
	private static final int CONNECT_TIMEOUT = 30000;      // 30s
	private static final int READ_TIMEOUT = 15000;         // 15s
	private static final int READ_BUF_SIZE = 2048;
	private static final int RESULT_BUILDER_INIT_SIZE = 131072; // Yes, 128K
	private static final int REQUEST_BUILDER_INIT_SIZE = 80;
	
	public static final int PROJECT_UPDATE  = 1;
	public static final int PROJECT_SUSPEND = 2;
	public static final int PROJECT_RESUME  = 3;
	public static final int PROJECT_NNW     = 4;
	public static final int PROJECT_ANW     = 5;

	public static final int RESULT_SUSPEND  = 1;
	public static final int RESULT_RESUME   = 2;
	public static final int RESULT_ABORT    = 3;

	public static final int TRANSFER_RETRY  = 1;
	public static final int TRANSFER_ABORT  = 2;

	private Socket mSocket;
	private OutputStreamWriter mOutput;
	private InputStream mInput;
	private byte[] mReadBuffer = new byte[READ_BUF_SIZE];
	private StringBuilder mResult = new StringBuilder(RESULT_BUILDER_INIT_SIZE);
	private StringBuilder mRequest = new StringBuilder(REQUEST_BUILDER_INIT_SIZE);
	private NetStats mNetStats = null;

	public RpcClient() {}

	public RpcClient(NetStats netStats) {
		mNetStats = netStats;
	}


	/*
	 * Private classes - Helpers
	 */

	private class Auth1Parser extends DefaultHandler {
		private StringBuilder mResult = null;
		private String mCurrentElement = null;
		private boolean mNonceParsed = false;

		public Auth1Parser(StringBuilder result) {
			mResult = result;
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			super.characters(ch, start, length);
			// put it into StringBuilder
	        mCurrentElement = new String(ch, start, length);
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			super.endElement(uri, localName, qName);
			if (localName.equalsIgnoreCase("nonce") && !mNonceParsed) {
				mResult.append(mCurrentElement);
				mNonceParsed = true;
			}
			mCurrentElement = null;
		}
	}

	private class Auth2Parser extends DefaultHandler {
		private StringBuilder mResult = null;
		private boolean mParsed = false;

		public Auth2Parser(StringBuilder result) {
			mResult = result;
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			super.endElement(uri, localName, qName);
			if (localName.equalsIgnoreCase("authorized") && !mParsed) {
				mResult.append("authorized");
				mParsed = true;
			}
			else if (localName.equalsIgnoreCase("unauthorized") && !mParsed) {
				mResult.append("unauthorized");
				mParsed = true;
			}
		}
	}

	/*
	 * Helper methods
	 */

	private static final String modeName(int mode) {
		switch (mode) {
		case 1: return "<always/>";
		case 2: return "<auto/>";
		case 3: return "<never/>";
		case 4: return "<restore/>";
		default: return "";
		}
	}

	/*
	 * Methods for connection - opening/closing/authorization/status
	 */

	/**
	 * Connect to BOINC core client
	 * @param address Internet address of client (hostname or IP-address)
	 * @param port Port of BOINC client (default port is 31416)
	 * @throws ConnectionFailedException in case connection cannot be established
	 */
	public void open(String address, int port) throws ConnectionFailedException {
		if (isConnected()) {
			// Already connected
			if (Logging.ERROR) Log.e(TAG, "Attempt to connect when already connected");
			// We better close current connection and reconnect (address/port could be different)
			close();
		}
		try {
			mSocket = new Socket();
			mSocket.connect(new InetSocketAddress(address, port), CONNECT_TIMEOUT);
			mSocket.setSoTimeout(READ_TIMEOUT);
			mInput = mSocket.getInputStream();
			mOutput = new OutputStreamWriter(mSocket.getOutputStream(), "ISO8859_1");
		}
		catch (UnknownHostException e) {
			mSocket = null;
			throw new ConnectionFailedException("Connection failed: unknown host \"" + address + "\"", e);
		}
		catch (IllegalArgumentException e) {
			mSocket = null;
			throw new ConnectionFailedException("Connection failed: illegal argument", e);
		}
		catch (IOException e) {
			mSocket = null;
			throw new ConnectionFailedException("Connect failed", e);
		}
		if (mNetStats != null) {
			mNetStats.connectionOpened();
		}
		if (Logging.DEBUG) Log.d(TAG, "open(" + address + ", " + port + ") - Connected successfully");
	}

	/**
	 * Closes the currently opened connection to BOINC core client
	 */
	public void close() {
		if (mSocket == null) {
			// Not connected - just return (can be cleanup "for sure")
			return;
		}
		try {
			mInput.close();
		}
		catch (IOException e) {
			if (Logging.WARNING) Log.w(TAG, "input close failure", e);
		}
		try {
			mOutput.close();
		}
		catch (IOException e) {
			if (Logging.WARNING) Log.w(TAG, "output close failure", e);
		}
		try {
			mSocket.close();
			if (Logging.DEBUG) Log.d(TAG, "close() - Socket closed");
		}
		catch (IOException e) {
			if (Logging.WARNING) Log.w(TAG, "socket close failure", e);
		}
		finally {
			mSocket = null;
			mInput = null;
			mOutput = null;
		}
		if (mNetStats != null) {
			mNetStats.connectionClosed();
		}
	}

	/**
	 * Performs the BOINC authorization towards currently connected client. 
	 * The authorization uses MD5 hash of client's password and random value. 
	 * Clear-text password is never sent over network.
	 * @param password Clear text password used for authorization
	 * @throws RpcClientFailedException in case authorization is fails
	 * <ul>
	 * <li>{@link AuthorizationFailedException} in case authorization is unsuccessful (i.e. wrong password)</li>
	 * <li>{@link ConnectionFailedException} in case connection fails</li>
	 * <li>{@link InvalidDataReceivedException} in case of incorrect data received</li>
	 * </ul>
	 */
	public void authorize(String password) throws RpcClientFailedException {
		if (!isConnected()) {
			throw new ConnectionFailedException("Not connected");
		}
		try {
			// Phase 1: get nonce
			sendRequest("<auth1/>\n");
			String auth1Rsp = receiveReply();
			mRequest.setLength(0);
			Xml.parse(auth1Rsp, new Auth1Parser(mRequest)); // get nonce value
			// Operation: combine nonce & password, make MD5 hash
			mRequest.append(password);
			String nonceHash = Md5.hash(mRequest.toString());
			// Phase 2: send hash to client
			mRequest.setLength(0);
			mRequest.append("<auth2>\n<nonce_hash>");
			mRequest.append(nonceHash);
			mRequest.append("</nonce_hash>\n</auth2>\n");
			sendRequest(mRequest.toString());
			String auth2Rsp = receiveReply();
			mRequest.setLength(0);
			Xml.parse(auth2Rsp, new Auth2Parser(mRequest));
			if (!mRequest.toString().equals("authorized")) {
				if (Logging.DEBUG) Log.d(TAG, "authorize() - Failure");
				throw new AuthorizationFailedException();
			}
			if (Logging.DEBUG) Log.d(TAG, "authorize() - Successful");
		}
		catch (IOException e) {
			throw new ConnectionFailedException("Connection failed during authorization", e);
		}
		catch (SAXException e) {
			throw new InvalidDataReceivedException("Malformed data received during authorization", e);
		}
	}

	/**
	 * Checks the current state of connection
	 * @return true if connected to BOINC core client, false if not connected
	 */
	public final boolean isConnected() {
		return (mSocket != null) ? mSocket.isConnected() : false;
	}

	/**
	 * Checks whether current connection can be used (data can be sent and received)
	 * This is achieved by sending the empty BOINC command - so in case there is
	 * socket still opened, but other side already closed connection, it will be detected.
	 * 
	 * @return true if other side responds, false if data cannot be sent or received
	 */
	public boolean connectionAlive() {
		if (!isConnected()) return false;
		try {
			// We just get the status via socket and do not parse reply
			sendRequest("<get_cc_status/>\n");
			String result = receiveReply();
			if (result.length() == 0) {
				// End of stream reached and no data were received in reply
				// We assume that socket is closed on the other side,
				// most probably client shut down
				return false;
			}
			return true;
		}
		catch (IOException e) {
			if (Logging.DEBUG) Log.d(TAG, "connectionAlive(): disconnected", e);
			return false;
		}
	}

	/*
	 * Private methods for send/receive data
	 */

	/**
	 * Send RPC request to BOINC core client (XML-formatted)
	 * 
	 * @param request The request itself
	 * @throws IOException if error occurs when sending the request
	 */
	private void sendRequest(String request) throws IOException {
		if (Debugging.PERFORMANCE) Log.d(TAG, "mRequest.capacity() = " + mRequest.capacity());
		if (Debugging.DATA) Log.d(TAG, "Sending request: \n" + request.toString());
		mOutput.write("<boinc_gui_rpc_request>\n");
		mOutput.write(request);
		mOutput.write("</boinc_gui_rpc_request>\n\003");
		mOutput.flush();
		if (mNetStats != null) {
			mNetStats.bytesTransferred(50 + request.length());
		}
	}

	/**
	 * Read the reply from BOINC core client
	 * 
	 * @return the data read from socket
	 * @throws IOException if error occurs when reading from socket
	 */
	private String receiveReply() throws IOException {
		mResult.setLength(0);
		if (Debugging.PERFORMANCE) Log.d(TAG, "mResult.capacity() = " + mResult.capacity());

		long readStart;
		if (Debugging.PERFORMANCE) readStart = System.nanoTime();

		// Speed is (with large data): ~ 45 KB/s for buffer size 1024
		//                             ~ 90 KB/s for buffer size 2048
		//                             ~ 95 KB/s for buffer size 4096
		// The chosen buffer size is 2048
		int bytesRead;
		do {
			bytesRead = mInput.read(mReadBuffer);
			if (bytesRead == -1) break;
			mResult.append(new String(mReadBuffer, 0, bytesRead));
			if (mReadBuffer[bytesRead-1] == '\003') {
				// Last read byte marks the end of transfer
				mResult.setLength(mResult.length() - 1);
				break;
			}
		} while (true);
		if (mResult.length() == 0) {
			// Nothing was read at all
			// Possibly closed socket on other side
			throw new IOException("No data received");
		}

		if (Debugging.PERFORMANCE) {
			float duration = (System.nanoTime() - readStart)/1000000000.0F;
			long bytesCount = mResult.length();
			if (duration == 0) duration = 0.001F;
			Log.d(TAG, "Reading from socket took " + duration + " seconds, " + bytesCount + " bytes read (" + (bytesCount / duration) + " bytes/second)");
			Log.d(TAG, "mResult.capacity() = " + mResult.capacity());
		}

		if (mNetStats != null) {
			mNetStats.bytesReceived(mResult.length());
		}

		if (Debugging.DATA) {
			BufferedReader dbr = new BufferedReader(new StringReader(mResult.toString()), 1024);
			String dl;
			int ln = 0;
			try {
				while ((dl = dbr.readLine()) != null) {
					++ln;
					Log.d(TAG, String.format("%4d: %s", ln, dl));
				}
			}
			catch (IOException ioe) {
			}
		}
		return mResult.toString();
	}

	/*
	 * GUI RPC calls
	 */

	/**
	 * Attempts to retrieve version of the connected client
	 * 
	 * @return parsed result of RPC call in case of success, 
	 *         null if (older) client does not support the operation
	 * @throws RpcClientFailedException in case of failure:
	 * <ul>
	 * <li>{@link AuthorizationFailedException} in case of unauthorized</li>
	 * <li>{@link InvalidDataReceivedException} in case of incorrect data received</li>
	 * <li>{@link ConnectionFailedException} in case connection fails</li>
	 * </ul>
	 */
	public VersionInfo exchangeVersions() throws RpcClientFailedException {
		mRequest.setLength(0);
		mRequest.append("<exchange_versions>\n  <major>");
		mRequest.append(Boinc.MAJOR_VERSION);
		mRequest.append("</major>\n  <minor>");
		mRequest.append(Boinc.MINOR_VERSION);
		mRequest.append("</minor>\n  <release>");
		mRequest.append(Boinc.RELEASE);
		mRequest.append("</release>\n</exchange_versions>\n");
		try {
			sendRequest(mRequest.toString());
			VersionInfo versionInfo = VersionInfoParser.parse(receiveReply());
			return versionInfo;
		}
		catch (IOException e) {
			throw new ConnectionFailedException("Connection failed in exchangeVersions()", e);
		}
	}

	/**
	 * Performs {@code <get_cc_status/>} RPC towards BOINC client
	 * 
	 * @return parsed result of RPC call
	 * @throws RpcClientFailedException in case of failure:
	 * <ul>
	 * <li>{@link AuthorizationFailedException} in case of unauthorized</li>
	 * <li>{@link InvalidDataReceivedException} in case of incorrect data received</li>
	 * <li>{@link ConnectionFailedException} in case connection fails</li>
	 * </ul>
	 */
	public CcStatus getCcStatus() throws RpcClientFailedException {
		try {
			sendRequest("<get_cc_status/>\n");
			CcStatus ccStatus = CcStatusParser.parse(receiveReply());
			return ccStatus;
		}
		catch (IOException e) {
			throw new ConnectionFailedException("Connection failed in getCcStatus()", e);
		}
	}

	/**
	 * Performs {@code <get_file_transfers/>} RPC towards BOINC client
	 * 
	 * @return parsed result of RPC call
	 * @throws RpcClientFailedException in case of failure:
	 * <ul>
	 * <li>{@link AuthorizationFailedException} in case of unauthorized</li>
	 * <li>{@link InvalidDataReceivedException} in case of incorrect data received</li>
	 * <li>{@link ConnectionFailedException} in case connection fails</li>
	 * </ul>
	 */
	public Vector<Transfer> getFileTransfers() throws RpcClientFailedException {
		try {
			sendRequest("<get_file_transfers/>\n");
			Vector<Transfer> transfers = TransfersParser.parse(receiveReply());
			return transfers;
		}
		catch (IOException e) {
			throw new ConnectionFailedException("Connection failed in getFileTransfers()", e);
		}
	}

	/**
	 * Performs {@code <get_host_info/>} RPC towards BOINC client
	 * 
	 * @return parsed result of RPC call
	 * @throws RpcClientFailedException in case of failure:
	 * <ul>
	 * <li>{@link AuthorizationFailedException} in case of unauthorized</li>
	 * <li>{@link InvalidDataReceivedException} in case of incorrect data received</li>
	 * <li>{@link ConnectionFailedException} in case connection fails</li>
	 * </ul>
	 */
	public HostInfo getHostInfo() throws RpcClientFailedException {
		try {
			sendRequest("<get_host_info/>\n");
			HostInfo hostInfo = HostInfoParser.parse(receiveReply());
			return hostInfo;
		}
		catch (IOException e) {
			throw new ConnectionFailedException("Connection failed in getHostInfo()", e);
		}
	}

	/**
	 * Performs {@code <get_message_count/>} RPC towards BOINC client
	 * 
	 * @return number of messages (-1 for unsupported operation on older clients)
	 * @throws RpcClientFailedException in case of failure:
	 * <ul>
	 * <li>{@link AuthorizationFailedException} in case of unauthorized</li>
	 * <li>{@link InvalidDataReceivedException} in case of incorrect data received</li>
	 * <li>{@link ConnectionFailedException} in case connection fails</li>
	 * </ul>
	 */
	public int getMessageCount() throws RpcClientFailedException {
		try {
			sendRequest("<get_message_count/>\n");
			return MessageCountParser.getSeqno(receiveReply());
		}
		catch (IOException e) {
			throw new ConnectionFailedException("Connection failed in getMessageCount()", e);
		}
	}

	/**
	 * Performs {@code <get_messages/>} RPC towards BOINC client
	 * 
	 * @return parsed result of RPC call
	 * @throws RpcClientFailedException in case of failure:
	 * <ul>
	 * <li>{@link AuthorizationFailedException} in case of unauthorized</li>
	 * <li>{@link InvalidDataReceivedException} in case of incorrect data received</li>
	 * <li>{@link ConnectionFailedException} in case connection fails</li>
	 * </ul>
	 */
	public Vector<Message> getMessages(int seqNo) throws RpcClientFailedException {
		try {
			String request;
			if (seqNo == 0) {
				// get all messages
				request = "<get_messages/>\n";
			}
			else {
				request =
					"<get_messages>\n" +
					" <seqno>" + seqNo + "</seqno>\n" +
					"</get_messages>\n";
			}
			sendRequest(request);
			Vector<Message> messages = MessagesParser.parse(receiveReply());
			return messages;
		}
		catch (IOException e) {
			throw new ConnectionFailedException("Connection failed in getMessages()", e);
		}
	}

	/**
	 * Performs {@code <get_project_status/>} RPC towards BOINC client
	 * 
	 * @return parsed result of RPC call
	 * @throws RpcClientFailedException in case of failure:
	 * <ul>
	 * <li>{@link AuthorizationFailedException} in case of unauthorized</li>
	 * <li>{@link InvalidDataReceivedException} in case of incorrect data received</li>
	 * <li>{@link ConnectionFailedException} in case connection fails</li>
	 * </ul>
	 */
	public Vector<Project> getProjectStatus() throws RpcClientFailedException {
		try {
			sendRequest("<get_project_status/>\n");
			Vector<Project> projects = ProjectsParser.parse(receiveReply());
			return projects;
		}
		catch (IOException e) {
			throw new ConnectionFailedException("Connection failed in getProjectStatus()", e);
		}
	}

	/**
	 * Performs {@code <get_results/>} RPC towards BOINC client (only active results)
	 * 
	 * @return parsed result of RPC call
	 * @throws RpcClientFailedException in case of failure:
	 * <ul>
	 * <li>{@link AuthorizationFailedException} in case of unauthorized</li>
	 * <li>{@link InvalidDataReceivedException} in case of incorrect data received</li>
	 * <li>{@link ConnectionFailedException} in case connection fails</li>
	 * </ul>
	 */
	public Vector<Result> getActiveResults() throws RpcClientFailedException {
		final String request =
			"<get_results>\n" +
			"<active_only>1</active_only>\n" +
			"</get_results>\n";
		try {
			sendRequest(request);
			Vector<Result> results = ResultsParser.parse(receiveReply());
			return results;
		}
		catch (IOException e) {
			throw new ConnectionFailedException("Connection failed in getActiveResults()", e);
		}
	}

	/**
	 * Performs {@code <get_results/>} RPC towards BOINC client (all results)
	 * 
	 * @return parsed result of RPC call
	 * @throws RpcClientFailedException in case of failure:
	 * <ul>
	 * <li>{@link AuthorizationFailedException} in case of unauthorized</li>
	 * <li>{@link InvalidDataReceivedException} in case of incorrect data received</li>
	 * <li>{@link ConnectionFailedException} in case connection fails</li>
	 * </ul>
	 */
	public Vector<Result> getResults() throws RpcClientFailedException {
		try {
			sendRequest("<get_results/>\n");
			Vector<Result> results = ResultsParser.parse(receiveReply());
			return results;
		}
		catch (IOException e) {
			throw new ConnectionFailedException("Connection failed in getResults()", e);
		}
	}

	/**
	 * Performs get_state RPC towards BOINC client
	 * 
	 * @return parsed result of RPC call in case of success
	 * @throws RpcClientFailedException in case of failure:
	 * <ul>
	 * <li>{@link AuthorizationFailedException} in case of unauthorized</li>
	 * <li>{@link InvalidDataReceivedException} in case of incorrect data received</li>
	 * <li>{@link ConnectionFailedException} in case connection fails</li>
	 * </ul>
	 */
	public CcState getState() throws RpcClientFailedException {
		try {
			sendRequest("<get_state/>\n");
			CcState result = CcStateParser.parse(receiveReply());
			return result;
		}
		catch (IOException e) {
			throw new ConnectionFailedException("Connection failed in getState()", e);
		}
	}

	/**
	 * Tells the BOINC core client that a network connection is available,
	 * and that it should do as much network activity as it can.
	 * 
	 * @return true for success response, false for failure response
	 * @throws RpcClientFailedException in case of failed operation:
	 * <ul>
	 * <li>{@link AuthorizationFailedException} in case of unauthorized</li>
	 * <li>{@link InvalidDataReceivedException} in case of incorrect data received</li>
	 * <li>{@link ConnectionFailedException} in case connection fails</li>
	 * </ul>
	 */
	public boolean networkAvailable() throws RpcClientFailedException {
		try {
			sendRequest("<network_available/>\n");
			return SimpleReplyParser.isSuccess(receiveReply());
		}
		catch (IOException e) {
			throw new ConnectionFailedException("Connection failed in networkAvailable()", e);
		}
	}

	/**
	 * Triggers change of state of project in BOINC core client
	 * 
	 * @param operation operation to be triggered
	 * @param projectUrl master URL of project
	 * @return true for success response, false for failure response
	 * @throws RpcClientFailedException in case of failed operation:
	 * <ul>
	 * <li>{@link AuthorizationFailedException} in case of unauthorized</li>
	 * <li>{@link InvalidDataReceivedException} in case of incorrect data received</li>
	 * <li>{@link ConnectionFailedException} in case connection fails</li>
	 * </ul>
	 */
	public boolean projectOp(int operation, String projectUrl) throws RpcClientFailedException {
		try {
			String opTag;
			switch (operation) {
			case PROJECT_UPDATE:
				opTag = "project_update";
				break;
			case PROJECT_SUSPEND:
				opTag = "project_suspend";
				break;
			case PROJECT_RESUME:
				opTag = "project_resume";
				break;
			case PROJECT_NNW:
				opTag = "project_nomorework";
				break;
			case PROJECT_ANW:
				opTag = "project_allowmorework";
				break;
			default:
				throw new UnsupportedOperationException("projectOp() - unsupported operation: " + operation);
			}
			String request =
				"<" + opTag + ">\n" +
				"<project_url>" + projectUrl + "</project_url>\n" +
				"</" + opTag + ">\n";

			sendRequest(request);
			return SimpleReplyParser.isSuccess(receiveReply());
		}
		catch (IOException e) {
			throw new ConnectionFailedException("Connection failed in projectOp()", e);
		}
	}

	/**
	 * Triggers operation on task in BOINC core client
	 * 
	 * @param operation operation to be triggered
	 * @param projectUrl master URL of project
	 * @param taskName name of the task
	 * @return true for success response, false for failure response
	 * @throws RpcClientFailedException in case of failed operation:
	 * <ul>
	 * <li>{@link AuthorizationFailedException} in case of unauthorized</li>
	 * <li>{@link InvalidDataReceivedException} in case of incorrect data received</li>
	 * <li>{@link ConnectionFailedException} in case connection fails</li>
	 * </ul>
	 */
	public boolean resultOp(int operation, String projectUrl, String taskName) throws RpcClientFailedException {
		try {
			String opTag;
			switch (operation) {
			case RESULT_SUSPEND:
				opTag = "suspend_result";
				break;
			case RESULT_RESUME:
				opTag = "resume_result";
				break;
			case RESULT_ABORT:
				opTag = "abort_result";
				break;
			default:
				throw new UnsupportedOperationException("resultOp() - unsupported operation: " + operation);
			}
			mRequest.setLength(0);
			mRequest.append("<");
			mRequest.append(opTag);
			mRequest.append(">\n   <project_url>");
			mRequest.append(projectUrl);
			mRequest.append("</project_url>\n   <name>");
			mRequest.append(taskName);
			mRequest.append("</name>\n</");
			mRequest.append(opTag);
			mRequest.append(">\n");

			sendRequest(mRequest.toString());
			return SimpleReplyParser.isSuccess(receiveReply());
		}
		catch (IOException e) {
			throw new ConnectionFailedException("Connection failed in resultOp()", e);
		}
	}

	/**
	 * Tells the BOINC core client to exit.
	 * 
	 * @return true for success response, false for failure response
	 * @throws RpcClientFailedException in case of failed operation:
	 * <ul>
	 * <li>{@link AuthorizationFailedException} in case of unauthorized</li>
	 * <li>{@link InvalidDataReceivedException} in case of incorrect data received</li>
	 * <li>{@link ConnectionFailedException} in case connection fails</li>
	 * </ul>
	 */
	public boolean quit() throws RpcClientFailedException {
		try {
			sendRequest("<quit/>\n");
			return SimpleReplyParser.isSuccess(receiveReply());
		}
		catch (IOException e) {
			throw new ConnectionFailedException("Connection failed in quit()", e);
		}
	}

	/**
	 * Run the CPU benchmarks
	 * 
	 * @return true for success response, false for failure response
	 * @throws RpcClientFailedException in case of failed operation:
	 * <ul>
	 * <li>{@link AuthorizationFailedException} in case of unauthorized</li>
	 * <li>{@link InvalidDataReceivedException} in case of incorrect data received</li>
	 * <li>{@link ConnectionFailedException} in case connection fails</li>
	 * </ul>
	 */
	public boolean runBenchmarks() throws RpcClientFailedException {
		try {
			sendRequest("<run_benchmarks/>\n");
			return SimpleReplyParser.isSuccess(receiveReply());
		}
		catch (IOException e) {
			throw new ConnectionFailedException("Connection failed in runBenchmarks()", e);
		}
	}

	/**
	 * Set the GPU mode
	 * 
	 * @param mode 1 = always, 2 = auto, 3 = never, 4 = restore
	 * @param duration If duration is zero, mode is permanent. Otherwise revert to
	 *        last permanent mode after duration seconds elapse.
	 * @return true for success response, false for failure response
	 * @throws RpcClientFailedException in case of failed operation:
	 * <ul>
	 * <li>{@link AuthorizationFailedException} in case of unauthorized</li>
	 * <li>{@link InvalidDataReceivedException} in case of incorrect data received</li>
	 * <li>{@link ConnectionFailedException} in case connection fails</li>
	 * </ul>
	 */
	public boolean setGpuMode(int mode, double duration) throws RpcClientFailedException {
		final String request =
			"<set_gpu_mode>\n" +
			modeName(mode) + "\n" +
			"<duration>" + duration + "</duration>\n" +
			"</set_gpu_mode>\n";
		try {
			sendRequest(request);
			return SimpleReplyParser.isSuccess(receiveReply());
		}
		catch (IOException e) {
			throw new ConnectionFailedException("Connection failed in setGpuMode()", e);
		}
	}

	/**
	 * Set the network mode
	 * 
	 * @param mode 1 = always, 2 = auto, 3 = never, 4 = restore
	 * @param duration If duration is zero, mode is permanent. Otherwise revert to
	 *        last permanent mode after duration seconds elapse.
	 * @return true for success response, false for failure response
	 * @throws RpcClientFailedException in case of failed operation:
	 * <ul>
	 * <li>{@link AuthorizationFailedException} in case of unauthorized</li>
	 * <li>{@link InvalidDataReceivedException} in case of incorrect data received</li>
	 * <li>{@link ConnectionFailedException} in case connection fails</li>
	 * </ul>
	 */
	public boolean setNetworkMode(int mode, double duration) throws RpcClientFailedException {
		final String request =
			"<set_network_mode>\n" +
			modeName(mode) + "\n" +
			"<duration>" + duration + "</duration>\n" +
			"</set_network_mode>\n";
		try {
			sendRequest(request);
			return SimpleReplyParser.isSuccess(receiveReply());
		}
		catch (IOException e) {
			throw new ConnectionFailedException("Connection failed in setNetworkMode()", e);
		}
	}

	/**
	 * Set the run mode
	 * 
	 * @param mode 1 = always, 2 = auto, 3 = never, 4 = restore
	 * @param duration If duration is zero, mode is permanent. Otherwise revert to
	 *        last permanent mode after duration seconds elapse.
	 * @return true for success response, false for failure response
	 * @throws RpcClientFailedException in case of failed operation:
	 * <ul>
	 * <li>{@link AuthorizationFailedException} in case of unauthorized</li>
	 * <li>{@link InvalidDataReceivedException} in case of incorrect data received</li>
	 * <li>{@link ConnectionFailedException} in case connection fails</li>
	 * </ul>
	 */
	public boolean setRunMode(int mode, double duration) throws RpcClientFailedException {
		final String request =
			"<set_run_mode>\n" +
			modeName(mode) + "\n" +
			"<duration>" + duration + "</duration>\n" +
			"</set_run_mode>\n";
		try {
			sendRequest(request);
			return SimpleReplyParser.isSuccess(receiveReply());
		}
		catch (IOException e) {
			throw new ConnectionFailedException("Connection failed in setRunMode()", e);
		}
	}

	/**
	 * Triggers operation on transfer in BOINC core client
	 * 
	 * @param operation operation to be triggered
	 * @param projectUrl master URL of project
	 * @param fileName name of the file
	 * @return true for success response, false for failure response
	 * @throws RpcClientFailedException in case of failed operation:
	 * <ul>
	 * <li>{@link AuthorizationFailedException} in case of unauthorized</li>
	 * <li>{@link InvalidDataReceivedException} in case of incorrect data received</li>
	 * <li>{@link ConnectionFailedException} in case connection fails</li>
	 * </ul>
	 */
	public boolean transferOp(int operation, String projectUrl, String fileName) throws RpcClientFailedException {
		try {
			String opTag;
			switch (operation) {
			case TRANSFER_RETRY:
				opTag = "retry_file_transfer";
				break;
			case TRANSFER_ABORT:
				opTag = "abort_file_transfer";
				break;
			default:
				throw new UnsupportedOperationException("transferOp() - unsupported operation: " + operation);
			}
			mRequest.setLength(0);
			mRequest.append("<");
			mRequest.append(opTag);
			mRequest.append(">\n   <project_url>");
			mRequest.append(projectUrl);
			mRequest.append("</project_url>\n   <filename>");
			mRequest.append(fileName);
			mRequest.append("</filename>\n</");
			mRequest.append(opTag);
			mRequest.append(">\n");
			sendRequest(mRequest.toString());
			return SimpleReplyParser.isSuccess(receiveReply());
		}
		catch (IOException e) {
			throw new ConnectionFailedException("Connection failed in transferOp()", e);
		}
	}
}
