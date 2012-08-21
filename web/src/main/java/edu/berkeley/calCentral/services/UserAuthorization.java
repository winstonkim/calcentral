/**
 * UserAuthorization.java
 * Copyright (c) 2012 The Regents of the University of California
 */
package edu.berkeley.calcentral.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import edu.berkeley.calcentral.daos.IUserAuthorizationDao;

/**
 * Main service/helper used to check user authorization. Also used by
 * spring security for pageAuth checks.
 */
@Service
public class UserAuthorization implements UserDetailsService {

	/** Dao interface. */
	private IUserAuthorizationDao dao;
	
	public UserDetails loadUserByUsername(String uid) throws UsernameNotFoundException {
	    return dao.getUserDetails(uid);
	}
	
	/**
	 * Whether or not the user is a sysUser.
	 * 
	 * @param uid uid of user.
	 * @return true/false if the user is SYS.
	 * @throws UsernameNotFoundException when uid is not found.
	 */
	public boolean isSysUser(String uid) throws UsernameNotFoundException {
	    return dao.isSysUser(uid);
	}
	
	/**
	 * @return the dao
	 */
	public IUserAuthorizationDao getDao() {
		return dao;
	}

	/**
	 * @param dao the dao to set
	 */
	public void setDao(IUserAuthorizationDao dao) {
		this.dao = dao;
	}
	
}
