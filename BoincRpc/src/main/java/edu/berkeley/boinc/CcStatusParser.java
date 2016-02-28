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


public class CcStatusParser extends BaseParser {
    private static final String TAG = "CcStatusParser";

    private CcStatus mCcStatus;


    public final CcStatus getCcStatus() throws AuthorizationFailedException, InvalidDataReceivedException {
        if (mUnauthorized) throw new AuthorizationFailedException();
        if (null == mCcStatus) throw new InvalidDataReceivedException();
        return mCcStatus;
    }

    /**
     * Parse the RPC result (cc_status)
     * @param rpcResult String returned by RPC call of core client
     * @return CcStatus
     * @throws RpcClientFailedException in case of error:
     * <ul>
     * <li>{@link AuthorizationFailedException} in case of unauthorized</li>
     * <li>{@link InvalidDataReceivedException} in case XML cannot be parsed
     *     or does not contain valid {@code <cc_status>} tag</li>
     * </ul>
     */
    public static CcStatus parse(String rpcResult) throws AuthorizationFailedException, InvalidDataReceivedException {
        try {
            CcStatusParser parser = new CcStatusParser();
            Xml.parse(rpcResult, parser);
            return parser.getCcStatus();
        }
        catch (SAXException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Malformed XML:\n" + rpcResult);
            throw new InvalidDataReceivedException("Malformed XML while parsing <cc_status>");
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (localName.equalsIgnoreCase("cc_status")) {
            if (mCcStatus != null) {
                // previous <cc_status> not closed - dropping it!
                Log.i(TAG, "Dropping unfinished <cc_status> data");
            }
            mCcStatus = new CcStatus();
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
            if (mCcStatus != null) {
                // We are inside <cc_status>
                if (localName.equalsIgnoreCase("cc_status")) {
                    // Closing tag of <cc_status> - nothing to do at the moment
                }
                else {
                    trimEnd();
                    // Not the closing tag - we decode possible inner tags
                    if (localName.equalsIgnoreCase("task_mode")) {
                        mCcStatus.task_mode = Integer.parseInt(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("task_mode_perm")) {
                        mCcStatus.task_mode_perm = Integer.parseInt(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("task_mode_delay")) {
                        mCcStatus.task_mode_delay = Double.parseDouble(mCurrentElement.toString());
                    }
//					else if (localName.equalsIgnoreCase("task_suspend_reason")) {
//						mCcStatus.task_suspend_reason = Integer.parseInt(mCurrentElement.toString());
//					}
                    if (localName.equalsIgnoreCase("gpu_mode")) {
                        mCcStatus.gpu_mode = Integer.parseInt(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("gpu_mode_perm")) {
                        mCcStatus.gpu_mode_perm = Integer.parseInt(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("gpu_mode_delay")) {
                        mCcStatus.gpu_mode_delay = Double.parseDouble(mCurrentElement.toString());
                    }
                    if (localName.equalsIgnoreCase("network_mode")) {
                        mCcStatus.network_mode = Integer.parseInt(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("network_mode_perm")) {
                        mCcStatus.network_mode_perm = Integer.parseInt(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("network_mode_delay")) {
                        mCcStatus.network_mode_delay = Double.parseDouble(mCurrentElement.toString());
                    }
//					else if (localName.equalsIgnoreCase("network_suspend_reason")) {
//						mCcStatus.network_suspend_reason = Integer.parseInt(mCurrentElement.toString());
//					}
                    else if (localName.equalsIgnoreCase("network_status")) {
                        mCcStatus.network_status = Integer.parseInt(mCurrentElement.toString());
                    }
//					else if (localName.equalsIgnoreCase("ams_password_error")) {
//						if (mCurrentElement.length() > 1) {
//							mCcStatus.ams_password_error = (0 != Integer.parseInt(mCurrentElement.toString()));
//						}
//						else {
//							mCcStatus.ams_password_error = true;
//						}
//					}
//					else if (localName.equalsIgnoreCase("manager_must_quit")) {
//						if (mCurrentElement.length() > 1) {
//							mCcStatus.manager_must_quit = (0 != Integer.parseInt(mCurrentElement.toString()));
//						}
//						else {
//							mCcStatus.manager_must_quit = true;
//						}
//					}
//					else if (localName.equalsIgnoreCase("disallow_attach")) {
//						if (mCurrentElement.length() > 1) {
//							mCcStatus.disallow_attach = (0 != Integer.parseInt(mCurrentElement.toString()));
//						}
//						else {
//							mCcStatus.disallow_attach = true;
//						}
//					}
//					else if (localName.equalsIgnoreCase("simple_gui_only")) {
//						if (mCurrentElement.length() > 1) {
//							mCcStatus.simple_gui_only = (0 != Integer.parseInt(mCurrentElement.toString()));
//						}
//						else {
//							mCcStatus.simple_gui_only = true;
//						}
//					}
                }
            }
        }
        catch (NumberFormatException e) {
            Log.i(TAG, "Exception when decoding " + localName);
        }
        mElementStarted = false;
    }
}
