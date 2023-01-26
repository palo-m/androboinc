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

package sk.boinc.androboinc.bridge;

import sk.boinc.androboinc.clientconnection.ConnectionManagerCallback.DisconnectCause;
import sk.boinc.androboinc.clientconnection.ConnectionManagerCallback.ProgressInd;
import sk.boinc.androboinc.clientconnection.VersionInfo;
import sk.boinc.androboinc.util.ClientId;


public interface ClientBridgeCallback {

	/**
	 * Indicates the ongoing network connection operation
	 * 
	 * @param progress - progress indicator
	 */
    void bridgeConnectionProgress(ProgressInd progress);

	/**
	 * Indicates that client has been connected
	 * 
	 * @param clientId - the identity of connected client
	 * @param clientVersion - BOINC version of the connected client
	 */
    void bridgeConnected(ClientId clientId, VersionInfo clientVersion);

	/**
	 * Indicates that client has been disconnected
	 * 
	 * @param clientId - the identity of client which disconnected
	 * @param cause - disconnect reason
	 */
    void bridgeDisconnected(ClientId clientId, DisconnectCause cause);

}
