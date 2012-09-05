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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Proxy for talking to Berkeley's Canvas servers. If you want to hit a Canvas URL of the form
 * http://ucberkeley.instructure.com/api/v1/foo/bar then you call http://{calcentral}/api/canvas/foo/bar and
 * the proxy will forward your request with form parameters intact.
 *
 * Canvas API documentation is here: {@link https://canvas.instructure.com/doc/api/index.html}
 */
@SuppressWarnings("unchecked")
@Service
@Path(Urls.CANVAS)
public class CanvasProxy {

	private static final Logger LOGGER = Logger.getLogger(CanvasProxy.class);

	@Autowired
	private Properties calcentralProperties;

	private String canvasRoot;

	private RestTemplate restTemplate;

	private HttpHeaders headers;

	@PostConstruct
	public void init() {
		this.canvasRoot = calcentralProperties.getProperty("canvasProxy.canvasRoot");
		String accessToken = calcentralProperties.getProperty("canvasProxy.accessToken");
		LOGGER.info("canvasRoot = " + canvasRoot + "; canvas access token = " + accessToken);
		headers = new HttpHeaders();
		headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
		headers.set("Authorization", "Bearer " + accessToken);
		restTemplate = new RestTemplate();
	}

	/**
	 * Get an URL on the Canvas server.
	 * @param canvasPath The Canvas API path to get, not including the server name and /api/v1 part.
	 * @return The Canvas server's response.
	 */
	@GET
	@Path("{canvaspath:.*}")
	@Produces({MediaType.APPLICATION_JSON})
	public String get(@PathParam(value = "canvaspath") String canvasPath) {
		return doMethod(HttpMethod.GET, new HttpEntity<String>(headers), canvasPath, new HashMap<String, Object>(0));
	}

	/**
	 * Do a PUT on the Canvas server.
	 * @param canvasPath The Canvas API path to PUT to, not including the server name and /api/v1 part.
	 * @return The Canvas server's response.
	 */
	@PUT
	@Path("{canvaspath:.*}")
	@Produces({MediaType.APPLICATION_JSON})
	public String put(@PathParam(value = "canvaspath") String canvasPath, @Context HttpServletRequest request) {
		HttpEntity<MultiValueMap<String, String>> entity = convertToEntity(request);
		return doMethod(HttpMethod.PUT, entity, canvasPath, new HashMap<String, Object>(0));
	}

	/**
	 * POST to an URL on the Canvas server.
	 * @param canvasPath The Canvas API path to POST to, not including the server name and /api/v1 part.
	 * @return The Canvas server's response.
	 */
	@POST
	@Path("{canvaspath:.*}")
	@Produces({MediaType.APPLICATION_JSON})
	public String post(@PathParam(value = "canvaspath") String canvasPath, @Context HttpServletRequest request) {
		HttpEntity<MultiValueMap<String, String>> entity = convertToEntity(request);
		return doMethod(HttpMethod.POST, entity, canvasPath, new HashMap<String, Object>(0));
	}

	/**
	 * Delete an URL on the Canvas server.
	 * @param canvasPath The Canvas API path to DELETE, not including the server name and /api/v1 part.
	 * @return The Canvas server's response.
	 */
	@DELETE
	@Path("{canvaspath:.*}")
	@Produces({MediaType.APPLICATION_JSON})
	public String delete(@PathParam(value = "canvaspath") String canvasPath, @Context HttpServletRequest request) {
		return doMethod(HttpMethod.DELETE, new HttpEntity<String>(headers), canvasPath, request.getParameterMap());
	}

	private String doMethod(HttpMethod method, HttpEntity entity, String canvasPath, Map<String, Object> params) {
		String url = canvasRoot + "/api/v1/" + StringUtils.trimLeadingCharacter(canvasPath, '/');
		LOGGER.info("Doing " + method.toString() + " on url " + url);
		try {
			ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class, params);
			LOGGER.debug("Response: " + response.getStatusCode() + "; body = " + response.getBody());
			LOGGER.debug("Response headers: " + response.getHeaders().toSingleValueMap());
			return response.getBody();
		} catch (HttpServerErrorException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}

	private HttpEntity<MultiValueMap<String, String>> convertToEntity(HttpServletRequest request) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		Enumeration<String> requestParams = request.getParameterNames();
		while (requestParams.hasMoreElements()) {
			String paramName = requestParams.nextElement();
			params.add(paramName, request.getParameter(paramName));
		}
		return new HttpEntity<MultiValueMap<String, String>>(params, headers);
	}

}
