/**
 * UserDataController.java
 * Copyright (c) 2012 The Regents of the University of California
 */
package edu.berkeley.calcentral.services;

import com.google.common.collect.Maps;
import edu.berkeley.calcentral.Params;
import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.daos.UserDao;
import edu.berkeley.calcentral.daos.WidgetDataDao;
import edu.berkeley.calcentral.domain.User;
import edu.berkeley.calcentral.domain.WidgetData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@Path(Urls.SPECIFIC_USER)
public class UserService implements UserDetailsService {

	private static final Log LOGGER = LogFactory.getLog(UserService.class);

	private ObjectMapper jMapper = new ObjectMapper();

	@Autowired
	private UserDao userDao;

	@Autowired
	private WidgetDataDao widgetDataDao;

	@Autowired
	private CampusPersonDataService campusPersonDataService;

	/**
	 * Get all the information about a user.
	 *
	 * @param userID The user ID to retrieve
	 * @return JSON data: <pre>
	 *                 {
	 *                   user : {@link edu.berkeley.calcentral.domain.User},
	 *                   widgetData : {@link edu.berkeley.calcentral.domain.WidgetData},
	 *                   campusData : {@link java.util.Map}
	 *                 }
	 *                 </pre>
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Map<String, Object> getUser(@PathParam(Params.USER_ID) String userID) {
		Map<String, Object> userData = Maps.newHashMap();
		try {
			User user = userDao.get(userID);
			userData.put("user", user);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
		List<WidgetData> widgetData = widgetDataDao.getAllWidgetData(userID);
		userData.put("widgetData", widgetData);
		userData.put("campusData", campusPersonDataService.getPersonAttributes(userID));
		return userData;
	}

	/**
	 * Used by Spring framework to get user authentication. Inserts a new row into our local database
	 * if it's not already in the calcentral_users table, seeding data from the campus data source.
	 *
	 * @param uid The user id
	 * @return Authenticated user as a {@link User} instance.
	 * @throws UsernameNotFoundException
	 */
	public UserDetails loadUserByUsername(String uid) throws UsernameNotFoundException {
		User user;
		try {
			user = userDao.get(uid);
		} catch (EmptyResultDataAccessException e) {
			Map<String, Object> campusPersonData = campusPersonDataService.getPersonAttributes(uid);
			String preferredName = null;
			Object preferredNameObj = campusPersonData.get("PERSON_NAME");
			if ( preferredNameObj != null ) {
				preferredName = String.valueOf(preferredNameObj);
			}
			userDao.insert(uid, preferredName);
			user = userDao.get(uid);
		}
		return user;
	}

	@POST
	@Produces({MediaType.APPLICATION_JSON})
	public Map<String, Object> saveUserData(@PathParam(Params.USER_ID) String userID,
																					@FormParam(Params.DATA) String jsonData) throws IOException {
		User userToSave = jMapper.readValue(jsonData, User.class);
		userToSave.setUid(userID);
		LOGGER.info("Saving user: " + userToSave);
		userDao.update(userToSave);
		return getUser(userID);
	}

	@DELETE
	public void deleteUserAndWidgetData(@PathParam(Params.USER_ID) String userID) {
		userDao.delete(userID);
		widgetDataDao.deleteAllWidgetData(userID);
	}

}
