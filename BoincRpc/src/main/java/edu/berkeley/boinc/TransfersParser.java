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


public class TransfersParser extends BaseParser {
    private static final String TAG = "TransfersParser";

    private Vector<Transfer> mTransfers = new Vector<>();
    private Transfer mTransfer = null;

    public final Vector<Transfer> getTransfers() throws AuthorizationFailedException {
        if (mUnauthorized) throw new AuthorizationFailedException();
        return mTransfers;
    }

    /**
     * Parse the RPC result (file_transfers) and generate vector of transfers info
     *
     * @param rpcResult String returned by RPC call of core client
     * @return vector of projects info
     * @throws AuthorizationFailedException in case of unauthorized
     * @throws InvalidDataReceivedException in case XML cannot be parsed
     */
    public static Vector<Transfer> parse(String rpcResult) throws AuthorizationFailedException, InvalidDataReceivedException {
        try {
            TransfersParser parser = new TransfersParser();
            Xml.parse(rpcResult, parser);
            return parser.getTransfers();
        }
        catch (SAXException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Malformed XML:\n" + rpcResult);
            throw new InvalidDataReceivedException("Malformed XML while parsing <file_transfers>", e);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (localName.equalsIgnoreCase("file_transfer")) {
            if (mTransfer != null) {
                // previous <file_transfer> not closed - dropping it!
                Log.i(TAG, "Dropping unfinished <file_transfer> data");
            }
            mTransfer = new Transfer();
        }
        else if (localName.equalsIgnoreCase("file_xfer")) {
            // Just constructor, flag should be set if it's present
            if (mTransfer != null) {
                mTransfer.xfer_active = true;
            }
        }
        else if (localName.equalsIgnoreCase("persistent_file_xfer")) {
            // Just constructor, but nothing to do here
            // We just do not set mElementStarted flag here, so we will
            // avoid unnecessary work in BaseParser.characters()
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
            if (mTransfer != null) {
                // We are inside <file_transfer>
                if (localName.equalsIgnoreCase("file_transfer")) {
                    // Closing tag of <project> - add to vector and be ready for next one
                    if (!mTransfer.project_url.equals("") && !mTransfer.name.equals("")) {
                        // project_url is a must
                        mTransfers.add(mTransfer);
                    }
                    mTransfer = null;
                }
                else {
                    // Not the closing tag - we decode possible inner tags
                    trimEnd();
                    if (localName.equalsIgnoreCase("project_url")) {
                        mTransfer.project_url = mCurrentElement.toString();
                    }
                    else if (localName.equalsIgnoreCase("name")) {
                        mTransfer.name = mCurrentElement.toString();
                    }
                    else if (localName.equalsIgnoreCase("is_upload")) {
                        mTransfer.is_upload = !mCurrentElement.toString().equals("0");
                    }
                    else if (localName.equalsIgnoreCase("generated_locally")) {
                        // The <generated_locally> is deprecated legacy tag
                        mTransfer.is_upload = !mCurrentElement.toString().equals("0");
                    }
                    else if (localName.equalsIgnoreCase("nbytes")) {
                        mTransfer.nbytes = (long)Double.parseDouble(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("status")) {
                        mTransfer.status = Integer.parseInt(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("time_so_far")) {
                        // inside <persistent_file_xfer>
                        mTransfer.time_so_far = (long)Double.parseDouble(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("next_request_time")) {
                        // inside <persistent_file_xfer>
                        mTransfer.next_request_time = (long)Double.parseDouble(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("last_bytes_xferred")) {
                        // inside <persistent_file_xfer>
                        // See also <bytes_xferred> below, both are setting the same parameters
                        if (mTransfer.bytes_xferred == 0) {
                            // Not set yet
                            mTransfer.bytes_xferred = (long)Double.parseDouble(mCurrentElement.toString());
                        }
                    }
                    else if (localName.equalsIgnoreCase("bytes_xferred")) {
                        // Total bytes transferred, but this info is not available if networking
                        // is suspended. This info is present only inside <file_xfer> (active transfer)
                        // In such case we overwrite value set by <last_bytes_xferred>
                        mTransfer.bytes_xferred = (long)Double.parseDouble(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("xfer_speed")) {
                        // inside <file_xfer>
                        mTransfer.xfer_speed = Float.parseFloat(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("project_backoff")) {
                        mTransfer.project_backoff = (long)Double.parseDouble(mCurrentElement.toString());
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
