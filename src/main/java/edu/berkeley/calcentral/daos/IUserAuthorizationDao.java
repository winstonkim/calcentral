/**
 * IUserAuthorizationDao.java
 * Copyright (c) 2012 The Regents of the University of California
 */
package edu.berkeley.calcentral.daos;

import org.springframework.security.core.userdetails.User;

/**
 * Interface for user authorization dao objects.
 *
 */
public interface IUserAuthorizationDao {
	/**
	 * Return the Spring security defined simple user object with the "principal" 
	 * (calnetUID passed back from authentication source, calnet).
	 * 
	 * @param uid calnet uid.
	 * @return User object.
	 */
	User getUserDetails(String uid);

    /**
     * Whether or not a user is a sys user.
     * 
     * @param uid calnet UID.
     * @return true/false if user is SYS.
     */
    boolean isSysUser(String uid);
}
