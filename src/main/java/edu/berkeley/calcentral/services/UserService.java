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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.spi.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
	 * @return JSON data:
	 *	<pre>
	 *	{
	 *		user : {@link edu.berkeley.calcentral.domain.User},
	 *		widgetData : {@link java.util.List}
	 *	}
	 *	</pre>
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Map<String, Object> getUser(@PathParam(Params.USER_ID) String userID) {
		Map<String, Object> userData = Maps.newHashMap();
		User user;
		try {
			user = userDao.get(userID);
			campusPersonDataService.mergeCampusData(user);
			userData.put("user", user);
		} catch (EmptyResultDataAccessException e) {
			throw new NotFoundException("User " + userID + " could not be found");
		}
		List<Map<String, Object>> widgetData = widgetDataDao.getAllWidgetData(userID);
		userData.put("widgetData", widgetData);
		return userData;
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
		LOGGER.info("Deleting user: " + userID);
		userDao.delete(userID);
		widgetDataDao.deleteAllWidgetData(userID);
	}

	/**
	 * Used by Spring framework to get user authentication. Inserts a new row into our local database
	 * if it's not already in the calcentral_users table, seeding data from the campus data source.
	 *
	 * @param uid The user id
	 * @return Authenticated user as an instance of {@link org.springframework.security.core.userdetails.User}.
	 * @throws UsernameNotFoundException
	 */
	public UserDetails loadUserByUsername(String uid) throws UsernameNotFoundException {
		User user;
		try {
			user = userDao.get(uid);
		} catch (EmptyResultDataAccessException e) {
			userDao.insert(uid);
			user = userDao.get(uid);
		}
		if (user == null) {
			throw new UsernameNotFoundException("User " + uid + " does not exist");
		}
		Collection<GrantedAuthority> roles = new ArrayList<GrantedAuthority>(1);
		roles.add(new SimpleGrantedAuthority("ROLE_USER"));
		return new org.springframework.security.core.userdetails.User(
				user.getUid(), "", true, true, true, true, roles);
	}

}
