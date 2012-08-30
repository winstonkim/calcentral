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
import edu.berkeley.calcentral.domain.CalCentralUser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@SuppressWarnings("unchecked")
public class UserServiceTest extends DatabaseAwareTest {

	private static final Log LOGGER = LogFactory.getLog(UserServiceTest.class);

	@Autowired
	private UserService userService;

	@Test
	public void getUser() throws Exception {
		Map<String, Object> userMap = userService.getUser("2040");
		LOGGER.info(userMap);
		Map<String, Object> campusData = (Map<String, Object>)userMap.get("campusData");
		assertNotNull(campusData);
		CalCentralUser user = (CalCentralUser) userMap.get("user");
		assertNotNull(user);
		assertNull(userMap.get("widgetData"));
	}

	@Test
	public void getNonexistent() throws Exception {
		Map<String, Object> userMap = userService.getUser("00000000");
		LOGGER.info(userMap);
		assertNull(userMap.get("user"));
		assertNull(userMap.get("widgetData"));
	}

	@Test
	public void saveUserData() throws Exception {
		// TODO write test
	}

	@Test
	public void deleteUserAndWidgetData() throws Exception {
		// TODO write test
	}
}
