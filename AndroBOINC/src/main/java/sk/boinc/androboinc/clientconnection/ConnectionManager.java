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
 * Interface for manager of connection to BOINC client. 
 * The connection manager is entry point for every user of connection to client
 * <p>
 * The class which wants to be receive data from connected client must 
 * implement interface {@link ClientReplyReceiver} and register itself at connection
 * manager as status observer via method {@link #registerDataReceiver(ClientReplyReceiver)}.
 * The data will be sent until method {@link #unregisterDataReceiver(ClientReplyReceiver)}
 * is called.
 * <p>
 * The class which wants to initiate or terminate the connection or wants 
 * to receive connection progress indicator must implement {@link ConnectionManagerCallback}
 * and use methods {@link #connect(ConnectionManagerCallback, ClientId, boolean)} or 
 * {@link #disconnect(ConnectionManagerCallback)}
 * 
 * @see ConnectionManagerCallback
 * @see ClientReplyReceiver
 */
public interface ConnectionManager {

	/**
	 * Starts sending of data from connected client.
	 * The data will be received until receiver is unregistered.
	 * 
	 * @param receiver - the new receiver of client data since now
	 * @see #unregisterDataReceiver(ClientReplyReceiver)
	 */
	public abstract void registerDataReceiver(ClientReplyReceiver receiver);

	/**
	 * Stops sending of data from connected client.
	 * 
	 * @param receiver - former receiver of data
	 * @see #registerDataReceiver(ClientReplyReceiver)
	 */
	public abstract void unregisterDataReceiver(ClientReplyReceiver receiver);

	/**
	 * Starts sending of updates about status of connected client.
	 * The data will be received until observer is unregistered.
	 * 
	 * @param observer - the new observer of client status
	 * @see #unregisterStatusObserver
	 */
	public abstract void registerStatusObserver(ConnectionManagerCallback observer);

	/**
	 * Stops sending of updates about status of connected client.
	 * 
	 * @param observer - former observer of client status
	 * @see #registerStatusObserver
	 */
	public abstract void unregisterStatusObserver(ConnectionManagerCallback observer);

	/**
	 * Retrieves ID of currently connected client
	 * 
	 * @return ID of connected client or {@code null} if no client is connected
	 */
	public abstract ClientId getClientId();

	/**
	 * Connect to the specified client and optionally retrieve the full initial data from it.
	 * 
	 * @param callback - used for result of operation. Cannot be {@code null}.
	 * @param host - host to connect to
	 * @param retrieveInitialData - if set to {@code true} requests automatic retrieval of full data
	 *        after successful connect
	 * @see ConnectionManagerCallback#clientConnected(VersionInfo)
	 */
	public abstract void connect(ConnectionManagerCallback callback, ClientId host, boolean retrieveInitialData);

	/**
	 * Disconnect the currently connected client.
	 * 
	 * @param callback - used for result of operation. Cannot be {@code null}.
	 * @see ConnectionManagerCallback#clientDisconnected(DisconnectCauseOLD)
	 */
	public abstract void disconnect(ConnectionManagerCallback callback);

}
