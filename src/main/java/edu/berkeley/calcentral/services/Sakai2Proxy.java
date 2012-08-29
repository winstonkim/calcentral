/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package edu.berkeley.calcentral.services;


import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.util.Signature;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.IOException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
@Path(Urls.BSPACE_FAVORITES)
public class Sakai2Proxy {

	private static final Logger logger = Logger.getLogger(Sakai2Proxy.class);

	private static final String SECURE_TOKEN_HEADER_NAME = "x-sakai-token";

	private static final String TOKEN_SEPARATOR = ";";

	@Autowired
	private Properties calcentralProperties;

	String sharedSecret;

	@PostConstruct
	public void init() {
		this.sharedSecret = calcentralProperties.getProperty("sakai2Proxy.sharedSecret");
	}

	@GET
	public Map<String, Object> get() throws IOException {
		logger.debug("Shared secret = " + sharedSecret);
		HttpClient httpClient = new HttpClient();
		httpClient.setState(new HttpState());
		HttpClientParams params = new HttpClientParams();
		params.setSoTimeout(5000);
		httpClient.setParams(params);
		HostConfiguration hostConfiguration = new HostConfiguration();
		hostConfiguration.setHost("sakai-dev.berkeley.edu", 443, Protocol.getProtocol("https"));

		String user = "2040"; // TODO replace with request.getRemoteUser()
		String hmac;
		final String message = user + TOKEN_SEPARATOR + System.currentTimeMillis();
		try {
			hmac = Signature.calculateRFC2104HMAC(message, sharedSecret);
		} catch (SignatureException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new Error(e);
		}

		GetMethod get = new GetMethod("/sakai-hybrid/sites?categorized=true");
		get.setRequestHeader(SECURE_TOKEN_HEADER_NAME, hmac + TOKEN_SEPARATOR + message);
		httpClient.executeMethod(hostConfiguration, get);

		if (logger.isDebugEnabled()) {
			logger.debug("Proxy GET of " + hostConfiguration.getHostURL() + get.getURI() + " returned " + get.getStatusCode() + " " + get.getStatusText());
			for ( Header header : get.getRequestHeaders() ) {
				logger.trace("Request header: " + header.getName() + "=" + header.getValue());
			}
			for ( Header header : get.getResponseHeaders() ) {
				logger.trace("Response header: " + header.getName() + "=" + header.getValue());
			}
			logger.trace("Response body: " + get.getResponseBodyAsString());
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("body", get.getResponseBodyAsString());
		return result;
	}

}
