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

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import edu.berkeley.calcentral.DatabaseAwareTest;

public class WidgetDataDaoTest extends DatabaseAwareTest {

	@Autowired
	private WidgetDataDao dao;

	@Test
	public void testSaveWidgetData() throws Exception {
		dao.saveWidgetData("joe", "abc", "{\"somedata\":\"foo\"}");
	}

	@Test
	public void testDeleteWidgetData() throws Exception {
		dao.saveWidgetData("joe", "abc", "{\"somedata\":\"foo\"}");
		dao.saveWidgetData("jane", "abc", "{\"somedata\":\"foo\"}");
		dao.deleteWidgetData("joe", "abc");
		assertNull(dao.getWidgetData("joe", "abc"));
		assertNotNull(dao.getWidgetData("jane", "abc"));
	}

	@Test
	public void testDeleteAllWidgetData() throws Exception {
		dao.saveWidgetData("joe", "abc", "{\"somedata\":\"foo\"}");
		dao.saveWidgetData("joe", "def", "{\"somedata\":\"foo\"}");
		dao.deleteAllWidgetData("joe");
		assertNull(dao.getAllWidgetData("joe"));
	}

	@Test
	public void testGetWidgetData() throws Exception {
		dao.saveWidgetData("joe", "abc", "{\"somedata\":\"foo\"}");
		assertNotNull(dao.getWidgetData("joe", "abc"));
	}

	@Test
	public void testGetAllWidgetData() throws Exception {
		dao.saveWidgetData("joe", "abc", "{\"somedata\":\"foo\"}");
		dao.saveWidgetData("joe", "def", "{\"somedata\":\"foo\"}");
		assertEquals(2, dao.getAllWidgetData("joe").size());
	}
}
