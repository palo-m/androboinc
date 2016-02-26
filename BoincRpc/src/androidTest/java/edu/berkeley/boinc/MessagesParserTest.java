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
import java.util.Vector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


@RunWith(AndroidJUnit4.class)
@SmallTest
public class MessagesParserTest {

    @Test
    public void parseNormal() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<msgs>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12692</seqno>\n" +
                " <body>\n" +
                "Scheduler request completed\n" +
                "</body>\n" +
                " <time>1456395487</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>2</pri>\n" +
                " <seqno>12693</seqno>\n" +
                " <body>\n" +
                "Computation for task 123625.9+343405_area27054047_0 finished\n" +
                "</body>\n" +
                " <time>1456397767</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12694</seqno>\n" +
                " <body>\n" +
                "Starting task 123625.9+343405_area27054014_0\n" +
                "</body>\n" +
                " <time>1456397767</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12695</seqno>\n" +
                " <body>\n" +
                "Started upload of 123625.9+343405_area27054047_0_0\n" +
                "</body>\n" +
                " <time>1456397769</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12696</seqno>\n" +
                " <body>\n" +
                "Finished upload of 123625.9+343405_area27054047_0_0\n" +
                "</body>\n" +
                " <time>1456397788</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12697</seqno>\n" +
                " <body>\n" +
                "Sending scheduler request: To report completed tasks.\n" +
                "</body>\n" +
                " <time>1456397791</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12698</seqno>\n" +
                " <body>\n" +
                "Reporting 1 completed tasks\n" +
                "</body>\n" +
                " <time>1456397791</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12699</seqno>\n" +
                " <body>\n" +
                "Not requesting tasks: don't need (not highest priority project)\n" +
                "</body>\n" +
                " <time>1456397791</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12700</seqno>\n" +
                " <body>\n" +
                "Scheduler request completed\n" +
                "</body>\n" +
                " <time>1456397795</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12701</seqno>\n" +
                " <body>\n" +
                "Sending scheduler request: Requested by project.\n" +
                "</body>\n" +
                " <time>1456399090</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12702</seqno>\n" +
                " <body>\n" +
                "Not requesting tasks: non CPU intensive\n" +
                "</body>\n" +
                " <time>1456399090</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12703</seqno>\n" +
                " <body>\n" +
                "Scheduler request completed\n" +
                "</body>\n" +
                " <time>1456399095</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12704</seqno>\n" +
                " <body>\n" +
                "Started upload of FAH2_000060_avx17260_000022_0001_023_0_r856542112_3\n" +
                "</body>\n" +
                " <time>1456401646</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12705</seqno>\n" +
                " <body>\n" +
                "Started upload of FAH2_000060_avx17260_000022_0001_023_0_r856542112_13\n" +
                "</body>\n" +
                " <time>1456401646</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12706</seqno>\n" +
                " <body>\n" +
                "Sending scheduler request: To send trickle-up message.\n" +
                "</body>\n" +
                " <time>1456401646</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12707</seqno>\n" +
                " <body>\n" +
                "Not requesting tasks: don't need (not highest priority project)\n" +
                "</body>\n" +
                " <time>1456401646</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12708</seqno>\n" +
                " <body>\n" +
                "Finished upload of FAH2_000060_avx17260_000022_0001_023_0_r856542112_13\n" +
                "</body>\n" +
                " <time>1456401651</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12709</seqno>\n" +
                " <body>\n" +
                "Scheduler request completed\n" +
                "</body>\n" +
                " <time>1456401662</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12710</seqno>\n" +
                " <body>\n" +
                "Finished upload of FAH2_000060_avx17260_000022_0001_023_0_r856542112_3\n" +
                "</body>\n" +
                " <time>1456401665</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12711</seqno>\n" +
                " <body>\n" +
                "Started upload of FAH2_avx38708-ls_000089_0013_034_wcgfahb00040000_1_r1607865037_6\n" +
                "</body>\n" +
                " <time>1456401967</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12712</seqno>\n" +
                " <body>\n" +
                "Started upload of FAH2_avx38708-ls_000089_0013_034_wcgfahb00040000_1_r1607865037_16\n" +
                "</body>\n" +
                " <time>1456401967</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12713</seqno>\n" +
                " <body>\n" +
                "Sending scheduler request: To send trickle-up message.\n" +
                "</body>\n" +
                " <time>1456401969</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12714</seqno>\n" +
                " <body>\n" +
                "Not requesting tasks: don't need (not highest priority project)\n" +
                "</body>\n" +
                " <time>1456401969</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12715</seqno>\n" +
                " <body>\n" +
                "Finished upload of FAH2_avx38708-ls_000089_0013_034_wcgfahb00040000_1_r1607865037_6\n" +
                "</body>\n" +
                " <time>1456401973</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12716</seqno>\n" +
                " <body>\n" +
                "Scheduler request completed\n" +
                "</body>\n" +
                " <time>1456401974</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12717</seqno>\n" +
                " <body>\n" +
                "Finished upload of FAH2_avx38708-ls_000089_0013_034_wcgfahb00040000_1_r1607865037_16\n" +
                "</body>\n" +
                " <time>1456401977</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12718</seqno>\n" +
                " <body>\n" +
                "Sending scheduler request: Requested by project.\n" +
                "</body>\n" +
                " <time>1456402700</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12719</seqno>\n" +
                " <body>\n" +
                "Not requesting tasks: non CPU intensive\n" +
                "</body>\n" +
                " <time>1456402700</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12720</seqno>\n" +
                " <body>\n" +
                "Scheduler request completed\n" +
                "</body>\n" +
                " <time>1456402706</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12721</seqno>\n" +
                " <body>\n" +
                "Computation for task 123625.9+343405_area27053933_0 finished\n" +
                "</body>\n" +
                " <time>1456403621</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12722</seqno>\n" +
                " <body>\n" +
                "Starting task 123625.9+343405_area27054059_1\n" +
                "</body>\n" +
                " <time>1456403621</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12723</seqno>\n" +
                " <body>\n" +
                "Started upload of 123625.9+343405_area27053933_0_0\n" +
                "</body>\n" +
                " <time>1456403623</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12724</seqno>\n" +
                " <body>\n" +
                "Finished upload of 123625.9+343405_area27053933_0_0\n" +
                "</body>\n" +
                " <time>1456403639</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12725</seqno>\n" +
                " <body>\n" +
                "Sending scheduler request: To report completed tasks.\n" +
                "</body>\n" +
                " <time>1456403641</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12726</seqno>\n" +
                " <body>\n" +
                "Reporting 1 completed tasks\n" +
                "</body>\n" +
                " <time>1456403641</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12727</seqno>\n" +
                " <body>\n" +
                "Not requesting tasks: don't need (not highest priority project)\n" +
                "</body>\n" +
                " <time>1456403641</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12728</seqno>\n" +
                " <body>\n" +
                "Scheduler request completed\n" +
                "</body>\n" +
                " <time>1456403650</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12729</seqno>\n" +
                " <body>\n" +
                "Sending scheduler request: Requested by project.\n" +
                "</body>\n" +
                " <time>1456406311</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12730</seqno>\n" +
                " <body>\n" +
                "Not requesting tasks: non CPU intensive\n" +
                "</body>\n" +
                " <time>1456406311</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12731</seqno>\n" +
                " <body>\n" +
                "Scheduler request completed\n" +
                "</body>\n" +
                " <time>1456406324</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12732</seqno>\n" +
                " <body>\n" +
                "Sending scheduler request: Requested by project.\n" +
                "</body>\n" +
                " <time>1456409928</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12733</seqno>\n" +
                " <body>\n" +
                "Not requesting tasks: non CPU intensive\n" +
                "</body>\n" +
                " <time>1456409928</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12734</seqno>\n" +
                " <body>\n" +
                "Scheduler request completed\n" +
                "</body>\n" +
                " <time>1456409932</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12735</seqno>\n" +
                " <body>\n" +
                "Started upload of FAH2_avx38708-ls_000089_0013_034_wcgfahb00040000_1_r1607865037_7\n" +
                "</body>\n" +
                " <time>1456409996</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12736</seqno>\n" +
                " <body>\n" +
                "Started upload of FAH2_avx38708-ls_000089_0013_034_wcgfahb00040000_1_r1607865037_17\n" +
                "</body>\n" +
                " <time>1456409996</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12737</seqno>\n" +
                " <body>\n" +
                "Sending scheduler request: To send trickle-up message.\n" +
                "</body>\n" +
                " <time>1456409998</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12738</seqno>\n" +
                " <body>\n" +
                "Not requesting tasks: don't need (not highest priority project)\n" +
                "</body>\n" +
                " <time>1456409998</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12739</seqno>\n" +
                " <body>\n" +
                "Finished upload of FAH2_avx38708-ls_000089_0013_034_wcgfahb00040000_1_r1607865037_17\n" +
                "</body>\n" +
                " <time>1456410007</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12740</seqno>\n" +
                " <body>\n" +
                "Finished upload of FAH2_avx38708-ls_000089_0013_034_wcgfahb00040000_1_r1607865037_7\n" +
                "</body>\n" +
                " <time>1456410008</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>3</pri>\n" +
                " <seqno>12741</seqno>\n" +
                " <body>\n" +
                "Scheduler request completed\n" +
                "</body>\n" +
                " <time>1456410012</time>\n" +
                "</msg>\n" +
                "</msgs>\n" +
                "</boinc_gui_rpc_reply>\n";
        Vector<Message> messages = new Vector<Message>();
        try {
            messages = MessagesParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
        assertThat(messages.size(), is(equalTo(50)));
        Message message1 = messages.get(0);
        assertThat(message1.project, is(equalTo("WUProp@Home")));
        assertThat(message1.priority, is(equalTo(1)));
        assertThat(message1.seqno, is(equalTo(12692)));
        assertThat(message1.timestamp, is(equalTo(1456395487L)));
        assertThat(message1.body, is(equalTo("Scheduler request completed")));
        Message message2 = messages.get(1);
        assertThat(message2.project, is(equalTo("pogs")));
        assertThat(message2.priority, is(equalTo(2)));
        assertThat(message2.seqno, is(equalTo(12693)));
        assertThat(message2.timestamp, is(equalTo(1456397767L)));
        assertThat(message2.body, is(equalTo("Computation for task 123625.9+343405_area27054047_0 finished")));
        Message message50 = messages.get(49);
        assertThat(message50.project, is(equalTo("World Community Grid")));
        assertThat(message50.priority, is(equalTo(3)));
        assertThat(message50.seqno, is(equalTo(12741)));
        assertThat(message50.timestamp, is(equalTo(1456410012L)));
        assertThat(message50.body, is(equalTo("Scheduler request completed")));
    }

