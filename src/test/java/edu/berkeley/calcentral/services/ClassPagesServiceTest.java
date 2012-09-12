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
import edu.berkeley.calcentral.daos.ClassPagesDaoTest;
import edu.berkeley.calcentral.domain.ClassPage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ClassPagesServiceTest extends DatabaseAwareTest {

	private static final Log LOGGER = LogFactory.getLog(ClassPagesDaoTest.class);

	@Autowired
	private ClassPagesService classPagesService;

	@Test
	public void getDepartment() throws Exception {
		ClassPage classPage = classPagesService.getClassInfo("2012D7308");
		assertNotNull(classPage);
		assertEquals("BIOLOGY", classPage.getDepartment());
		assertNotNull(classPage.getCourseinfo());
		assertTrue(classPage.getInstructors().size() > 0);
		assertTrue(classPage.getSections().size() > 0);
		assertNotNull(classPage.getSections().get(0).getSection_instructors());
		assertNotNull(classPage.getSchedule());
		LOGGER.debug(classPage);
	}
}
