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

import edu.berkeley.calcentral.daos.UserServiceDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserAuthorizationTest {

	private UserAuthorization userAuthorization;

	@Mock
	private UserServiceDao iUserAuthorizationDao;

	@Before
	public void setUp() throws Exception {
		this.userAuthorization = new UserAuthorization();
		this.userAuthorization.setUserServiceDao(this.iUserAuthorizationDao);
	}

	@Test
	public void testLoadUserByUsername() throws Exception {
		this.userAuthorization.loadUserByUsername("joe");
		Mockito.verify(iUserAuthorizationDao).getUserDetails("joe");
	}
}
