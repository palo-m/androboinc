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

import java.util.Vector;


/**
 * Callback interface for BOINC client connection.
 * The UI classes (Activities) should implement this interface to receive data
 * from connector (which is running independently in worker thread)
 * 
 * @see ClientRequestHandler
 */
public interface ClientReplyReceiver {

	/**
	 * Indicates that the client is connected
	 * 
	 * @param requestHandler - the handler which can be used for requests from now on
	 */
    void clientConnected(ClientRequestHandler requestHandler);

	/**
	 * Indicates that the network connection was disconnected
	 */
    void clientDisconnected();

	/**
	 * Notifies about current run-mode, network-mode and GPU-mode of a client
	 * 
	 * @param modeInfo - the data about client
	 * @return true if further updates should be sent, false otherwise
	 */
    boolean updatedClientMode(ModeInfo modeInfo);

	/**
	 * Notifies about currently retrieved host information
	 * 
	 * @param hostInfo - the data about client
	 * @return true if further updates should be sent, false otherwise
	 */
    boolean updatedHostInfo(HostInfo hostInfo);

	/**
	 * Notifies about the latest retrieved projects of a client
	 * 
	 * @param projects - the list of projects
	 * @return true if further updates should be sent, false otherwise
	 */
    boolean updatedProjects(Vector<ProjectInfo> projects);

	/**
	 * Notifies about the latest retrieved tasks of a client
	 * 
	 * @param tasks - the list of tasks
	 * @return true if further updates should be sent, false otherwise
	 */
    boolean updatedTasks(Vector<TaskInfo> tasks);

	/**
	 * Notifies about the latest retrieved transfers of a client
	 * 
	 * @param transfers - the list of transfers
	 * @return true if further updates should be sent, false otherwise
	 */
    boolean updatedTransfers(Vector<TransferInfo> transfers);

	/**
	 * Notifies about the latest retrieved messages of a client
	 * 
	 * @param messages - the list of messages
	 * @return true if further updates should be sent, false otherwise
	 */
    boolean updatedMessages(Vector<MessageInfo> messages);

}
