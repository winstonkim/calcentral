package edu.berkeley.calcentral.system;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.j2ee.J2eePreAuthenticatedProcessingFilter;

/**
 * Overrides the predefined J2EE Preauthenticated filter to fit system needs. 
 * Does only one thing (for now):
 * 
 * 1) Extends the normal J2eePreAuthProcessing filter to inject in other interesting 
 * tidbits about the user in the httpRequest after a successful authentication.
 * Items are injected into the session and persists until user destroys their session.
 * 
 */
public class CustomPreAuthFilter extends J2eePreAuthenticatedProcessingFilter{

	private boolean alreadyCasAuthed = false;

	/** 
	 * Inject the session with additional Bebop specific information.
	 * @see org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter#successfulAuthentication(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.springframework.security.core.Authentication)
	 */
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) {
		super.successfulAuthentication(request, response, authResult);
		UserDetails principal = (UserDetails) authResult.getPrincipal();
		String uid = principal.getUsername();
		if (!alreadyCasAuthed) {
			logEvent(request, uid);
		}
	}

	private void logEvent(HttpServletRequest request,
			String uid) {
		String uidString = new StringBuffer()
		.append("UID: ")
		.append(uid).toString();
		String IP = new StringBuffer()
		.append("IP: ")
		.append(request.getRemoteAddr()).toString();
		StringBuffer event = new StringBuffer()
		.append("Authentication Success - ")
		.append(uidString).append("; ")
		.append(IP).append("; ");
		logger.warn(event.toString());
	}
}
