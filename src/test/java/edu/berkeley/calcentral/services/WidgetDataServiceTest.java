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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jettison.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class WidgetDataServiceTest extends DatabaseAwareTest {

	private static final Log LOGGER = LogFactory.getLog(WidgetDataServiceTest.class);

	private String user;

	@Autowired
	private WidgetDataService service;

	@Before
	public void setup() {
		user = "jane" + randomness();
	}

	@Test
	public void getWithNoContent() throws IOException {
		assertNull(service.getAllForUser(user));
		assertNull(service.get(user, "abc"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getWithContent() throws IOException, JSONException {
		assertNotNull(service.save(user, "abc", "{\"foo\":\"bar\"}"));
		List<Map<String, Object>> allData =  service.getAllForUser(user);

		assertNotNull(allData);
		assertEquals(1, allData.size());
		Map<String, Object> first = allData.get(0);
		Map<String, Object> widgetData = ((Map<String, Object>)first.get("widgetData"));
		LOGGER.info("widgetData = " + widgetData);
		ObjectNode data = ((ObjectNode)(widgetData.get("data")));
		LOGGER.info("data = " + data);
		assertEquals(user, widgetData.get("uid"));
		assertEquals("abc", widgetData.get("widgetID"));
		assertEquals("bar", data.get("foo").getTextValue());
	}

	@Test
	public void getMalformedJSONContent() throws IOException, JSONException {
		assertNull(service.save(user, "bad", "bad JSON"));
	}
	
	@Test
	public void delete() throws IOException, JSONException {
		assertNotNull(service.save(user, "abc", null));
		assertNotNull(service.get(user, "abc"));
		service.delete(user, "abc");
		assertNull(service.get(user, "abc"));
	}
}
