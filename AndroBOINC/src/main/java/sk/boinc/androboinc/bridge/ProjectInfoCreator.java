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

import android.content.res.Resources;
import sk.boinc.androboinc.R;
import sk.boinc.androboinc.clientconnection.ProjectInfo;
import edu.berkeley.boinc.lite.Project;


public class ProjectInfoCreator {
	public static ProjectInfo create(final Project prj, float totalResources, final Formatter formatter) {
		Resources resources = formatter.getResources();
		float pctShare = prj.resource_share/totalResources*100;
		int resShare = (int)pctShare;
		String share = String.format("%.0f (%.2f%%)", prj.resource_share, pctShare);
		StringBuilder sb = formatter.getStringBuilder();
		int statusId = 0; // 0 = active
		if (prj.suspended_via_gui) {
			sb.append(resources.getString(R.string.projectStatusSuspended));
			statusId |= ProjectInfo.SUSPENDED;
		}
		if (prj.dont_request_more_work) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(resources.getString(R.string.projectStatusNNW));
			statusId |= ProjectInfo.NNW;
		}
		if (statusId == 0) {
			// not suspended & new tasks allowed
			sb.append(resources.getString(R.string.projectStatusActive));
		}
		return new ProjectInfo(prj.master_url,
				statusId,
				resShare,
				prj.getName(),
				prj.user_name,
				prj.team_name,
				prj.user_total_credit,
				prj.user_expavg_credit,
				prj.host_total_credit,
				prj.host_expavg_credit,
				share,
				sb.toString());
	}
}
