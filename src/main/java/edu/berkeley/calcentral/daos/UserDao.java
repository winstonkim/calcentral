package edu.berkeley.calcentral.daos;

import com.google.common.collect.Maps;
import edu.berkeley.calcentral.domain.User;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Map;

@Repository
public class UserDao extends BaseDao {

	public User get(String uid) {
		String sql = "SELECT uid, preferredName, link, firstLogin " +
				"FROM calcentral_users " +
				"WHERE uid = :uid";
		Map<String, String> params = Maps.newHashMap();
		params.put("uid", uid);
		return queryRunner.queryForObject(sql, params, new BeanPropertyRowMapper<User>(User.class));
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
		queryRunner.update(sql, params);
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
		queryRunner.update(sql, params);
	}

	public void delete(String uid) {
		String sql = "DELETE FROM calcentral_users " +
				"WHERE uid = :uid";
		Map<String, String> params = Maps.newHashMap();
		params.put("uid", uid);
		queryRunner.update(sql, params);
	}

}
