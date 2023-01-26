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

import android.util.Log;
import java.security.MessageDigest;


/**
 * Wrapper class for MD5 hash operations for BOINC purpose.
 * 
 * @author Palo M.
 */
public class Md5 {
    public static final String TAG = "Md5";

    /**
     * Hashes the input string
     *
     * @param text The text to be hashed
     * @return The hash of the input converted to string
     */
    public static String hash(String text) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(text.getBytes("iso-8859-1"), 0, text.length());
            byte[] md5hash = md5.digest();
            StringBuilder sb = new StringBuilder();
            for (byte hash : md5hash) {
                sb.append(String.format("%02x", hash));
            }
            return sb.toString();
        }
        catch (Exception e) {
            Log.w(TAG, "Error when calculating MD5 hash");
            return "";
        }
    }
}
