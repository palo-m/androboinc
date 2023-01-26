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

package sk.boinc.androboinc.util;

/*
 * These constants define names of file and properties which will store 
 * network statistics data.
 */
public interface NetStatsStorage {
	String NET_STATS_FILE = "NetworkStats";
	String NET_STATS_WARNING = "warningDisabled";
	String NET_STATS_TOTAL_RCVD = "totalReceived";
	String NET_STATS_TOTAL_SENT = "totalSent";
}
