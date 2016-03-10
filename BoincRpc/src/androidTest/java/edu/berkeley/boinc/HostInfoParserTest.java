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
public class HostInfoParserTest {

    @Test
    public void parseNormalNoGpu() {
        final String received = TestSupport.readResource(edu.berkeley.boinc.test.R.raw.get_host_info_reply_nogpu);
        assertThat(received.length(), is(equalTo(1263)));
        HostInfo hostInfo = null;
        try {
            hostInfo = HostInfoParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
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
        assertThat(hostInfo.g_ngpus, is(equalTo(0)));
    }

    @Test
    public void parseNormalWithGpu() {
        final String received = TestSupport.readResource(edu.berkeley.boinc.test.R.raw.get_host_info_reply_gpu);
        assertThat(received.length(), is(equalTo(4476)));
        HostInfo hostInfo = null;
        try {
            hostInfo = HostInfoParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
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
    public void emptyAnswer() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<host_info>\n" +
                "</host_info>\n" +
                "</boinc_gui_rpc_reply>\n";
        HostInfo hostInfo = null;
        try {
            hostInfo = HostInfoParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (InvalidDataReceivedException e) {
            fail("InvalidDataReceivedException unexpected");
        }
        assertNotNull(hostInfo);
        assertNull(hostInfo.domain_name);
        assertNull(hostInfo.ip_addr);
        assertNull(hostInfo.host_cpid);
    }

    @Test
    public void elementNotPresent() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<core_client_major_version>7</core_client_major_version>\n" +
                "<core_client_minor_version>4</core_client_minor_version>\n" +
                "<core_client_release>23</core_client_release>\n" +
                "</boinc_gui_rpc_reply>\n";
        HostInfo hostInfo = null;
        String errorMsg = "";
        try {
            hostInfo = HostInfoParser.parse(received);
            fail("Successful parsing unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (AuthorizationFailedException e) {
            fail("AuthorizationFailedException unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (InvalidDataReceivedException e) {
            errorMsg = e.getMessage();
        }
        assertThat(errorMsg, is(equalTo("Invalid data received")));
        assertNull(hostInfo);
    }

    @Test
    public void unauthorized() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<unauthorized/>\n" +
                "</boinc_gui_rpc_reply>\n";
        HostInfo hostInfo = null;
        String errorMsg = "";
        try {
            hostInfo = HostInfoParser.parse(received);
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
        assertNull(hostInfo);
    }

    @Test
    public void invalidData() {
        final String received = TestSupport.readResource(edu.berkeley.boinc.test.R.raw.get_host_info_reply_nogpu, 1262);
        HostInfo hostInfo = null;
        String errorMsg = "";
        try {
            hostInfo = HostInfoParser.parse(received);
            fail("Successful parsing unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (AuthorizationFailedException e) {
            errorMsg = e.getMessage();
            fail("AuthorizationFailedException unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (InvalidDataReceivedException e) {
            errorMsg = e.getMessage();
        }
        assertThat(errorMsg, is(equalTo("Malformed XML while parsing <host_info>")));
        assertNull(hostInfo);
    }
}
