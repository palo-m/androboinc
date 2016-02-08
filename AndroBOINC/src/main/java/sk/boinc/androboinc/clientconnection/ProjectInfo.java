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
 * Description of BOINC project for AndroBOINC purpose
 * Reflects the class Project of BOINC-library
 */
public class ProjectInfo {
	public final String masterUrl;    // Project.master_url - Master URL of the project - unique ID
	public final int    statusId;     // Status in numerical form
	// integer representation: 0 = active, 1 = suspended, 2 = no new work, 3 = suspended & no new work
	public static final int SUSPENDED = 1;  // bit 1 of statusId
	public static final int NNW = 2;        // bit 2 of statusId

	public final int    resShare;     // Resources share in numerical form
	public final String project;      // Project.getName()
	public final String account;      // Project.user_name
	public final String team;         // Project.team_name
	public final double user_credit;  // Project.user_total_credit
	public final double user_rac;     // Project.user_expavg_credit
	public final double host_credit;  // Project.host_total_credit
	public final double host_rac;     // Project.host_expavg_credit
	public final String share;        // Project.resource_share converted to percentage
	public final String status;       // Combined Project.suspended_via_gui & Project.dont_request_more_work converted to string

	public ProjectInfo(String masterUrl,
			int    statusId,
			int    resShare,
			String project,
			String account,
			String team,
			double user_credit,
			double user_rac,
			double host_credit,
			double host_rac,
			String share,
			String status
			) {
		this.masterUrl = masterUrl;
		this.statusId = statusId;
		this.resShare = resShare;
		this.project = project;
		this.account = account;
		this.team = team;
		this.user_credit = user_credit;
		this.user_rac = user_rac;
		this.host_credit = host_credit;
		this.host_rac = host_rac;
		this.share = share;
		this.status = status;
	}
}
