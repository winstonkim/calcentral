package edu.berkeley.calcentral.daos;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.berkeley.calcentral.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Repository
public class UserDao {

	@Autowired @Qualifier("dataSource")
	private DataSource dataSource;

	public User get(String uid) {
		String sql = "SELECT uid, preferredName, link, firstLogin FROM calcentral_users WHERE uid = :uid";
		Map<String, String> params = Maps.newHashMap();
		params.put("uid", uid);
		NamedParameterJdbcTemplate queryRunner = new NamedParameterJdbcTemplate(dataSource);
		try {
			return queryRunner.queryForObject(sql, params, new BeanPropertyRowMapper<User>(User.class));
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public void update(User user) {
		//TODO: fill me in
	}

	public void delete(String uid) {
		//TODO: fill me in    
	}

	public org.springframework.security.core.userdetails.User getUserDetails(String uid) {
		String sql = "SELECT cu.uid username FROM calcentral_users cu WHERE cu.uid = :calnetUID";
		Map<String, String> params = Maps.newHashMap();
		params.put("calnetUID", uid);
		NamedParameterJdbcTemplate query = new NamedParameterJdbcTemplate(dataSource);
		Map<String, Object> results = query.queryForMap(sql, params);
		String username = Strings.nullToEmpty((String) results.get("username"));
		boolean active = true;

		//Only one role for now to make @PreAuth simple and easy.
		List<SimpleGrantedAuthority> authorities = Lists.newArrayList();
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

		return new org.springframework.security.core.userdetails.User(username, "testuser", active, active, active, active, authorities);
	}

	public void insert(String uid, String preferredName) {
		String sql = "INSERT INTO calcentral_users ( uid, preferredName, link, firstLogin ) " +
				"VALUES ( :uid, :preferredName, :link, :firstLogin ) ";
		Map<String, Object> params = Maps.newHashMap();
		params.put("uid", uid);
		params.put("preferredName", preferredName);
		params.put("link", "https://calnet.berkeley.edu/directory/details.pl?uid=" + uid);
		params.put("firstLogin", new Timestamp(System.currentTimeMillis()));
		NamedParameterJdbcTemplate query = new NamedParameterJdbcTemplate(dataSource);
		query.update(sql, params);
	}

}
