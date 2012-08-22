/**
 * UserAuthorization.java
 * Copyright (c) 2012 The Regents of the University of California
 */
package edu.berkeley.calcentral.services;

import edu.berkeley.calcentral.daos.UserServiceDao;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Main service/helper used to check user authorization. Also used by
 * spring security for pageAuth checks.
 */
@Service
public class UserAuthorization implements UserDetailsService {

	/** Dao interface. */
	private UserServiceDao dao;
	
	public UserDetails loadUserByUsername(String uid) throws UsernameNotFoundException {
	    return dao.getUserDetails(uid);
	}
	
	/**
	 * @return the dao
	 */
	public UserServiceDao getDao() {
		return dao;
	}

	/**
	 * @param dao the dao to set
	 */
	public void setDao(UserServiceDao dao) {
		this.dao = dao;
	}
	
}
