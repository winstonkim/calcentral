package edu.berkeley.calCentral.system;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.filter.OncePerRequestFilter;


/**
 * Page Logger filter logs access for pages. 
 *
 */
public class PageLoggerFilter extends OncePerRequestFilter  {

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession();
        String calnetId = new StringBuffer()
            .append("CalnetID: ")
            .append((String) session.getAttribute("s_calnetid")).toString();
        String httpMethod = new StringBuffer()
                .append("Request Type: ")
                .append(httpRequest.getMethod()).toString();
        String path = new StringBuffer()
            .append("Path: ")
            .append(httpRequest.getPathInfo()).toString();
        String servletPath = new StringBuffer()
            .append("Servlet: ")
            .append(httpRequest.getContextPath()).toString();
        String uid = new StringBuffer()
            .append("UID: ")
            .append(httpRequest.getUserPrincipal().getName()).toString();
        StringBuffer logMessage = new StringBuffer()
            .append("User Request - ")
            .append(httpMethod).append("; ")
            .append(uid).append("; ")
            .append(calnetId).append("; ")
            .append(servletPath).append("; ")
            .append(path).append("; ");
        logger.warn(logMessage);
        chain.doFilter(request, response);
    }

}
