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

import edu.berkeley.calcentral.services.UserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.tags.RequestContextAwareTag;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Service
public class UserJSONTag extends RequestContextAwareTag {

	private static final Log LOGGER = LogFactory.getLog(UserJSONTag.class);

	@Override
	protected int doStartTagInternal() throws Exception {

		UserService userService = (UserService) getRequestContext().getWebApplicationContext().getBean("userService");

		ServletRequest request = pageContext.getRequest();
		if (request instanceof HttpServletRequest) {
			HttpServletRequest hrequest = (HttpServletRequest) request;
			LOGGER.debug("Remote user is " + hrequest.getRemoteUser());
			if (hrequest.getRemoteUser() == null) {
				writeEmptyJSON();
				return 0;
			}
			try {
				ObjectMapper mapper = new ObjectMapper();
				String userJSON = mapper.writeValueAsString(userService.getUser(hrequest.getRemoteUser()));
				pageContext.getOut().write(userJSON);
			} catch (Exception e) {
				LOGGER.error("Exception while mapping user data to JSON", e);
				writeEmptyJSON();
			}
		} else {
			LOGGER.warn("Could not get HttpServletRequest from page context");
			writeEmptyJSON();
		}
		return 0;
	}

	private void writeEmptyJSON() throws IOException {
		pageContext.getOut().write("{}");
	}
}
