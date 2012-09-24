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

import edu.berkeley.calcentral.Params;
import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.daos.OAuth2Dao;
import edu.berkeley.calcentral.system.RestUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.spi.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.UserDeniedAuthorizationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;

/**
 * Proxy for talking to Berkeley's Google Api endpoints. If you want to hit a Google API URL of the form
 * http://www.googleapis.com/fooservice/v3/yummy/bacon then you call http://{calcentral}/api/google/v3/yummy/bacon and
 * the proxy will forward your request with form parameters intact.
 * <p/>
 * String point for Google API is here: {@link https://code.google.com/apis/console}. Unlike the Canvas proxy, there
 * won't be an "admin account" for pass-through requests.
 */
@Service
@Path(Urls.GOOGLE)
public class GoogleAppsProxy {
	private static final Logger LOGGER = Logger.getLogger(CanvasProxy.class);

	public static final String GOOGLE_APP_ID = "google";

	public static final String googleBaseUrl = "https://www.googleapis.com/";

	@Autowired
	private OAuth2RestTemplate gappsRestTemplate;

	@Autowired
	private OAuth2Dao oAuth2Dao;

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private OAuth2ProtectedResourceDetails gappsSecurity;


	@PostConstruct
	public void init() {
		LOGGER.info("Opening OAuth Proxy to GApps with: " + gappsSecurity.getClientId());
		LOGGER.info("GApps Scopes: " + StringUtils.collectionToCommaDelimitedString(gappsSecurity.getScope()));
	}

	/**
	 * GET an URL on the Google Apps apis as the current user.
	 *
	 * @param googlePath The google API path to get, not including the "www.googleapis.com"
	 * @return The Google API server's response.
	 */
	@GET
	@Path("/{googlepath:.*}")
	@Produces({MediaType.APPLICATION_JSON})
	public String get(@PathParam(value = "googlepath") String googlePath,
					  @Context HttpServletRequest request,
					  @Context HttpServletResponse response) {
		String accessToken = getAccessToken(request);
		String fullGetPath = "";
		if (request.getQueryString() != null) {
			try {
				/*
				 * This should help support both the cases of encoded and non-encoded query strings.
				 * If the query string is already decoded, then there should be no change, else the
				 * query string will be decoded and then handled by restTemplate (so it doesn't encode twice).
				 */
				fullGetPath = googlePath + "?" + URLDecoder.decode(request.getQueryString(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				String errorString = "Could not decode query string: " + e.getMessage();
				LOGGER.error(errorString);
				throw new WriterException(errorString);
			}
		}
		return doMethod(HttpMethod.GET, RestUtils.convertToEntity(request, accessToken),  fullGetPath);
	}

	/**
	 * Do a PUT on the Google Apps apis as the current user.
	 *
	 * @param googlePath The google API path to get, not including the "www.googleapis.com"
	 * @return The Google API server's response.
	 */
	@PUT
	@Path("/{googlepath:.*}")
	@Produces({MediaType.APPLICATION_JSON})
	public String put(@PathParam(value = "googlepath") String googlePath,
					  @Context HttpServletRequest request,
					  @Context HttpServletResponse response) {
		String accessToken = getAccessToken(request);
		return doMethod(HttpMethod.PUT, RestUtils.convertToEntity(request, accessToken),  googlePath);
	}

	/**
	 * POST to an URL on the Google Apps apis as the current user.
	 *
	 * @param googlePath The google API path to get, not including the "www.googleapis.com"
	 * @return The Google API server's response.
	 */
	@POST
	@Path("/{googlepath:.*}")
	@Produces({MediaType.APPLICATION_JSON})
	public String post(@PathParam(value = "googlepath") String googlePath,
					   @Context HttpServletRequest request,
					   @Context HttpServletResponse response) {
		String accessToken = getAccessToken(request);
		return doMethod(HttpMethod.POST, RestUtils.convertToEntity(request, accessToken), googlePath);
	}

	/**
	 * DELETE an URL on the Google Apps apis as the current user.
	 *
	 * @param googlePath The google API path to get, not including the "www.googleapis.com"
	 * @return The Google API server's response.
	 */
	@DELETE
	@Path("/{googlepath:.*}")
	@Produces({MediaType.APPLICATION_JSON})
	public String delete(@PathParam(value = "googlepath") String googlePath,
						 @Context HttpServletRequest request,
						 @Context HttpServletResponse response) {
		String accessToken = getAccessToken(request);
		return doMethod(HttpMethod.DELETE, RestUtils.convertToEntity(request, accessToken), googlePath);
	}

	private String doMethod(HttpMethod method, HttpEntity entity, String googleUrl) {
		String url = googleBaseUrl + StringUtils.trimLeadingCharacter(googleUrl, '/');
		LOGGER.info("Doing " + method.toString() + " on url " + url);
		RestTemplate restTemplate = new RestTemplate();
		try {
			ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);
			LOGGER.debug("Response: " + response.getStatusCode() + "; body = " + response.getBody());
			LOGGER.debug("Response headers: " + response.getHeaders().toSingleValueMap());
			return response.getBody();
		} catch (HttpServerErrorException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Remove the current user's stored API key for google.
	 */
	@POST
	@Path("gappsOAuthEnabled")
	public void disableOAuth(@Context HttpServletRequest request, @FormParam(Params.METHOD) String method) {
		String uid = request.getRemoteUser();
		if (uid != null && method != null && method.equalsIgnoreCase("delete")) {
			oAuth2Dao.delete(uid, GOOGLE_APP_ID);
		}
	}

	private String getAccessToken(@Context HttpServletRequest request) {
		String oauthTokenId = null;
		String userId = request.getRemoteUser();
		if (userId != null) {
			oauthTokenId = oAuth2Dao.getToken(userId, GOOGLE_APP_ID);
		}
		return oauthTokenId;
	}


	/**
	 * Endpoint for handling the google authorization tokens. This is explicitly set in the google developer apis (configurable)
	 * and is responsible for resolving accessTokens.
	 *
	 * @return will result in a redirect to refresh "/secure/dashboard" or redirectUri
	 */
	@GET
	@Path("gappsOAuthResponse")
	public Response handleAuthRequest(@Context HttpServletRequest request, @FormParam("redirectUri") String redirectPath) {
		String userId = request.getRemoteUser();
		if (userId != null) {
			try {
				// Either forward to Google to request a token, or interpret
				// Google's redirect back to us afterward.
				gappsRestTemplate.getOAuth2ClientContext().setAccessToken(null);
				OAuth2AccessToken accessToken = gappsRestTemplate.getAccessToken();
				LOGGER.info("access token = " + accessToken);
				if (accessToken != null) {
					String oauthTokenId = accessToken.getValue();
					LOGGER.info("access token = " + oauthTokenId);
					if (oAuth2Dao.getToken(userId, GOOGLE_APP_ID) == null) {
						LOGGER.info("Adding access token for user " + userId);
						oAuth2Dao.insert(userId, GOOGLE_APP_ID, oauthTokenId);
					} else {
						LOGGER.info("Updating access token for user " + userId);
						oAuth2Dao.update(userId, GOOGLE_APP_ID, oauthTokenId);
					}
				}
			} catch (UserDeniedAuthorizationException e) {
				LOGGER.info("OAuth2 access refused for user " + userId);
				oAuth2Dao.delete(userId, GOOGLE_APP_ID);
			}
		}

		if (redirectPath == null) {
			redirectPath = "/secure/dashboard";
		}
		URI redirectUri = URI.create(redirectPath);
		return Response.seeOther(redirectUri).build();
	}

}
