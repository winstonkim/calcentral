package edu.berkeley.calcentral.system;

import org.jboss.resteasy.spi.WriterException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Map;

@Service

/**
 * Utilities class for dealing with preparing elements for Restful requests.
 */
public class RestUtils {

	@SuppressWarnings("unchecked")
	public static HttpEntity<MultiValueMap<String, Object>> convertToEntity(HttpServletRequest request, String accessToken) {
		MultiValueMap<String, Object> params = new LinkedMultiValueMap<String, Object>();
		Enumeration<String> requestParams = request.getParameterNames();
		while (requestParams.hasMoreElements()) {
			String paramName = requestParams.nextElement();
			params.add(paramName, request.getParameter(paramName));
		}
		return convertToEntity(params, accessToken);
	}

	@SuppressWarnings("unchecked")
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

	public static String pathPlusQueryString(String path, HttpServletRequest request) {
		String fullGetPath;
		if (request.getQueryString() != null) {
			try {
				/*
				 * This should help support both the cases of encoded and non-encoded query strings.
				 * If the query string is already decoded, then there should be no change, else the
				 * query string will be decoded and then handled by restTemplate (so it doesn't encode twice).
				 */
				fullGetPath = path + "?" + URLDecoder.decode(request.getQueryString(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				String errorString = "Could not decode query string: " + e.getMessage();
				throw new WriterException(errorString);
			}
		} else {
			fullGetPath = path;
		}
		return fullGetPath;
	}

}