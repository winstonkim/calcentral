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


import edu.berkeley.calcentral.Urls;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Path(Urls.BSPACE_FAVORITES)
public class BspaceFavoritesProxy {

	private static final Logger logger = Logger.getLogger(BspaceFavoritesProxy.class);

	@GET
	public Map<String, Object> get() throws IOException {
		HttpClient httpClient = new HttpClient();
		httpClient.setState(new HttpState());
		GetMethod get = new GetMethod("http://sakai-dev.berkeley.edu:80/sakai-hybrid/sites?categorized=true");
		httpClient.executeMethod(get);
		if (logger.isDebugEnabled()) {
			logger.debug("Proxy get of url " + get.getURI() + " returned statusCode=" + get.getStatusCode() + " " + get.getStatusText());
			logger.trace("Response body: " + get.getResponseBodyAsString());
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("body", get.getResponseBodyAsString());
		return result;
	}

}
