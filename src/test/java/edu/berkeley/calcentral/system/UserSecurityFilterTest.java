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

import edu.berkeley.calcentral.BaseTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UserSecurityFilterTest extends BaseTest {

	private UserSecurityFilter filter;

	@MockitoAnnotations.Mock
	HttpServletRequest request;

	@MockitoAnnotations.Mock
	HttpServletResponse response;

	@MockitoAnnotations.Mock
	FilterChain filterChain;

	@Before
	public void setup() {
		filter = new UserSecurityFilter();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void filterUserIsAllowed() throws IOException, ServletException {
		Mockito.when(request.getRemoteUser()).thenReturn("alice");
		Mockito.when(request.getRequestURI()).thenReturn("/api/user/alice");
		filter.doFilter(request, response, filterChain);

		Mockito.when(request.getRequestURI()).thenReturn("/api/user/alice/foo/bar/baz");
		filter.doFilter(request, response, filterChain);
		Mockito.verify(filterChain, Mockito.times(2)).doFilter(request, response);

	}

	@Test
	public void filterUserNotAllowed() throws IOException, ServletException {
		Mockito.when(request.getRemoteUser()).thenReturn("alice");

		Mockito.when(request.getRequestURI()).thenReturn("/api/user/bob");
		filter.doFilter(request, response, filterChain);

		Mockito.when(request.getRequestURI()).thenReturn("/api/user/bob/widgetData");
		filter.doFilter(request, response, filterChain);
		Mockito.verify(response, Mockito.times(2)).sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission to operate on that user");

		Mockito.verify(filterChain, Mockito.never()).doFilter(request, response);
	}

}

