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
import java.util.Vector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;


@RunWith(AndroidJUnit4.class)
@SmallTest
public class ResultsParserTest {

    @Test
    public void parseNormal() {
        final String received = TestSupport.readResource(edu.berkeley.boinc.test.R.raw.get_results_reply);
        assertThat(received.length(), is(equalTo(56564)));
        Vector<Result> results = null;
        try {
            results = ResultsParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
        assertNotNull(results);
        assertThat(results.size(), is(equalTo(79)));
        assertThat(results.elementAt(0).name, is(equalTo("110355.5+373030_area26987221_1")));
        assertThat(results.elementAt(0).wu_name, is(equalTo("110355.5+373030_area26987221")));
        assertThat(results.elementAt(0).project_url, is(equalTo("http://pogs.theskynet.org/pogs/")));
        assertThat(results.elementAt(0).version_num, is(equalTo(402)));
        assertThat(results.elementAt(0).report_deadline, is(equalTo(1456776412L)));
        assertThat(results.elementAt(0).received_time, is(equalTo(1456171613L)));
        assertThat(results.elementAt(0).final_cpu_time, is(equalTo(0.000000d)));
        assertThat(results.elementAt(0).final_elapsed_time, is(equalTo(0.000000d)));
        assertThat(results.elementAt(0).state, is(equalTo(2)));
        assertFalse(results.elementAt(0).suspended_via_gui);
        assertFalse(results.elementAt(0).project_suspended_via_gui);
        assertTrue(results.elementAt(0).active_task);
        assertThat(results.elementAt(0).active_task_state, is(equalTo(0)));
        assertThat(results.elementAt(0).app_version_num, is(equalTo(402)));
        assertThat(results.elementAt(0).checkpoint_cpu_time, is(equalTo(9711.279000d)));
        assertThat(results.elementAt(0).current_cpu_time, is(equalTo(9711.279000d)));
        assertThat(results.elementAt(0).fraction_done, is(equalTo(0.590909f)));
        assertThat(results.elementAt(0).elapsed_time, is(equalTo(9826.891117d)));
        assertThat(results.elementAt(0).swap_size, is(equalTo(82063360.000000d)));
        assertThat(results.elementAt(0).working_set_size_smoothed, is(equalTo(40103936.000000d)));
        assertThat(results.elementAt(0).estimated_cpu_time_remaining, is(equalTo(6404.006892d)));
        assertNull(results.elementAt(0).resources);
        assertThat(results.elementAt(1).name, is(equalTo("110355.5+373030_area26987200_1")));
        assertThat(results.elementAt(1).wu_name, is(equalTo("110355.5+373030_area26987200")));
        assertThat(results.elementAt(1).project_url, is(equalTo("http://pogs.theskynet.org/pogs/")));
        assertThat(results.elementAt(1).version_num, is(equalTo(402)));
        assertThat(results.elementAt(1).report_deadline, is(equalTo(1456776412L)));
        assertThat(results.elementAt(1).received_time, is(equalTo(1456171613L)));
        assertThat(results.elementAt(1).final_cpu_time, is(equalTo(0.000000d)));
        assertThat(results.elementAt(1).final_elapsed_time, is(equalTo(0.000000d)));
        assertThat(results.elementAt(1).state, is(equalTo(2)));
        assertFalse(results.elementAt(1).suspended_via_gui);
        assertFalse(results.elementAt(1).project_suspended_via_gui);
        assertFalse(results.elementAt(1).active_task);
        assertThat(results.elementAt(1).active_task_state, is(equalTo(0)));
        assertThat(results.elementAt(1).app_version_num, is(equalTo(0)));
        assertThat(results.elementAt(1).checkpoint_cpu_time, is(equalTo(0.0d)));
        assertThat(results.elementAt(1).current_cpu_time, is(equalTo(0.0d)));
        assertThat(results.elementAt(1).fraction_done, is(equalTo(0.0f)));
        assertThat(results.elementAt(1).elapsed_time, is(equalTo(0.0d)));
        assertThat(results.elementAt(1).swap_size, is(equalTo(0.0d)));
        assertThat(results.elementAt(1).working_set_size_smoothed, is(equalTo(0.0d)));
        assertThat(results.elementAt(1).estimated_cpu_time_remaining, is(equalTo(15654.235590d)));
        assertNull(results.elementAt(1).resources);
        assertThat(results.elementAt(16).name, is(equalTo("FAH2_000075_avx17680_000099_0046_013_0")));
        assertThat(results.elementAt(16).wu_name, is(equalTo("FAH2_000075_avx17680_000099_0046_013")));
        assertThat(results.elementAt(16).project_url, is(equalTo("http://www.worldcommunitygrid.org/")));
        assertThat(results.elementAt(16).version_num, is(equalTo(715)));
        assertThat(results.elementAt(16).report_deadline, is(equalTo(1456617504L)));
        assertThat(results.elementAt(16).received_time, is(equalTo(1456271908L)));
        assertThat(results.elementAt(16).final_cpu_time, is(equalTo(0.000000d)));
        assertThat(results.elementAt(16).final_elapsed_time, is(equalTo(0.000000d)));
        assertThat(results.elementAt(16).state, is(equalTo(2)));
        assertFalse(results.elementAt(16).suspended_via_gui);
        assertFalse(results.elementAt(16).project_suspended_via_gui);
        assertTrue(results.elementAt(16).active_task);
        assertThat(results.elementAt(16).active_task_state, is(equalTo(1)));
        assertThat(results.elementAt(16).app_version_num, is(equalTo(715)));
        assertThat(results.elementAt(16).checkpoint_cpu_time, is(equalTo(66350.780000d)));
        assertThat(results.elementAt(16).current_cpu_time, is(equalTo(66545.040000d)));
        assertThat(results.elementAt(16).fraction_done, is(equalTo(0.992840f)));
        assertThat(results.elementAt(16).elapsed_time, is(equalTo(67239.363522d)));
        assertThat(results.elementAt(16).swap_size, is(equalTo(202452992.000000d)));
        assertThat(results.elementAt(16).working_set_size_smoothed, is(equalTo(74428416.000000d)));
        assertThat(results.elementAt(16).estimated_cpu_time_remaining, is(equalTo(487.628218d)));
        assertNull(results.elementAt(16).resources);
    }

    @Test
    public void emptyAnswer() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<results>\n" +
                "</results>\n" +
                "</boinc_gui_rpc_reply>\n";
        Vector<Result> results = null;
        try {
            results = ResultsParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void elementNotPresent() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<core_client_major_version>7</core_client_major_version>\n" +
                "<core_client_minor_version>4</core_client_minor_version>\n" +
                "<core_client_release>23</core_client_release>\n" +
                "</boinc_gui_rpc_reply>\n";
        Vector<Result> results = null;
        try {
            results = ResultsParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void unauthorized() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<unauthorized/>\n" +
                "</boinc_gui_rpc_reply>\n";
        Vector<Result> results = null;
        String errorMsg = "";
        try {
            results = ResultsParser.parse(received);
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
        assertNull(results);
    }

    @Test
    public void invalidData() {
        final String received = TestSupport.readResource(edu.berkeley.boinc.test.R.raw.get_results_reply, 56562);
        Vector<Result> results = null;
        String errorMsg = "";
        try {
            results = ResultsParser.parse(received);
            fail("Successful parsing unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (AuthorizationFailedException e) {
            errorMsg = e.getMessage();
            fail("AuthorizationFailedException unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (InvalidDataReceivedException e) {
            errorMsg = e.getMessage();
        }
        assertThat(errorMsg, is(equalTo("Malformed XML while parsing <results>")));
        assertNull(results);
    }
}
