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
public class TransfersParserTest {

    @Test
    public void parseNormal() {
        final String received = TestSupport.readResource(edu.berkeley.boinc.test.R.raw.get_file_transfers_reply);
        assertThat(received.length(), is(equalTo(11517)));
        Vector<Transfer> transfers = null;
        try {
            transfers = TransfersParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
        assertNotNull(transfers);
        assertThat(transfers.size(), is(equalTo(17)));
        assertThat(transfers.elementAt(0).name, is(equalTo("data_collect_v4_1454745134_512096_0_0")));
        assertThat(transfers.elementAt(0).project_url, is(equalTo("http://wuprop.boinc-af.org/")));
        assertTrue(transfers.elementAt(0).is_upload);
        assertThat(transfers.elementAt(0).nbytes, is(equalTo(70211L)));
        assertTrue(transfers.elementAt(0).xfer_active);
        assertThat(transfers.elementAt(0).status, is(equalTo(1)));
        assertThat(transfers.elementAt(0).next_request_time, is(equalTo(1456543231L)));
        assertThat(transfers.elementAt(0).time_so_far, is(equalTo(11L)));
        assertThat(transfers.elementAt(0).bytes_xferred, is(equalTo(65536L)));
        assertThat(transfers.elementAt(0).xfer_speed, is(equalTo(7243.123531f)));
        assertThat(transfers.elementAt(0).project_backoff, is(equalTo(0L)));
        assertThat(transfers.elementAt(1).name, is(equalTo("GW_BBH2.jpg")));
        assertThat(transfers.elementAt(1).project_url, is(equalTo("http://einstein.phys.uwm.edu/")));
        assertFalse(transfers.elementAt(1).is_upload);
        assertThat(transfers.elementAt(1).nbytes, is(equalTo(0L)));
        assertTrue(transfers.elementAt(1).xfer_active);
        assertThat(transfers.elementAt(1).status, is(equalTo(0)));
        assertThat(transfers.elementAt(1).next_request_time, is(equalTo(1456543420L)));
        assertThat(transfers.elementAt(1).time_so_far, is(equalTo(1L)));
        assertThat(transfers.elementAt(1).bytes_xferred, is(equalTo(15049L)));
        assertThat(transfers.elementAt(1).xfer_speed, is(equalTo(11297.681883f)));
        assertThat(transfers.elementAt(1).project_backoff, is(equalTo(0L)));
        assertThat(transfers.elementAt(3).name, is(equalTo("LIGO_Livingston.jpg")));
        assertThat(transfers.elementAt(3).project_url, is(equalTo("http://einstein.phys.uwm.edu/")));
        assertFalse(transfers.elementAt(3).is_upload);
        assertThat(transfers.elementAt(3).nbytes, is(equalTo(0L)));
        assertFalse(transfers.elementAt(3).xfer_active);
        assertThat(transfers.elementAt(3).status, is(equalTo(0)));
        assertThat(transfers.elementAt(3).next_request_time, is(equalTo(1456543420L)));
        assertThat(transfers.elementAt(3).time_so_far, is(equalTo(0L)));
        assertThat(transfers.elementAt(3).bytes_xferred, is(equalTo(0L)));
        assertThat(transfers.elementAt(3).xfer_speed, is(equalTo(0.0f)));
        assertThat(transfers.elementAt(3).project_backoff, is(equalTo(0L)));
    }

    @Test
    public void emptyAnswer() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<file_transfers>\n" +
                "</file_transfers>\n" +
                "</boinc_gui_rpc_reply>\n";
        Vector<Transfer> transfers = null;
        try {
            transfers = TransfersParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
        assertNotNull(transfers);
        assertTrue(transfers.isEmpty());
    }

    @Test
    public void elementNotPresent() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<core_client_major_version>7</core_client_major_version>\n" +
                "<core_client_minor_version>4</core_client_minor_version>\n" +
                "<core_client_release>23</core_client_release>\n" +
                "</boinc_gui_rpc_reply>\n";
        Vector<Transfer> transfers = null;
        try {
            transfers = TransfersParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
        assertNotNull(transfers);
        assertTrue(transfers.isEmpty());
    }

    @Test
    public void unauthorized() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<unauthorized/>\n" +
                "</boinc_gui_rpc_reply>\n";
        Vector<Transfer> transfers = null;
        String errorMsg = "";
        try {
            transfers = TransfersParser.parse(received);
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
        assertNull(transfers);
    }

    @Test
    public void invalidData() {
        final String received = TestSupport.readResource(edu.berkeley.boinc.test.R.raw.get_file_transfers_reply, 11515);
        Vector<Transfer> transfers = null;
        String errorMsg = "";
        try {
            transfers = TransfersParser.parse(received);
            fail("Successful parsing unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (AuthorizationFailedException e) {
            errorMsg = e.getMessage();
            fail("AuthorizationFailedException unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (InvalidDataReceivedException e) {
            errorMsg = e.getMessage();
        }
        assertThat(errorMsg, is(equalTo("Malformed XML while parsing <file_transfers>")));
        assertNull(transfers);
    }
}
