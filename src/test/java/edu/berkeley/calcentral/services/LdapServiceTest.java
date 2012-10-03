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
import edu.berkeley.calcentral.domain.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class LdapServiceTest extends DatabaseAwareTest {

	private static final Log LOGGER = LogFactory.getLog(LdapServiceTest.class);

	@Autowired
	private LdapService ldapService;


	@Test
	public void getUserWithLdapData() throws Exception {
		User user = new User();
		user.setUid("323487");
		user.setPreferredName("Jon Hays, MA");
		try {
			ldapService.mergeLdapData(user);
			assertNotNull(user);
			assertEquals("Jon Hays, MA", user.getPreferredName());
			assertNotNull(user.getAddress());
		} catch (IOException e) {
			LOGGER.error("LDAP issues: " + e.getMessage());
		}
	}

	@Test
	public void getTestUserWithoutLDAPData() throws Exception {
		User user = new User();
		user.setUid("300846");
		user.setPreferredName("Stu Test-300846");
		try {
			ldapService.mergeLdapData(user);
			assertNotNull(user);
			assertEquals("Stu Test-300846", user.getPreferredName());
			assertNull(user.getAddress());
		} catch (IOException e) {
			LOGGER.error("LDAP issues: " + e.getMessage());
		}
	}

	@Test
	public void getBogusUser() throws Exception {
		User user = new User();
		try {
			ldapService.mergeLdapData(user);
			assertNull(user.getUid());
			assertNull(user.getAddress());
		} catch (IOException e) {
			LOGGER.error("LDAP issues: " + e.getMessage());
			return;
		}
	}
}
