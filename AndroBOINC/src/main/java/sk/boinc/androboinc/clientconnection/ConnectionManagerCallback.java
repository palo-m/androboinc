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

package sk.boinc.androboinc.clientconnection;

import sk.boinc.androboinc.util.ClientId;


/**
 * Interface for connection manager callback.
 * The connection manager operations run in separate thread and they take some time.
 * Instead of blocking requester until operation is finished, the result of operation 
 * is reported via this callback after requested operation is finished.
 * 
 * @see ConnectionManager
 */
public interface ConnectionManagerCallback {

	/**
	 * The indicator of the ongoing network operation
	 */
	public static enum ProgressInd {
		/**
		 * No indication
		 */
		NONE,

		/**
		 * Connection starting
		 */
		CONNECTING,

		/**
		 * Authorization in progress
		 */
		AUTHORIZATION_PENDING,

		/**
		 * Retrieving initial data
		 */
		INITIAL_DATA,

		/**
		 * Start of data transfer
		 */
		XFER_STARTED,

		/**
		 * Data transfer finished
		 */
		XFER_FINISHED
	}

	/**
	 * The reason of client disconnection
	 */
	public static enum DisconnectCause {
		/**
		 * Expected disconnect (e.g. requested by user)
		 */
		NORMAL,

		/**
		 * Failure to connect because there is no network connectivity
		 */
		NO_CONNECTIVITY,

		/**
		 * Failure when initiating the connection (e.g. host unreachable)
		 */
		CONNECT_FAILURE,

		/**
		 * Authorization failure when connecting without password
		 * (i.e. missing password)
		 */
		AUTH_FAIL_NO_PWD,

		/**
		 * Authorization failure when connecting with password
		 * (i.e. wrong password supplied)
		 */
		AUTH_FAIL_WRONG_PWD,

		/**
		 * Connection failure during operation after authorization
		 * (e.g. network failure, mobile/WiFi network unreachable or unexpected remote client shutdown)
		 */
		CONNECTION_DROP
	}

	/**
	 * Indicates about the ongoing network connection operation
	 * 
	 * @param progress - the indication about currently executed operation
	 */
	public abstract void clientConnectionProgress(ProgressInd progress);

	/**
	 * Indicates that client is connected.
	 * <p>
	 * This indication is triggered in following cases:
	 * <ul>
	 * <li>The connection was previously requested via {@link ConnectionManager#connect(ConnectionManagerCallback, ClientId, boolean)}
	 *     and it succeeded right now</li>
	 * <li>The status observer was just registered via {@link ConnectionManager#registerStatusObserver(ConnectionManagerCallback)}
	 *     while the client was already connected</li>
	 * </ul>
	 * 
	 * @param clientId - identity of connected client
	 * @param clientVersion - the BOINC version of connected client
	 */
	public abstract void clientConnected(ClientId clientId, VersionInfo clientVersion);

	/**
	 * Indicates that the client was disconnected.
	 * <p>
	 * This can happen in following cases:
	 * <ul>
	 * <li>The disconnect was previously requested via {@link ConnectionManager#disconnect(ConnectionManagerCallback)}
	 *     and the disconnect finished right now</li>
	 * <li>The connection was previously requested via {@link ConnectionManager#connect(ConnectionManagerCallback, ClientId, boolean)}
	 *     but it was unsuccessful</li>
	 * <li>There was unsolicited disconnect of the connected client</li>
	 * </ul>
	 * 
	 * @param clientId - identity of disconnected client
	 * @param cause - the reason of disconnect, typically
	 * <ul>
	 * <li>{@link DisconnectCauseOLD#NORMAL NORMAL} in case disconnect was requested</li>
	 * <li>{@link DisconnectCauseOLD#NO_CONNECTIVITY NO_CONNECTIVITY} in case there is no network connectivity</li>
	 * <li>{@link DisconnectCauseOLD#CONNECT_FAILURE CONNECT_FAILURE}, 
	 *     {@link DisconnectCauseOLD#AUTH_FAIL_NO_PWD AUTH_FAIL_NO_PWD}, or
	 *     {@link DisconnectCauseOLD#AUTH_FAIL_WRONG_PWD AUTH_FAIL_WRONG_PWD} in case connect was unsuccessful</li>
	 * <li>{@link DisconnectCauseOLD#CONNECTION_DROP CONNECTION_DROP} in case of unsolicited disconnect</li>
	 * </ul>
	 */
	public abstract void clientDisconnected(ClientId clientId, DisconnectCause cause);

}
