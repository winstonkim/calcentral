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
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
@Path(Urls.BSPACE_FAVORITES)
public class Sakai2Proxy {

	private static final Logger LOGGER = Logger.getLogger(Sakai2Proxy.class);

	private static final String SECURE_TOKEN_HEADER_NAME = "x-sakai-token";

	private static final String TOKEN_SEPARATOR = ";";

	@Autowired
	private Properties calcentralProperties;

	@Autowired
	private HttpConnectionManager connectionManager;

	String sharedSecret;

	String sakai2Host;

	@PostConstruct
	public void init() {
		this.sharedSecret = calcentralProperties.getProperty("sakai2Proxy.sharedSecret");
		this.sakai2Host = calcentralProperties.getProperty("sakai2Proxy.sakai2Host");
	}

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Map<String, Object> get(@Context HttpServletRequest request) {
		return get(request.getRemoteUser());
	}

	public Map<String, Object> get(String username) {
		Map<String, Object> result = new HashMap<String, Object>();

		GetMethod get = new GetMethod("/sakai-hybrid/sites?categorized=true");
		setSakaiToken(get, username);

		try {
			HttpClient httpClient = getHttpClient();
			LOGGER.info("Attempting Proxy GET of " + httpClient.getHostConfiguration().getHost() + get.getURI() + " on behalf of user "
					+ username + ", with x-sakai-token = " + get.getRequestHeader(SECURE_TOKEN_HEADER_NAME));
			httpClient.executeMethod(get);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Proxy GET of " + httpClient.getHostConfiguration().getHostURL() + get.getURI() + " returned "
						+ get.getStatusCode() + " " + get.getStatusText());
				for (Header header : get.getRequestHeaders()) {
					LOGGER.trace("Request header: " + header.getName() + "=" + header.getValue());
				}
				for (Header header : get.getResponseHeaders()) {
					LOGGER.trace("Response header: " + header.getName() + "=" + header.getValue());
				}
				LOGGER.trace("Response body: " + get.getResponseBodyAsString());
			}
			result.put("body", get.getResponseBodyAsString());
			result.put("statusCode", get.getStatusCode());
			result.put("statusText", get.getStatusText());
		} catch (IOException ioe) {
			result.put("body", "");
			result.put("statusCode", 503);
			result.put("statusText", "Server unreachable");
			LOGGER.error("Sakai2 Proxy server unreachable due to IOException. Message: " + ioe.getMessage());
		}
		return result;
	}

	private void setSakaiToken(GetMethod get, String username) {
		String hmac;
		final String message = username + TOKEN_SEPARATOR + System.currentTimeMillis();
		try {
			LOGGER.debug("Shared secret = " + sharedSecret);
			hmac = Signature.calculateRFC2104HMAC(message, sharedSecret);
		} catch (SignatureException e) {
			LOGGER.error(e.getLocalizedMessage(), e);
			throw new Error(e);
		}
		get.setRequestHeader(SECURE_TOKEN_HEADER_NAME, hmac + TOKEN_SEPARATOR + message);
	}

	private HttpClient getHttpClient() {
		HostConfiguration hostConfiguration = new HostConfiguration();
		hostConfiguration.setHost(sakai2Host, 443, Protocol.getProtocol("https"));
		HttpClientParams params = new HttpClientParams();
		params.setSoTimeout(3000);
		HttpClient httpClient = new HttpClient(params, connectionManager);
		httpClient.setHostConfiguration(hostConfiguration);
		return httpClient;
	}

}
