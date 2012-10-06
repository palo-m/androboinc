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
 * Description of BOINC-client mode for AndroBOINC purpose
 */
public class ModeInfo {
	public final int task_mode;  	// run-mode of BOINC-client
	public final int gpu_mode;  	// GPU-mode of BOINC-client
	public final int network_mode;  // network-mode of BOINC-client

	public ModeInfo(int task_mode, int gpu_mode, int network_mode) {
		this.task_mode = task_mode;
		this.gpu_mode = gpu_mode;
		this.network_mode = network_mode;
	}
}
