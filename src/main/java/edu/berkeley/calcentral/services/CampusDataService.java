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

import com.google.common.collect.ImmutableMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Map;

@Service
@Path("/api/campusdata")
public class CampusDataService {
	private static final Log LOGGER = LogFactory.getLog(CampusDataService.class);
	public static final String SELECT_PERSON_SQL =
			"select pi.LDAP_UID, pi.UG_GRAD_FLAG, pi.FIRST_NAME, pi.LAST_NAME, pi.EMAIL_ADDRESS, pi.AFFILIATIONS, " +
					"sm.MAJOR_NAME, sm.MAJOR_TITLE, sm.COLLEGE_ABBR, sm.MAJOR_NAME2, sm.MAJOR_TITLE2, sm.COLLEGE_ABBR2, " +
					"sm.MAJOR_NAME3, sm.MAJOR_TITLE3, sm.COLLEGE_ABBR3, sm.MAJOR_NAME4, sm.MAJOR_TITLE4, sm.COLLEGE_ABBR4 " +
					"from BSPACE_PERSON_INFO_VW pi " +
					"left join BSPACE_STUDENT_MAJOR_VW sm on pi.LDAP_UID = sm.LDAP_UID " +
					"where pi.LDAP_UID = :ldap_uid";
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Autowired @Qualifier("campusDataSource")
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	@GET
	@Path("/person/{id}")
	public Map<String, Object> getPersonAttributes(@PathParam("id") String personId) {
		long ldapUid;
		try {
			ldapUid = Long.parseLong(personId);
		} catch (NumberFormatException e) {
			LOGGER.warn("Person ID " + personId + " is not numeric");
			return null;
		}
		return jdbcTemplate.queryForMap(SELECT_PERSON_SQL, ImmutableMap.of("ldap_uid", ldapUid));
	}
}
