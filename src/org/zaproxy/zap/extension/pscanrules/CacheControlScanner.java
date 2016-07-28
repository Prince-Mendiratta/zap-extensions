/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.pscanrules;

import java.util.Vector;

import net.htmlparser.jericho.Source;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpStatusCode;
import org.zaproxy.zap.extension.pscan.PassiveScanThread;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;

public class CacheControlScanner extends PluginPassiveScanner {

	/**
	 * Prefix for internationalised messages used by this rule
	 */
	private static final String MESSAGE_PREFIX = "pscanrules.cachecontrolscanner.";
	
	private PassiveScanThread parent = null;
	
	@Override
	public void setParent(PassiveScanThread parent) {
		this.parent = parent;
		
	}
	
	@Override
	public void scanHttpRequestSend(HttpMessage msg, int id) {

	}

	@Override
	public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
		if (msg.getRequestHeader().isSecure() && msg.getResponseBody().length() > 0) {
			
			if (AlertThreshold.LOW.equals(this.getLevel())) {
				if (msg.getResponseHeader().isImage()) {
					return;
				}
				if (msg.getRequestHeader().getURI().toString().toLowerCase().endsWith(".css")) {
					return;
				}
			} else {
				// MEDIUM or HIGH thresholds
				if (HttpStatusCode.isRedirection(msg.getResponseHeader().getStatusCode()) ||
						HttpStatusCode.isClientError(msg.getResponseHeader().getStatusCode()) ||
						HttpStatusCode.isServerError(msg.getResponseHeader().getStatusCode())) {
					return;
				} else if (! msg.getResponseHeader().isText() ||
						msg.getResponseHeader().isJavaScript()) {
					// Covers HTML, XML, JSON and TEXT while excluding JS
					return;
				}
			}
			
			Vector<String> cacheControlVect = msg.getResponseHeader().getHeaders(HttpHeader.CACHE_CONTROL);
			String cacheControlHeaders = (cacheControlVect != null) ? cacheControlVect.toString().toLowerCase() : "";

			if (cacheControlHeaders.isEmpty() || //No Cache-Control header at all 
					cacheControlHeaders.indexOf("no-store") < 0 || 
					cacheControlHeaders.indexOf("no-cache") < 0 || 
					cacheControlHeaders.indexOf("must-revalidate") < 0 ||
					cacheControlHeaders.indexOf("private") < 0) {
				this.raiseAlert(msg, id, HttpHeader.CACHE_CONTROL, cacheControlHeaders);
			}
			
			Vector<String> pragma = msg.getResponseHeader().getHeaders(HttpHeader.PRAGMA);
			if (pragma != null) {
				for (String pragmaDirective : pragma) {
					if (pragmaDirective.toLowerCase().indexOf("no-cache") < 0){
						this.raiseAlert(msg, id, HttpHeader.PRAGMA, pragmaDirective);
					}
				}
			}
		}
	}
	
	private void raiseAlert(HttpMessage msg, int id, String header, String evidence) {
		if (evidence.startsWith("[") && evidence.endsWith("]")) {
			// Due to casting a Vector to a string
			// Strip so that if a single headers used the highlighting will work
			evidence = evidence.substring(1, evidence.length() - 1);
		}
	    Alert alert = new Alert(getPluginId(), Alert.RISK_LOW, Alert.CONFIDENCE_MEDIUM, 
		    	getName());
		    	alert.setDetail(
		    	    getDescription(), 
		    	    msg.getRequestHeader().getURI().toString(),
		    	    header,
		    	    "", "", 
		    	    getSolution(), 
		            getReference(), 
		            evidence,
		            525, //CWE
		            13,	// WASC-13: Information Leakage
		            msg);
	
    	parent.raiseAlert(id, alert);
	}
	
	@Override
	public int getPluginId() {
		return 10015;
	}
	
	@Override
	public String getName() {
		return Constant.messages.getString(MESSAGE_PREFIX + "name");
	}
	
    private String getDescription() {
        return Constant.messages.getString(MESSAGE_PREFIX + "desc");
    }

    private String getSolution() {
        return Constant.messages.getString(MESSAGE_PREFIX + "soln");
    }

    private String getReference() {
    	return Constant.messages.getString(MESSAGE_PREFIX + "refs");
    }

}
