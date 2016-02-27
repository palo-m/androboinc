/*
 * AndroBOINC - BOINC Manager for Android
 * Copyright (C) 2010 - 2016, Pavol Michalec
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

package edu.berkeley.boinc.testutil;

import android.support.test.InstrumentationRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


public class TestSupport {

    public static String readResource(int resourceId, int maxSize) {
        InputStream stream = InstrumentationRegistry.getInstrumentation().getContext().getResources().openRawResource(resourceId);
        assertNotNull(stream);
        StringBuilder sb = new StringBuilder();
        String strLine;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            while ((strLine = reader.readLine()) != null) {
                sb.append(strLine);
                sb.append("\n");
            }
        }
        catch (IOException e) {
            fail("IOException");
        }
        if (maxSize > 0) {
            if (sb.length() > maxSize) {
                sb.setLength(maxSize);
            }
        }
        return sb.toString();
    }

    public static String readResource(int resourceId) {
        return readResource(resourceId, 0);
    }
}
