/* 
 * AndroBOINC - BOINC Manager for Android
 * Copyright (C) 2010, Pavol Michalec
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package sk.boinc.androboinc.bridge;

import sk.boinc.androboinc.clientconnection.HostInfo;


public class HostInfoCreator {
	public static HostInfo create(final edu.berkeley.boinc.lite.HostInfo hostInfo, final Formatter formatter) {
		HostInfo hi = new HostInfo();
		hi.hostCpId = hostInfo.host_cpid;
		StringBuilder sb = formatter.getStringBuilder();
		// TODO: Use Resources for strings
		sb.append("<b>Domain name:</b> ");
		sb.append(hostInfo.domain_name);
		sb.append("<br /><b>IP-address:</b> ");
		sb.append(hostInfo.ip_addr);
		sb.append("<br /><b>Cross-project ID:</b> ");
		sb.append(hostInfo.host_cpid);
		sb.append("<br /><b>OS name:</b> ");
		sb.append(hostInfo.os_name);
		sb.append("<br /><b>OS version:</b> ");
		sb.append(hostInfo.os_version);
		sb.append("<br /><b>#CPUs:</b> ");
		sb.append(hostInfo.p_ncpus);
		sb.append("<br /><b>Vendor:</b> ");
		sb.append(hostInfo.p_vendor);
		sb.append("<br /><b>Model:</b> ");
		sb.append(hostInfo.p_model);
		sb.append("<br /><b>Features:</b> ");
		sb.append(hostInfo.p_features);
		sb.append("<br /><b>MFLOPS:</b> ");
		sb.append(String.format("%.2f", hostInfo.p_fpops/1000000));
		sb.append("<br /><b>MIPS:</b> ");
		sb.append(String.format("%.2f", hostInfo.p_iops/1000000));
		sb.append("<br /><b>Memory Bandwidth:</b> ");
		sb.append(String.format("%.2f", hostInfo.p_membw/1000000));
		sb.append("<br /><b>Benchmarks calculated:</b> ");
		sb.append(formatter.formatDate(hostInfo.p_calculated));
		sb.append("<br /><b>Memory size:</b> ");
		sb.append(String.format("%.0f", hostInfo.m_nbytes/1024/1024));
		sb.append("MiB");
		sb.append("<br /><b>Cache size:</b> ");
		sb.append(String.format("%.0f", hostInfo.m_cache/1024));
		sb.append("KiB");
		sb.append("<br /><b>Swap size:</b> ");
		sb.append(String.format("%.0f", hostInfo.m_swap/1024/1024));
		sb.append("MiB");
		sb.append("<br /><b>Disk size:</b> ");
		sb.append(String.format("%.1f", hostInfo.d_total/1000000000));
		sb.append("GB");
		sb.append("<br /><b>Disk free:</b> ");
		sb.append(String.format("%.1f", hostInfo.d_free/1000000000));
		sb.append("GB");
		sb.append("<br /><br />");
		hi.htmlText = sb.toString();
		return hi;
	}
}
