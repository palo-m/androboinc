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
 * The values of following constants must be consistent with the corresponding keys 
 * in file preferences.xml (res/xml directory)
 */
public interface PreferenceName {
	String SCREEN_ORIENTATION = "screenOrientation";
	String LOCK_SCREEN_ON = "lockScreenOn";
	String AUTO_CONNECT = "autoConnect";
	String AUTO_CONNECT_HOST = "autoConnectHost";
	String AUTO_UPDATE_WIFI = "autoUpdateIntervalWiFi";
	String AUTO_UPDATE_MOBILE = "autoUpdateIntervalMobile";
	String COLLECT_STATS = "trackNetworkUsage";
	String UPGRADE_INFO_SHOWN_VERSION = "upgradeInfoShownVersion";
	String LAST_ACTIVE_TAB = "lastActiveTab";
	String LIMIT_MESSAGES = "limitMessages";
}
