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

import edu.berkeley.calcentral.DatabaseAwareTest;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClientException;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class CanvasProxyTest extends DatabaseAwareTest {

	private static final Logger LOGGER = Logger.getLogger(CanvasProxyTest.class);

	@Autowired
	private CanvasProxy proxy;

	@Test
	public void getCourses() throws Exception {
		try {
			String get = proxy.get("courses");
			JSONArray json = new JSONArray(get);
			LOGGER.info(json.toString(2));
		} catch (RestClientException e) {
			LOGGER.error("Got a RestClientException, is canvas server properly configured or unavailable?", e);
		}
	}

	@Test
	public void getSpecificCourse() throws Exception {
		try {
			String allCourses = proxy.get("courses");
			JSONArray json = new JSONArray(allCourses);
			LOGGER.info(json.toString(2));
			JSONObject first = json.getJSONObject(0);
			int id = first.getInt("id");
			String firstResponse = proxy.get("courses/" + id);
			LOGGER.info(firstResponse);
			assertNotNull(firstResponse);
		} catch (RestClientException e) {
			LOGGER.error("Got a RestClientException, is canvas server properly configured or unavailable?", e);
		}
	}

	@Test
	public void put() throws Exception {
		try {
			String name = "Edited by test " + randomString();
			HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
			Vector<String> paramNames = new Vector<String>();
			paramNames.add("course[name]");
			Mockito.when(request.getParameterNames()).thenReturn(paramNames.elements());
			Mockito.when(request.getParameter("course[name]")).thenReturn(name);
			String response = proxy.put("/courses/767330", request);

			JSONObject json = new JSONObject(response);
			LOGGER.info(json.toString(2));
			assertEquals(name, json.getString("name"));
		} catch (RestClientException e) {
			LOGGER.error("Got a RestClientException, is canvas server properly configured or unavailable?", e);
		}
	}
}