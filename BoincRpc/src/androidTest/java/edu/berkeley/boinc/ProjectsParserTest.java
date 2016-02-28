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
public class ProjectsParserTest {

    @Test
    public void parseNormal() {
        final String received = TestSupport.readResource(edu.berkeley.boinc.test.R.raw.get_project_status_reply);
        assertThat(received.length(), is(equalTo(9792)));
        Vector<Project> projects = null;
        try {
            projects = ProjectsParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
        assertNotNull(projects);
        assertThat(projects.size(), is(equalTo(3)));
        // Project 1
        assertThat(projects.elementAt(0).getName(), is(equalTo("WUProp@Home")));
        assertThat(projects.elementAt(0).master_url, is(equalTo("http://wuprop.boinc-af.org/")));
        assertThat(projects.elementAt(0).resource_share, is(equalTo(0.010000f)));
        assertThat(projects.elementAt(0).project_name, is(equalTo("WUProp@Home")));
        assertThat(projects.elementAt(0).user_name, is(equalTo("Test1")));
        assertThat(projects.elementAt(0).team_name, is(equalTo("TestTeam")));
        assertThat(projects.elementAt(0).hostid, is(equalTo(15982)));
        assertNotNull(projects.elementAt(0).gui_urls);
        assertThat(projects.elementAt(0).gui_urls.size(), is(equalTo(4)));
        assertThat(projects.elementAt(0).gui_urls.elementAt(0).name, is(equalTo("Your account")));
        assertThat(projects.elementAt(0).gui_urls.elementAt(0).description, is(equalTo("View your account information and credit totals")));
        assertThat(projects.elementAt(0).gui_urls.elementAt(0).url, is(equalTo("http://wuprop.boinc-af.org/home.php")));
        assertThat(projects.elementAt(0).gui_urls.elementAt(3).name, is(equalTo("Results")));
        assertThat(projects.elementAt(0).gui_urls.elementAt(3).description, is(equalTo("View data collected by the project")));
        assertThat(projects.elementAt(0).gui_urls.elementAt(3).url, is(equalTo("http://wuprop.boinc-af.org/results.html")));
        assertThat(projects.elementAt(0).user_total_credit, is(equalTo(739160.640681d)));
        assertThat(projects.elementAt(0).user_expavg_credit, is(equalTo(588.897256d)));
        assertThat(projects.elementAt(0).host_total_credit, is(equalTo(94459.589717d)));
        assertThat(projects.elementAt(0).host_expavg_credit, is(equalTo(46.815763d)));
        assertThat(projects.elementAt(0).min_rpc_time, is(equalTo(1456406331.797230d)));
        assertThat(projects.elementAt(0).download_backoff, is(equalTo(0d)));
        assertThat(projects.elementAt(0).upload_backoff, is(equalTo(0d)));
        assertThat(projects.elementAt(0).cpu_short_term_debt, is(equalTo(0d)));
        assertThat(projects.elementAt(0).cpu_long_term_debt, is(equalTo(0d)));
        assertThat(projects.elementAt(0).duration_correction_factor, is(equalTo(1.000000d)));
        assertFalse(projects.elementAt(0).master_url_fetch_pending);
        assertThat(projects.elementAt(0).sched_rpc_pending, is(equalTo(1)));
        assertFalse(projects.elementAt(0).suspended_via_gui);
        assertFalse(projects.elementAt(0).dont_request_more_work);
        assertFalse(projects.elementAt(0).scheduler_rpc_in_progress);
        assertFalse(projects.elementAt(0).trickle_up_pending);
        // Project 2
        assertThat(projects.elementAt(1).getName(), is(equalTo("World Community Grid")));
        assertThat(projects.elementAt(1).master_url, is(equalTo("http://www.worldcommunitygrid.org/")));
        assertThat(projects.elementAt(1).resource_share, is(equalTo(50.000000f)));
        assertThat(projects.elementAt(1).project_name, is(equalTo("World Community Grid")));
        assertThat(projects.elementAt(1).user_name, is(equalTo("Palo M.")));
        assertThat(projects.elementAt(1).team_name, is(equalTo("BOINC.SK")));
        assertThat(projects.elementAt(1).hostid, is(equalTo(1538122)));
        assertNotNull(projects.elementAt(1).gui_urls);
        assertThat(projects.elementAt(1).gui_urls.size(), is(equalTo(7)));
        assertThat(projects.elementAt(1).gui_urls.elementAt(0).name, is(equalTo("Research Overview")));
        assertThat(projects.elementAt(1).gui_urls.elementAt(0).description, is(equalTo("Learn about the projects hosted at World Community Grid")));
        assertThat(projects.elementAt(1).gui_urls.elementAt(0).url, is(equalTo("http://www.worldcommunitygrid.org/research/viewAllProjects.do")));
        assertThat(projects.elementAt(1).user_total_credit, is(equalTo(32667955.277421d)));
        assertThat(projects.elementAt(1).user_expavg_credit, is(equalTo(12688.155962d)));
        assertThat(projects.elementAt(1).host_total_credit, is(equalTo(2136389.130008d)));
        assertThat(projects.elementAt(1).host_expavg_credit, is(equalTo(1344.771599d)));
        assertThat(projects.elementAt(1).min_rpc_time, is(equalTo(1456402096.062290d)));
        // Project 3
        assertThat(projects.elementAt(2).getName(), is(equalTo("pogs")));
        assertThat(projects.elementAt(2).master_url, is(equalTo("http://pogs.theskynet.org/pogs/")));
        assertThat(projects.elementAt(2).resource_share, is(equalTo(10.000000f)));
        assertThat(projects.elementAt(2).project_name, is(equalTo("pogs")));
        assertNotNull(projects.elementAt(2).gui_urls);
        assertThat(projects.elementAt(2).gui_urls.size(), is(equalTo(4)));
    }

    @Test
    public void emptyAnswer() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<projects>\n" +
                "</projects>\n" +
                "</boinc_gui_rpc_reply>\n";
        Vector<Project> projects = null;
        try {
            projects = ProjectsParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
        assertNotNull(projects);
        assertTrue(projects.isEmpty());
    }

    @Test
    public void elementNotPresent() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<core_client_major_version>7</core_client_major_version>\n" +
                "<core_client_minor_version>4</core_client_minor_version>\n" +
                "<core_client_release>23</core_client_release>\n" +
                "</boinc_gui_rpc_reply>\n";
        Vector<Project> projects = null;
        try {
            projects = ProjectsParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
        assertNotNull(projects);
        assertTrue(projects.isEmpty());
    }

    @Test
    public void unauthorized() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<unauthorized/>\n" +
                "</boinc_gui_rpc_reply>\n";
        Vector<Project> projects = null;
        String errorMsg = "";
        try {
            projects = ProjectsParser.parse(received);
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
        assertNull(projects);
    }

    @Test
    public void invalidData() {
        final String received = TestSupport.readResource(edu.berkeley.boinc.test.R.raw.get_project_status_reply, 9790);
        Vector<Project> projects = null;
        String errorMsg = "";
        try {
            projects = ProjectsParser.parse(received);
            fail("Successful parsing unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (AuthorizationFailedException e) {
            errorMsg = e.getMessage();
            fail("AuthorizationFailedException unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (InvalidDataReceivedException e) {
            errorMsg = e.getMessage();
        }
        assertThat(errorMsg, is(equalTo("Malformed XML while parsing <project>")));
        assertNull(projects);
    }
}
