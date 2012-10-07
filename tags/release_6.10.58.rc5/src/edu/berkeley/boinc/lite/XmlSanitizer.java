/* 
 * AndroBOINC - BOINC Manager for Android
 * Copyright (C) 2010, Pavol Michalec
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

package edu.berkeley.boinc.lite;

import sk.boinc.androboinc.debug.Debugging;
import sk.boinc.androboinc.debug.Logging;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class XmlSanitizer {
	private static final String TAG = "XmlSanitizer";

	/**
	 * Transforms the input string to the valid XML.
	 * <p>
	 * The characters {@code <}, {@code >}, {@code '}, {@code "} and {@code &}
	 * are escaped to {@code &lt;}, {@code &gt;}, {@code &apos;}, {@code &quot;} and {@code &amp;} 
	 * respectively, if they appear inside the specified tags.
	 * <p>
	 * For example, for the input string {@code "<l1><l2>See <l3>data</l3> for more</l2></l1>"}
	 * it returns following results:
	 * <ul>
	 * <li>{@code "<l1><l2>See <l3>data</l3> for more</l2></l1>"} for sanitize(input, "l3")</li>
	 * <li>{@code "<l1><l2>See &lt;l3&gt;data&lt;/l3&gt; for more</l2></l1>"} for sanitize(input, "l2")</li>
	 * <li>{@code "<l1>&lt;l2&gt;See &lt;l3&gt;data&lt;/l3&gt; for more&lt;/l2&gt;</l1>"} for sanitize(input, "l1")</li>
	 * <li></li>
	 * </ul>
	 * This method currently works only for simple tags (no attributes).
	 * 
	 * @param input - the string of data to be sanitized
	 * @param tag - the tag where the data should be sanitized
	 * @return sanitized string
	 */
	public static String sanitize(final String input, final String tag) {
		Pattern p = Pattern.compile("<" + tag + ">(.+?)</" + tag + ">", Pattern.DOTALL);
		if (Logging.DEBUG) Log.d(TAG, "sanitize(): Using pattern \"" + p.pattern() + "\"");
		Matcher m = p.matcher(input);
		StringBuffer sb = new StringBuffer();
		if (!m.find()) {
			// No such pattern; short-cut return
			return input;
		}
		int pos = 0;
		while (m.find(pos)) {
			sb = sb.append(input, pos, m.start(1));
			sb = sb.append(escapeXml(m.group(1)));
			pos = m.end(1);
		}
		sb = sb.append(input, pos, input.length());
		if (Debugging.DATA) {
			Log.d(TAG, "Sanitized string: ");
			BufferedReader dbr = new BufferedReader(new StringReader(sb.toString()), 1024);
			String dl;
			int ln = 0;
			try {
				while ((dl = dbr.readLine()) != null) {
					++ln;
					Log.d(TAG, String.format("%4d: %s", ln, dl));
				}
			}
			catch (IOException ioe) {
			}
		}
		return sb.toString();
	}

	/**
	 * Replaces characters {@code <}, {@code >}, {@code '}, {@code "} and {@code &}
	 * by escaped XML entities {@code &lt;}, {@code &gt;}, {@code &apos;}, {@code &quot;} 
	 * and {@code &amp;} respectively.
	 * 
	 * @param s - input string
	 * @return string with replaced characters or string "" if null is used as input
	 */
	public static String escapeXml(final String s) {
		if (s == null) return "";
		String result;
		result = s.replaceAll("&","&quot;");
		result = result.replaceAll("<","&lt;");
		result = result.replaceAll(">","&gt;");
		result = result.replaceAll("\"","&quot;");
		result = result.replaceAll("'","&apos;");
		return result;
	}

	/**
	 * Replaces escaped XML entities {@code &lt;}, {@code &gt;}, {@code &apos;}, {@code &quot;} and {@code &amp;}
	 * by characters {@code <}, {@code >}, {@code '}, {@code "} and {@code &} respectively.
	 * 
	 * @param s - input string
	 * @return string with replaced characters or string "" if null is used as input
	 */
	public static String unescapeXml(final String s) {
		if (s == null) return "";
		String result;
		result = s.replaceAll("&lt;","<");
		result = result.replaceAll("&gt;",">");
		result = result.replaceAll("&quot;","\"");
		result = result.replaceAll("&apos;","'");
		result = result.replaceAll("&quot;","&");
		return result;
	}
}
