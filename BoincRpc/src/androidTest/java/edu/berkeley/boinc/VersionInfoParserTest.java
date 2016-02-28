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

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;


@RunWith(AndroidJUnit4.class)
@SmallTest
public class VersionInfoParserTest {

    @Test
    public void parseNormal() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<server_version>\n" +
                "   <major>7</major>\n" +
                "   <minor>4</minor>\n" +
                "   <release>23</release>\n" +
                "</server_version>\n" +
                "</boinc_gui_rpc_reply>\n";
        VersionInfo versionInfo = null;
        try {
            versionInfo = VersionInfoParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
        assertThat(versionInfo.major, is(equalTo(7)));
        assertThat(versionInfo.minor, is(equalTo(4)));
        assertThat(versionInfo.release, is(equalTo(23)));
        assertFalse(versionInfo.prerelease);
    }

    @Test
    public void emptyAnswer() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "</boinc_gui_rpc_reply>\n";
        VersionInfo versionInfo = null;
        try {
            versionInfo = VersionInfoParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
        assertNull(versionInfo);
    }

    @Test
    public void elementNotPresent() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<core_client_major_version>7</core_client_major_version>\n" +
                "<core_client_minor_version>4</core_client_minor_version>\n" +
                "<core_client_release>23</core_client_release>\n" +
                "</boinc_gui_rpc_reply>\n";
        VersionInfo versionInfo = null;
        try {
            versionInfo = VersionInfoParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
        assertNull(versionInfo);
    }

    @Test
    public void unauthorized() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<unauthorized/>\n" +
                "</boinc_gui_rpc_reply>\n";
        VersionInfo versionInfo = null;
        String errorMsg = "";
        try {
            versionInfo = VersionInfoParser.parse(received);
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
        assertNull(versionInfo);
    }

    @Test
    public void invalidData() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<server_version>\n" +
                "   <major>7</major>\n" +
                "   <minor>4</minor>\n" +
                "   <release>23</release>\n" +
                "</server_version>\n" +
                "</boinc_gui_rpc_reply";
        VersionInfo versionInfo = null;
        String errorMsg = "";
        try {
            versionInfo = VersionInfoParser.parse(received);
            fail("Successful parsing unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (AuthorizationFailedException e) {
            errorMsg = e.getMessage();
            fail("AuthorizationFailedException unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (InvalidDataReceivedException e) {
            errorMsg = e.getMessage();
        }
        assertThat(errorMsg, is(equalTo("Malformed XML while parsing <server_version>")));
        assertNull(versionInfo);
    }
}
