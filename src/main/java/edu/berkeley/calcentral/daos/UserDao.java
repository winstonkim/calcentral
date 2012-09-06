package edu.berkeley.calcentral.daos;

import com.google.common.collect.Maps;
import edu.berkeley.calcentral.domain.User;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

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
		// update only non-null non-blank fields
		StringBuilder sql = new StringBuilder("UPDATE calcentral_users SET uid=uid");
		Map<String, String> params = Maps.newHashMap();
		if (StringUtils.hasLength(user.getPreferredName())) {
			sql.append(" ,preferredName = :preferredName");
			params.put("preferredName", user.getPreferredName());
		}
		if (StringUtils.hasLength(user.getLink())) {
			sql.append(" ,link = :link");
			params.put("link", user.getLink());
		}
		sql.append(" WHERE uid = :uid");
		params.put("uid", user.getUid());
		queryRunner.update(sql.toString(), params);
	}

	public void insert(String uid) {
		String sql = "INSERT INTO calcentral_users " +
				"( uid, firstLogin ) " +
				"VALUES " +
				"( :uid, :firstLogin ) ";
		Map<String, Object> params = Maps.newHashMap();
		params.put("uid", uid);
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
