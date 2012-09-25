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

import com.Ostermiller.util.Base64;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.MemoryCredentialStore;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.googleapis.services.GoogleClient;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.CalendarScopes;
import edu.berkeley.calcentral.Params;
import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.daos.OAuth2Dao;
import edu.berkeley.calcentral.system.RestUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.spi.InternalServerErrorException;
import org.jboss.resteasy.spi.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedClientException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import sun.misc.BASE64Encoder;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOError;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Collections;

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
	private static final Logger LOGGER = Logger.getLogger(GoogleAppsProxy.class);

	public static final String GOOGLE_APP_ID = "google";

	public static final String googleBaseUrl = "https://www.googleapis.com/";

	@Autowired
	private OAuth2Dao oAuth2Dao; // TODO have the dao implement CredentialStore

	private GoogleAuthorizationCodeFlow flow;

	private MemoryCredentialStore credentialStore = new MemoryCredentialStore();

	private String clientId;

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	private String clientSecret;

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	@PostConstruct
	public void init() {
		flow = new GoogleAuthorizationCodeFlow.Builder(
				new NetHttpTransport(),
				new JacksonFactory(),
				clientId,
				clientSecret,
				Collections.singleton(CalendarScopes.CALENDAR)).
				setCredentialStore(credentialStore).
				setAccessType("offline").
				build();
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
		Credential credential = null;
		try {
			credential = flow.loadCredential(request.getRemoteUser());
		} catch (IOException e) {
			LOGGER.error("Error looking up credential", e);
		}
		if (credential != null) {
			LOGGER.info("We've got a credential on file! Token = " + credential.getAccessToken() + " expires in " + credential.getExpiresInSeconds());
			try {
				boolean refreshed = credential.refreshToken();
				LOGGER.info("Credential refreshed: " + refreshed);
			} catch (IOException e) {
				LOGGER.error("Error refreshing token", e);
			}
		} else {
			// UX should detect 401 and make a request to /api/google/requestAuthorization
			throw new UnauthorizedClientException("Google Authorization required");
		}

		String fullGetPath;
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
		} else {
			fullGetPath = googlePath;
		}

		return doMethod(HttpMethod.GET, RestUtils.convertToEntity(request, credential.getAccessToken()), fullGetPath);
	}

	/**
	 * Do a PUT on the Google Apps apis as the current user.
	 *
	 * @param googlePath The google API path to get, not including the "www.googleapis.com"
	 * @return The Google API server's response.
	 */
	/*@PUT
	@Path("/{googlepath:.*}")
	@Produces({MediaType.APPLICATION_JSON})
	public String put(@PathParam(value = "googlepath") String googlePath,
	                  @Context HttpServletRequest request,
	                  @Context HttpServletResponse response) {
		String accessToken = getAccessToken(request);
		return doMethod(HttpMethod.PUT, RestUtils.convertToEntity(request, accessToken), googlePath);
	}*/

	/**
	 * POST to an URL on the Google Apps apis as the current user.
	 *
	 * @param googlePath The google API path to get, not including the "www.googleapis.com"
	 * @return The Google API server's response.
	 */
	/*@POST
	@Path("/{googlepath:.*}")
	@Produces({MediaType.APPLICATION_JSON})
	public String post(@PathParam(value = "googlepath") String googlePath,
	                   @Context HttpServletRequest request,
	                   @Context HttpServletResponse response) {
		String accessToken = getAccessToken(request);
		return doMethod(HttpMethod.POST, RestUtils.convertToEntity(request, accessToken), googlePath);
	}*/

	/**
	 * DELETE an URL on the Google Apps apis as the current user.
	 *
	 * @param googlePath The google API path to get, not including the "www.googleapis.com"
	 * @return The Google API server's response.
	 */
	/*@DELETE
	@Path("/{googlepath:.*}")
	@Produces({MediaType.APPLICATION_JSON})
	public String delete(@PathParam(value = "googlepath") String googlePath,
	                     @Context HttpServletRequest request,
	                     @Context HttpServletResponse response) {
		String accessToken = getAccessToken(request);
		return doMethod(HttpMethod.DELETE, RestUtils.convertToEntity(request, accessToken), googlePath);
	}*/

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
	@Path("requestAuthorization")
	public Response requestAuthorization(@Context HttpServletRequest request, @QueryParam("afterAuthUrl") String afterAuthUrl) {
		String userId = request.getRemoteUser();
		if (userId != null) {
			try {
				GoogleAuthorizationCodeRequestUrl googleAuthUrl = flow.newAuthorizationUrl();
				googleAuthUrl.setRedirectUri("http://localhost:8080/api/google/gappsOAuthResponse"); // TODO parameterize localhost and encode URL
				if (afterAuthUrl != null) {
					String state = Base64.encode(afterAuthUrl, "UTF-8");
					googleAuthUrl.setState(state);
				}
				googleAuthUrl.build();
				LOGGER.info("Redirecting to Google Authorization URL = " + googleAuthUrl.toString());
				return Response.seeOther(URI.create(googleAuthUrl.toString())).build();
			} catch (IOException e) {
				LOGGER.error(e);
				throw new InternalServerErrorException(e.getMessage());
			}
		}
		throw new UnauthorizedClientException("User must be logged in to request Google authorization");
	}

	@GET
	@Path("gappsOAuthResponse")
	public Response handleGoogleAuthorizationCallback(@Context HttpServletRequest request, @QueryParam("state") String state) {
		String userId = request.getRemoteUser();
		LOGGER.info("Got a Google auth callback for user " + userId + "; query string = " + request.getQueryString());
		String code = request.getParameter("code");

		if (userId != null && code != null) {

			GoogleAuthorizationCodeTokenRequest tokenRequest = flow.newTokenRequest(code);
			tokenRequest.setRedirectUri("http://localhost:8080/api/google/gappsOAuthResponse"); // TODO parameterize localhost

			try {
				GoogleTokenResponse tokenResponse = tokenRequest.execute();
				Credential credential = flow.createAndStoreCredential(tokenResponse, userId);
				LOGGER.info("We have a credential. Token = " + credential.getAccessToken() + "; refresh Token = " + credential.getRefreshToken()
						+ "; expires = " + credential.getExpiresInSeconds());
				String token = credential.getAccessToken();

				if (oAuth2Dao.getToken(userId, GOOGLE_APP_ID) == null) {
					LOGGER.info("Adding access token for user " + userId);
					oAuth2Dao.insert(userId, GOOGLE_APP_ID, token);
				} else {
					LOGGER.info("Updating access token for user " + userId);
					oAuth2Dao.update(userId, GOOGLE_APP_ID, token);
				}

			} catch (IOException e) {
				LOGGER.error(e);
				throw new InternalServerErrorException(e.getMessage());
			}
		} else {
			LOGGER.warn("Could not get auth code from Google auth callback");
			oAuth2Dao.delete(userId, GOOGLE_APP_ID);
		}

		// redirect to the page the UI originally requested
		String afterAuthUrl = "/secure/dashboard";
		if (state != null) {
			try {
				afterAuthUrl = Base64.decode(state, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				LOGGER.error("UTF-8 is not supported??");
			}
		}
		return Response.seeOther(URI.create(afterAuthUrl)).build();

	}

}
