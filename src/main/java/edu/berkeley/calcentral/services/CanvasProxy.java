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
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


@Service
@Path(Urls.CANVAS)
public class CanvasProxy {

	private static final Logger LOGGER = Logger.getLogger(CanvasProxy.class);

	@Autowired
	private Properties calcentralProperties;

	private String canvasRoot;

	private RestTemplate restTemplate;

	private HttpEntity<String> headersEntity;

	@PostConstruct
	public void init() {
		this.canvasRoot = calcentralProperties.getProperty("canvasProxy.canvasRoot");
		String accessToken = calcentralProperties.getProperty("canvasProxy.accessToken");
		LOGGER.info("canvasRoot = " + canvasRoot + "; canvas access token = " + accessToken);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
		headers.set("Authorization", "Bearer " + accessToken);
		headersEntity = new HttpEntity<String>(headers);
		restTemplate = new RestTemplate();
	}

	@GET
	@Path("{canvaspath:.*}")
	@Produces({MediaType.APPLICATION_JSON})
	public String get(@PathParam(value = "canvaspath") String canvasPath) {
		return doMethod(HttpMethod.GET, canvasPath, new HashMap<String, Object>(0));
	}

	@PUT
	@Path("{canvaspath:.*}")
	@Produces({MediaType.APPLICATION_JSON})
	public String put(@PathParam(value = "canvaspath") String canvasPath, @Context HttpServletRequest request) {
		HashMap<String, Object> parms = new HashMap<String, Object>();
		parms.put("course[name]", "API Edited Course");
		return doMethod(HttpMethod.PUT, canvasPath, parms);
	}

	@POST
	@Path("{canvaspath:.*}")
	@Produces({MediaType.APPLICATION_JSON})
	public String post(@PathParam(value = "canvaspath") String canvasPath, @Context HttpServletRequest request) {
		return doMethod(HttpMethod.POST, canvasPath, request.getParameterMap());
	}

	@DELETE
	@Path("{canvaspath:.*}")
	@Produces({MediaType.APPLICATION_JSON})
	public String delete(@PathParam(value = "canvaspath") String canvasPath, @Context HttpServletRequest request) {
		return doMethod(HttpMethod.DELETE, canvasPath, request.getParameterMap());
	}

	public String doMethod(HttpMethod method, String canvasPath, Map<String, Object> params) {
		String url = canvasRoot + "/api/v1/" + StringUtils.trimLeadingCharacter(canvasPath, '/');
		LOGGER.info("Doing " + method.toString() + " on url " + url);
		try {
			ResponseEntity<String> response = restTemplate.exchange(url, method, headersEntity, String.class, params);
			LOGGER.debug("Response: " + response.getStatusCode() + "; body = " + response.getBody());
			LOGGER.debug("Response headers: " + response.getHeaders().toSingleValueMap());
			return response.getBody();
		} catch (HttpServerErrorException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}
}
