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
import com.Ostermiller.util.CSVPrinter;
import edu.berkeley.calcentral.daos.BaseDao;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class UserCSVGenerator extends BaseDao {

	private static final Logger LOGGER = Logger.getLogger(UserCSVGenerator.class);

	/**
	 * Append to users csv file based on the users found within the supplied enrollments csv file.
	 *
	 * @param enrollmentCSVPath The enrollments csv file in which to find users
	 * @param outputPath        The users file to append to
	 * @throws IOException
	 */
	public void generate(String enrollmentCSVPath, String outputPath) throws IOException {
		Set<String> users = readUsersFromEnrollmentCSV(enrollmentCSVPath);
		writeUserCSV(outputPath, getUserData(users));
	}

	Set<String> readUsersFromEnrollmentCSV(String path) throws IOException {
		Set<String> users = new HashSet<String>();
		CSVParser parser = new CSVParser(new FileReader(path));
		String[][] values = parser.getAllValues();
		for (String[] value : values) {
			users.add(value[1]);
		}
		LOGGER.debug("Found " + users.size() + " unique users in " + path);
		return users;
	}

	List<Map<String, Object>> getUserData(Set<String> users) {
		List<Map<String, Object>> results = campusQueryRunner.queryForList(
				"SELECT LDAP_UID, FIRST_NAME, LAST_NAME " +
						"FROM BSPACE_PERSON_INFO_VW " +
						"WHERE LDAP_UID IN ( :userlist )",
				new MapSqlParameterSource("userlist", users));
		for (Map<String, Object> result : results) {
			LOGGER.debug("getUserData: " + result);
		}
		return results;
	}

	void writeUserCSV(String path, List<Map<String, Object>> users) throws IOException {
		CSVPrinter printer = null;
		try {
			printer = new CSVPrinter(new FileWriter(path, true));
			for (Map<String, Object> user : users) {
				printer.writeln(new String[]{
						// user_id,login_id,password,first_name,last_name,email,status
						user.get("LDAP_UID").toString(),
						user.get("LDAP_UID").toString(),
						null,
						user.get("FIRST_NAME").toString(),
						user.get("LAST_NAME").toString(),
						user.get("LDAP_UID").toString() + "@example.edu",
						"active"});
			}
		} finally {
			if (printer != null) {
				printer.close();
			}
		}
	}
}
