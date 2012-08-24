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

package edu.berkeley.calcentral;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

public abstract class IntegrationTest extends Assert {

	private Logger logger; 
	
	private final HttpClient httpClient = new HttpClient();

	protected void setup(Logger logger) {
		HttpState state = new HttpState();
		httpClient.setState(state);
		this.logger = logger;
	}

	protected GetMethod doGet(String url) throws IOException {
		// TODO figure out how to configure localhost and port
		GetMethod getMethod = new GetMethod("http://localhost:8080" + url);
		httpClient.executeMethod(getMethod);
		logger.info("HTTP GET of " + getMethod.getPath() + "; statusCode = " + getMethod.getStatusCode());
		return getMethod;
	}

	protected PostMethod doPost(String url, Map<String, String> params) throws IOException {
		PostMethod postMethod = new PostMethod("http://localhost:8080" + url);
		if (params != null) {
			for (String key : params.keySet()) {
				postMethod.setParameter(key, params.get(key));
			}
		}
		httpClient.executeMethod(postMethod);
		logger.info("HTTP POST of " + postMethod.getPath() + "; statusCode = " + postMethod.getStatusCode()
				+ "; params = " + postMethod.getParams().toString());
		return postMethod;
	}

	protected DeleteMethod doDelete(String url) throws IOException {
		DeleteMethod deleteMethod = new DeleteMethod("http://localhost:8080" + url);
		httpClient.executeMethod(deleteMethod);
		logger.info("HTTP DELETE of " + deleteMethod.getPath() + "; statusCode = " + deleteMethod.getStatusCode());
		return deleteMethod;
	}

	protected long randomness() {
		return new Random().nextLong();
	}

	protected void assertResponse(int expectedStatus, HttpMethod method) throws IOException {
		if (method.getStatusCode() != expectedStatus) {
			logger.error("HTTP assertion failed. Response body: " + method.getResponseBodyAsString());
			fail("Expected statusCode of " + expectedStatus + " but got " + method.getStatusCode()
					+ " " + method.getStatusText());
		}
	}

	protected JSONArray toJSONArray(GetMethod get) throws IOException {
		try {
			return new JSONArray(get.getResponseBodyAsString());
		} catch (JSONException e) {
			logger.error("Could not convert body to JSONArray. Body text: " + get.getResponseBodyAsString(), e);
			fail(e.getMessage());
		}
		return null;
	}

	protected JSONObject toJSONObject(GetMethod get) throws IOException {
		try {
			return new JSONObject(get.getResponseBodyAsString());
		} catch (JSONException e) {
			logger.error("Could not convert body to JSONObject. Body text: " + get.getResponseBodyAsString(), e);
			fail(e.getMessage());
		}
		return null;
	}

}
