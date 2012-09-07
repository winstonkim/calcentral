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
import org.codehaus.jettison.json.JSONException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;

public class Sakai2ProxyIT extends DatabaseAwareTest {

	private static final Log LOGGER = LogFactory.getLog(WidgetDataServiceTest.class);

	@Autowired
	private Sakai2Proxy proxy;

	@Test
	public void get() throws IOException, JSONException {
		Map<String, Object> response = proxy.get(randomString(), "/sakai-hybrid/sites?categorized=true");
		assertNotNull(response);
		assertNotNull(response.get("body"));
		assertNotNull(response.get("statusCode"));
		assertNotNull(response.get("statusText"));
	}

	@Test
	public void getUnread() throws IOException, JSONException {
		Map<String, Object> response = proxy.get(randomString(), "/sakai-hybrid/sites?unread=true");
		assertNotNull(response);
		assertNotNull(response.get("body"));
		assertNotNull(response.get("statusCode"));
		assertNotNull(response.get("statusText"));
	}

}
