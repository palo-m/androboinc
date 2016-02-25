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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class XmlSanitizerTest {
    private static final String rawText1 =
            "<msg>\n" +
            "  <seqno>1</seqno>\n" +
            "  <prj></prj>\n" +
            "  <body>A new version of BOINC is available. <a href=http://boinc.berkeley.edu/download.php>Download it.</a></body>\n" +
            "</msg>";
    private static final String sanitizedText1 =
            "<msg>\n" +
            "  <seqno>1</seqno>\n" +
            "  <prj></prj>\n" +
            "  <body>A new version of BOINC is available. &lt;a href=http://boinc.berkeley.edu/download.php&gt;Download it.&lt;/a&gt;</body>\n" +
            "</msg>";
    private static final String allUnsafeChars = "<>\"'&";
    private static final String allSanitizedChars = "&lt;&gt;&quot;&apos;&amp;";

    @Test
    public void sanitizeEmptyString() {
        final String received = "";
        String sanitized = XmlSanitizer.sanitize(received, "body");
        assertThat(sanitized, is(equalTo(received)));
    }

    @Test
    public void sanitizeEmptyBody() {
        final String received = "<body></body>";
        String sanitized = XmlSanitizer.sanitize(received, "body");
        assertThat(sanitized, is(equalTo(received)));
    }

    @Test
    public void sanitizeTagNotFound() {
        String sanitized = XmlSanitizer.sanitize(rawText1, "arbitrary");
        assertThat(sanitized, is(equalTo(rawText1)));
    }

    @Test
    public void sanitizeDifferentTag() {
        String sanitized = XmlSanitizer.sanitize(rawText1, "prj");
        assertThat(sanitized, is(equalTo(rawText1)));
    }

    @Test
    public void sanitizeNullTag() {
        String sanitized = XmlSanitizer.sanitize(rawText1, "");
        assertThat(sanitized, is(equalTo(rawText1)));
    }

    @Test
    public void sanitizeBoincNewVersion() {
        String sanitized = XmlSanitizer.sanitize(rawText1, "body");
        assertThat(sanitized, is(equalTo(sanitizedText1)));
    }

    @Test
    public void unescapeBoincNewVersion() {
        String unescaped = XmlSanitizer.unescapeXml(sanitizedText1);
        assertThat(unescaped, is(equalTo(rawText1)));
    }

    @Test
    public void escapeAllSupported() {
        String sanitized = XmlSanitizer.escapeXml(allUnsafeChars);
        assertThat(sanitized, is(equalTo(allSanitizedChars)));
    }

    @Test
    public void unescapeAllSupported() {
        String sanitized = XmlSanitizer.unescapeXml(allSanitizedChars);
        assertThat(sanitized, is(equalTo(allUnsafeChars)));
    }
}
