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
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.tasks.TasksScopes;
import com.google.common.collect.Lists;
import edu.berkeley.calcentral.Params;
import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.daos.OAuth2Dao;
import edu.berkeley.calcentral.system.RestUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.InternalServerErrorException;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
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

	public static final String googleBaseUrl = "https://www.googleapis.com/";

	@Autowired
	private GoogleCredentialStore credentialStore;

	@Autowired
	private OAuth2Dao oAuth2Dao;

	private GoogleAuthorizationCodeFlow flow;

	private String clientId;

	@SuppressWarnings("UnusedDeclaration")
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	private String clientSecret;

	@SuppressWarnings("UnusedDeclaration")
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	private String authCallbackUrl = "http://localhost:8080/api/google/gappsOAuthResponse";

	@SuppressWarnings("UnusedDeclaration")
	public void setAuthCallbackUrl(String authCallbackUrl) {
		this.authCallbackUrl = authCallbackUrl;
	}

	@PostConstruct
	public void init() {
		flow = new GoogleAuthorizationCodeFlow.Builder(
				new NetHttpTransport(),
				new JacksonFactory(),
				clientId,
				clientSecret,
				Lists.newArrayList(CalendarScopes.CALENDAR, TasksScopes.TASKS)).
				setCredentialStore(credentialStore).
				setAccessType("offline").             // "offline" and "force" so we get refresh token
				setApprovalPrompt("force").           // on every auth request, not just the first.
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
	public Response get(@PathParam(value = "googlepath") String googlePath,
	                    @Context HttpServletRequest request) {
		String userID = request.getRemoteUser();
		if (userID == null) {
			throw new UnauthorizedException("Google proxy is not supported for anonymous users");
		}

		Credential credential = getCredential(userID);

		try {
			return doMethod(HttpMethod.GET, RestUtils.convertToEntity(request, credential.getAccessToken()),
					RestUtils.pathPlusQueryString(googlePath, request));
		} catch (HttpClientErrorException error4xx) {
			if (error4xx.getStatusCode().value() == HttpResponseCodes.SC_UNAUTHORIZED) {
				oAuth2Dao.delete(userID, GoogleCredentialStore.GOOGLE_APP_ID);
			}
			return ServerResponse.ok().status(error4xx.getStatusCode().value()).entity(error4xx.getMessage()).build();
		}
	}

	/**
	 * Remove the current user's stored API key for google.
	 */
	@POST
	@Path("gappsOAuthEnabled")
	public void disableOAuth(@Context HttpServletRequest request, @FormParam(Params.METHOD) String method) {
		String uid = request.getRemoteUser();
		if (uid != null && method != null && method.equalsIgnoreCase("delete")) {
			oAuth2Dao.delete(uid, GoogleCredentialStore.GOOGLE_APP_ID);
		}
	}

	/**
	 * Endpoint for requesting a Google authorization token.
	 * This is explicitly set in the Google developer API control panel (configurable)
	 * and is responsible for starting the authorization request chain.
	 *
	 * @return will result in a redirect to the google.com authorization URL.
	 */
	@GET
	@Path("requestAuthorization")
	public Response requestAuthorization(@Context HttpServletRequest request, @QueryParam("afterAuthUrl") String afterAuthUrl) {
		String userId = request.getRemoteUser();
		if (userId == null) {
			throw new UnauthorizedException("Google proxy is not supported for anonymous users");
		}

		try {
			GoogleAuthorizationCodeRequestUrl googleAuthUrl = flow.newAuthorizationUrl();
			googleAuthUrl.setRedirectUri(authCallbackUrl);
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

	/**
	 * Endpoint for handling the callback from Google authorization server. Reads the authorization code
	 * from the request and makes a request to get the auth token and refresh token.
	 * @return Redirect to the afterAuthUrl specified in the call to requestAuthorization(), or /secure/dashboard
	 * if no aftetAuthUrl was specified.
	 */
	@GET
	@Path("gappsOAuthResponse")
	public Response handleGoogleAuthorizationCallback(@Context HttpServletRequest request, @QueryParam("state") String state) {
		String userId = request.getRemoteUser();
		if (userId == null) {
			throw new UnauthorizedException("Google proxy is not supported for anonymous users");
		}
		LOGGER.info("Got a Google auth callback for user " + userId + "; query string = " + request.getQueryString());
		String code = request.getParameter("code");

		if (code != null) {

			GoogleAuthorizationCodeTokenRequest tokenRequest = flow.newTokenRequest(code);
			tokenRequest.setRedirectUri(authCallbackUrl);

			try {
				GoogleTokenResponse tokenResponse = tokenRequest.execute();
				Credential credential = flow.createAndStoreCredential(tokenResponse, userId);
				logCredential(userId, credential);
			} catch (IOException e) {
				LOGGER.error(e);
				throw new InternalServerErrorException(e.getMessage());
			}
		} else {
			LOGGER.warn("Could not get auth code from Google auth callback");
			oAuth2Dao.delete(userId, GoogleCredentialStore.GOOGLE_APP_ID);
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

	private Response doMethod(HttpMethod method, HttpEntity entity, String googleUrl) {
		String url = googleBaseUrl + StringUtils.trimLeadingCharacter(googleUrl, '/');
		LOGGER.info("Doing " + method.toString() + " on url " + url);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(url, method, entity, String.class);
			LOGGER.debug("Response: " + response.getStatusCode() + "; body = " + response.getBody());
			LOGGER.debug("Response headers: " + response.getHeaders().toSingleValueMap());
			return ServerResponse.ok().status(response.getStatusCode().value()).entity(response.getBody()).build();
		} catch (HttpServerErrorException error5xx) {
			LOGGER.error(error5xx.getMessage(), error5xx);
			if (response != null) {
				return ServerResponse.serverError().status(response.getStatusCode().value()).entity(response.getBody()).build();
			}
		}
		return null;
	}

	private Credential getCredential(String userID) {
		Credential credential = null;

		try {
			credential = flow.loadCredential(userID);
		} catch (IOException e) {
			LOGGER.error("Error looking up credential for user " + userID, e);
			oAuth2Dao.delete(userID, GoogleCredentialStore.GOOGLE_APP_ID);
		}

		if ( credential == null ) {
			throw new InternalServerErrorException("Could not get Google credential for user " + userID);
		}

		logCredential(userID, credential);
		if (credential.getExpiresInSeconds() == null || credential.getExpiresInSeconds() < 100) {
			try {
				boolean refreshed = credential.refreshToken();
				LOGGER.info("Credential refreshed: " + refreshed);
			} catch (IOException e) {
				LOGGER.error("Error refreshing credential for user " + userID, e);
				oAuth2Dao.delete(userID, GoogleCredentialStore.GOOGLE_APP_ID);
				throw new InternalServerErrorException("Could not refresh Google authorization for user " + userID
						+ " due to IOException: " + e.getMessage());
			}
		}
		return credential;
	}

	private void logCredential(String userId, Credential credential) {
		LOGGER.info("We have a credential for " + userId + ". Token = " + credential.getAccessToken() + "; refresh Token = " + credential.getRefreshToken()
				+ "; expires in " + credential.getExpiresInSeconds() + "s");
	}

}
