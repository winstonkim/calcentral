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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.berkeley.calcentral.Params;
import edu.berkeley.calcentral.Urls;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

/**
 * Proxy for talking to Berkeley's Canvas servers. If you want to hit a Canvas URL of the form
 * http://ucberkeley.instructure.com/api/v1/foo/bar then you call http://{calcentral}/api/canvas/foo/bar and
 * the proxy will forward your request with form parameters intact.
 * <p/>
 * Canvas API documentation is here: {@link https://canvas.instructure.com/doc/api/index.html}
 */
@SuppressWarnings("unchecked")
@Service
@Path(Urls.CANVAS)
public class CanvasProxy {

	private static final Logger LOGGER = Logger.getLogger(CanvasProxy.class);

	private String canvasRoot;
	public void setCanvasRoot(String canvasRoot) {
		this.canvasRoot = canvasRoot;
	}

	private String accessToken;
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	private String accountId;
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	private RestTemplate restTemplate;

	private HttpHeaders headers;

	private ObjectMapper mapper = new ObjectMapper();

	@PostConstruct
	public void init() {
		LOGGER.info("canvasRoot = " + canvasRoot + "; canvas access token = " + accessToken +
				"; account ID = " + accountId);
		headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken);
		restTemplate = new RestTemplate();
	}

	/**
	 * Get an URL on the Canvas server.
	 *
	 * @param canvasPath The Canvas API path to get, not including the server name and /api/v1 part.
	 * @return The Canvas server's response.
	 */
	public String get(String canvasPath) {
		return doMethod(HttpMethod.GET, new HttpEntity<String>(headers), canvasPath);
	}

	/**
	 * Handling a user's request of "their current courses." This is likely to change once OAuth comes around to let canvas
	 * know what user it's speaking to (so we don't have to fudge the functionality with two separate API calls).
	 *
	 * @return list of active courses for the current user, or null on errors.
	 */
	@GET
	@Path("courses")
	@Produces({MediaType.APPLICATION_JSON})
	public Map<String, Object> getMyCourses(@Context HttpServletRequest request) {
		String uid = request.getRemoteUser();
		//sanity check.
		if (uid == null) {
			LOGGER.error("Not authenticated.");
			return null;
		}
		String enrollment_url = "users/sis_user_id:" + uid + "/enrollments";
		String currentEnrollment = get(enrollment_url);
		String allCourses = get(Urls.CANVAS_ACCOUNT_PATH + "/courses");
		if (currentEnrollment == null || allCourses == null) {
			LOGGER.error("Bad responses. currentEnrollment=" + currentEnrollment + ", allCourses=" + allCourses);
			return null;
		} else {
			Map<String, Object> result = Maps.newHashMap();
			result.put("canvasRoot", canvasRoot);
			result.put("courses", intersectEnrollmentAndCourses(currentEnrollment, allCourses));
			return result;
		}
	}

	private ArrayNode intersectEnrollmentAndCourses(String enrollment, String courses) {
		Set<String> myCourseIds = Sets.newHashSet();
		ArrayNode returnNode = mapper.createArrayNode();
		try {
			JsonNode enrollmentArray = mapper.readTree(enrollment);
			for (JsonNode enrollmentNode : enrollmentArray) {
				myCourseIds.add(enrollmentNode.get("course_id").getValueAsText());
			}
			JsonNode coursesArray = mapper.readTree(courses);
			for (JsonNode courseNode : coursesArray) {
				String courseId = courseNode.get("id").getValueAsText();
				if (myCourseIds.contains(courseId)) {
					returnNode.add(courseNode);
				}
			}
			return returnNode;
		} catch (Exception e) {
			LOGGER.error("Json parsing error: ", e);
			return null;
		}
	}

	/**
	 * Do a PUT on the Canvas server from an HTTP client request.
	 *
	 * @param canvasPath The Canvas API path to PUT to, not including the server name and /api/v1 part.
	 * @return The Canvas server's response.
	 */
	public String put(String canvasPath, HttpServletRequest request) {
		HttpEntity<MultiValueMap<String, Object>> entity = convertToEntity(request);
		return doMethod(HttpMethod.PUT, entity, canvasPath);
	}

	/**
	 * POST to an URL on the Canvas server from an HTTP client request.
	 *
	 * @param canvasPath The Canvas API path to POST to, not including the server name and /api/v1 part.
	 * @return The Canvas server's response.
	 */
	public String post(String canvasPath, HttpServletRequest request) {
		HttpEntity<MultiValueMap<String, Object>> entity = convertToEntity(request);
		return doMethod(HttpMethod.POST, entity, canvasPath);
	}

	/**
	 * Delete an URL on the Canvas server.
	 *
	 * @param canvasPath The Canvas API path to DELETE, not including the server name and /api/v1 part.
	 * @return The Canvas server's response.
	 */
	public String delete(String canvasPath) {
		return doMethod(HttpMethod.DELETE, new HttpEntity<String>(headers), canvasPath);
	}

	/**
	 * POST to an URL on the Canvas server from server-side code.
	 *
	 * @param canvasPath The Canvas API path to POST to, not including the server name and /api/v1 part.
	 * @return The Canvas server's response.
	 */
	public String post(String canvasPath, Map<String, ?> data) {
		return doMethod(HttpMethod.POST, convertToEntity(data), canvasPath);
	}

	/**
	 * PUT to an URL on the Canvas server from server-side code.
	 *
	 * @param canvasPath The Canvas API path to PUT to, not including the server name and /api/v1 part.
	 * @return The Canvas server's response.
	 */
	public String put(String canvasPath, Map<String, ?> data) {
		return doMethod(HttpMethod.PUT, convertToEntity(data), canvasPath);
	}

	private String doMethod(HttpMethod method, HttpEntity entity, String canvasPath) {
		String url = canvasRoot + "/api/v1/" + StringUtils.trimLeadingCharacter(canvasPath, '/');
		Map<String, String> params = ImmutableMap.of(
				Params.CANVAS_ACCOUNT_ID, accountId
		);
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

	private HttpEntity<MultiValueMap<String, Object>> convertToEntity(HttpServletRequest request) {
		MultiValueMap<String, Object> params = new LinkedMultiValueMap<String, Object>();
		Enumeration<String> requestParams = request.getParameterNames();
		while (requestParams.hasMoreElements()) {
			String paramName = requestParams.nextElement();
			params.add(paramName, request.getParameter(paramName));
		}
		return convertToEntity(params);
	}

	private HttpEntity<MultiValueMap<String, Object>> convertToEntity(Map data) {
		MultiValueMap<String, Object> params;
		if (data instanceof MultiValueMap) {
			params = (MultiValueMap) data;
		} else {
			params = new LinkedMultiValueMap<String, Object>();
			params.setAll(data);
		}
		return new HttpEntity<MultiValueMap<String, Object>>(params, headers);
	}

}
