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

package edu.berkeley.calcentral.daos;

import edu.berkeley.calcentral.DatabaseAwareTest;
import edu.berkeley.calcentral.domain.CalCentralUser;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UserDataDaoTest extends DatabaseAwareTest {

	@Autowired
	private UserDataDao dao;

	@Test
	public void testGet() throws Exception {
		CalCentralUser user = dao.get("2040");
		assertNotNull(user);
		assertEquals("Oliver", user.getFirstName());
		assertNull(dao.get("nonexistent_user"));
	}

	@Test
	public void testUpdate() throws Exception {
		//TODO: fill me in
	}

	@Test
	public void testDelete() throws Exception {
		//TODO: fill me in
	}
}
