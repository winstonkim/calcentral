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
import edu.berkeley.calcentral.domain.User;
import edu.berkeley.calcentral.domain.WidgetData;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import edu.berkeley.calcentral.Params;
import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.daos.UserDao;
import edu.berkeley.calcentral.daos.WidgetDataDao;

import java.util.List;
import java.util.Map;

@Service
@Path(Urls.SPECIFIC_USER)
public class UserService implements UserDetailsService {

	private ObjectMapper jMapper = new ObjectMapper();

	@Autowired
	private UserDao userDao;

	@Autowired
	private WidgetDataDao widgetDataDao;

	@Autowired
	private CampusPersonDataService campusPersonDataService;

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Map<String, Object> getUser(@PathParam(Params.USER_ID) String userID) {
		Map<String, Object> userData = Maps.newHashMap();
		User user = userDao.get(userID);
		userData.put("user", user);
		List<WidgetData> widgetData = widgetDataDao.getAllWidgetData(userID);
		userData.put("widgetData", widgetData);
		userData.put("campusData", campusPersonDataService.getPersonAttributes(userID));
		return userData;
	}

	@POST
	@Produces({MediaType.APPLICATION_JSON})
	public User saveUserData(@PathParam(Params.USER_ID) String userID,
													 @FormParam(Params.DATA) String jsonData) {
		User userToSave = null;
		try {
			//making sure items serialize and deserialize properly before attempting to save.
			userToSave = jMapper.readValue(jsonData, User.class);
			userDao.update(userToSave);
			return userToSave;
		} catch (Exception e) {
			//ignore malformed data.
			return null;
		}
	}

	@DELETE
	public void deleteUserAndWidgetData(@PathParam(Params.USER_ID) String userID) {
		userDao.delete(userID);
		widgetDataDao.deleteAllWidgetData(userID);
	}

	public UserDetails loadUserByUsername(String uid) throws UsernameNotFoundException {
		UserDetails userDetails;
		try {
			userDetails = userDao.getUserDetails(uid);
		} catch (EmptyResultDataAccessException e) {
			Map<String, Object> campusPersonData = campusPersonDataService.getPersonAttributes(uid);
			userDao.insert(uid, String.valueOf(campusPersonData.get("FIRST_NAME")) + " "
					+ String.valueOf(campusPersonData.get("LAST_NAME")));
			userDetails = userDao.getUserDetails(uid);
		}
		return userDetails;
	}
}
