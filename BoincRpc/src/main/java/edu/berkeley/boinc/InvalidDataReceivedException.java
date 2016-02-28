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

package edu.berkeley.boinc;


/**
 * This exception is thrown when invalid or unexpected data is received
 * during RpcClient call 
 */
public class InvalidDataReceivedException extends RpcClientFailedException {
    private static final long serialVersionUID = -4865117105470215454L; // Generated

    public InvalidDataReceivedException() {
        super("Invalid data received");
    }

    public InvalidDataReceivedException(String detailMessage) {
        super(detailMessage);
    }

    public InvalidDataReceivedException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public InvalidDataReceivedException(Throwable throwable) {
        super("Invalid data received", throwable);
    }
}
