package edu.berkeley.calcentral.system;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.j2ee.J2eePreAuthenticatedProcessingFilter;

/**
 * Overrides the predefined J2EE Preauthenticated filter to fit system needs. 
 * Does two main functions:
 * 1) Blow away existing authentication tokens, since it will end up caching
 * page_role information after a user authenticates. Rather do the queries on the
 * database instead of reloading the app every time someone's permissions changes. 
 * - This can probably be removed eventually once AuthDataEntry becomes enhanced to 
 * use SecurityContextFilters to find and manipulate particular authentication tokens 
 * to expire, and then roll this together with a cache of sorts to cache auth tokens.
 * 
 * 2) Extends the normal J2eePreAuthProcessing filter to inject in other interesting 
 * tidbits about the user in the httpRequest after a successful authentication.
 * Items are injected into the session and persists until user destroys their session.
 * 
 */
public class CustomPreAuthFilter extends J2eePreAuthenticatedProcessingFilter{
    
//    private BebopUserService userService;
    
    private boolean alreadyCasAuthed = false;
    
    private class LoggingParams {
        private String uid;
        private String calnetId;
    }
    
    /**
     * @return the userService
     */
//    public BebopUserService getUserService() {
//        return userService;
//    }


    /**
     * @param userService the userService to set
     */
//    public void setUserService(BebopUserService userService) {
//        this.userService = userService;
//    }

    /**
     * Blow away the old authentication token to always force reload from db.
     * @see org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        alreadyCasAuthed = (SecurityContextHolder.getContext().getAuthentication() != null);
        SecurityContextHolder.getContext().setAuthentication(null);
        super.doFilter(request, response, chain);
    }
    
    /** 
     * Inject the session with additional Bebop specific information.
     * @see org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter#successfulAuthentication(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.springframework.security.core.Authentication)
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) {
        super.successfulAuthentication(request, response, authResult);
        LoggingParams loggingParams = injectSessionParams(request, authResult);
        if (!alreadyCasAuthed) {
            logEvent(request, loggingParams);
        }
    }


    private void logEvent(HttpServletRequest request,
            LoggingParams loggingParams) {
        String uid = new StringBuffer()
            .append("UID: ")
            .append(loggingParams.uid).toString();
        String calnetId = new StringBuffer()
            .append("CalnetID: ")
            .append(loggingParams.calnetId).toString();
        String IP = new StringBuffer()
            .append("IP: ")
            .append(request.getRemoteAddr()).toString();
        StringBuffer event = new StringBuffer()
            .append("Authentication Success - ")
            .append(uid).append("; ")
            .append(calnetId).append("; ")
            .append(IP).append("; ");
        logger.warn(event.toString());
    }


    private LoggingParams injectSessionParams(HttpServletRequest request,
            Authentication authResult) {
        UserDetails principal = (UserDetails) authResult.getPrincipal();
        String uid = principal.getUsername();
        HttpSession session = request.getSession(true);
        
        String calnetID = (String) session.getAttribute("s_calnetid");
        String bebopUserId = (String) session.getAttribute("s_bebopuserid");
//        BebopUser user = null;
//        if (session.getAttribute("s_bebopuserid") == null
//                || session.getAttribute("s_calnetid") == null) {
//            user = userService.getUser(uid);
//        }
        
        if (calnetID == null) {
//            calnetID = user.getCalnetId();
            session.setAttribute("s_calnetid", calnetID);
        }
        
        if (bebopUserId == null) {
//            bebopUserId = user.getStaffId();
            session.setAttribute("s_bebopuserid", bebopUserId);
        }
        
        session.setAttribute("s_calnetuid", uid);
        LoggingParams returnParams = new LoggingParams();
        returnParams.uid = uid;
        returnParams.calnetId = calnetID;
        return returnParams;
    }
}
