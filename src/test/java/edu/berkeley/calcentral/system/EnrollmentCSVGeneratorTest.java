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
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnrollmentCSVGeneratorTest extends DatabaseAwareTest {

	private static final Logger LOGGER = Logger.getLogger(EnrollmentCSVGeneratorTest.class);

	@Autowired
	private EnrollmentCSVGenerator generator;

	@Test
	public void readSections() throws IOException {
		List<String> sections = generator.readSectionsFromCSV();
		assertNotNull(sections);
		assertTrue(sections.size() > 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void badSectionID() {
		generator.getStudentsForSection("abc");
	}

	@Test
	public void getStudentsForSection() throws Exception {
		List<Map<String, Object>> enrollments = generator.getStudentsForSection("2012-D-73974");
		assertNotNull(enrollments);
		assertTrue(enrollments.size() > 0);
	}

	@Test
	public void getInstructorsForSection() throws Exception {
		List<Map<String, Object>> enrollments = generator.getInstructorsForSection("2012-D-73974");
		assertNotNull(enrollments);
		assertTrue(enrollments.size() > 0);
	}

	@Test
	public void writeEnrollmentCSV() throws Exception {
		String csvPath = "target/enrollments-test-" + System.currentTimeMillis() + ".csv";
		FileUtils.touch(new File(csvPath));

		List<Map<String, Object>> enrollments = new ArrayList<Map<String, Object>>(1);
		enrollments.add(ImmutableMap.<String, Object>of("LDAP_UID", "12345", "ENROLL_STATUS", "E", "ROLE", "student"));
		generator.writeEnrollmentCSV("2012-D-32203", enrollments, csvPath);

		CSVParser parser = new CSVParser(new FileReader(csvPath));
		String[] values = parser.getLine();
		assertEquals(5, values.length);
		assertEquals("", values[0]);
	}

	@Test
	public void generate() throws Exception {
		String csvPath = "target/enrollments-fulltest-" + System.currentTimeMillis() + ".csv";
		FileUtils.touch(new File(csvPath));
		generator.generate(csvPath);

		CSVParser parser = new CSVParser(new FileReader(csvPath));
		String[] values = parser.getLine();
		assertEquals(5, values.length);
		values = parser.getLine();
		assertEquals(5, values.length);
	}
}
