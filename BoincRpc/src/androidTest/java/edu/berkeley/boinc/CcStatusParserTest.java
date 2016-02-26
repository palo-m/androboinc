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
import static org.junit.Assert.fail;


@RunWith(AndroidJUnit4.class)
@SmallTest
public class CcStatusParserTest {

    @Test
    public void parseNormal() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<cc_status>\n" +
                "   <network_status>0</network_status>\n" +
                "   <ams_password_error>0</ams_password_error>\n" +
                "   <task_suspend_reason>0</task_suspend_reason>\n" +
                "   <task_mode>1</task_mode>\n" +
                "   <task_mode_perm>2</task_mode_perm>\n" +
                "   <task_mode_delay>120.000000</task_mode_delay>\n" +
                "   <gpu_suspend_reason>0</gpu_suspend_reason>\n" +
                "   <gpu_mode>0</gpu_mode>\n" +
                "   <gpu_mode_perm>2</gpu_mode_perm>\n" +
                "   <gpu_mode_delay>60.000000</gpu_mode_delay>\n" +
                "   <network_suspend_reason>0</network_suspend_reason>\n" +
                "   <network_mode>0</network_mode>\n" +
                "   <network_mode_perm>1</network_mode_perm>\n" +
                "   <network_mode_delay>90.000000</network_mode_delay>\n" +
                "   <disallow_attach>0</disallow_attach>\n" +
                "   <simple_gui_only>0</simple_gui_only>\n" +
                "   <max_event_log_lines>2000</max_event_log_lines>\n" +
                "</cc_status>\n" +
                "</boinc_gui_rpc_reply>\n";
        CcStatus ccStatus = new CcStatus();
        try {
            ccStatus = CcStatusParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            ccStatus.task_mode = -2;
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            ccStatus.task_mode = -3;
            fail("InvalidDataReceivedException unexpected");
        }
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
    public void unauthorized() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<unauthorized/>\n" +
                "</boinc_gui_rpc_reply>\n";
        CcStatus ccStatus = new CcStatus();
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
        assertThat(ccStatus.task_mode, is(equalTo(-1)));
    }

    @Test
    public void invalidData() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<cc_status>\n" +
                "   <network_status>0</network_status>\n" +
                "   <ams_password_error>0</ams_password_error>\n" +
                "   <task_suspend_reason>0</task_suspend_reason>\n" +
                "   <task_mode>1</task_mode>\n" +
                "   <task_mode_perm>2</task_mode_perm>\n" +
                "   <task_mode_delay>120.000000</task_mode_delay>\n" +
                "   <gpu_suspend_reason>0</gpu_suspend_reason>\n" +
                "   <gpu_mode>0</gpu_mode>\n" +
                "   <gpu_mode_perm>2</gpu_mode_perm>\n" +
                "   <gpu_mode_delay>60.000000</gpu_mode_delay>\n" +
                "   <network_suspend_reason>0</network_suspend_reason>\n" +
                "   <network_mode>0</network_mode>\n" +
                "   <network_mode_perm>1</network_mode_perm>\n" +
                "   <network_mode_delay>90.000000</network_mode_delay>\n" +
                "   <disallow_attach>0</disallow_attach>\n" +
                "   <simple_gui_only>0</simple_gui_only>\n" +
                "   <max_event_log_lines>2000</max_event_log_lines>\n" +
                "</cc_status>\n" +
                "</boinc_gui_rpc_reply";
        CcStatus ccStatus = new CcStatus();
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
        assertThat(ccStatus.task_mode, is(equalTo(-1)));
   }

    @Test
    public void elementNotPresent() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<core_client_major_version>7</core_client_major_version>\n" +
                "<core_client_minor_version>4</core_client_minor_version>\n" +
                "<core_client_release>23</core_client_release>\n" +
                "</boinc_gui_rpc_reply>\n";
        CcStatus ccStatus = new CcStatus();
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
        assertThat(ccStatus.task_mode, is(equalTo(-1)));
    }
}
