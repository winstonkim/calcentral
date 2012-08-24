/**
 * UserAuthorization.java
 * Copyright (c) 2012 The Regents of the University of California
 */
package edu.berkeley.calcentral.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import edu.berkeley.calcentral.daos.UserServiceDao;

/**
 * Main service/helper used to check user authorization. Also used by
 * spring security for pageAuth checks.
 */
@Service
public class UserAuthorization implements UserDetailsService {

	/** Dao interface. */
	@Autowired
	private UserServiceDao userServiceDao;
	
	public UserDetails loadUserByUsername(String uid) throws UsernameNotFoundException {
	    return userServiceDao.getUserDetails(uid);
	}
	
	/**
	 * @return the dao
	 */
	public UserServiceDao getUserServiceDao() {
		return userServiceDao;
	}

	/**
	 * @param dao the dao to set
	 */
	public void setUserServiceDao(UserServiceDao userServiceDao) {
		this.userServiceDao = userServiceDao;
	}
	
}
