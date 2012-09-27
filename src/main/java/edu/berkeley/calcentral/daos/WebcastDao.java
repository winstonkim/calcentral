package edu.berkeley.calcentral.daos;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class WebcastDao extends BaseDao {

	public String getWebcastId(String classPageId) {
		String sql = "SELECT webcastId FROM calcentral_webcasts WHERE classPageId = :classPageId ";
		MapSqlParameterSource params = new MapSqlParameterSource("classPageId", classPageId);
		return queryRunner.queryForObject(sql, params, String.class);
	}
}
