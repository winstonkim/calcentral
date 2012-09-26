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

import com.google.common.collect.ImmutableMap;
import edu.berkeley.calcentral.DatabaseAwareTest;
import edu.berkeley.calcentral.Urls;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;

import javax.ws.rs.core.Response;
import java.util.Map;

public class CanvasProxyTest extends DatabaseAwareTest {

	private static final Logger LOGGER = Logger.getLogger(CanvasProxyTest.class);

	@Autowired
	private CanvasProxy proxy;

	@Test
	public void getCourses() throws Exception {
		try {
			Response get = proxy.doAdminMethod(HttpMethod.GET, "courses");
			JSONArray json = new JSONArray(get.getEntity().toString());
			LOGGER.info(json.toString(2));
		} catch (RestClientException e) {
			LOGGER.error("Got a RestClientException, is canvas server properly configured or unavailable?", e);
		}
	}

	@Test
	public void getSpecificCourse() throws Exception {
		try {
			Response allCourses = proxy.doAdminMethod(HttpMethod.GET, Urls.CANVAS_ACCOUNT_PATH + "/courses");
			JSONArray json = new JSONArray(allCourses.getEntity().toString());
			LOGGER.info(json.toString(2));
			JSONObject first = json.getJSONObject(0);
			int id = first.getInt("id");
			Response firstResponse = proxy.doAdminMethod(HttpMethod.GET, "courses/" + id);
			LOGGER.info(firstResponse);
			assertNotNull(firstResponse);
		} catch (RestClientException e) {
			LOGGER.error("Got a RestClientException, is canvas server properly configured or unavailable?", e);
		}
	}

	@Test
	public void accountSavvy() throws Exception {
		try {
			Response response = proxy.doAdminMethod(HttpMethod.GET, Urls.CANVAS_ACCOUNT_PATH + "/users");
			JSONArray json = new JSONArray(response.getEntity().toString());
			assertNotNull(json);
		} catch (RestClientException e) {
			LOGGER.error("Got a RestClientException, is canvas server properly configured or unavailable?", e);
		}
	}

	@Test
	public void serverSideCRUD() throws Exception {
		String groupName = "testgroup-" + randomString();
		try {
			Map<String, Object> params = ImmutableMap.<String, Object>of(
					"name", groupName,
					"is_public", "false"
			);
			// Create a temporary group.
			JSONObject json = new JSONObject(proxy.doAdminMethod(HttpMethod.POST, "groups", params).getEntity().toString());
			final String groupPath = "groups/" + json.getString("id");
			assertEquals(groupName, json.getString("name"));
			// Edit it.
			groupName = "revised " + groupName;
			params = ImmutableMap.<String, Object>of("name", groupName);
			proxy.doAdminMethod(HttpMethod.PUT, groupPath, params);
			json = new JSONObject(proxy.doAdminMethod(HttpMethod.GET, groupPath).getEntity().toString());
			assertEquals(groupName, json.getString("name"));
			// Delete it.
			proxy.doAdminMethod(HttpMethod.DELETE, groupPath);

			Response response = proxy.doAdminMethod(HttpMethod.GET, groupPath);
			assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

		} catch (RestClientException e) {
			LOGGER.error("Got a RestClientException, is canvas server properly configured or unavailable?", e);
		}
	}
}