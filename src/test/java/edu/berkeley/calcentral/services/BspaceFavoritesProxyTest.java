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

import edu.berkeley.calcentral.DatabaseAwareTest;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;

public class BspaceFavoritesProxyTest extends DatabaseAwareTest {

	private static final Logger logger = Logger.getLogger(BspaceFavoritesProxyTest.class);

	@Autowired
	private BspaceFavoritesProxy proxy;

	@Test
	public void testGet() throws Exception {
		try {
			Map<String, Object> result = proxy.get();
			//logger.info(result);
		} catch (IOException e) {
			// warn about IOE but don't fail the test just because remote resource is down
			logger.warn("Got IOException running test, is BSpace server reachable?", e);
		}
	}
}
