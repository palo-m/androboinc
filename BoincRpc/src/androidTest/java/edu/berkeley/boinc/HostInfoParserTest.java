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
public class HostInfoParserTest {

    @Test
    public void parseNormalNoGpu() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<host_info>\n" +
                "    <timezone>28800</timezone>\n" +
                "    <domain_name>machine3</domain_name>\n" +
                "    <ip_addr>192.168.0.3</ip_addr>\n" +
                "    <host_cpid>a8c15a2da71502bb52c8d4a5025e70b6</host_cpid>\n" +
                "    <p_ncpus>4</p_ncpus>\n" +
                "    <p_vendor>GenuineIntel</p_vendor>\n" +
                "    <p_model>Intel(R) Core(TM)2 Quad CPU    Q6600  @ 2.40GHz [Family 6 Model 15 Stepping 11]</p_model>\n" +
                "    <p_features>fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush dts acpi mmx fxsr sse sse2 ss ht tm pbe syscall nx lm constant_tsc arch_perfmon pebs bts rep_good nopl aperfmperf pni dtes64 monitor ds_cpl vmx est tm2 ssse3 cx16 xtpr pdcm lahf_lm dtherm tpr_shadow vnmi flexpriority</p_features>\n" +
                "    <p_fpops>2227719169.069331</p_fpops>\n" +
                "    <p_iops>12828645195.720629</p_iops>\n" +
                "    <p_membw>1000000000.000000</p_membw>\n" +
                "    <p_calculated>1456151145.533448</p_calculated>\n" +
                "    <p_vm_extensions_disabled>0</p_vm_extensions_disabled>\n" +
                "    <m_nbytes>4150296576.000000</m_nbytes>\n" +
                "    <m_cache>4194304.000000</m_cache>\n" +
                "    <m_swap>5999947776.000000</m_swap>\n" +
                "    <d_total>131933581312.000000</d_total>\n" +
                "    <d_free>67023462400.000000</d_free>\n" +
                "    <os_name>Linux</os_name>\n" +
                "    <os_version>3.2.0-4-amd64</os_version>\n" +
                "    <coprocs>\n" +
                "    </coprocs>\n" +
                "</host_info>\n" +
                "</boinc_gui_rpc_reply>\n";
        HostInfo hostInfo = new HostInfo();
        try {
            hostInfo = HostInfoParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            hostInfo.ip_addr = e.getMessage();
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            hostInfo.ip_addr = e.getMessage();
            fail("InvalidDataReceivedException unexpected");
        }
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
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<host_info>\n" +
                "    <timezone>28800</timezone>\n" +
                "    <domain_name>machine3</domain_name>\n" +
                "    <ip_addr>192.168.0.3</ip_addr>\n" +
                "    <host_cpid>a8c15a2da71502bb52c8d4a5025e70b6</host_cpid>\n" +
                "    <p_ncpus>4</p_ncpus>\n" +
                "    <p_vendor>GenuineIntel</p_vendor>\n" +
                "    <p_model>Intel(R) Core(TM)2 Quad CPU    Q6600  @ 2.40GHz [Family 6 Model 15 Stepping 11]</p_model>\n" +
                "    <p_features>fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush dts acpi mmx fxsr sse sse2 ss ht tm pbe syscall nx lm constant_tsc arch_perfmon pebs bts rep_good nopl aperfmperf pni dtes64 monitor ds_cpl vmx est tm2 ssse3 cx16 xtpr pdcm lahf_lm dtherm tpr_shadow vnmi flexpriority</p_features>\n" +
                "    <p_fpops>2227719169.069331</p_fpops>\n" +
                "    <p_iops>12828645195.720629</p_iops>\n" +
                "    <p_membw>1000000000.000000</p_membw>\n" +
                "    <p_calculated>1456151145.533448</p_calculated>\n" +
                "    <p_vm_extensions_disabled>0</p_vm_extensions_disabled>\n" +
                "    <m_nbytes>4150296576.000000</m_nbytes>\n" +
                "    <m_cache>4194304.000000</m_cache>\n" +
                "    <m_swap>5999947776.000000</m_swap>\n" +
                "    <d_total>131933581312.000000</d_total>\n" +
                "    <d_free>67023462400.000000</d_free>\n" +
                "    <os_name>Linux</os_name>\n" +
                "    <os_version>3.2.0-4-amd64</os_version>\n" +
                "    <coprocs>\n" +
                "<coproc_ati>\n" +
                "   <count>1</count>\n" +
                "   <name>ATI Radeon HD 4700/4800 (RV740/RV770)</name>\n" +
                "   <available_ram>1040187392.000000</available_ram>\n" +
                "   <have_cal>1</have_cal>\n" +
                "   <have_opencl>1</have_opencl>\n" +
                "   <peak_flops>2400000000000.000000</peak_flops>\n" +
                "   <CALVersion>1.4.1734</CALVersion>\n" +
                "   <target>5</target>\n" +
                "   <localRAM>1024</localRAM>\n" +
                "   <uncachedRemoteRAM>1731</uncachedRemoteRAM>\n" +
                "   <cachedRemoteRAM>491</cachedRemoteRAM>\n" +
                "   <engineClock>750</engineClock>\n" +
                "   <memoryClock>900</memoryClock>\n" +
                "   <wavefrontSize>64</wavefrontSize>\n" +
                "   <numberOfSIMD>10</numberOfSIMD>\n" +
                "   <doublePrecision>1</doublePrecision>\n" +
                "   <pitch_alignment>256</pitch_alignment>\n" +
                "   <surface_alignment>256</surface_alignment>\n" +
                "   <maxResource1DWidth>8192</maxResource1DWidth>\n" +
                "   <maxResource2DWidth>8192</maxResource2DWidth>\n" +
                "   <maxResource2DHeight>8192</maxResource2DHeight>\n" +
                "    <atirt_detected/>\n" +
                "   <coproc_opencl>\n" +
                "      <name>ATI Radeon HD 4700/4800 (RV740/RV770)</name>\n" +
                "      <vendor>Advanced Micro Devices, Inc.</vendor>\n" +
                "      <vendor_id>4098</vendor_id>\n" +
                "      <available>1</available>\n" +
                "      <half_fp_config>0</half_fp_config>\n" +
                "      <single_fp_config>62</single_fp_config>\n" +
                "      <double_fp_config>63</double_fp_config>\n" +
                "      <endian_little>1</endian_little>\n" +
                "      <execution_capabilities>1</execution_capabilities>\n" +
                "      <extensions>cl_khr_fp64 cl_amd_fp64 cl_khr_gl_sharing cl_amd_device_attribute_query</extensions>\n" +
                "      <global_mem_size>1073741824</global_mem_size>\n" +
                "      <local_mem_size>16384</local_mem_size>\n" +
                "      <max_clock_frequency>750</max_clock_frequency>\n" +
                "      <max_compute_units>10</max_compute_units>\n" +
                "      <opencl_platform_version>OpenCL 1.2 AMD-APP (937.2)</opencl_platform_version>\n" +
                "      <opencl_device_version>OpenCL 1.0 AMD-APP (937.2)</opencl_device_version>\n" +
                "      <opencl_driver_version>CAL 1.4.1734</opencl_driver_version>\n" +
                "   </coproc_opencl>\n" +
                "</coproc_ati>\n" +
                "    </coprocs>\n" +
                "<opencl_cpu_prop>\n" +
                "   <platform_vendor>Advanced Micro Devices, Inc.</platform_vendor>\n" +
                "   <opencl_cpu_info>\n" +
                "      <name>AMD Phenom(tm) II X4 965 Processor</name>\n" +
                "      <vendor>AuthenticAMD</vendor>\n" +
                "      <vendor_id>4098</vendor_id>\n" +
                "      <available>1</available>\n" +
                "      <half_fp_config>0</half_fp_config>\n" +
                "      <single_fp_config>191</single_fp_config>\n" +
                "      <double_fp_config>63</double_fp_config>\n" +
                "      <endian_little>1</endian_little>\n" +
                "      <execution_capabilities>3</execution_capabilities>\n" +
                "      <extensions>cl_khr_fp64 cl_amd_fp64 cl_khr_global_int32_base_atomics cl_khr_global_int32_extended_atomics cl_khr_local_int32_base_atomics cl_khr_local_int32_extended_atomics cl_khr_int64_base_atomics cl_khr_int64_extended_atomics cl_khr_byte_addressable_store cl_khr_gl_sharing cl_ext_device_fission cl_amd_device_attribute_query cl_amd_vec3 cl_amd_printf cl_amd_media_ops cl_amd_popcnt</extensions>\n" +
                "      <global_mem_size>4156575744</global_mem_size>\n" +
                "      <local_mem_size>32768</local_mem_size>\n" +
                "      <max_clock_frequency>2200</max_clock_frequency>\n" +
                "      <max_compute_units>4</max_compute_units>\n" +
                "      <opencl_platform_version>OpenCL 1.2 AMD-APP (937.2)</opencl_platform_version>\n" +
                "      <opencl_device_version>OpenCL 1.2 AMD-APP (937.2)</opencl_device_version>\n" +
                "      <opencl_driver_version>2.0 (sse2)</opencl_driver_version>\n" +
                "   </opencl_cpu_info>\n" +
                "</opencl_cpu_prop>\n" +
                "</host_info>\n" +
                "</boinc_gui_rpc_reply>\n";
        HostInfo hostInfo = new HostInfo();
        try {
            hostInfo = HostInfoParser.parse(received);
        }
        catch (AuthorizationFailedException e) {
            hostInfo.ip_addr = e.getMessage();
            fail("AuthorizationFailedException unexpected");
        }
        catch (InvalidDataReceivedException e) {
            hostInfo.ip_addr = e.getMessage();
            fail("InvalidDataReceivedException unexpected");
        }
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
    public void unauthorized() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<unauthorized/>\n" +
                "</boinc_gui_rpc_reply>\n";
        HostInfo hostInfo = new HostInfo();
        try {
            hostInfo = HostInfoParser.parse(received);
            fail("Successful parsing unexpected, AuthorizationFailedException should be thrown instead");
        }
        catch (AuthorizationFailedException e) {
            hostInfo.ip_addr = e.getMessage();
        }
        catch (InvalidDataReceivedException e) {
            hostInfo.ip_addr = e.getMessage();
            fail("InvalidDataReceivedException unexpected, AuthorizationFailedException should be thrown instead");
        }
        assertThat(hostInfo.ip_addr, is(equalTo("Authorization Failed")));
    }

    @Test
    public void invalidData() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<host_info>\n" +
                "    <timezone>28800</timezone>\n" +
                "    <domain_name>machine3</domain_name>\n" +
                "    <ip_addr>192.168.0.3</ip_addr>\n" +
                "    <host_cpid>a8c15a2da71502bb52c8d4a5025e70b6</host_cpid>\n" +
                "    <p_ncpus>4</p_ncpus>\n" +
                "    <p_vendor>GenuineIntel</p_vendor>\n" +
                "    <p_model>Intel(R) Core(TM)2 Quad CPU    Q6600  @ 2.40GHz [Family 6 Model 15 Stepping 11]</p_model>\n" +
                "    <p_features>fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush dts acpi mmx fxsr sse sse2 ss ht tm pbe syscall nx lm constant_tsc arch_perfmon pebs bts rep_good nopl aperfmperf pni dtes64 monitor ds_cpl vmx est tm2 ssse3 cx16 xtpr pdcm lahf_lm dtherm tpr_shadow vnmi flexpriority</p_features>\n" +
                "    <p_fpops>2227719169.069331</p_fpops>\n" +
                "    <p_iops>12828645195.720629</p_iops>\n" +
                "    <p_membw>1000000000.000000</p_membw>\n" +
                "    <p_calculated>1456151145.533448</p_calculated>\n" +
                "    <p_vm_extensions_disabled>0</p_vm_extensions_disabled>\n" +
                "    <m_nbytes>4150296576.000000</m_nbytes>\n" +
                "    <m_cache>4194304.000000</m_cache>\n" +
                "    <m_swap>5999947776.000000</m_swap>\n" +
                "    <d_total>131933581312.000000</d_total>\n" +
                "    <d_free>67023462400.000000</d_free>\n" +
                "    <os_name>Linux</os_name>\n" +
                "    <os_version>3.2.0-4-amd64</os_version>\n" +
                "    <coprocs>\n" +
                "    </coprocs>\n" +
                "</host_info>\n" +
                "</boinc_gui_rpc_reply";
        HostInfo hostInfo = new HostInfo();
        try {
            hostInfo = HostInfoParser.parse(received);
            fail("Successful parsing unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (AuthorizationFailedException e) {
            hostInfo.ip_addr = e.getMessage();
            fail("AuthorizationFailedException unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (InvalidDataReceivedException e) {
            hostInfo.ip_addr = e.getMessage();
        }
        assertThat(hostInfo.ip_addr, is(equalTo("Malformed XML while parsing <host_info>")));
    }

    @Test
    public void elementNotPresent() {
        final String received =
                "<boinc_gui_rpc_reply>\n" +
                "<core_client_major_version>7</core_client_major_version>\n" +
                "<core_client_minor_version>4</core_client_minor_version>\n" +
                "<core_client_release>23</core_client_release>\n" +
                "</boinc_gui_rpc_reply>\n";
        HostInfo hostInfo = new HostInfo();
        try {
            hostInfo = HostInfoParser.parse(received);
            fail("Successful parsing unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (AuthorizationFailedException e) {
            hostInfo.ip_addr = e.getMessage();
            fail("AuthorizationFailedException unexpected, InvalidDataReceivedException should be thrown instead");
        }
        catch (InvalidDataReceivedException e) {
            hostInfo.ip_addr = e.getMessage();
        }
        assertThat(hostInfo.ip_addr, is(equalTo("Invalid data received")));
    }
}
