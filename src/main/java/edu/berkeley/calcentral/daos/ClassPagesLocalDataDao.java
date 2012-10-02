package edu.berkeley.calcentral.daos;

import com.google.common.base.Strings;
import edu.berkeley.calcentral.domain.ClassPage;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class ClassPagesLocalDataDao extends BaseDao {

	public void mergeLocalData(final ClassPage page) {
		String sql = "SELECT webcastId, canvasCourseId " +
				"FROM calcentral_classpages_localdata " +
				"WHERE classPageId = :classPageId ";
		MapSqlParameterSource params = new MapSqlParameterSource("classPageId", page.getClassId());
		queryRunner.queryForObject(sql, params, new RowMapper<Object>() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				page.getCourseinfo().setWebcastId(Strings.nullToEmpty(rs.getString("webcastId")));
				page.getCourseinfo().setCanvasCourseId(Strings.nullToEmpty(rs.getString("canvasCourseId")));
				return "1";
			}
		});
	}

	public void updateWebcastId(String classPageId, String webcastId) {
		String sql = "UPDATE calcentral_classpages_localdata " +
				"SET webcastId = :webcastId " +
				"WHERE classPageId = :classPageId ";
		MapSqlParameterSource params = new MapSqlParameterSource("classPageId", classPageId).
				addValue("webcastId", webcastId);
		if (queryRunner.update(sql, params) != 1) {
			sql = "INSERT INTO calcentral_classpages_localdata ( classPageId, webcastId ) " +
					"VALUES ( :classPageId, :webcastId ) ";
			queryRunner.update(sql, params);
		}
	}

	public void updateCanvasId(String classPageId, String canvasCourseId) {
		String sql = "UPDATE calcentral_classpages_localdata " +
				"SET canvasCourseId = :canvasCourseId " +
				"WHERE classPageId = :classPageId ";
		MapSqlParameterSource params = new MapSqlParameterSource("classPageId", classPageId).
				addValue("canvasCourseId", canvasCourseId);
		if (queryRunner.update(sql, params) != 1) {
			sql = "INSERT INTO calcentral_classpages_localdata ( classPageId, canvasCourseId ) " +
					"VALUES ( :classPageId, :canvasCourseId ) ";
			queryRunner.update(sql, params);
		}
	}

}
