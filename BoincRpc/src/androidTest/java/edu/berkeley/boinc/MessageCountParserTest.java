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
public class MessageCountParserTest {

    @Test
    public void parseNormal() {
        String received =
                "<boinc_gui_rpc_reply>\n" +
                "<seqno>12741</seqno>\n" +
                "</boinc_gui_rpc_reply>\n";
        int seqNo;
        try {
            seqNo = MessageCountParser.getSeqno(received);
        }
        catch (AuthorizationFailedException e) {
            seqNo = -1;
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            seqNo = -2;
            fail("InvalidDataReceivedException unexpected");
        }
        assertThat(seqNo, is(equalTo(12741)));
    }

    @Test
    public void unauthorized() {
        String received =
                "<boinc_gui_rpc_reply>\n" +
                "<unauthorized/>\n" +
                "</boinc_gui_rpc_reply>\n";
        int seqNo;
        try {
            seqNo = MessageCountParser.getSeqno(received);
            fail("Successful parsing unexpected, AuthorizationFailedException should be thrown instead");
        }
        catch (AuthorizationFailedException e) {
            seqNo = -1;
        }
        catch (InvalidDataReceivedException e) {
            seqNo = -2;
            fail("InvalidDataReceivedException unexpected, AuthorizationFailedException should be thrown instead");
        }
        assertThat(seqNo, is(equalTo(-1)));
    }

    @Test
    public void invalidData() {
        String received =
                "<boinc_gui_rpc_reply>\n" +
                "<seqno>12741</seqno>\n" +
                "</boinc_gui_rpc_reply";
        int seqNo;
        try {
            seqNo = MessageCountParser.getSeqno(received);
            fail("Successful parsing unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (AuthorizationFailedException e) {
            seqNo = -1;
            fail("AuthorizationFailedException unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (InvalidDataReceivedException e) {
            seqNo = -2;
        }
        assertThat(seqNo, is(equalTo(-2)));
    }
}
