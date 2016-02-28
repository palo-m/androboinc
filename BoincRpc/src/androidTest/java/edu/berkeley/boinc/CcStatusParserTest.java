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

import edu.berkeley.boinc.testutil.TestSupport;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;


@RunWith(AndroidJUnit4.class)
@SmallTest
public class CcStatusParserTest {

    @Test
    public void parseNormal() {
        final String received = TestSupport.readResource(edu.berkeley.boinc.test.R.raw.get_cc_status_reply);
        assertThat(received.length(), is(equalTo(788)));
        CcStatus ccStatus = null;
        try {
            ccStatus = CcStatusParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
        assertNotNull(ccStatus);
        assertThat(ccStatus.task_mode, is(equalTo(1)));
        assertThat(ccStatus.task_mode_perm, is(equalTo(2)));
        assertThat(ccStatus.task_mode_delay, is(equalTo(120.000000d)));
        assertThat(ccStatus.gpu_mode, is(equalTo(0)));
        assertThat(ccStatus.gpu_mode_perm, is(equalTo(2)));
        assertThat(ccStatus.gpu_mode_delay, is(equalTo(60.000000d)));
        assertThat(ccStatus.network_mode, is(equalTo(0)));
        assertThat(ccStatus.network_mode_perm, is(equalTo(1)));
        assertThat(ccStatus.network_mode_delay, is(equalTo(90.000000d)));
        assertThat(ccStatus.network_status, is(equalTo(0)));
    }

    @Test
    public void emptyAnswer() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<cc_status>\n" +
                "</cc_status>\n" +
                "</boinc_gui_rpc_reply>\n";
        CcStatus ccStatus = null;
        try {
            ccStatus = CcStatusParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
        assertNotNull(ccStatus);
        assertThat(ccStatus.task_mode, is(equalTo(-1)));
        assertThat(ccStatus.task_mode_perm, is(equalTo(-1)));
        assertThat(ccStatus.gpu_mode, is(equalTo(-1)));
        assertThat(ccStatus.gpu_mode_perm, is(equalTo(-1)));
        assertThat(ccStatus.network_mode, is(equalTo(-1)));
        assertThat(ccStatus.network_mode_perm, is(equalTo(-1)));
        assertThat(ccStatus.network_status, is(equalTo(-1)));
    }

    @Test
    public void elementNotPresent() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<core_client_major_version>7</core_client_major_version>\n" +
                "<core_client_minor_version>4</core_client_minor_version>\n" +
                "<core_client_release>23</core_client_release>\n" +
                "</boinc_gui_rpc_reply>\n";
        CcStatus ccStatus = null;
        String errorMsg = "";
        try {
            ccStatus = CcStatusParser.parse(received);
            fail("Successful parsing unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (AuthorizationFailedException e) {
            errorMsg = e.getMessage();
            fail("AuthorizationFailedException unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (InvalidDataReceivedException e) {
            errorMsg = e.getMessage();
        }
        assertThat(errorMsg, is(equalTo("Invalid data received")));
        assertNull(ccStatus);
    }

    @Test
    public void unauthorized() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<unauthorized/>\n" +
                "</boinc_gui_rpc_reply>\n";
        CcStatus ccStatus = null;
        String errorMsg = "";
        try {
            ccStatus = CcStatusParser.parse(received);
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
        assertNull(ccStatus);
    }

    @Test
    public void invalidData() {
        final String received = TestSupport.readResource(edu.berkeley.boinc.test.R.raw.get_cc_status_reply, 786);
        CcStatus ccStatus = null;
        String errorMsg = "";
        try {
            ccStatus = CcStatusParser.parse(received);
            fail("Successful parsing unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (AuthorizationFailedException e) {
            errorMsg = e.getMessage();
            fail("AuthorizationFailedException unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (InvalidDataReceivedException e) {
            errorMsg = e.getMessage();
        }
        assertThat(errorMsg, is(equalTo("Malformed XML while parsing <cc_status>")));
        assertNull(ccStatus);
   }
}
