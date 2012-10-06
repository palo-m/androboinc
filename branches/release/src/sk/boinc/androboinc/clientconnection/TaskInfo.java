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
 * Description of BOINC task for AndroBOINC purpose
 * Reflects the classes of BOINC-library: Result, Project, Workunit, App
 */
public class TaskInfo {
	public final String taskName;     // Result.name - unique ID
	public final String projectUrl;   // Result.project_url
	public final int    stateControl; // state control in numerical form
	public static final int DOWNLOADING = 1;
	public static final int READY_TO_START = 2;
	public static final int RUNNING = 3;
	public static final int PREEMPTED = 4;
	public static final int UPLOADING = 5;
	public static final int READY_TO_REPORT = 6;
	public static final int SUSPENDED = 7;
	public static final int ABORTED = 8;
	public static final int ERROR = 9;

	public final int    progInd;      // Progress indication in numerical form
	public final long   deadlineNum;  // Deadline in numerical form
	public final String project;      // Project.getName()
	public final String application;  // App.getName() + Workunit.version_num converted to string
	public final String elapsed;      // Result.elapsed_time converted to time-string
	public final String progress;     // Result.fraction_done converted to percentage string
	public final String toCompletion; // Result.estimated_cpu_time_remaining converted to time-string
	public final String deadline;     // Result.report_deadline converted to date-string
	public final String virtMemSize;  // Result.swap_size converted to size-string (base 2) (can be null)
	public final String workSetSize;  // Result.working_set_size_smoothed converted to size-string (base 10) (can be null)
	public final String cpuTime;      // Result.current_cpu_time converted to time-string (can be null)
	public final String chckpntTime;  // Result.checkpoint_cpu_time converted to time-string (can be null)
	public final String resources;    // Result.resources (can be null)
	public final String state;        // Result.state combined with Result.active_task_state converted to string

	public TaskInfo(String taskName,
			String projectUrl,
			int    stateControl,
			int    progInd,
			long   deadlineNum,
			String project,
			String application,
			String elapsed,
			String progress,
			String toCompletion,
			String deadline,
			String virtMemSize,
			String workSetSize,
			String cpuTime,
			String chckpntTime,
			String resources,
			String state
			) {
		this.taskName = taskName;
		this.projectUrl = projectUrl;
		this.stateControl = stateControl;
		this.progInd = progInd;
		this.deadlineNum = deadlineNum;
		this.project = project;
		this.application = application;
		this.elapsed = elapsed;
		this.progress = progress;
		this.toCompletion = toCompletion;
		this.deadline = deadline;
		this.virtMemSize = virtMemSize;
		this.workSetSize = workSetSize;
		this.cpuTime = cpuTime;
		this.chckpntTime = chckpntTime;
		this.resources = resources;
		this.state = state;
	}
}
