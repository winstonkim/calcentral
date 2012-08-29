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
import edu.berkeley.calcentral.services.CampusPersonDataService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class CampusPersonDataServiceTest extends DatabaseAwareTest {
	private static final Log LOGGER = LogFactory.getLog(CampusPersonDataServiceTest.class);

	@Autowired
	CampusPersonDataService campusPersonDataService;

	@Test
	public void checkKnownPersonAttributes() {
		Map<String, Object> personAttributes = campusPersonDataService.getPersonAttributes("2040");
		assertNotNull(personAttributes);
		assertEquals("Oliver", personAttributes.get("FIRST_NAME"));
	}

	@Test
	public void unknownPersonHandling() {
		Map<String, Object> personAttributes = campusPersonDataService.getPersonAttributes("00000");
		assertNotNull(personAttributes);
		assertEquals(0, personAttributes.size());
	}
}
