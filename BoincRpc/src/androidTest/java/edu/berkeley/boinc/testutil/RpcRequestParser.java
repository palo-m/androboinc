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

package edu.berkeley.boinc.testutil;

import edu.berkeley.boinc.BaseParser;
import android.util.Log;
import android.util.Xml;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


public class RpcRequestParser extends BaseParser {
    private static final String TAG = "RpcRequestParser";

    private boolean mInRequest = false;
    private boolean mRequestIdentified = false;
    private String mRequest = "";

    // Disable direct instantiation of this class
    private RpcRequestParser() {}

    public static String parse(String rpcRequest) {
        try {
            RpcRequestParser parser = new RpcRequestParser();
            Xml.parse(rpcRequest, parser);
            return parser.mRequest;
        }
        catch (SAXException e) {
            Log.d(TAG, "Malformed XML:\n" + rpcRequest);
            return "";
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (localName.equalsIgnoreCase("boinc_gui_rpc_request")) {
            mInRequest = true;
        }
        else if (mInRequest && !mRequestIdentified) {
            String tag = localName.toLowerCase();
            switch (tag) {
                case "auth1":
                case "auth2":
                case "exchange_versions":
                case "get_cc_status":
                case "get_file_transfers":
                case "get_host_info":
                case "get_message_count":
                case "get_messages":
                case "get_project_status":
                case "get_results":
                case "get_state":
                case "network_available":
                case "project_update":
                case "project_suspend":
                case "project_resume":
                case "project_nomorework":
                case "project_allowmorework":
                case "suspend_result":
                case "resume_result":
                case "abort_result":
                case "quit":
                case "run_benchmarks":
                case "set_gpu_mode":
                case "set_network_mode":
                case "set_run_mode":
                case "retry_file_transfer":
                case "abort_file_transfer":
                    mRequest = tag;
                    mRequestIdentified = true;
                    break;
                default:
                    // Must be some other tag...
                    // Just ignore
                    break;
            }
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
        if (mRequestIdentified && mRequest.equals("auth2")) {
            // we are inside <auth2>
            if (localName.equalsIgnoreCase("nonce_hash")) {
                // Closing tag of <nonce_hash>
                mRequest += ":" + mCurrentElement.toString();
            }
        }
        mElementStarted = false; // to be clean for next one
    }
}
