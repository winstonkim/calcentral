/**
 * UserDataController.java
 * Copyright (c) 2012 The Regents of the University of California
 */
package edu.berkeley.calcentral.services;

import edu.berkeley.calcentral.Urls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Service
@Path(Urls.CURRENT_USER)
public class CurrentUserDataService {

	@Autowired
	private UserDataService userDataService;

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Map<String, Object> getCurrentUser(@Context HttpServletRequest request) {
		if (request.getUserPrincipal() != null) {
			String uid = request.getUserPrincipal().getName();
			return userDataService.getUser(uid);
		} else {
			return new HashMap<String, Object>(0);
		}
	}

}
