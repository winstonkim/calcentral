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
import edu.berkeley.calcentral.domain.WidgetData;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class WidgetDataDaoTest extends DatabaseAwareTest {

	@Autowired
	private WidgetDataDao dao;

	@Test
	public void testSaveWidgetData() throws Exception {
		WidgetData data = new WidgetData("joe", "abc", "somedata");
		dao.saveWidgetData(data);
	}

	@Test
	public void testDeleteWidgetData() throws Exception {
		WidgetData data = new WidgetData("joe", "abc", "somedata");
		dao.saveWidgetData(data);
		WidgetData janesData = new WidgetData("jane", "abc", "somedata");
		dao.saveWidgetData(janesData);
		dao.deleteWidgetData("joe", "abc");
		assertNull(dao.getWidgetData("joe", "abc"));
		assertNotNull(dao.getWidgetData("jane", "abc"));
	}

	@Test
	public void testDeleteAllWidgetData() throws Exception {
		dao.saveWidgetData(new WidgetData("joe", "abc", "somedata"));
		dao.saveWidgetData(new WidgetData("joe", "def", "somedata"));
		dao.deleteAllWidgetData("joe");
		assertNull(dao.getAllWidgetData("joe"));
	}

	@Test
	public void testGetWidgetData() throws Exception {
		dao.saveWidgetData(new WidgetData("joe", "abc", "somedata"));
		assertNotNull(dao.getWidgetData("joe", "abc"));
	}

	@Test
	public void testGetAllWidgetData() throws Exception {
		dao.saveWidgetData(new WidgetData("joe", "abc", "somedata"));
		dao.saveWidgetData(new WidgetData("joe", "def", "somedata"));
		assertEquals(2, dao.getAllWidgetData("joe").size());
	}
}
