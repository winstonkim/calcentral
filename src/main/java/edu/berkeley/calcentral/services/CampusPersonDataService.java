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

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import edu.berkeley.calcentral.daos.BaseDao;
import edu.berkeley.calcentral.domain.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
public class CampusPersonDataService extends BaseDao {
	private static final Log LOGGER = LogFactory.getLog(CampusPersonDataService.class);
	public static final String SELECT_PERSON_SQL =
			"select pi.LDAP_UID, pi.UG_GRAD_FLAG, pi.FIRST_NAME, pi.LAST_NAME, pi.PERSON_NAME, pi.EMAIL_ADDRESS, pi.AFFILIATIONS, " +
					"sm.MAJOR_NAME, sm.MAJOR_TITLE, sm.COLLEGE_ABBR, sm.MAJOR_NAME2, sm.MAJOR_TITLE2, sm.COLLEGE_ABBR2, " +
					"sm.MAJOR_NAME3, sm.MAJOR_TITLE3, sm.COLLEGE_ABBR3, sm.MAJOR_NAME4, sm.MAJOR_TITLE4, sm.COLLEGE_ABBR4 " +
					"from BSPACE_PERSON_INFO_VW pi " +
					"left join BSPACE_STUDENT_MAJOR_VW sm on pi.LDAP_UID = sm.LDAP_UID " +
					"where pi.LDAP_UID = :ldap_uid";

	public Map<String, Object> getPersonAttributes(String personId) {
		long ldapUid;
		try {
			ldapUid = Long.parseLong(personId);
		} catch (NumberFormatException e) {
			LOGGER.warn("Person ID " + personId + " is not numeric");
			return null;
		}
		Map<String, Object> personAttributes = Maps.newHashMap();
		List<Map<String, Object>> sqlResults = campusQueryRunner.queryForList(
				SELECT_PERSON_SQL,
				new MapSqlParameterSource("ldap_uid", ldapUid));
		if (sqlResults.size() > 1) {
			LOGGER.error("Multiple rows found for ldap_uid " + ldapUid);
		} else if (sqlResults.size() == 1) {
			personAttributes = sqlResults.get(0);
		}
		return personAttributes;
	}

	public void mergeCampusData(User user) {
		// merge in campus data if the user's fields are null
		Map<String, Object> campusPersonData = this.getPersonAttributes(user.getUid());

		if (!StringUtils.hasLength(user.getPreferredName())) {
			Object preferredNameObj = campusPersonData.get("PERSON_NAME");
			if (preferredNameObj != null) {
				user.setPreferredName(String.valueOf(preferredNameObj));
			} else {
				user.setPreferredName(user.getUid());
			}
		}

		if (!StringUtils.hasLength(user.getLink())) {
			user.setLink("https://calnet.berkeley.edu/directory/details.pl?uid=" + user.getUid());
		}

		//Maybe support user preference emails later but for now, email must be the campus primary email
		//to help deal with bCal calendarId issues (and probably for other gApps related references).
		String primaryEmail = String.valueOf(campusPersonData.get("EMAIL_ADDRESS"));
		user.setEmail(Strings.nullToEmpty(primaryEmail));
	}

}
