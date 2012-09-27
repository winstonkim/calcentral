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
import edu.berkeley.calcentral.daos.UserDao;
import edu.berkeley.calcentral.domain.ClassPage;
import edu.berkeley.calcentral.domain.ClassPageInstructor;
import edu.berkeley.calcentral.domain.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;

public class ClassPagesServiceTest extends DatabaseAwareTest {

	private static final Log LOGGER = LogFactory.getLog(ClassPagesDaoTest.class);

	@Autowired
	private ClassPagesService classPagesService;

	@Autowired
	private UserDao userDao;

	@Test
	public void getDepartment() throws Exception {
		ClassPage classPage = classPagesService.getClassInfo("2012D7308");
		assertNotNull(classPage);
		assertEquals("Biology", classPage.getDepartment());
		assertNotNull(classPage.getCourseinfo());
		assertEquals("BIOLOGY", classPage.getCourseinfo().getMisc_deptname());
		assertTrue(classPage.getInstructors().size() > 0);
		assertTrue(classPage.getSections().size() > 0);
		assertNotNull(classPage.getSections().get(0).getSection_instructors());
		assertTrue(classPage.getSections().get(0).getSection_instructors().size() > 0);
		assertNotNull(classPage.getSchedule());
		LOGGER.debug(classPage);
	}

	@Test
	public void overrideInstructorUrl() throws Exception {
		ClassPage classPage = classPagesService.getClassInfo("2012D7308");
		List<ClassPageInstructor> instructors = classPage.getInstructors();
		ClassPageInstructor instructor = instructors.get(0);
		String instructorId = instructor.getId();
		assertEquals("https://calnet.berkeley.edu/directory/details.pl?uid=" + instructorId, instructor.getUrl());
		try {
			userDao.get(instructorId);
		} catch (EmptyResultDataAccessException e) {
			userDao.insert(instructorId);
		}
		User userRecord = userDao.get(instructorId);
		String newUrl = "http://example.com/underconstruction";
		userRecord.setLink(newUrl);
		userDao.update(userRecord);
		instructors = classPagesService.getClassInfo("2012D7308").getInstructors();
		instructor = null;
		for (ClassPageInstructor candidate : instructors) {
			if (instructorId.equals(candidate.getId())) {
				instructor = candidate;
				break;
			}
		}
		assertNotNull(instructor);
		assertEquals(newUrl, instructor.getUrl());
	}
}
