/**
 * UserDataController.java
 * Copyright (c) 2012 The Regents of the University of California
 */
package edu.berkeley.calcentral.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.berkeley.calcentral.RESTConstants;
import edu.berkeley.calcentral.daos.UserDataDao;
import edu.berkeley.calcentral.daos.WidgetDataDao;
import edu.berkeley.calcentral.domain.CalCentralUser;
import edu.berkeley.calcentral.domain.CurrentUser;
import edu.berkeley.calcentral.domain.UserData;

@Service
@Path(RESTConstants.PATH_API)
public class UserDataController {

	private ObjectMapper jMapper = new ObjectMapper();

	@Autowired
	private UserDataDao userDataDao;

	@Autowired
	private WidgetDataDao widgetDataDao;

	@GET
	@Path("currentUser")
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

	@GET
	@Path("user/{userID}")
	@Produces({MediaType.APPLICATION_JSON})
	public CurrentUser getUser(@PathParam(RESTConstants.PARAM_USER_ID) String userID) {
		UserData user = userDataDao.getUserAndWidgetData(userID);
		CurrentUser currentUser = new CurrentUser(user);
		return currentUser;
	}

	@POST
	@Path("user/{userID}")
	@Produces({MediaType.APPLICATION_JSON})
	public CalCentralUser saveUserData(@PathParam(RESTConstants.PARAM_USER_ID) String userID,
			@FormParam(RESTConstants.PARAM_DATA) String jsonData) {
		CalCentralUser userToSave = null;
		try {
			//making sure items serialize and deserialize properly before attempting to save.
			userToSave = jMapper.readValue(jsonData, CalCentralUser.class);
			userDataDao.update(userToSave);
			return userToSave;
		} catch(Exception e) {
			//ignore malformed data.
			return null;
		}
	}

	@DELETE
	@Path("user/{userID}")
	public void deleteUserAndWidgetData(@PathParam(RESTConstants.PARAM_USER_ID) String userID) {
		userDataDao.delete(userID);
		widgetDataDao.deleteAllWidgetData(userID);
	}

}
