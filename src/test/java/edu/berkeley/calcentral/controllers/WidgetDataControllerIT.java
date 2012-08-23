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

import com.google.common.collect.ImmutableMap;
import edu.berkeley.calcentral.IntegrationTest;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class WidgetDataControllerIT extends IntegrationTest {

	private static final Logger LOGGER = Logger.getLogger(WidgetDataControllerIT.class);

	String user;

	@Before
	public void setup() {
		super.setup();
		user = "jane" + randomness();
	}

	@Test
	public void getWithNoContent() throws IOException {
		GetMethod get = doGet("/api/user/" + user + "/widgetData");
		assertEquals(HttpResponseCodes.SC_NO_CONTENT, get.getStatusCode());
		get = doGet("/api/user/" + user + "/widgetData/abc");
		assertEquals(HttpResponseCodes.SC_NO_CONTENT, get.getStatusCode());
	}

	@Test
	public void getWithContent() throws IOException, JSONException {
		PostMethod post = doPost("/api/user/" + user + "/widgetData/abc",
				ImmutableMap.<String, String>of("data", "{foo:bar}"));
		assertEquals(HttpResponseCodes.SC_OK, post.getStatusCode());

		GetMethod get = doGet("/api/user/" + user + "/widgetData");
		assertEquals(HttpResponseCodes.SC_OK, get.getStatusCode());
		LOGGER.info(get.getResponseBodyAsString());
		JSONArray json = new JSONArray(get.getResponseBodyAsString());
		assertEquals(1, json.length());
		JSONObject widget = json.getJSONObject(0).getJSONObject("widgetData");
		assertEquals(user, widget.get("uid"));
		assertEquals("abc", widget.get("widgetID"));
		assertEquals("{foo:bar}", widget.get("data"));
	}

  @Test
  public void getRevisedContent() throws IOException, JSONException {
    PostMethod post = doPost("/api/user/" + user + "/widgetData/abc",
        ImmutableMap.<String, String>of("data", "{foo:initialvalue}"));
    assertEquals(HttpResponseCodes.SC_OK, post.getStatusCode());
    post = doPost("/api/user/" + user + "/widgetData/abc",
        ImmutableMap.<String, String>of("data", "{foo:newvalue}"));
    assertEquals(HttpResponseCodes.SC_OK, post.getStatusCode());

    GetMethod get = doGet("/api/user/" + user + "/widgetData");
    assertEquals(HttpResponseCodes.SC_OK, get.getStatusCode());
    LOGGER.info(get.getResponseBodyAsString());
    JSONArray json = new JSONArray(get.getResponseBodyAsString());
    assertEquals(1, json.length());
    JSONObject widget = json.getJSONObject(0).getJSONObject("widgetData");
    assertEquals(user, widget.get("uid"));
    assertEquals("abc", widget.get("widgetID"));
    assertEquals("{foo:newvalue}", widget.get("data"));
  }

  @Test
	public void delete() throws IOException, JSONException {
		PostMethod post = doPost("/api/user/" + user + "/widgetData/abc", null);
		assertEquals(HttpResponseCodes.SC_OK, post.getStatusCode());

		GetMethod get = doGet("/api/user/" + user + "/widgetData/abc");
		assertEquals(HttpResponseCodes.SC_OK, get.getStatusCode());

		DeleteMethod delete = doDelete("/api/user/" + user + "/widgetData/abc");
		assertEquals(HttpResponseCodes.SC_NO_CONTENT, delete.getStatusCode());

		get = doGet("/api/user/" + user + "/widgetData/abc");
		assertEquals(HttpResponseCodes.SC_NO_CONTENT, get.getStatusCode());
	}
}
