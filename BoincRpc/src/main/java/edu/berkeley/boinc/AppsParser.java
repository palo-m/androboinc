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


public class AppsParser extends BaseParser {
    private static final String TAG = "AppsParser";

    private Vector<App> mApps = new Vector<>();
    private App mApp = null;

    public final Vector<App> getApps() {
        return mApps;
    }

    /**
     * Parse the RPC result (app) and generate vector of app
     *
     * @param rpcResult String returned by RPC call of core client
     * @return vector of app
     * @throws InvalidDataReceivedException in case XML cannot be parsed
     */
    public static Vector<App> parse(String rpcResult) throws InvalidDataReceivedException {
        try {
            AppsParser parser = new AppsParser();
            Xml.parse(rpcResult, parser);
            return parser.getApps();
        }
        catch (SAXException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Malformed XML:\n" + rpcResult);
            throw new InvalidDataReceivedException("Malformed XML while parsing <app>", e);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (localName.equalsIgnoreCase("app")) {
            mApp = new App();
        }
        else {
            // Another element, hopefully primitive and not constructor
            // (although unknown constructor does not hurt, because there will be primitive start anyway)
            mElementStarted = true;
            mCurrentElement.setLength(0);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (mApp != null) {
            // We are inside <app>
            if (localName.equalsIgnoreCase("app")) {
                // Closing tag of <app> - add to vector and be ready for next one
                if (!mApp.name.equals("")) {
                    // name is a must
                    mApps.add(mApp);
                }
                mApp = null;
            }
            else {
                // Not the closing tag - we decode possible inner tags
                trimEnd();
                if (localName.equalsIgnoreCase("name")) {
                    mApp.name = mCurrentElement.toString();
                }
                else if (localName.equalsIgnoreCase("user_friendly_name")) {
                    mApp.user_friendly_name = mCurrentElement.toString();
                }
            }
        }
        mElementStarted = false;
    }
}
