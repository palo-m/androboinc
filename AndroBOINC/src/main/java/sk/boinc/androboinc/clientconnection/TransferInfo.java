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
 * Description of BOINC transfer for AndroBOINC purpose
 * Reflects the classes of BOINC-library
 */
public class TransferInfo {
	public final String fileName;     // unique ID
	public final String projectUrl;   // Transfer.project_url
	public final int    stateControl; // state control in numerical form
	public static final int SUSPENDED = 1; // bit 1 of stateControl
	public static final int ABORTED = 2;   // bit 2 of stateControl
	public static final int FAILED = 4;    // bit 3 of stateControl
	public static final int RUNNING = 8;   // bit 4 of stateControl
	public static final int STARTED = 16;  // bit 5 of stateControl

	public final int    progInd;      // Progress indication in numerical form
	public final String project;      // Project.getName()
	public final String progress;     // converted to percentage
	public final String size;         // converted to string
	public final String elapsed;      // converted to time-string
	public final String speed;        // converted to string
	public final String state;        // converted to string

	public TransferInfo(String fileName,
			String projectUrl,
			int    stateControl,
			int    progInd,
			String project,
			String progress,
			String size,
			String elapsed,
			String speed,
			String state
			) {
		this.fileName = fileName;
		this.projectUrl = projectUrl;
		this.stateControl = stateControl;
		this.progInd = progInd;
		this.project = project;
		this.progress = progress;
		this.size = size;
		this.elapsed = elapsed;
		this.speed = speed;
		this.state = state;
	}
}
