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
import java.util.Vector;


public class WorkunitsParser extends BaseParser {
    private static final String TAG = "WorkunitsParser";

    private Vector<Workunit> mWorkunits = new Vector<>();
    private Workunit mWorkunit = null;

    public final Vector<Workunit> getWorkunits() throws AuthorizationFailedException {
        if (mUnauthorized) throw new AuthorizationFailedException();
        return mWorkunits;
    }

    /**
     * Parse the RPC result (workunit) and generate corresponding vector
     *
     * @param rpcResult String returned by RPC call of core client
     * @return vector of workunits
     * @throws AuthorizationFailedException in case of unauthorized
     * @throws InvalidDataReceivedException in case XML cannot be parsed
     */
    public static Vector<Workunit> parse(String rpcResult) throws AuthorizationFailedException, InvalidDataReceivedException {
        try {
            WorkunitsParser parser = new WorkunitsParser();
            Xml.parse(rpcResult, parser);
            return parser.getWorkunits();
        }
        catch (SAXException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Malformed XML:\n" + rpcResult);
            throw new InvalidDataReceivedException("Malformed XML while parsing <workunits>", e);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (localName.equalsIgnoreCase("workunit")) {
            mWorkunit = new Workunit();
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
            if (mWorkunit != null) {
                // We are inside <workunit>
                if (localName.equalsIgnoreCase("workunit")) {
                    // Closing tag of <workunit> - add to vector and be ready for next one
                    if (!mWorkunit.name.equals("")) {
                        // name is a must
                        mWorkunits.add(mWorkunit);
                    }
                    mWorkunit = null;
                }
                else {
                    // Not the closing tag - we decode possible inner tags
                    trimEnd();
                    if (localName.equalsIgnoreCase("name")) {
                        mWorkunit.name = mCurrentElement.toString();
                    }
                    else if (localName.equalsIgnoreCase("app_name")) {
                        mWorkunit.app_name = mCurrentElement.toString();
                    }
                    else if (localName.equalsIgnoreCase("version_num")) {
                        mWorkunit.version_num = Integer.parseInt(mCurrentElement.toString());
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
