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
import com.Ostermiller.util.LabeledCSVParser;
import com.google.common.collect.ImmutableMap;
import edu.berkeley.calcentral.daos.BaseDao;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class EnrollmentCSVGenerator extends BaseDao {

	private static final Logger LOGGER = Logger.getLogger(EnrollmentCSVGenerator.class);

	List<String> readSections() throws IOException {
		List<String> sections = new ArrayList<String>();
		LabeledCSVParser parser = new LabeledCSVParser(new CSVParser(new ClassPathResource("/canvas/sections.csv").getInputStream()));
		while (parser.getLine() != null) {
			String courseID = parser.getValueByLabel("course_id");
			String sectionID = parser.getValueByLabel("section_id");
			LOGGER.info("Section ID = " + sectionID + " CourseID = " + courseID);
			sections.add(sectionID);
		}
		return sections;
	}

	void getEnrollmentsForSection(String sectionID) {
		String[] sectionIDParts = sectionID.split("-");
		if (sectionIDParts.length != 3) {
			throw new IllegalArgumentException("Got a section ID that doesn't conform to format: " + sectionID);
		}
		List<Map<String, Object>> sqlResults = campusQueryRunner.queryForList(
				"select roster.STUDENT_LDAP_UID, roster.UNIT, roster.ENROLL_STATUS, " +
						"roster.PNP_FLAG, roster.CREDIT_CD, roster.TERM_YR, roster.TERM_CD, roster.COURSE_CNTL_NUM " +
						"from BSPACE_CLASS_ROSTER_VW roster " +
						"where roster.TERM_YR = :term_yr and roster.TERM_CD = :term_cd and roster.COURSE_CNTL_NUM = :course_cntl_num",
				ImmutableMap.of(
						"term_yr", sectionIDParts[0],
						"term_cd", sectionIDParts[1],
						"course_cntl_num", sectionIDParts[2]));
		LOGGER.info("Got " + sqlResults.size() + " enrollments for section " + sectionID);
		for (Map<String, Object> enrollment : sqlResults) {
			LOGGER.info(enrollment);
		}
	}
}
