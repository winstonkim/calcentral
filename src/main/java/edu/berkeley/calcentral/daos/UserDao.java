package edu.berkeley.calcentral.daos;

import com.google.common.collect.Maps;
import edu.berkeley.calcentral.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.Map;

@Repository
public class UserDao {

	@Autowired
	@Qualifier("dataSource")
	private DataSource dataSource;

	private NamedParameterJdbcTemplate template;

	@PostConstruct
	private void init() {
		template = new NamedParameterJdbcTemplate(dataSource);
	}

	public User get(String uid) {
		String sql = "SELECT uid, preferredName, link, firstLogin " +
				"FROM calcentral_users " +
				"WHERE uid = :uid";
		Map<String, String> params = Maps.newHashMap();
		params.put("uid", uid);
		return template.queryForObject(sql, params, new BeanPropertyRowMapper<User>(User.class));
	}

	public void update(User user) {
		String sql = "UPDATE calcentral_users " +
				"SET preferredName = :preferredName, " +
				"link = :link " +
				"WHERE uid = :uid";
		Map<String, String> params = Maps.newHashMap();
		params.put("uid", user.getUid());
		params.put("preferredName", user.getPreferredName());
		params.put("link", user.getLink());
		template.update(sql, params);
	}

	public void insert(String uid, String preferredName) {
		String sql = "INSERT INTO calcentral_users " +
				"( uid, preferredName, link, firstLogin ) " +
				"VALUES " +
				"( :uid, :preferredName, :link, :firstLogin ) ";
		Map<String, Object> params = Maps.newHashMap();
		params.put("uid", uid);
		params.put("preferredName", preferredName);
		params.put("link", "https://calnet.berkeley.edu/directory/details.pl?uid=" + uid);
		params.put("firstLogin", new Timestamp(System.currentTimeMillis()));
		template.update(sql, params);
	}

	public void delete(String uid) {
		String sql = "DELETE FROM calcentral_users " +
				"WHERE uid = :uid";
		Map<String, String> params = Maps.newHashMap();
		params.put("uid", uid);
		template.update(sql, params);
	}

}
