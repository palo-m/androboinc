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

package edu.berkeley.boinc;

import android.util.Log;
import android.util.Xml;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class SimpleReplyParser extends DefaultHandler {
    private static final String TAG = "SimpleReplyParser";

    private boolean mParsed = false;
    private boolean mInReply = false;
    private boolean mSuccess = false;
    private boolean mUnauthorized = false;

    // Disable direct instantiation of this class
    private SimpleReplyParser() {}

    public final boolean result() throws AuthorizationFailedException {
        if (mUnauthorized) throw new AuthorizationFailedException();
        return mSuccess;
    }

    /**
     * Parse the RPC result of command
     *
     * @param rpcResult String returned by RPC call of core client
     * @return true in case of {@code <success/>}, false in case of {@code <failure/>}
     * @throws AuthorizationFailedException in case of unauthorized
     * @throws InvalidDataReceivedException in case XML cannot be parsed
     */
    public static boolean isSuccess(String rpcResult) throws AuthorizationFailedException, InvalidDataReceivedException {
        try {
            SimpleReplyParser parser = new SimpleReplyParser();
            Xml.parse(rpcResult, parser);
            return parser.result();
        }
        catch (SAXException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Malformed XML:\n" + rpcResult);
            throw new InvalidDataReceivedException("Malformed XML while parsing simple reply", e);
        }

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (localName.equalsIgnoreCase("boinc_gui_rpc_reply")) {
            mInReply = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (mInReply) {
            if (localName.equalsIgnoreCase("boinc_gui_rpc_reply")) {
                mInReply = false;
            }
            else if (!mParsed) {
                if (localName.equalsIgnoreCase("success")) {
                    mSuccess = true;
                    mParsed = true;
                }
                else if (localName.equalsIgnoreCase("failure")) {
                    mSuccess = false;
                    mParsed = true;
                }
                else if (localName.equalsIgnoreCase("unauthorized")) {
                    // There is <unauthorized/> inside <boinc_gui_rpc_reply>
                    mUnauthorized = true;
                    mParsed = true;
                }
            }
        }
    }
}
