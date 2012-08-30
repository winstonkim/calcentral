/**
 * UserDataController.java
 * Copyright (c) 2012 The Regents of the University of California
 */
package edu.berkeley.calcentral.services;

import com.google.common.collect.Maps;
import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.daos.UserDataDao;
import edu.berkeley.calcentral.domain.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Service
@Path(Urls.CURRENT_USER)
public class CurrentUserDataService {

	@Autowired
	private UserDataDao userDataDao;

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Map<String, Object> getCurrentUser(@Context HttpServletRequest request) {
		Map<String, Object> result = Maps.newHashMap();
		if (request.getUserPrincipal() != null) {
			String uid = request.getUserPrincipal().getName();
			UserData userData = userDataDao.getUserAndWidgetData(uid);
			result.put("currentUser", userData);
		} else {
			result.put("currentUser", Maps.newHashMap());
		}
		return result;
	}

}
