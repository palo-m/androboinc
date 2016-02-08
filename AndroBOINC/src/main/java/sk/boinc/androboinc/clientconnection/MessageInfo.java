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

/**
 * Description of BOINC-client message for AndroBOINC purpose
 * Reflects the class Message of BOINC-library (and also class Project)
 */
public class MessageInfo {
	public final int    seqNo;		// Message.seqno - sequence number of message - unique ID
	public final int    priority;   // Message.priority
	public final String project;    // Project.getName()
	public final String time;       // Message.timestamp converted to date-String
	public final String body;       // Message.body

	public MessageInfo(int seqNo,
			int priority,
			String project,
			String time,
			String body) {
		this.seqNo = seqNo;
		this.priority = priority;
		this.project = project;
		this.time = time;
		this.body = body;
	}
}
