package edu.berkeley.calcentral.daos;

import edu.berkeley.calcentral.domain.User;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;

@Repository
public class UserDao extends BaseDao {

	public User get(String uid) {
		String sql = "SELECT uid, preferredName, link, firstLogin " +
				"FROM calcentral_users " +
				"WHERE uid = :uid";
		MapSqlParameterSource params = new MapSqlParameterSource("uid", uid);
		return queryRunner.queryForObject(sql, params, new BeanPropertyRowMapper<User>(User.class));
	}

	public void update(User user) {
		// update only non-null non-blank fields
		StringBuilder sql = new StringBuilder("UPDATE calcentral_users SET uid=uid");
		MapSqlParameterSource params = new MapSqlParameterSource("uid", user.getUid());
		if (StringUtils.hasLength(user.getPreferredName())) {
			sql.append(" ,preferredName = :preferredName");
			params.addValue("preferredName", user.getPreferredName());
		}
		if (StringUtils.hasLength(user.getLink())) {
			sql.append(" ,link = :link");
			params.addValue("link", user.getLink());
		}
		sql.append(" WHERE uid = :uid");
		queryRunner.update(sql.toString(), params);
	}

	public void insert(String uid) {
		String sql = "INSERT INTO calcentral_users " +
				"( uid, firstLogin ) " +
				"VALUES " +
				"( :uid, :firstLogin ) ";
		MapSqlParameterSource params = new MapSqlParameterSource("uid", uid)
			.addValue("firstLogin", new Timestamp(System.currentTimeMillis()));
		queryRunner.update(sql, params);
	}

	public void delete(String uid) {
		String sql = "DELETE FROM calcentral_users " +
				"WHERE uid = :uid";
		MapSqlParameterSource params = new MapSqlParameterSource("uid", uid);
		queryRunner.update(sql, params);
	}

}
