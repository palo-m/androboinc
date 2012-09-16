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

package edu.berkeley.boinc.lite;

/**
 * This exception is thrown when authentication fails
 */
public class AuthorizationFailedException extends RpcClientFailedException {
	private static final long serialVersionUID = 7752615754130475737L; // Generated

	public AuthorizationFailedException() {
		super("Authorization Failed");
	}

	public AuthorizationFailedException(String detailMessage) {
		super(detailMessage);
	}

	public AuthorizationFailedException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public AuthorizationFailedException(Throwable throwable) {
		super("Authorization Failed", throwable);
	}
}
