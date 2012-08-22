/**
 * UserServiceDao.java
 * Copyright (c) 2012 The Regents of the University of California
 */
package edu.berkeley.calcentral.daos;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.google.common.collect.Lists;

/**
 * Dao Used for populating a spring security user object.
 */
public class UserServiceDao {

	private DataSource dataSource;
	
	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Pass through from UserAuthorization service/helper for "loadUserByUsername"
	 * 
	 * @param uid username provided by cas
	 * @return User object
	 * @throws UsernameNotFoundException on issues looking up the user.
	 */
	public User getUserDetails(String uid) throws UsernameNotFoundException {
	    //sanity check
	    if (uid == null || uid.isEmpty()) {
	        throw new UsernameNotFoundException("User: " + uid + " not found!");
	    }
	    
	    //Only one role for now to make @PreAuth simple and easy.
	    List<SimpleGrantedAuthority> authorities = Lists.newArrayList();
	    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
	    
	    //eventually this flag will be populated by the user object from the database.
	    boolean active = true;
	    
	    //db password: can probably use this when we eventually want to setup the cas bypass
	    String testpassword = "testuser";
	    
	    return new User(uid, testpassword, active, active, active, active, authorities);
    }
    
}
