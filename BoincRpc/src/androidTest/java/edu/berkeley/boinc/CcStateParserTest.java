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
import android.test.suitebuilder.annotation.MediumTest;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.berkeley.boinc.testutil.TestSupport;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;


@RunWith(AndroidJUnit4.class)
@MediumTest
public class CcStateParserTest {

    @Test
    public void parseNormal() {
        final String received = TestSupport.readResource(edu.berkeley.boinc.test.R.raw.get_state_reply);
        assertThat(received.length(), is(equalTo(98963)));
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
        assertNotNull(ccState.version_info);
        assertThat(ccState.version_info.major, is(equalTo(7)));
        assertThat(ccState.version_info.minor, is(equalTo(4)));
        assertThat(ccState.version_info.release, is(equalTo(23)));
        assertNotNull(ccState.host_info);
        assertThat(ccState.host_info.domain_name, is(equalTo("machine3")));
        assertNotNull(ccState.projects);
        assertThat(ccState.projects.size(), is(equalTo(3)));
        assertThat(ccState.projects.elementAt(0).getName(), is(equalTo("WUProp@Home")));
        assertNotNull(ccState.projects.elementAt(0).gui_urls);
        assertThat(ccState.projects.elementAt(0).gui_urls.size(), is(equalTo(4)));
        assertThat(ccState.projects.elementAt(1).getName(), is(equalTo("World Community Grid")));
        assertNotNull(ccState.projects.elementAt(1).gui_urls);
        assertThat(ccState.projects.elementAt(1).gui_urls.size(), is(equalTo(7)));
        assertThat(ccState.projects.elementAt(2).getName(), is(equalTo("pogs")));
        assertNotNull(ccState.projects.elementAt(2).gui_urls);
        assertThat(ccState.projects.elementAt(2).gui_urls.size(), is(equalTo(4)));
        assertNotNull(ccState.apps);
        assertThat(ccState.apps.size(), is(equalTo(32)));
        assertThat(ccState.apps.elementAt(0).name, is(equalTo("data_collect")));
        assertThat(ccState.apps.elementAt(0).user_friendly_name, is(equalTo("Data collect")));
        assertThat(ccState.apps.elementAt(0).getName(), is(equalTo("Data collect")));
        assertThat(ccState.apps.elementAt(3).name, is(equalTo("data_collect_v4")));
        assertThat(ccState.apps.elementAt(3).user_friendly_name, is(equalTo("Data collect version 4")));
        assertThat(ccState.apps.elementAt(3).getName(), is(equalTo("Data collect version 4")));
        assertThat(ccState.apps.elementAt(4).name, is(equalTo("hcc1")));
        assertThat(ccState.apps.elementAt(4).user_friendly_name, is(equalTo("Help Conquer Cancer")));
        assertThat(ccState.apps.elementAt(4).getName(), is(equalTo("Help Conquer Cancer")));
        assertThat(ccState.apps.elementAt(30).name, is(equalTo("fahb")));
        assertThat(ccState.apps.elementAt(30).user_friendly_name, is(equalTo("FightAIDS@Home - Phase 2")));
        assertThat(ccState.apps.elementAt(30).getName(), is(equalTo("FightAIDS@Home - Phase 2")));
        assertThat(ccState.apps.elementAt(31).name, is(equalTo("magphys_wrapper")));
        assertThat(ccState.apps.elementAt(31).user_friendly_name, is(equalTo("fitsedwrapper")));
        assertThat(ccState.apps.elementAt(31).getName(), is(equalTo("fitsedwrapper")));
        assertNotNull(ccState.workunits);
        assertThat(ccState.workunits.size(), is(equalTo(5)));
        assertThat(ccState.workunits.elementAt(0).name, is(equalTo("data_collect_v4_1454745134_472003")));
        assertThat(ccState.workunits.elementAt(0).app_name, is(equalTo("data_collect_v4")));
        assertThat(ccState.workunits.elementAt(0).version_num, is(equalTo(419)));
        assertThat(ccState.workunits.elementAt(1).name, is(equalTo("FAH2_avx38708-ls_000089_0013_034_wcgfahb00040000")));
        assertThat(ccState.workunits.elementAt(1).app_name, is(equalTo("fahb")));
        assertThat(ccState.workunits.elementAt(1).version_num, is(equalTo(715)));
        assertThat(ccState.workunits.elementAt(4).name, is(equalTo("123625.9+343405_area27054065")));
        assertThat(ccState.workunits.elementAt(4).app_name, is(equalTo("magphys_wrapper")));
        assertThat(ccState.workunits.elementAt(4).version_num, is(equalTo(402)));
        assertNotNull(ccState.results);
        assertThat(ccState.results.size(), is(equalTo(5)));
        assertThat(ccState.results.elementAt(0).name, is(equalTo("data_collect_v4_1454745134_472003_0")));
        assertThat(ccState.results.elementAt(0).wu_name, is(equalTo("data_collect_v4_1454745134_472003")));
        assertThat(ccState.results.elementAt(0).project_url, is(equalTo("http://wuprop.boinc-af.org/")));
        assertThat(ccState.results.elementAt(0).state, is(equalTo(2)));
        assertFalse(ccState.results.elementAt(0).suspended_via_gui);
        assertFalse(ccState.results.elementAt(0).project_suspended_via_gui);
        assertTrue(ccState.results.elementAt(0).active_task);
        assertThat(ccState.results.elementAt(0).active_task_state, is(equalTo(1)));
        assertThat(ccState.results.elementAt(0).fraction_done, is(equalTo(0.814404f)));
        assertThat(ccState.results.elementAt(1).name, is(equalTo("FAH2_avx38708-ls_000089_0013_034_wcgfahb00040000_1")));
        assertThat(ccState.results.elementAt(1).wu_name, is(equalTo("FAH2_avx38708-ls_000089_0013_034_wcgfahb00040000")));
        assertThat(ccState.results.elementAt(1).project_url, is(equalTo("http://www.worldcommunitygrid.org/")));
        assertThat(ccState.results.elementAt(1).state, is(equalTo(2)));
        assertFalse(ccState.results.elementAt(1).suspended_via_gui);
        assertFalse(ccState.results.elementAt(1).project_suspended_via_gui);
        assertTrue(ccState.results.elementAt(1).active_task);
        assertThat(ccState.results.elementAt(1).active_task_state, is(equalTo(1)));
        assertThat(ccState.results.elementAt(1).fraction_done, is(equalTo(0.693690f)));
        assertThat(ccState.results.elementAt(4).name, is(equalTo("123625.9+343405_area27054071_1")));
        assertThat(ccState.results.elementAt(4).wu_name, is(equalTo("123625.9+343405_area27054071")));
        assertThat(ccState.results.elementAt(4).project_url, is(equalTo("http://pogs.theskynet.org/pogs/")));
        assertThat(ccState.results.elementAt(4).state, is(equalTo(2)));
        assertFalse(ccState.results.elementAt(4).suspended_via_gui);
        assertFalse(ccState.results.elementAt(4).project_suspended_via_gui);
        assertFalse(ccState.results.elementAt(4).active_task);
        assertThat(ccState.results.elementAt(4).active_task_state, is(equalTo(0)));
        assertThat(ccState.results.elementAt(4).fraction_done, is(equalTo(0.0f)));
    }

