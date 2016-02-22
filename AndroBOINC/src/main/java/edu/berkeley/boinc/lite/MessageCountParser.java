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


public class MessageCountParser extends BaseParser {
	private static final String TAG = "MessageCountParser";

	private int mSeqno = -1;
	private boolean mInReply = false;
	private boolean mUnauthorized = false;

	// Disable direct instantiation of this class
	private MessageCountParser() {}

	public final int seqno() throws AuthorizationFailedException {
		if (mUnauthorized) throw new AuthorizationFailedException();
		return mSeqno;
	}

	/**
	 * Parse the RPC result (seqno) and generate corresponding vector
	 * 
	 * @param rpcResult String returned by RPC call of core client
	 * @return number of messages
	 * @throws AuthorizationFailedException in case of unauthorized
	 * @throws InvalidDataReceivedException in case XML cannot be parsed
	 */
	public static int getSeqno(String rpcResult) throws AuthorizationFailedException, InvalidDataReceivedException {
		try {
			MessageCountParser parser = new MessageCountParser();
			Xml.parse(rpcResult, parser);
			return parser.seqno();
		}
		catch (SAXException e) {
			if (BuildConfig.DEBUG) Log.d(TAG, "Malformed XML:\n" + rpcResult);
			throw new InvalidDataReceivedException("Malformed XML while parsing <seqno>", e);
		}		

	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (localName.equalsIgnoreCase("boinc_gui_rpc_reply")) {
			mInReply = true;
		}
		else {
			// Another element, hopefully primitive and not constructor
			// (although unknown constructor does not hurt, because there will be primitive start anyway)
			mElementStarted = true;
			mCurrentElement.setLength(0);
		}
	}

	// Method characters(char[] ch, int start, int length) is implemented by BaseParser,
	// filling mCurrentElement (including stripping of leading whitespaces)
	//@Override
	//public void characters(char[] ch, int start, int length) throws SAXException { }

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		try {
			if (mInReply) {
				// We are inside <boinc_gui_rpc_reply>
				if (localName.equalsIgnoreCase("boinc_gui_rpc_reply")) {
					mInReply = false;
				}
				else {
					trimEnd();
					if (localName.equalsIgnoreCase("seqno")) {
						mSeqno = Integer.parseInt(mCurrentElement.toString());
					}
					else if (localName.equalsIgnoreCase("unauthorized")) {
						// There is <unauthorized/> inside <boinc_gui_rpc_reply>
						mUnauthorized = true;
					}
				}
			}
		}
		catch (NumberFormatException e) {
			Log.i(TAG, "Exception when decoding " + localName);
		}
		mElementStarted = false;
	}
}
