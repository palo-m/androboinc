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
public class MessagesParserTest {

    @Test
    public void parseNormal() {
        final String received = TestSupport.readResource(edu.berkeley.boinc.test.R.raw.get_messages_reply);
        assertThat(received.length(), is(equalTo(8896)));
        Vector<Message> messages = null;
        try {
            messages = MessagesParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
        assertNotNull(messages);
        assertThat(messages.size(), is(equalTo(50)));
        assertThat(messages.elementAt(0).project, is(equalTo("WUProp@Home")));
        assertThat(messages.elementAt(0).priority, is(equalTo(1)));
        assertThat(messages.elementAt(0).seqno, is(equalTo(12692)));
        assertThat(messages.elementAt(0).timestamp, is(equalTo(1456395487L)));
        assertThat(messages.elementAt(0).body, is(equalTo("Scheduler request completed")));
        assertThat(messages.elementAt(1).project, is(equalTo("pogs")));
        assertThat(messages.elementAt(1).priority, is(equalTo(2)));
        assertThat(messages.elementAt(1).seqno, is(equalTo(12693)));
        assertThat(messages.elementAt(1).timestamp, is(equalTo(1456397767L)));
        assertThat(messages.elementAt(1).body, is(equalTo("Computation for task 123625.9+343405_area27054047_0 finished")));
        assertThat(messages.elementAt(49).project, is(equalTo("World Community Grid")));
        assertThat(messages.elementAt(49).priority, is(equalTo(3)));
        assertThat(messages.elementAt(49).seqno, is(equalTo(12741)));
        assertThat(messages.elementAt(49).timestamp, is(equalTo(1456410012L)));
        assertThat(messages.elementAt(49).body, is(equalTo("Scheduler request completed")));
    }

    @Test
    public void emptyAnswer() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<msgs>\n" +
                "</msgs>\n" +
                "</boinc_gui_rpc_reply>\n";
        Vector<Message> messages = null;
        try {
            messages = MessagesParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
        assertNotNull(messages);
        assertTrue(messages.isEmpty());
        }

    @Test
    public void elementNotPresent() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<core_client_major_version>7</core_client_major_version>\n" +
                "<core_client_minor_version>4</core_client_minor_version>\n" +
                "<core_client_release>23</core_client_release>\n" +
                "</boinc_gui_rpc_reply>\n";
        Vector<Message> messages = null;
        try {
            messages = MessagesParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
                fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
                fail("InvalidDataReceivedException unexpected");
        }
        assertNotNull(messages);
        assertTrue(messages.isEmpty());
    }

    @Test
    public void unauthorized() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<unauthorized/>\n" +
                "</boinc_gui_rpc_reply>\n";
        Vector<Message> messages = null;
        String errorMsg = "";
        try {
            messages = MessagesParser.parse(received);
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
        assertNull(messages);
    }

    @Test
    public void invalidData() {
        final String received = TestSupport.readResource(edu.berkeley.boinc.test.R.raw.get_messages_reply, 8894);
        Vector<Message> messages = null;
        String errorMsg = "";
        try {
            messages = MessagesParser.parse(received);
            fail("Successful parsing unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (AuthorizationFailedException e) {
            errorMsg = e.getMessage();
            fail("AuthorizationFailedException unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (InvalidDataReceivedException e) {
            errorMsg = e.getMessage();
        }
        assertThat(errorMsg, is(equalTo("Malformed XML while parsing <msgs>")));
        assertNull(messages);
    }
}