    @Test
    public void unauthorized() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<unauthorized/>\n" +
                "</boinc_gui_rpc_reply>\n";
        Vector<Message> messages = new Vector<Message>();
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
        assertThat(messages.size(), is(equalTo(0)));
        assertThat(errorMsg, is(equalTo("Authorization Failed")));
    }

    @Test
    public void invalidData() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<msgs>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12692</seqno>\n" +
                " <body>\n" +
                "Scheduler request completed\n" +
                "</body>\n" +
                " <time>1456395487</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>2</pri>\n" +
                " <seqno>12693</seqno>\n" +
                " <body>\n" +
                "Computation for task 123625.9+343405_area27054047_0 finished\n" +
                "</body>\n" +
                " <time>1456397767</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12694</seqno>\n" +
                " <body>\n" +
                "Starting task 123625.9+343405_area27054014_0\n" +
                "</body>\n" +
                " <time>1456397767</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12695</seqno>\n" +
                " <body>\n" +
                "Started upload of 123625.9+343405_area27054047_0_0\n" +
                "</body>\n" +
                " <time>1456397769</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12696</seqno>\n" +
                " <body>\n" +
                "Finished upload of 123625.9+343405_area27054047_0_0\n" +
                "</body>\n" +
                " <time>1456397788</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12697</seqno>\n" +
                " <body>\n" +
                "Sending scheduler request: To report completed tasks.\n" +
                "</body>\n" +
                " <time>1456397791</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12698</seqno>\n" +
                " <body>\n" +
                "Reporting 1 completed tasks\n" +
                "</body>\n" +
                " <time>1456397791</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12699</seqno>\n" +
                " <body>\n" +
                "Not requesting tasks: don't need (not highest priority project)\n" +
                "</body>\n" +
                " <time>1456397791</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12700</seqno>\n" +
                " <body>\n" +
                "Scheduler request completed\n" +
                "</body>\n" +
                " <time>1456397795</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12701</seqno>\n" +
                " <body>\n" +
                "Sending scheduler request: Requested by project.\n" +
                "</body>\n" +
                " <time>1456399090</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12702</seqno>\n" +
                " <body>\n" +
                "Not requesting tasks: non CPU intensive\n" +
                "</body>\n" +
                " <time>1456399090</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12703</seqno>\n" +
                " <body>\n" +
                "Scheduler request completed\n" +
                "</body>\n" +
                " <time>1456399095</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12704</seqno>\n" +
                " <body>\n" +
                "Started upload of FAH2_000060_avx17260_000022_0001_023_0_r856542112_3\n" +
                "</body>\n" +
                " <time>1456401646</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12705</seqno>\n" +
                " <body>\n" +
                "Started upload of FAH2_000060_avx17260_000022_0001_023_0_r856542112_13\n" +
                "</body>\n" +
                " <time>1456401646</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12706</seqno>\n" +
                " <body>\n" +
                "Sending scheduler request: To send trickle-up message.\n" +
                "</body>\n" +
                " <time>1456401646</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12707</seqno>\n" +
                " <body>\n" +
                "Not requesting tasks: don't need (not highest priority project)\n" +
                "</body>\n" +
                " <time>1456401646</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12708</seqno>\n" +
                " <body>\n" +
                "Finished upload of FAH2_000060_avx17260_000022_0001_023_0_r856542112_13\n" +
                "</body>\n" +
                " <time>1456401651</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12709</seqno>\n" +
                " <body>\n" +
                "Scheduler request completed\n" +
                "</body>\n" +
                " <time>1456401662</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12710</seqno>\n" +
                " <body>\n" +
                "Finished upload of FAH2_000060_avx17260_000022_0001_023_0_r856542112_3\n" +
                "</body>\n" +
                " <time>1456401665</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12711</seqno>\n" +
                " <body>\n" +
                "Started upload of FAH2_avx38708-ls_000089_0013_034_wcgfahb00040000_1_r1607865037_6\n" +
                "</body>\n" +
                " <time>1456401967</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12712</seqno>\n" +
                " <body>\n" +
                "Started upload of FAH2_avx38708-ls_000089_0013_034_wcgfahb00040000_1_r1607865037_16\n" +
                "</body>\n" +
                " <time>1456401967</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12713</seqno>\n" +
                " <body>\n" +
                "Sending scheduler request: To send trickle-up message.\n" +
                "</body>\n" +
                " <time>1456401969</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12714</seqno>\n" +
                " <body>\n" +
                "Not requesting tasks: don't need (not highest priority project)\n" +
                "</body>\n" +
                " <time>1456401969</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12715</seqno>\n" +
                " <body>\n" +
                "Finished upload of FAH2_avx38708-ls_000089_0013_034_wcgfahb00040000_1_r1607865037_6\n" +
                "</body>\n" +
                " <time>1456401973</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12716</seqno>\n" +
                " <body>\n" +
                "Scheduler request completed\n" +
                "</body>\n" +
                " <time>1456401974</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12717</seqno>\n" +
                " <body>\n" +
                "Finished upload of FAH2_avx38708-ls_000089_0013_034_wcgfahb00040000_1_r1607865037_16\n" +
                "</body>\n" +
                " <time>1456401977</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12718</seqno>\n" +
                " <body>\n" +
                "Sending scheduler request: Requested by project.\n" +
                "</body>\n" +
                " <time>1456402700</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12719</seqno>\n" +
                " <body>\n" +
                "Not requesting tasks: non CPU intensive\n" +
                "</body>\n" +
                " <time>1456402700</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12720</seqno>\n" +
                " <body>\n" +
                "Scheduler request completed\n" +
                "</body>\n" +
                " <time>1456402706</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12721</seqno>\n" +
                " <body>\n" +
                "Computation for task 123625.9+343405_area27053933_0 finished\n" +
                "</body>\n" +
                " <time>1456403621</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12722</seqno>\n" +
                " <body>\n" +
                "Starting task 123625.9+343405_area27054059_1\n" +
                "</body>\n" +
                " <time>1456403621</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12723</seqno>\n" +
                " <body>\n" +
                "Started upload of 123625.9+343405_area27053933_0_0\n" +
                "</body>\n" +
                " <time>1456403623</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12724</seqno>\n" +
                " <body>\n" +
                "Finished upload of 123625.9+343405_area27053933_0_0\n" +
                "</body>\n" +
                " <time>1456403639</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12725</seqno>\n" +
                " <body>\n" +
                "Sending scheduler request: To report completed tasks.\n" +
                "</body>\n" +
                " <time>1456403641</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12726</seqno>\n" +
                " <body>\n" +
                "Reporting 1 completed tasks\n" +
                "</body>\n" +
                " <time>1456403641</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12727</seqno>\n" +
                " <body>\n" +
                "Not requesting tasks: don't need (not highest priority project)\n" +
                "</body>\n" +
                " <time>1456403641</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>pogs</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12728</seqno>\n" +
                " <body>\n" +
                "Scheduler request completed\n" +
                "</body>\n" +
                " <time>1456403650</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12729</seqno>\n" +
                " <body>\n" +
                "Sending scheduler request: Requested by project.\n" +
                "</body>\n" +
                " <time>1456406311</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12730</seqno>\n" +
                " <body>\n" +
                "Not requesting tasks: non CPU intensive\n" +
                "</body>\n" +
                " <time>1456406311</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12731</seqno>\n" +
                " <body>\n" +
                "Scheduler request completed\n" +
                "</body>\n" +
                " <time>1456406324</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12732</seqno>\n" +
                " <body>\n" +
                "Sending scheduler request: Requested by project.\n" +
                "</body>\n" +
                " <time>1456409928</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12733</seqno>\n" +
                " <body>\n" +
                "Not requesting tasks: non CPU intensive\n" +
                "</body>\n" +
                " <time>1456409928</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>WUProp@Home</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12734</seqno>\n" +
                " <body>\n" +
                "Scheduler request completed\n" +
                "</body>\n" +
                " <time>1456409932</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12735</seqno>\n" +
                " <body>\n" +
                "Started upload of FAH2_avx38708-ls_000089_0013_034_wcgfahb00040000_1_r1607865037_7\n" +
                "</body>\n" +
                " <time>1456409996</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12736</seqno>\n" +
                " <body>\n" +
                "Started upload of FAH2_avx38708-ls_000089_0013_034_wcgfahb00040000_1_r1607865037_17\n" +
                "</body>\n" +
                " <time>1456409996</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12737</seqno>\n" +
                " <body>\n" +
                "Sending scheduler request: To send trickle-up message.\n" +
                "</body>\n" +
                " <time>1456409998</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12738</seqno>\n" +
                " <body>\n" +
                "Not requesting tasks: don't need (not highest priority project)\n" +
                "</body>\n" +
                " <time>1456409998</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12739</seqno>\n" +
                " <body>\n" +
                "Finished upload of FAH2_avx38708-ls_000089_0013_034_wcgfahb00040000_1_r1607865037_17\n" +
                "</body>\n" +
                " <time>1456410007</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>1</pri>\n" +
                " <seqno>12740</seqno>\n" +
                " <body>\n" +
                "Finished upload of FAH2_avx38708-ls_000089_0013_034_wcgfahb00040000_1_r1607865037_7\n" +
                "</body>\n" +
                " <time>1456410008</time>\n" +
                "</msg>\n" +
                "<msg>\n" +
                " <project>World Community Grid</project>\n" +
                " <pri>3</pri>\n" +
                " <seqno>12741</seqno>\n" +
                " <body>\n" +
                "Scheduler request completed\n" +
                "</body>\n" +
                " <time>1456410012</time>\n" +
                "</msg>\n" +
                "</msgs>\n" +
                "</boinc_gui_rpc_reply";
        Vector<Message> messages = new Vector<Message>();
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
        assertThat(messages.size(), is(equalTo(0)));
        assertThat(errorMsg, is(equalTo("Malformed XML while parsing <msgs>")));
    }

    @Test
    public void elementNotPresent() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<core_client_major_version>7</core_client_major_version>\n" +
                "<core_client_minor_version>4</core_client_minor_version>\n" +
                "<core_client_release>23</core_client_release>\n" +
                "</boinc_gui_rpc_reply>\n";
        Vector<Message> messages = new Vector<Message>();
        messages.add(new Message());
        assertThat(messages.size(), is(equalTo(1)));
        try {
            messages = MessagesParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
        assertThat(messages.size(), is(equalTo(0)));
    }
}
