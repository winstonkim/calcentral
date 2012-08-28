package edu.berkeley.calcentral.system;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Page Logger filter logs access for pages. 
 */
public class PageLoggerFilter extends OncePerRequestFilter  {

	@Override
	public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String httpMethod = new StringBuffer()
		.append("Request Type: ")
		.append(httpRequest.getMethod()).toString();
		String path = new StringBuffer()
		.append("Path: ")
		.append(httpRequest.getPathInfo()).toString();
		String servletPath = new StringBuffer()
		.append("Servlet: ")
		.append(httpRequest.getContextPath()).toString();
		String userUid = "<anonymous>";
		if (request.getUserPrincipal() != null) {
			userUid = request.getUserPrincipal().getName();
		}
		String uid = new StringBuffer()
		.append("UID: ")
		.append(userUid).toString();
		StringBuffer logMessage = new StringBuffer()
		.append("User Request - ")
		.append(httpMethod).append("; ")
		.append(uid).append("; ")
		.append(servletPath).append("; ")
		.append(path).append("; ");
		logger.warn(logMessage);
		chain.doFilter(request, response);
	}

}
