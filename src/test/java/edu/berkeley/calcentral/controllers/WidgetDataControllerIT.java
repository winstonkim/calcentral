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

package edu.berkeley.calcentral.controllers;

import java.io.IOException;

import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.junit.Ignore;

import com.google.common.collect.ImmutableMap;

import edu.berkeley.calcentral.IntegrationTest;

public class WidgetDataControllerIT extends IntegrationTest {

	String user;

	@Override
	public void setup() {
		user = "jane" + randomness();
	}

	@Ignore
	public void getWithNoContent() throws IOException {
		GetMethod get = doGet("/api/user/" + user + "/widgetData");
		assertResponse(HttpResponseCodes.SC_NO_CONTENT, get);
		get = doGet("/api/user/" + user + "/widgetData/abc");
		assertResponse(HttpResponseCodes.SC_NO_CONTENT, get);
	}

	@Ignore
	public void getWithContent() throws IOException, JSONException {
		PostMethod post = doPost("/api/user/" + user + "/widgetData/abc",
				ImmutableMap.<String, String>of("data", "{foo:bar}"));
		assertResponse(HttpResponseCodes.SC_OK, post);

		GetMethod get = doGet("/api/user/" + user + "/widgetData");
		assertResponse(HttpResponseCodes.SC_OK, get);
		logger.info(get.getResponseBodyAsString());
		JSONArray json = toJSONArray(get);
		assertEquals(1, json.length());
		JSONObject widget = json.getJSONObject(0).getJSONObject("widgetData");
		assertEquals(user, widget.get("uid"));
		assertEquals("abc", widget.get("widgetID"));
		assertEquals("{foo:bar}", widget.get("data"));
	}

	@Ignore
	public void getRevisedContent() throws IOException, JSONException {
		PostMethod post = doPost("/api/user/" + user + "/widgetData/abc",
				ImmutableMap.<String, String>of("data", "{foo:initialvalue}"));
		assertResponse(HttpResponseCodes.SC_OK, post);
		post = doPost("/api/user/" + user + "/widgetData/abc",
				ImmutableMap.<String, String>of("data", "{foo:newvalue}"));
		assertResponse(HttpResponseCodes.SC_OK, post);

		GetMethod get = doGet("/api/user/" + user + "/widgetData");
		assertResponse(HttpResponseCodes.SC_OK, get);
		logger.info(get.getResponseBodyAsString());
		JSONArray json = toJSONArray(get);
		assertEquals(1, json.length());
		JSONObject widget = json.getJSONObject(0).getJSONObject("widgetData");
		assertEquals(user, widget.get("uid"));
		assertEquals("abc", widget.get("widgetID"));
		assertEquals("{foo:newvalue}", widget.get("data"));
	}

	@Ignore
	public void delete() throws IOException, JSONException {
		PostMethod post = doPost("/api/user/" + user + "/widgetData/abc", null);
		assertResponse(HttpResponseCodes.SC_OK, post);

		GetMethod get = doGet("/api/user/" + user + "/widgetData/abc");
		assertResponse(HttpResponseCodes.SC_OK, get);

		DeleteMethod delete = doDelete("/api/user/" + user + "/widgetData/abc");
		assertResponse(HttpResponseCodes.SC_NO_CONTENT, delete);

		get = doGet("/api/user/" + user + "/widgetData/abc");
		assertResponse(HttpResponseCodes.SC_NO_CONTENT, get);
	}
}
