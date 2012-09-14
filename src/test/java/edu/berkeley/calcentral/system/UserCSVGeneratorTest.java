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

package edu.berkeley.calcentral.system;

import com.Ostermiller.util.CSVParser;
import com.google.common.collect.ImmutableMap;
import edu.berkeley.calcentral.DatabaseAwareTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileReader;
import java.util.*;

public class UserCSVGeneratorTest extends DatabaseAwareTest {

	@Autowired
	private UserCSVGenerator generator;

	@Autowired
	private EnrollmentCSVGenerator enrollmentCSVGenerator;

	@Test
	public void readUsersFromEnrollmentsCSV() throws Exception {
		Set<String> users = generator.readUsersFromEnrollmentCSV("src/test/resources/canvas/enrollments.csv");
		assertNotNull(users);
		assertTrue(users.size() > 0);
	}

	@Test
	public void getUserData() {
		Set<String> users = new HashSet<String>(1);
		users.add("904715");
		users.add("2040");
		List<Map<String, Object>> results = generator.getUserData(users);
		assertEquals(2, results.size());
	}

	@Test
	public void writeUserCSV() throws Exception {
		String csvPath = "target/users-test-" + System.currentTimeMillis() + ".csv";
		List<Map<String, Object>> users = new ArrayList<Map<String, Object>>(1);
		users.add(ImmutableMap.<String, Object>of("LDAP_UID", "12345", "FIRST_NAME", "Testy", "LAST_NAME", "Tester"));
		generator.writeUserCSV(csvPath, users);

		CSVParser parser = new CSVParser(new FileReader(csvPath));
		String[] values = parser.getLine();
		assertEquals(7, values.length);
		assertEquals("12345", values[0]);
		assertEquals("12345@example.edu", values[5]);
	}

	@Test
	public void generate() throws Exception {
		String now = String.valueOf(System.currentTimeMillis());
		String enrollmentFilename = "target/usercsvtest-enrollments-" + now + ".csv";
		String userFilename = "target/usercsvtest-users-" + now + ".csv";
		enrollmentCSVGenerator.generate(enrollmentFilename);
		generator.generate(enrollmentFilename, userFilename);

		CSVParser parser = new CSVParser(new FileReader(userFilename));
		String[][] values = parser.getAllValues();
		assertNotNull(values);
	}

}
