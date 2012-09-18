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
import edu.berkeley.calcentral.daos.OAuth2Dao;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
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

	public static final String CANVAS_APP_ID = "canvas";

	private String canvasRoot;
	public void setCanvasRoot(String canvasRoot) {
		this.canvasRoot = canvasRoot;
	}

	private String adminAccessToken;
	public void setAdminAccessToken(String adminAccessToken) {
		this.adminAccessToken = adminAccessToken;
	}

	private String accountId;
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	@Autowired @Qualifier("canvasRestTemplate")
	private OAuth2RestTemplate oauthRestTemplate;

	@Autowired
	private OAuth2Dao oAuth2Dao;

	@PostConstruct
	public void init() {
		LOGGER.info("canvasRoot = " + canvasRoot + "; canvas access token = " + adminAccessToken +
				"; account ID = " + accountId);
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
		String currentEnrollment = doAdminMethod(HttpMethod.GET, enrollment_url);
		String allCourses = doAdminMethod(HttpMethod.GET, Urls.CANVAS_ACCOUNT_PATH + "/courses");
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
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode returnNode = mapper.createArrayNode();
		try {
			JsonNode enrollmentArray = mapper.readTree(enrollment);
			for (JsonNode enrollmentNode : enrollmentArray) {
				myCourseIds.add(enrollmentNode.get("course_id").asText());
			}
			JsonNode coursesArray = mapper.readTree(courses);
			for (JsonNode courseNode : coursesArray) {
				String courseId = courseNode.get("id").asText();
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

	public String doAdminMethod(HttpMethod httpMethod, String canvasPath) {
		return doAdminMethod(httpMethod, canvasPath, null);
	}
	public String doAdminMethod(HttpMethod httpMethod, String canvasPath, Map<String, ?> data) {
		return doMethod(httpMethod, convertToEntity(data, adminAccessToken), canvasPath);
	}

	/**
	 * Get an URL on the Canvas server as the current user.
	 *
	 * @param canvasPath The Canvas API path to get, not including the server name and /api/v1 part.
	 * @return The Canvas server's response.
	 */
	@GET
	@Path("{canvaspath:.*}")
	@Produces({MediaType.APPLICATION_JSON})
	public String get(@PathParam(value = "canvaspath") String canvasPath, @Context HttpServletRequest request) {
		String accessToken = getAccessToken(request);
		return doMethod(HttpMethod.GET, convertToEntity(request, accessToken), canvasPath);
	}
	/**
	 * Do a PUT on the Canvas server as the current user.
	 *
	 * @param canvasPath The Canvas API path to PUT to, not including the server name and /api/v1 part.
	 * @return The Canvas server's response.
	 */
	@PUT
	@Path("{canvaspath:.*}")
	@Produces({MediaType.APPLICATION_JSON})
	public String put(@PathParam(value = "canvaspath") String canvasPath, @Context HttpServletRequest request) {
		String accessToken = getAccessToken(request);
		return doMethod(HttpMethod.PUT, convertToEntity(request, accessToken), canvasPath);
	}

	/**
	 * POST to an URL on the Canvas server as the current user.
	 *
	 * @param canvasPath The Canvas API path to POST to, not including the server name and /api/v1 part.
	 * @return The Canvas server's response.
	 */
	@POST
	@Path("{canvaspath:.*}")
	@Produces({MediaType.APPLICATION_JSON})
	public String post(@PathParam(value = "canvaspath") String canvasPath, @Context HttpServletRequest request) {
		String accessToken = getAccessToken(request);
		return doMethod(HttpMethod.POST, convertToEntity(request, accessToken), canvasPath);
	}

	/**
	 * Delete an URL on the Canvas server as the current user.
	 *
	 * @param canvasPath The Canvas API path to DELETE, not including the server name and /api/v1 part.
	 * @return The Canvas server's response.
	 */
	@DELETE
	@Path("{canvaspath:.*}")
	@Produces({MediaType.APPLICATION_JSON})
	public String delete(@PathParam(value = "canvaspath") String canvasPath, @Context HttpServletRequest request) {
		String accessToken = getAccessToken(request);
		return doMethod(HttpMethod.DELETE, convertToEntity(request, accessToken), canvasPath);
	}

	public boolean isOAuthGranted(String userId) {
		return (oAuth2Dao.getToken(userId, CANVAS_APP_ID) != null);
	}

	private String doMethod(HttpMethod method, HttpEntity entity, String canvasPath) {
		String url = canvasRoot + "/api/v1/" + StringUtils.trimLeadingCharacter(canvasPath, '/');
		Map<String, String> params = ImmutableMap.of(
				Params.CANVAS_ACCOUNT_ID, accountId
		);
		LOGGER.info("Doing " + method.toString() + " on url " + url);
		RestTemplate restTemplate = new RestTemplate();
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

	private HttpEntity<MultiValueMap<String, Object>> convertToEntity(HttpServletRequest request, String accessToken) {
		MultiValueMap<String, Object> params = new LinkedMultiValueMap<String, Object>();
		Enumeration<String> requestParams = request.getParameterNames();
		while (requestParams.hasMoreElements()) {
			String paramName = requestParams.nextElement();
			params.add(paramName, request.getParameter(paramName));
		}
		return convertToEntity(params, accessToken);
	}

	private HttpEntity<MultiValueMap<String, Object>> convertToEntity(Map data, String accessToken) {
		MultiValueMap<String, Object> params;
		if (data instanceof MultiValueMap) {
			params = (MultiValueMap) data;
		} else {
			params = new LinkedMultiValueMap<String, Object>();
			if (data != null) {
				params.setAll(data);
			}
		}
		HttpHeaders httpHeaders = new HttpHeaders();
		// If null, public access is assumed.
		if (accessToken != null) {
			httpHeaders.set("Authorization", "Bearer " + accessToken);
		}
		return new HttpEntity<MultiValueMap<String, Object>>(params, httpHeaders);
	}

	private String getAccessToken(@Context HttpServletRequest request) {
		String oauthTokenId = null;
		String userId = request.getRemoteUser();
		if (userId != null) {
			oauthTokenId = oAuth2Dao.getToken(userId, CANVAS_APP_ID);
			if (oauthTokenId == null) {
				OAuth2AccessToken accessToken = oauthRestTemplate.getAccessToken();
				LOGGER.info("access token = " + accessToken);
				if (accessToken != null) {
					oauthTokenId = accessToken.getValue();
					LOGGER.info("Adding access token for user " + userId);
					oAuth2Dao.insert(userId, CANVAS_APP_ID, oauthTokenId);
				}
			}
		}
		return oauthTokenId;
	}

}
