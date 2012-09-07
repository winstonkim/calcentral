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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Crude way to make sure that authenticated users don't act on other users' data.
 */
public class UserSecurityFilter extends OncePerRequestFilter {

	private static final Log LOGGER = LogFactory.getLog(UserSecurityFilter.class);

	private static final Pattern PATTERN_USER_ID = Pattern.compile("/api/user/([^/]+).*");

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		Matcher matcher = PATTERN_USER_ID.matcher(request.getRequestURI());
		if (matcher.matches()) {
			String targetUser = matcher.group(1);
			String remoteUser = request.getRemoteUser();
			if (targetUser != null && targetUser.equals(remoteUser)) {
				LOGGER.debug("Remote user " + remoteUser + " is allowed to operate on target user " + targetUser);
			} else {
				LOGGER.warn("Remote user " + remoteUser + " is NOT allowed to operate on target user " + targetUser);
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission to operate on that user");
				return;
			}
		}
		filterChain.doFilter(request, response);
	}
}
