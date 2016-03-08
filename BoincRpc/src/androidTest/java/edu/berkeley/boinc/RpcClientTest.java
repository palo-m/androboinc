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
import android.test.suitebuilder.annotation.LargeTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.berkeley.boinc.testutil.BoincClientStub;
import java.util.Vector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class RpcClientTest {
    private BoincClientStub boincClient;

    @Before
    public void startup() {
        boincClient = new BoincClientStub();
        boolean listening = boincClient.startListener();
        assertTrue(listening);
    }

    @Test
    public void connectDisconnect() {
        RpcClient rpcClient = new RpcClient();
        try {
            rpcClient.open("127.0.0.1", 31416);
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
    }

    @Test
    public void connectionFailed() {
        String errorMsg = "";
        RpcClient rpcClient = new RpcClient();
        try {
            rpcClient.open("127.0.0.1", 31417);
            fail("Successful connection unexpected, connection should fail instead");
        }
        catch (ConnectionFailedException e) {
            errorMsg = e.getMessage();
        }
        assertFalse(rpcClient.isConnected());
        assertThat(errorMsg, is(equalTo("Connect failed")));
    }

    @Test
    public void connectionClosed() {
        RpcClient rpcClient = new RpcClient();
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        boolean stopped = boincClient.stopListener();
        assertTrue(stopped);
        boincClient = null;
        assertTrue(rpcClient.isConnected());
        assertFalse(rpcClient.connectionAlive());
        rpcClient.close();
    }

    @Test
    public void exchangeVersions() {
        RpcClient rpcClient = new RpcClient();
        VersionInfo vi = null;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            vi = rpcClient.exchangeVersions();
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertNotNull(vi);
        assertThat(vi.major, is(equalTo(7)));
        assertThat(vi.minor, is(equalTo(4)));
        assertThat(vi.release, is(equalTo(23)));
    }

    @Test
    public void exchangeVersionsAuthorization() {
        boincClient.setPassword("123456");
        RpcClient rpcClient = new RpcClient();
        VersionInfo vi = null;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            rpcClient.authorize("123456");
            vi = rpcClient.exchangeVersions();
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException " + e.getMessage());
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertNotNull(vi);
        assertThat(vi.major, is(equalTo(7)));
        assertThat(vi.minor, is(equalTo(4)));
        assertThat(vi.release, is(equalTo(23)));
    }

    @Test
    public void authorizationFailure() {
        String errorMsg = "";
        boincClient.setPassword("123456");
        RpcClient rpcClient = new RpcClient();
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            rpcClient.authorize("123457");
            fail("Successful authorization unexpected, authorization should fail because of wrong password");
        }
        catch (AuthorizationFailedException e) {
            errorMsg = e.getMessage();
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertThat(errorMsg, is(equalTo("Authorization Failed")));
        rpcClient.close();
    }

    @Test
    public void exchangeVersionsUnauthorized() {
        String errorMsg = "";
        boincClient.setPassword("123456");
        RpcClient rpcClient = new RpcClient();
        VersionInfo vi = null;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            vi = rpcClient.exchangeVersions();
            fail("Successful version info retrieval unexpected, authorization should fail instead");
        }
        catch (AuthorizationFailedException e) {
            errorMsg = e.getMessage();
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertNull(vi);
        assertThat(errorMsg, is(equalTo("Authorization Failed")));
        rpcClient.close();
    }

    @Test
    public void exchangeVersionsNoReply() {
        String errorMsg = "";
        RpcClient rpcClient = new RpcClient();
        VersionInfo vi = null;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            boincClient.setBehavior(BoincClientStub.Behavior.NO_REPLY);
            vi = rpcClient.exchangeVersions();
            fail("Successful version info retrieval unexpected, timeout should happen instead");
        }
        catch (ConnectionFailedException e) {
            errorMsg = e.getMessage();
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertNull(vi);
        assertThat(errorMsg, is(equalTo("Connection failed in exchangeVersions()")));
        rpcClient.close();
    }

    @Test
    public void getCcStatus() {
        RpcClient rpcClient = new RpcClient();
        CcStatus ccStatus = null;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            ccStatus = rpcClient.getCcStatus();
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
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
    public void getCcStatusTruncated() {
        String errorMsg = "";
        RpcClient rpcClient = new RpcClient();
        CcStatus ccStatus = null;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            boincClient.setBehavior(BoincClientStub.Behavior.TRUNCATED_DATA);
            ccStatus = rpcClient.getCcStatus();
            fail("Successful cc_status retrieval unexpected, truncated data");
        }
        catch (ConnectionFailedException e) {
            errorMsg = e.getMessage();
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertNull(ccStatus);
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertThat(errorMsg, is(equalTo("Connection failed in getCcStatus()")));
    }

    @Test
    public void getFileTransfers() {
        RpcClient rpcClient = new RpcClient();
        Vector<Transfer> transfers = null;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            transfers = rpcClient.getFileTransfers();
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
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
    public void getHostInfo() {
        RpcClient rpcClient = new RpcClient();
        HostInfo hostInfo = null;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            hostInfo = rpcClient.getHostInfo();
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertNotNull(hostInfo);
        assertThat(hostInfo.timezone, is(equalTo(28800)));
        assertThat(hostInfo.domain_name, is(equalTo("machine3")));
        assertThat(hostInfo.ip_addr, is(equalTo("192.168.0.3")));
        assertThat(hostInfo.host_cpid, is(equalTo("a8c15a2da71502bb52c8d4a5025e70b6")));
        assertThat(hostInfo.p_ncpus, is(equalTo(4)));
        assertThat(hostInfo.p_vendor, is(equalTo("GenuineIntel")));
        assertThat(hostInfo.p_model, is(equalTo("Intel(R) Core(TM)2 Quad CPU    Q6600  @ 2.40GHz [Family 6 Model 15 Stepping 11]")));
        assertThat(hostInfo.p_features, is(equalTo("fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush dts acpi mmx fxsr sse sse2 ss ht tm pbe syscall nx lm constant_tsc arch_perfmon pebs bts rep_good nopl aperfmperf pni dtes64 monitor ds_cpl vmx est tm2 ssse3 cx16 xtpr pdcm lahf_lm dtherm tpr_shadow vnmi flexpriority")));
        assertThat(hostInfo.p_fpops, is(equalTo(2227719169.069331d)));
        assertThat(hostInfo.p_iops, is(equalTo(12828645195.720629d)));
        assertThat(hostInfo.p_membw, is(equalTo(1000000000.000000d)));
        assertThat(hostInfo.p_calculated, is(equalTo(1456151145L)));
        assertThat(hostInfo.m_nbytes, is(equalTo(4150296576.000000d)));
        assertThat(hostInfo.m_cache, is(equalTo(4194304.000000d)));
        assertThat(hostInfo.m_swap, is(equalTo(5999947776.000000d)));
        assertThat(hostInfo.d_total, is(equalTo(131933581312.000000d)));
        assertThat(hostInfo.d_free, is(equalTo(67023462400.000000d)));
        assertThat(hostInfo.os_name, is(equalTo("Linux")));
        assertThat(hostInfo.os_version, is(equalTo("3.2.0-4-amd64")));
        assertThat(hostInfo.g_ngpus, is(equalTo(1)));
    }

    @Test
    public void getMessageCount() {
        RpcClient rpcClient = new RpcClient();
        int seqNo = -1;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            seqNo = rpcClient.getMessageCount();
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertThat(seqNo, is(equalTo(12741)));
    }

    @Test
    public void getMessages() {
        RpcClient rpcClient = new RpcClient();
        Vector<Message> messages = null;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            messages = rpcClient.getMessages(0);
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
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
    public void getProjectStatus() {
        RpcClient rpcClient = new RpcClient();
        Vector<Project> projects = null;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            projects = rpcClient.getProjectStatus();
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
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
    public void getResults() {
        RpcClient rpcClient = new RpcClient();
        Vector<Result> results = null;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            results = rpcClient.getResults();
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
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
    public void getState() {
        RpcClient rpcClient = new RpcClient();
        CcState ccState = null;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            ccState = rpcClient.getState();
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
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
    public void networkAvailable() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.networkAvailable();
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void networkAvailableNACK() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            boincClient.setBehavior(BoincClientStub.Behavior.FAILURE);
            result = rpcClient.networkAvailable();
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertFalse(result);
    }

    @Test
    public void projectOpUpdate() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.projectOp(RpcClient.PROJECT_UPDATE, "http://www.worldcommunitygrid.org/");
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void projectOpSuspend() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.projectOp(RpcClient.PROJECT_SUSPEND, "http://www.worldcommunitygrid.org/");
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void projectOpResume() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.projectOp(RpcClient.PROJECT_RESUME, "http://www.worldcommunitygrid.org/");
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void projectOpNNW() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.projectOp(RpcClient.PROJECT_NNW, "http://www.worldcommunitygrid.org/");
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void projectOpANW() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.projectOp(RpcClient.PROJECT_ANW, "http://www.worldcommunitygrid.org/");
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void projectOpUnknown() {
        String errorMsg = "";
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.projectOp(6, "http://www.worldcommunitygrid.org/");
            fail("Successful projectOp unexpected, unknown operation error should happen");
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (UnsupportedOperationException e) {
            errorMsg = e.getMessage();
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertFalse(result);
        assertThat(errorMsg, is(equalTo("projectOp() - unsupported operation: 6")));
    }

    @Test
    public void resultOpSuspend() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.resultOp(RpcClient.RESULT_SUSPEND, "http://www.worldcommunitygrid.org/", "FAH2_000075_avx17680_000099_0046_013_0");
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void resultOpResume() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.resultOp(RpcClient.RESULT_RESUME, "http://www.worldcommunitygrid.org/", "FAH2_000075_avx17680_000099_0046_013_0");
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void resultOpAbort() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.resultOp(RpcClient.RESULT_ABORT, "http://www.worldcommunitygrid.org/", "FAH2_000075_avx17680_000099_0046_013_0");
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void resultOpUnknown() {
        String errorMsg = "";
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.resultOp(4, "http://www.worldcommunitygrid.org/", "FAH2_000075_avx17680_000099_0046_013_0");
            fail("Successful resultOp unexpected, unknown operation error should happen");
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (UnsupportedOperationException e) {
            errorMsg = e.getMessage();
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertFalse(result);
        assertThat(errorMsg, is(equalTo("resultOp() - unsupported operation: 4")));
    }

    @Test
    public void quit() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.quit();
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        assertFalse(rpcClient.connectionAlive());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void runBenchmarks() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.runBenchmarks();
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void setGpuModeAlways() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.setGpuMode(1, 0d);
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void setGpuModeAuto() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.setGpuMode(2, 0d);
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void setGpuModeSuspend1H() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.setGpuMode(3, 3600d);
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void setGpuModeRestore() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.setGpuMode(4, 0d);
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void setNetworkModeAlways() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.setNetworkMode(1, 0d);
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void setNetworkModeAuto() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.setNetworkMode(2, 0d);
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void setNetworkModeSuspend1H() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.setNetworkMode(3, 3600d);
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void setNetworkModeRestore() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.setNetworkMode(4, 0d);
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void setRunModeAlways() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.setRunMode(1, 0d);
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void setRunModeAuto() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.setRunMode(2, 0d);
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void setRunModeSuspend1H() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.setRunMode(3, 3600d);
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void setRunModeRestore() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.setRunMode(4, 0d);
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void transferOpRetry() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.transferOp(RpcClient.TRANSFER_RETRY, "http://einstein.phys.uwm.edu/", "GW_BBH2.jpg");
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void transferOpAbort() {
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.transferOp(RpcClient.TRANSFER_ABORT, "http://einstein.phys.uwm.edu/", "GW_BBH2.jpg");
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertTrue(result);
    }

    @Test
    public void transferOpUnknown() {
        String errorMsg = "";
        RpcClient rpcClient = new RpcClient();
        boolean result = false;
        try {
            rpcClient.open("127.0.0.1", 31416);
            assertTrue(rpcClient.isConnected());
            result = rpcClient.transferOp(3, "http://einstein.phys.uwm.edu/", "GW_BBH2.jpg");
            fail("Successful transferOp unexpected, unknown operation error should happen");
        }
        catch (ConnectionFailedException e) {
            fail("ConnectionFailedException " + e.getMessage());
        }
        catch (UnsupportedOperationException e) {
            errorMsg = e.getMessage();
        }
        catch (RpcClientFailedException e) {
            fail("RpcClientFailedException " + e.getMessage());
        }
        assertTrue(rpcClient.isConnected());
        rpcClient.close();
        assertFalse(result);
        assertThat(errorMsg, is(equalTo("transferOp() - unsupported operation: 3")));
    }

    @After
    public void tearDown() {
        if (boincClient != null) {
            boolean stopped = boincClient.stopListener();
            assertTrue(stopped);
            boincClient = null;
        }
    }
}
