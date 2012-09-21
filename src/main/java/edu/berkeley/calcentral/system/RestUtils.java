package edu.berkeley.calcentral.system;

import edu.berkeley.calcentral.daos.OAuth2Dao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.util.Enumeration;
import java.util.Map;

@Service

/**
 * Utilities class for dealing with preparing elements for Restful requests.
 */
public class RestUtils {

	public RestUtils() {}

	public static HttpEntity<MultiValueMap<String, Object>> convertToEntity(HttpServletRequest request, String accessToken) {
		MultiValueMap<String, Object> params = new LinkedMultiValueMap<String, Object>();
		Enumeration<String> requestParams = request.getParameterNames();
		while (requestParams.hasMoreElements()) {
			String paramName = requestParams.nextElement();
			params.add(paramName, request.getParameter(paramName));
		}
		return convertToEntity(params, accessToken);
	}

	public static HttpEntity<MultiValueMap<String, Object>> convertToEntity(Map data, String accessToken) {
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

}