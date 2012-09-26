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
import edu.berkeley.calcentral.Params;
import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.daos.OAuth2Dao;
import edu.berkeley.calcentral.system.RestUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.core.ServerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.UserDeniedAuthorizationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Map;

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
	public String getCanvasRoot() {
		return canvasRoot;
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

	public Response doAdminMethod(HttpMethod httpMethod, String canvasPath) {
		return doAdminMethod(httpMethod, canvasPath, null);
	}
	public Response doAdminMethod(HttpMethod httpMethod, String canvasPath, Map<String, ?> data) {
		return doMethod(httpMethod, RestUtils.convertToEntity(data, adminAccessToken), canvasPath);
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
	public Response get(@PathParam(value = "canvaspath") String canvasPath, @Context HttpServletRequest request) {
		String accessToken = getAccessToken(request);
		return doMethod(HttpMethod.GET, RestUtils.convertToEntity(request, accessToken), canvasPath);
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
	public Response put(@PathParam(value = "canvaspath") String canvasPath, @Context HttpServletRequest request) {
		String accessToken = getAccessToken(request);
		return doMethod(HttpMethod.PUT, RestUtils.convertToEntity(request, accessToken), canvasPath);
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
	public Response post(@PathParam(value = "canvaspath") String canvasPath, @Context HttpServletRequest request) {
		String accessToken = getAccessToken(request);
		return doMethod(HttpMethod.POST, RestUtils.convertToEntity(request, accessToken), canvasPath);
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
	public Response delete(@PathParam(value = "canvaspath") String canvasPath, @Context HttpServletRequest request) {
		String accessToken = getAccessToken(request);
		return doMethod(HttpMethod.DELETE, RestUtils.convertToEntity(request, accessToken), canvasPath);
	}

	/**
	 * Remove the current user's stored API key for canvas.
	 */
	@POST
	@Path("canvasOAuthEnabled")
	public void disableOAuth(@Context HttpServletRequest request, @FormParam(Params.METHOD) String method) {
		String uid = request.getRemoteUser();
		if (uid != null && method != null && method.equalsIgnoreCase("delete")) {
			oAuth2Dao.delete(uid, CANVAS_APP_ID);
		}
	}

	private Response doMethod(HttpMethod method, HttpEntity entity, String canvasPath) {
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
			return ServerResponse.ok().status(response.getStatusCode().value()).entity(response.getBody()).build();
		} catch (HttpServerErrorException error5xx) {
			LOGGER.error(error5xx.getMessage(), error5xx);
			return ServerResponse.serverError().status(error5xx.getStatusCode().value()).entity(error5xx.getMessage()).build();
		} catch (HttpClientErrorException error4xx) {
			LOGGER.error(error4xx.getMessage(), error4xx);
			return ServerResponse.serverError().status(error4xx.getStatusCode().value()).entity(error4xx.getMessage()).build();
		}
	}

	private String getAccessToken(@Context HttpServletRequest request) {
		String oauthTokenId = null;
		String userId = request.getRemoteUser();
		if (userId != null) {
			oauthTokenId = oAuth2Dao.getToken(userId, CANVAS_APP_ID);
		}
		return oauthTokenId;
	}

	/**
	 * This end-point serves two functions.
	 *
	 * First, the browser uses it to initiate a request for a Canvas OAuth2 token
	 * for the currently logged-in user. A "redirectUri" parameter can specify a
	 * landing location after Canvas is done; the default landing page is
	 * "/secure/dashboard".
	 *
	 * Second, Canvas will redirect to this end-point after it's done requesting
	 * permission from the currently logged-in user. If the new token was granted,
	 * CalCentral will store it locally; if the token was denied, CalCentral will
	 * remove any existing Canvas token for the user.
	 *
	 * @param request
	 * @return redirect to the post-Canvas landing location
	 */
	@GET
	@Path("oAuthToken")
	public Response getOAuthResponse(@Context HttpServletRequest request) {
		String userId = request.getRemoteUser();
		if (userId != null) {
			try {
				// Either forward to Canvas to request a token, or interpret
				// Canvas's redirect back to us afterward.
				oauthRestTemplate.getOAuth2ClientContext().setAccessToken(null);
				OAuth2AccessToken accessToken = oauthRestTemplate.getAccessToken();
				LOGGER.info("access token = " + accessToken);
				if (accessToken != null) {
					String oauthTokenId = accessToken.getValue();
					LOGGER.info("access token = " + oauthTokenId);
					if (oAuth2Dao.getToken(userId, CANVAS_APP_ID) == null) {
						LOGGER.info("Adding access token for user " + userId);
						oAuth2Dao.insert(userId, CANVAS_APP_ID, oauthTokenId, null, 0);
					} else {
						LOGGER.info("Updating access token for user " + userId);
						oAuth2Dao.update(userId, CANVAS_APP_ID, oauthTokenId, null, 0);
					}
				}
			} catch (UserDeniedAuthorizationException e) {
				LOGGER.info("OAuth2 access refused for user " + userId);
				oAuth2Dao.delete(userId, CANVAS_APP_ID);
			}
		}
		String redirectPath = request.getParameter("redirectUri");
		if (redirectPath == null) {
			redirectPath = "/secure/dashboard";
		}
		URI redirectUri = URI.create(redirectPath);
		return Response.seeOther(redirectUri).build();
	}

}