    @Test
    public void emptyAnswer() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<client_state>\n" +
                "</client_state>\n" +
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
        assertNotNull(ccState.version_info);
        assertThat(ccState.version_info.major, is(equalTo(0)));
        assertThat(ccState.version_info.minor, is(equalTo(0)));
        assertThat(ccState.version_info.release, is(equalTo(0)));
        assertNull(ccState.host_info);
        assertNotNull(ccState.projects);
        assertTrue(ccState.projects.isEmpty());
        assertNotNull(ccState.apps);
        assertTrue(ccState.apps.isEmpty());
        assertNotNull(ccState.workunits);
        assertTrue(ccState.workunits.isEmpty());
        assertNotNull(ccState.results);
        assertTrue(ccState.results.isEmpty());
    }

    @Test
    public void elementNotPresent() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<debug_unsupported>1</debug_unsupported>\n" +
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
        assertNotNull(ccState.version_info);
        assertThat(ccState.version_info.major, is(equalTo(0)));
        assertThat(ccState.version_info.minor, is(equalTo(0)));
        assertThat(ccState.version_info.release, is(equalTo(0)));
        assertNull(ccState.host_info);
        assertNotNull(ccState.projects);
        assertTrue(ccState.projects.isEmpty());
        assertNotNull(ccState.apps);
        assertTrue(ccState.apps.isEmpty());
        assertNotNull(ccState.workunits);
        assertTrue(ccState.workunits.isEmpty());
        assertNotNull(ccState.results);
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
        final String received = TestSupport.readResource(edu.berkeley.boinc.test.R.raw.get_state_reply, 98961);
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
