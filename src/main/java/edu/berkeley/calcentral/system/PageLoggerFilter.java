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
public class PageLoggerFilter extends OncePerRequestFilter {

	@Override
	public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
	                             FilterChain chain) throws IOException, ServletException {
		String userUid = "<anonymous>";
		if (request.getUserPrincipal() != null) {
			userUid = request.getUserPrincipal().getName();
		}
		String uid = "UID: " + userUid;
		StringBuilder logMessage = new StringBuilder()
				.append("User Request - ")
				.append(request.getMethod())
				.append(" ")
				.append(request.getRequestURL())
				.append("; ")
				.append(uid);
		logger.info(logMessage);
		chain.doFilter(request, response);
	}

}
