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
import edu.berkeley.calcentral.daos.OAuth2Dao;
import edu.berkeley.calcentral.domain.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONObject;
import org.jboss.resteasy.spi.NotFoundException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

@SuppressWarnings("unchecked")
public class UserServiceTest extends DatabaseAwareTest {

	private static final Log LOGGER = LogFactory.getLog(UserServiceTest.class);

	@Autowired
	private UserService userService;

	@Autowired
	private WidgetDataService widgetDataService;

	@Autowired
	private OAuth2Dao oAuth2Dao;

	@Autowired
	private CampusPersonDataService campusPersonDataService;

	@Test
	public void getUser() throws Exception {
		Map<String, Object> userMap;
		try {
			userMap = userService.getUserRelatedData("2040");
		} catch (NotFoundException e) {
			userService.loadUserByUsername("2040");
			userMap = userService.getUserRelatedData("2040");
		}
		LOGGER.info(userMap);

		User user = (User) userMap.get("user");
		assertNotNull(user);
		assertEquals("Oliver Heyer", user.getPreferredName());
		assertNotNull(user.getFirstLogin());
		assertNull(userMap.get("widgetData"));
	}

	@Test
	public void getUserNotRepresentedInCampusData() throws Exception {
		String uid = randomString();
		userService.loadUserByUsername(uid);
		Map<String, Object> userMap = userService.getUserRelatedData(uid);
		User user = (User) userMap.get("user");
		assertEquals(uid, user.getPreferredName());
	}

	@Test
	public void saveUserData() throws Exception {
		String uid = randomString();
		userService.loadUserByUsername(uid);
		User originalUser = (User) userService.getUserRelatedData(uid).get("user");
		LOGGER.info(originalUser);
		assertNotNull(originalUser.getPreferredName());

		JSONObject json = new JSONObject();
		json.put("preferredName", "Joe Blow");
		Map<String, Object> savedUserMap = userService.saveUserData(uid, json.toString());
		LOGGER.info(savedUserMap);
		assertNotNull(savedUserMap);
		User savedUser = (User) savedUserMap.get("user");
		assertEquals("Joe Blow", savedUser.getPreferredName());
	}

	@Test
	public void deleteUserAndWidgetData() throws Exception {
		String uid = randomString();
		userService.loadUserByUsername(uid);
		assertNotNull(userService.getUserRelatedData(uid));
		widgetDataService.save(uid, "abc", "{\"data\":\"foo\"}");
		assertEquals(1, widgetDataService.getAllForUser(uid).size());

		userService.deleteUserAndWidgetData(uid);
		boolean found = true;
		try {
			userService.getUserRelatedData(uid);
		} catch ( NotFoundException expected) {
			found = false;
		}
		assertFalse(found);
		assertNull(widgetDataService.getAllForUser(uid));
		assertNull(widgetDataService.get(uid, "abc"));
	}

	@Test
	public void testLoadUserByUsername() throws Exception {
		UserDetails details = this.userService.loadUserByUsername(randomString());
		assertNotNull(details);
		assertNotNull(details.getUsername());
	}

	@Test
	public void canvasOAuthData() throws Exception {
		String uid = randomString();
		userService.loadUserByUsername(uid);
		Map<String, Object> userMap = userService.getUserRelatedData(uid);
		Map<String, Object> oAuthData = (Map<String, Object>) userMap.get("oauth");
		assertFalse((Boolean) oAuthData.get(CanvasProxy.CANVAS_APP_ID));

		oAuth2Dao.insert(uid, CanvasProxy.CANVAS_APP_ID, "frippery", null, 0);
		userMap = userService.getUserRelatedData(uid);
		oAuthData = (Map<String, Object>) userMap.get("oauth");
		assertTrue((Boolean) oAuthData.get(CanvasProxy.CANVAS_APP_ID));
		userService.deleteUserAndWidgetData(uid);
		assertNull(oAuth2Dao.getToken(uid, CanvasProxy.CANVAS_APP_ID));
	}

	@Test
	public void passThroughUserData() throws Exception {
		String uid = "300945";
		try {
			userService.getUserRelatedData(uid);
			fail("Expected not to find user " + uid);
		} catch (NotFoundException expected) {
		}
		Map<String, Object> campusAttributes = campusPersonDataService.getPersonAttributes(uid);
		assertNotNull(campusAttributes.get("PERSON_NAME"));
		User user = userService.getUser(uid);
		assertEquals(uid, user.getUid());
		assertEquals(campusAttributes.get("PERSON_NAME"), user.getPreferredName());
		try {
			userService.getUserRelatedData(uid);
			fail("Expected not to find user " + uid);
		} catch (NotFoundException expected) {
		}
		uid = randomString();
		user = userService.getUser(uid);
		assertEquals(uid, user.getUid());
	}

}
