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
import edu.berkeley.calcentral.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.EnumMap;
import java.util.List;

@Service
public final class LdapService {

	@Autowired
	private LdapTemplate ldapTemplate;

	private enum LDAPFields {
		title, labeledUri, roomNumber, street, mail, l, st;
	}

	public void mergeLdapData(User user) {
		EnumMap<LDAPFields, String> searchResult = individualLdapInfo(user.getUid());
		if (searchResult == null) {
			return;
		} else {
			user.setTitle(Strings.nullToEmpty(searchResult.get(LDAPFields.title)));
			user.setWebsite(Strings.nullToEmpty(searchResult.get(LDAPFields.labeledUri)));
			String roomNumber = Strings.nullToEmpty(searchResult.get(LDAPFields.roomNumber));
			String street = Strings.nullToEmpty(searchResult.get(LDAPFields.street));
			if (!street.isEmpty() && !roomNumber.isEmpty()) {
				user.setAddress(new StringBuilder(roomNumber).append(" ").append(street).toString());
			}
			user.setCity(Strings.nullToEmpty(searchResult.get(LDAPFields.l)));
			user.setState(Strings.nullToEmpty(searchResult.get(LDAPFields.st)));
			user.setPublicEmail(Strings.nullToEmpty(searchResult.get(LDAPFields.mail)));
			return;
		}
	}

	private EnumMap<LDAPFields, String> individualLdapInfo(String uid) {
		if (uid == null || uid.isEmpty()) {
			return null;
		}
		List<EnumMap<LDAPFields, String>> results = ldapTemplate.search("", "uid=" + uid, ldapMapper);
		if (results != null && !results.isEmpty()) {
			return results.get(0);
		}
		return null;
	}

	private AttributesMapper ldapMapper = new AttributesMapper() {
		@Override
		public EnumMap<LDAPFields, String> mapFromAttributes(Attributes attributes) throws NamingException {
			EnumMap<LDAPFields, String> result = Maps.newEnumMap(LDAPFields.class);
			for(LDAPFields key : LDAPFields.values()) {
				Attribute value = attributes.get(key.name());
				if (value != null) {
					result.put(key, value.get().toString());
				}
			}
			return result;
		}
	};

}
