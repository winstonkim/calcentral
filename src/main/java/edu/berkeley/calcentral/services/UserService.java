/**
 * UserDataController.java
 * Copyright (c) 2012 The Regents of the University of California
 */
package edu.berkeley.calcentral.services;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.common.collect.Maps;
import edu.berkeley.calcentral.domain.WidgetData;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.berkeley.calcentral.Params;
import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.daos.UserDataDao;
import edu.berkeley.calcentral.daos.WidgetDataDao;
import edu.berkeley.calcentral.domain.CalCentralUser;

import java.util.List;
import java.util.Map;

@Service
@Path(Urls.SPECIFIC_USER)
public class UserService {

	private ObjectMapper jMapper = new ObjectMapper();

	@Autowired
	private UserDataDao userDataDao;

	@Autowired
	private WidgetDataDao widgetDataDao;

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Map<String, Object> getUser(@PathParam(Params.USER_ID) String userID) {
		Map<String, Object> userData = Maps.newHashMap();
		CalCentralUser user = userDataDao.get(userID);
		userData.put("user", user);
		List<WidgetData> widgetData = widgetDataDao.getAllWidgetData(userID);
		userData.put("widgetData", widgetData);
		return userData;
	}

	@POST
	@Produces({MediaType.APPLICATION_JSON})
	public CalCentralUser saveUserData(@PathParam(Params.USER_ID) String userID,
			@FormParam(Params.DATA) String jsonData) {
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
	public void deleteUserAndWidgetData(@PathParam(Params.USER_ID) String userID) {
		userDataDao.delete(userID);
		widgetDataDao.deleteAllWidgetData(userID);
	}

}
