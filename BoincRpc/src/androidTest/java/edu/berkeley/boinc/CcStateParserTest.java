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

package edu.berkeley.boinc;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;


@RunWith(AndroidJUnit4.class)
@SmallTest
public class CcStateParserTest {

    private String readResource(int resourceId, int maxSize) {
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
    //*
    @Test
    public void parseNormal() {
        final String received = readResource(edu.berkeley.boinc.test.R.raw.client_state, 0);
        assertThat(received.length(), is(equalTo(98918)));
        CcState ccState = null;
        try {
            ccState = CcStateParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
        assertNotNull(ccState);
        assertThat(ccState.version_info.major, is(equalTo(7)));
        assertThat(ccState.version_info.minor, is(equalTo(4)));
        assertThat(ccState.version_info.release, is(equalTo(23)));
        assertNotNull(ccState.host_info);
        assertThat(ccState.host_info.domain_name, is(equalTo("machine3")));
        assertNotNull(ccState.projects);
        assertThat(ccState.projects.size(), is(equalTo(3)));
        assertThat(ccState.projects.elementAt(0).getName(), is(equalTo("WUProp@Home")));
        assertThat(ccState.projects.elementAt(1).getName(), is(equalTo("World Community Grid")));
        assertThat(ccState.projects.elementAt(2).getName(), is(equalTo("pogs")));
        assertNotNull(ccState.apps);
        assertThat(ccState.apps.size(), is(equalTo(32)));
        assertNotNull(ccState.workunits);
        assertThat(ccState.workunits.size(), is(equalTo(5)));
        assertNotNull(ccState.results);
        assertThat(ccState.results.size(), is(equalTo(5)));
    }
    //*/

    @Test
    public void emptyAnswer() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "</boinc_gui_rpc_reply>\n";
        CcState ccState = null;
        try {
            ccState = CcStateParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
        assertNotNull(ccState);
        assertTrue(ccState.projects.isEmpty());
        assertTrue(ccState.apps.isEmpty());
        assertTrue(ccState.workunits.isEmpty());
        assertTrue(ccState.results.isEmpty());
    }

    @Test
    public void unauthorized() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<unauthorized/>\n" +
                "</boinc_gui_rpc_reply>\n";
        CcState ccState = null;
        String errorMsg = "";
        try {
            ccState = CcStateParser.parse(received);
            fail("Successful parsing unexpected, AuthorizationFailedException should be thrown instead");
        }
        catch (AuthorizationFailedException e) {
            errorMsg = e.getMessage();
        }
        catch (InvalidDataReceivedException e) {
            errorMsg = e.getMessage();
            fail("InvalidDataReceivedException unexpected, AuthorizationFailedException should be thrown instead");
        }
        assertThat(errorMsg, is(equalTo("Authorization Failed")));
        assertNull(ccState);
    }

    @Test
    public void invalidData() {
        final String received = readResource(edu.berkeley.boinc.test.R.raw.client_state, 96590 /* 98916 */);
        CcState ccState = null;
        String errorMsg = "";
        try {
            ccState = CcStateParser.parse(received);
            fail("Successful parsing unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (AuthorizationFailedException e) {
            errorMsg = e.getMessage();
            fail("AuthorizationFailedException unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (InvalidDataReceivedException e) {
            errorMsg = e.getMessage();
        }
        assertThat(errorMsg, is(equalTo("Malformed XML while parsing <cc_state>")));
        assertNull(ccState);
    }
}
