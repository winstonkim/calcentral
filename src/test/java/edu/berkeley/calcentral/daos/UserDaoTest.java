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
import edu.berkeley.calcentral.domain.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

public class UserDaoTest extends DatabaseAwareTest {

	private static final Log LOGGER = LogFactory.getLog(UserDaoTest.class);

	@Autowired
	private UserDao dao;

	@Test
	public void get() throws Exception {
		try {
			dao.get("2040");
		} catch (EmptyResultDataAccessException e) {
			// once authorized, user is created
			dao.insert("2040");
		}

		// now he should exist
		User user = dao.get("2040");
		LOGGER.info(user);
		assertEquals(null, user.getPreferredName());
	}

	@Test
	public void update() throws Exception {
		String uid = randomString();
		dao.insert(uid);
		User user = new User();
		user.setUid(uid);
		user.setLink("foo.com");
		user.setPreferredName("Joe Blow");
		dao.update(user);
		User updatedUser = dao.get(uid);
		assertEquals("Joe Blow", updatedUser.getPreferredName());
		assertEquals("foo.com", updatedUser.getLink());
	}

	@Test
	public void updatePartial() throws Exception {
		String uid = randomString();
		dao.insert(uid);
		User user = new User();
		user.setUid(uid);
		user.setLink("foo.com");
		dao.update(user);
		User updatedUser = dao.get(uid);
		assertNull(updatedUser.getPreferredName());
		assertEquals("foo.com", updatedUser.getLink());
		user.setLink(null);
		user.setPreferredName("Joe Blow");
		dao.update(user);
		updatedUser = dao.get(uid);
		assertEquals("foo.com", updatedUser.getLink());
		assertEquals("Joe Blow", updatedUser.getPreferredName());
	}

	@Test(expected = EmptyResultDataAccessException.class)
	public void delete() throws Exception {
		String uid = randomString();
		dao.insert(uid);
		assertNotNull(dao.get(uid));
		dao.delete(uid);
		dao.get(uid);
	}

	@Test(expected = EmptyResultDataAccessException.class)
	public void getNonExistent() throws Exception {
		dao.get("0000000");
	}

	@Test
	public void insertUser() throws Exception {
		String uid = randomString();
		dao.insert(uid);
		User user = dao.get(uid);
		assertNotNull(user);
	}

	@Test
	public void updateTimestamp() throws Exception {
		String uid = randomString();
		dao.insert(uid);
		User user = dao.get(uid);
		long oldTimestamp = user.getFirstLogin().getTime();
		dao.updateFirstAccessTimestamp(user);
		long newTimestamp = user.getFirstLogin().getTime();
		assertNotSame(oldTimestamp, newTimestamp);
	}

}
