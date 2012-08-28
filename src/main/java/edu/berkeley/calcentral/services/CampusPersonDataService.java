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
import com.google.common.collect.Maps;
import edu.berkeley.calcentral.Urls;
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
import java.util.List;
import java.util.Map;

@Service
@Path(Urls.CAMPUS_DATA)
public class CampusPersonDataService {
	private static final Log LOGGER = LogFactory.getLog(CampusPersonDataService.class);
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

	/**
	 * <pre>
	 * curl http://localhost:8080/api/campusdata/person/2040
	 * {LDAP_UID=2040, UG_GRAD_FLAG=null, FIRST_NAME=Oliver, LAST_NAME=Heyer, \
	 *   EMAIL_ADDRESS=oliver@media.berkeley.edu, AFFILIATIONS=EMPLOYEE-TYPE-STAFF,STUDENT-STATUS-EXPIRED, \
	 *   MAJOR_NAME=null, MAJOR_TITLE=null, COLLEGE_ABBR=null, MAJOR_NAME2=null, MAJOR_TITLE2=null, \
	 *   COLLEGE_ABBR2=null, MAJOR_NAME3=null, MAJOR_TITLE3=null, COLLEGE_ABBR3=null, MAJOR_NAME4=null, \
	 *   MAJOR_TITLE4=null, COLLEGE_ABBR4=null}
	 * curl http://localhost:8080/api/campusdata/person/000000
	 * {LDAP_UID=000000, UG_GRAD_FLAG=G, FIRST_NAME=WTUIWQWPPWQW, LAST_NAME=GBFSABTBIIB, \
	 *   EMAIL_ADDRESS=xyz@mba.berkeley.edu, AFFILIATIONS=EMPLOYEE-TYPE-STAFF,STUDENT-STATUS-EXPIRED, \
	 *   MAJOR_NAME=BUSINESS ADMIN-EWMBA, \
	 *   MAJOR_TITLE=BUSINESS ADMINISTRATION                                       , \
	 *   COLLEGE_ABBR=BUS ADM , MAJOR_NAME2=null, MAJOR_TITLE2=null, COLLEGE_ABBR2=null, \
	 *   MAJOR_NAME3=null, MAJOR_TITLE3=null, COLLEGE_ABBR3=null, MAJOR_NAME4=null, MAJOR_TITLE4=null, COLLEGE_ABBR4=null}
	 * </pre>
	 */
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
		Map<String, Object> personAttributes = Maps.newHashMap();
		List<Map<String, Object>> sqlResults = jdbcTemplate.queryForList(SELECT_PERSON_SQL, ImmutableMap.of("ldap_uid", ldapUid));
		if (sqlResults.size() > 1) {
			LOGGER.error("Multiple rows found for ldap_uid " + ldapUid);
		} else if (sqlResults.size() == 1) {
			personAttributes = sqlResults.get(0);
		}
		return personAttributes;

	}
}
