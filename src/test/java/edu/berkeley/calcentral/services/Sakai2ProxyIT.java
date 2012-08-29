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

import edu.berkeley.calcentral.IntegrationTest;
import edu.berkeley.calcentral.Urls;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.junit.Test;

import java.io.IOException;

public class Sakai2ProxyIT extends IntegrationTest {

	@Override
	protected void setup() {
		// no-op
	}

	@Test
	public void get() throws IOException, JSONException {
		GetMethod get = doGet(Urls.BSPACE_FAVORITES);
		assertResponse(HttpResponseCodes.SC_OK, get);
		JSONObject json = toJSONObject(get);
		logger.debug(json.toString(2));
		assertNotNull(json.get("body"));
		assertNotNull(json.get("statusCode"));
		assertNotNull(json.get("statusText"));
	}

	@Test
	public void getUnread() throws IOException, JSONException {
		GetMethod get = doGet(Urls.BSPACE_FAVORITES_UNREAD);
		assertResponse(HttpResponseCodes.SC_OK, get);
		JSONObject json = toJSONObject(get);
		logger.debug(json.toString(2));
		assertNotNull(json.get("body"));
		assertNotNull(json.get("statusCode"));
		assertNotNull(json.get("statusText"));
	}

}
