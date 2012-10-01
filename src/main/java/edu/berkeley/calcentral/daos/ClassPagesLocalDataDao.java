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

	public void updateLocalData(ClassPage page) {
		String sql = "UPDATE calcentral_classpages_localdata " +
				"SET webcastId = :webcastId,  " +
				"canvasCourseId = :canvasCourseId " +
				"WHERE classPageId = :classPageId ";
		MapSqlParameterSource params = new MapSqlParameterSource("classPageId", page.getClassId()).
				addValue("webcastId", page.getCourseinfo().getWebcastId()).
				addValue("canvasCourseId", page.getCourseinfo().getCanvasCourseId());
		queryRunner.update(sql, params);
	}

}
