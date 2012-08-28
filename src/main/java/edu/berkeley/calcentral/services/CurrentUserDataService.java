/**
 * UserDataController.java
 * Copyright (c) 2012 The Regents of the University of California
 */
package edu.berkeley.calcentral.services;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.daos.UserDataDao;
import edu.berkeley.calcentral.domain.CurrentUser;
import edu.berkeley.calcentral.domain.UserData;

@Service
@Path(Urls.CURRENT_USER)
public class CurrentUserDataService {

	@Autowired
	private UserDataDao userDataDao;

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public CurrentUser getCurrentUser(
			@Context HttpServletRequest request) {

		if (request.getUserPrincipal() == null) {
			return new CurrentUser();
		}
		String uid = request.getUserPrincipal().getName(); 
		UserData user = userDataDao.getUserAndWidgetData(uid);
		CurrentUser currentUser = new CurrentUser(user);
		return currentUser;
	}

}
