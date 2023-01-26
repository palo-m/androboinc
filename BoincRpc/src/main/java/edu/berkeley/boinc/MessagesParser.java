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
import org.xml.sax.SAXParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;


public class MessagesParser extends BaseParser {
    private static final String TAG = "MessagesParser";

    private Vector<Message> mMessages = new Vector<>();
    private Message mMessage = null;

    public final Vector<Message> getMessages() throws AuthorizationFailedException {
        if (mUnauthorized) throw new AuthorizationFailedException();
        return mMessages;
    }

    /**
     * Parse the RPC result (msgs) and generate corresponding vector
     *
     * @param rpcResult String returned by RPC call of core client
     * @return vector of messages
     * @throws AuthorizationFailedException in case of unauthorized
     * @throws InvalidDataReceivedException in case XML cannot be parsed
     */
    public static Vector<Message> parse(String rpcResult) throws AuthorizationFailedException, InvalidDataReceivedException {
        MessagesParser parser = new MessagesParser();
        try {
            // Try to parse as received first, in a hope it is valid XML
            Xml.parse(rpcResult, parser);
            return parser.getMessages();
        }
        catch (SAXException dummy) {
            // BOINC is known to not escape message body, resulting in invalid XML.
            // Typical non-escaped message is:
            // "A new version of BOINC is available. <a href=http://boinc.berkeley.edu/download.php>Download it.</a>"
            // Another example of invalid XML is produced when using the gui_rpc_debug log-flag
            // So, we try to sanitize the body of messages by escaping XML special characters
            // and then we try parsing the sanitized data
            try {
                String sanitized = XmlSanitizer.sanitize(rpcResult, "body");
                Xml.parse(sanitized, parser);
                return parser.getMessages();
            }
            catch (SAXException e) {
                // Still some trouble, even after sanitizing the message bodies
                if (BuildConfig.DEBUG) {
                    SAXParseException details = (SAXParseException)e;
                    Log.d(TAG, "Malformed XML: systemId=" + details.getSystemId() +
                            ", publicId=" + details.getPublicId() +
                            ", lineNumber=" + details.getLineNumber() +
                            ", columnNumber=" + details.getColumnNumber()
                            );
                    BufferedReader br = new BufferedReader(new StringReader(rpcResult));
                    String line;
                    int lineNum = 0;
                    try {
                        int errLine = details.getLineNumber();
                        while ((line = br.readLine()) != null) {
                            ++lineNum;
                            if ( (lineNum >= (errLine - 5)) && (lineNum <= (errLine + 5))) {
                                Log.d("Malformed XML", "line " + lineNum + ": " + line);
                            }
                        }
                    }
                    catch (IOException ioe) {
                    }
                    Log.d(TAG, "Decoded " + parser.getMessages().size() + " messages");
                }
                throw new InvalidDataReceivedException("Malformed XML while parsing <msgs>", e);
            }
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (localName.equalsIgnoreCase("msg")) {
            mMessage = new Message();
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
            if (mMessage != null) {
                // We are inside <msg>
                if (localName.equalsIgnoreCase("msg")) {
                    // Closing tag of <msg> - add to vector and be ready for next one
                    if (mMessage.seqno != 0) {
                        // seqno is a must
                        mMessages.add(mMessage);
                    }
                    mMessage = null;
                }
                else {
                    // Not the closing tag - we decode possible inner tags
                    trimEnd();
                    if (localName.equalsIgnoreCase("project")) {
                        mMessage.project = mCurrentElement.toString();
                    }
                    else if (localName.equalsIgnoreCase("seqno")) {
                        mMessage.seqno = Integer.parseInt(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("pri")) {
                        mMessage.priority = Integer.parseInt(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("time")) {
                        mMessage.timestamp = (long)Double.parseDouble(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("body")) {
                        mMessage.body = mCurrentElement.toString();
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
