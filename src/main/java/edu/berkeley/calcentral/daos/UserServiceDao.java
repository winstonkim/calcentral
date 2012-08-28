/**
 * UserServiceDao.java
 * Copyright (c) 2012 The Regents of the University of California
 */
package edu.berkeley.calcentral.daos;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Dao Used for populating a spring security user object.
 */
@Repository
public class UserServiceDao {

	@Autowired @Qualifier("dataSource")
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
	public User getUserDetails(String uid) {
		//sanity check
		if (uid == null || uid.isEmpty()) {
			throw new UsernameNotFoundException("User: " + uid + " not found!");
		}

		return fetchUser(uid);
	}

	/**
	 * Fetch the user from the database and populate a UserDetails/User object
	 * 
	 * @param uid calnet uid string
	 * @return UserDetails/User object.
	 */
	private User fetchUser(String uid) {
		Map<String, String> params = Maps.newHashMap();
		params.put("calnetUID", uid);

		NamedParameterJdbcTemplate paramedQueryRunner = new NamedParameterJdbcTemplate(dataSource);
		String sql = 
				"   SELECT cu.uid username, "
						+ "   'testuser' passwordString, "
						+ "   cu.activeFlag activeFlag "
						+ " FROM calcentral_users cu "
						+ " WHERE "
						+ "   cu.uid = :calnetUID";

		Map<String, Object> results =  paramedQueryRunner.queryForMap(sql, params);
		String username = Strings.nullToEmpty((String) results.get("username"));
		String password = Strings.nullToEmpty((String) results.get("passwordString"));
		boolean active = (Boolean) results.get("activeFlag");

		//sanity check
		if (username.isEmpty() || password.isEmpty()) {
			throw new UsernameNotFoundException("User: " + uid + " not found!");
		}

		//Only one role for now to make @PreAuth simple and easy.
		List<SimpleGrantedAuthority> authorities = Lists.newArrayList();
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

		return new User(username, password, active, active, active, active, authorities);
	}

}
