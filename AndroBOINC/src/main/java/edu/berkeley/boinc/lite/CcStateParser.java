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

import sk.boinc.androboinc.BuildConfig;
import android.util.Log;
import android.util.Xml;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


public class CcStateParser extends BaseParser {
	private static final String TAG = "CcStateParser";

	private CcState mCcState = new CcState();
	private VersionInfo mVersionInfo = new VersionInfo();
	private HostInfoParser mHostInfoParser = new HostInfoParser();
	private boolean mInHostInfo = false;
	private ProjectsParser mProjectsParser = new ProjectsParser();
	private boolean mInProject = false;
	private AppsParser mAppsParser = new AppsParser();
	private boolean mInApp = false;
	private WorkunitsParser mWorkunitsParser = new WorkunitsParser();
	private boolean mInWorkunit = false;
	private ResultsParser mResultsParser = new ResultsParser();
	private boolean mInResult = false;
	private boolean mUnauthorized = false;
	
	public final CcState getCcState() throws AuthorizationFailedException {
		if (mUnauthorized) throw new AuthorizationFailedException();
		return mCcState;
	}

	/**
	 * Parse the RPC result (state) and generate vector of projects info
	 * 
	 * @param rpcResult String returned by RPC call of core client
	 * @return connected client state
	 * @throws AuthorizationFailedException in case of unauthorized
	 * @throws InvalidDataReceivedException in case XML cannot be parsed
	 */
	public static CcState parse(String rpcResult) throws AuthorizationFailedException, InvalidDataReceivedException {
		try {
			CcStateParser parser = new CcStateParser();
			Xml.parse(rpcResult, parser);
			return parser.getCcState();
		}
		catch (SAXException e) {
			if (BuildConfig.DEBUG) Log.d(TAG, "Malformed XML:\n" + rpcResult);
			throw new InvalidDataReceivedException("Malformed XML while parsing <cc_state>", e);
		}		

	}

	@Override
	public void endDocument() {
		// Commit sub-parsers data to resulting CcState
		mCcState.version_info = mVersionInfo;
		try {
			mCcState.host_info = mHostInfoParser.getHostInfo();
		}
		catch (AuthorizationFailedException e) {
			mUnauthorized = true;
			// Abort further actions
			return;
		}
		catch (InvalidDataReceivedException e) {
			// The tag <host_info> is not mandatory in <cc_state>
			// It could be missing
			mCcState.host_info = null;
		}
		try {
			mCcState.projects = mProjectsParser.getProjects();
			mCcState.apps = mAppsParser.getApps();
			mCcState.workunits = mWorkunitsParser.getWorkunits();
			mCcState.results = mResultsParser.getResults();
		}
		catch (AuthorizationFailedException e) {
			mUnauthorized = true;
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (localName.equalsIgnoreCase("host_info")) {
			// Just stepped inside <host_info>
			mInHostInfo = true;
		}
		if (mInHostInfo) {
			mHostInfoParser.startElement(uri, localName, qName, attributes);
		}
		if (localName.equalsIgnoreCase("project")) {
			// Just stepped inside <project>
			mInProject = true;
		}
		if (mInProject) {
			mProjectsParser.startElement(uri, localName, qName, attributes);
		}
		if (localName.equalsIgnoreCase("app")) {
			// Just stepped inside <app>
			mInApp = true;
		}
		if (mInApp) {
			mAppsParser.startElement(uri, localName, qName, attributes);
		}
		if (localName.equalsIgnoreCase("workunit")) {
			// Just stepped inside <workunit>
			mInWorkunit = true;
		}
		if (mInWorkunit) {
			mWorkunitsParser.startElement(uri, localName, qName, attributes);
		}
		if (localName.equalsIgnoreCase("result")) {
			// Just stepped inside <result>
			mInResult = true;
		}
		if (mInResult) {
			mResultsParser.startElement(uri, localName, qName, attributes);
		}
		if ( localName.equalsIgnoreCase("core_client_major_version") ||
				localName.equalsIgnoreCase("core_client_minor_version") ||
				localName.equalsIgnoreCase("core_client_release")
				) {
			// VersionInfo elements
			mElementStarted = true;
			mCurrentElement.setLength(0);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
		if (mInHostInfo) {
			// We are inside <host_info>
			mHostInfoParser.characters(ch, start, length);
		}
		if (mInProject) {
			// We are inside <project>
			mProjectsParser.characters(ch, start, length);
		}
		if (mInApp) {
			// We are inside <project>
			mAppsParser.characters(ch, start, length);
		}
		if (mInWorkunit) {
			// We are inside <workunit>
			mWorkunitsParser.characters(ch, start, length);
		}
		if (mInResult) {
			// We are inside <result>
			mResultsParser.characters(ch, start, length);
		}
		// VersionInfo elements are handled in super.characters()
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		try {
			if (mInHostInfo) {
				// We are inside <host_info>
				// parse it by sub-parser in any case (to parse also closing element)
				mHostInfoParser.endElement(uri, localName, qName);
				if (localName.equalsIgnoreCase("host_info")) {
					mInHostInfo = false;
				}
			}
			if (mInProject) {
				// We are inside <project>
				// parse it by sub-parser in any case (must parse also closing element!)
				mProjectsParser.endElement(uri, localName, qName);
				if (localName.equalsIgnoreCase("project")) {
					// Closing tag of <project>
					mInProject = false;
				}
			}
			if (mInApp) {
				// We are inside <app>
				// parse it by sub-parser in any case (must parse also closing element!)
				mAppsParser.endElement(uri, localName, qName);
				if (localName.equalsIgnoreCase("app")) {
					// Closing tag of <app>
					mInApp = false;
				}
			}
			if (mInWorkunit) {
				// We are inside <workunit>
				// parse it by sub-parser in any case (must parse also closing element!)
				mWorkunitsParser.endElement(uri, localName, qName);
				if (localName.equalsIgnoreCase("workunit")) {
					// Closing tag of <workunit>
					mInWorkunit = false;
				}
			}
			if (mInResult) {
				// We are inside <result>
				// parse it by sub-parser in any case (must parse also closing element!)
				mResultsParser.endElement(uri, localName, qName);
				if (localName.equalsIgnoreCase("result")) {
					// Closing tag of <result>
					mInResult = false;
				}
			}
			if (mElementStarted) {
				trimEnd();
				// VersionInfo?
				if (localName.equalsIgnoreCase("core_client_major_version")) {
					mVersionInfo.major = Integer.parseInt(mCurrentElement.toString());
				}
				else if (localName.equalsIgnoreCase("core_client_minor_version")) {
					mVersionInfo.minor = Integer.parseInt(mCurrentElement.toString());
				}
				else if (localName.equalsIgnoreCase("core_client_release")) {
					mVersionInfo.release = Integer.parseInt(mCurrentElement.toString());
				}
				mElementStarted = false;
			}
			else if (localName.equalsIgnoreCase("unauthorized")) {
				// Not inside <app> and we received <unauthorized/>
				mUnauthorized = true;
			}
		}
		catch (NumberFormatException e) {
			Log.i(TAG, "Exception when decoding " + localName);
		}
	}
}
