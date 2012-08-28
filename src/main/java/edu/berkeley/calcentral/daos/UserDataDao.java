package edu.berkeley.calcentral.daos;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;

import edu.berkeley.calcentral.domain.CalCentralUser;
import edu.berkeley.calcentral.domain.UserData;
import edu.berkeley.calcentral.domain.WidgetData;

@Repository
public class UserDataDao {

	@Autowired @Qualifier("dataSource")
	private DataSource dataSource;

	@Autowired
	private WidgetDataDao widgetDataDao;

	public CalCentralUser get(String uid) {
		Map<String, String> params = Maps.newHashMap();
		params.put("uid", uid);

		NamedParameterJdbcTemplate queryRunner = new NamedParameterJdbcTemplate(dataSource);
		CalCentralUser user = null;
		try {
			user = queryRunner.queryForObject(SqlQueries.get, params, new BeanPropertyRowMapper<CalCentralUser>(CalCentralUser.class));
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

		return user;
	}

	public UserData getUserAndWidgetData(String uid) {
		UserData userData = new UserData();

		CalCentralUser user = this.get(uid);
		if (user == null) {
			return null;
		}
		userData.setUser(user);

		List<WidgetData> widgetData = widgetDataDao.getAllWidgetData(uid);
		if (widgetData != null) {
			userData.setWidgetData(widgetData);
		}

		return userData;
	}

	public void update(CalCentralUser user) {
		//TODO: fill me in
	}

	public void delete(String uid) {
		//TODO: fill me in    
	}

	private static class SqlQueries {
		static String get = " SELECT uid, firstname, lastname FROM calcentral_users WHERE uid = :uid";
	}
}
