package edu.berkeley.calcentral.daos;

import edu.berkeley.calcentral.domain.ClassPage;
import edu.berkeley.calcentral.domain.College;
import edu.berkeley.calcentral.domain.Department;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ClassListDao extends BaseDao {

	public College getCollege(String slug) {
		String sql = "SELECT id, slug, title_prefix, title, cssclass " +
				"FROM calcentral_classtree_colleges WHERE slug = :slug";
		SqlParameterSource params = new MapSqlParameterSource().addValue("slug", slug);
		return queryRunner.queryForObject(sql, params, new BeanPropertyRowMapper<College>(College.class));
	}

	public Department getDepartment(String departmentKey) {
		String sql = "SELECT dept_key AS key, title, id " +
				"FROM calcentral_classtree_departments " +
				"WHERE dept_key = :departmentKey " +
				"ORDER BY key";
		SqlParameterSource params = new MapSqlParameterSource().addValue("departmentKey", departmentKey);
		return queryRunner.queryForObject(sql, params, new BeanPropertyRowMapper<Department>(Department.class));
	}

	public List<Department> getDepartments(int collegeID) {
		String sql = "SELECT dept_key AS key, title, id " +
				"FROM calcentral_classtree_departments " +
				"WHERE college_id = :college_id " +
				"ORDER BY key";
		SqlParameterSource params = new MapSqlParameterSource().addValue("college_id", collegeID);
		return queryRunner.query(sql, params, new BeanPropertyRowMapper<Department>(Department.class));
	}

	public List<ClassPage> getClasses(List<Department> departments) {
		List<String> deptKeys = new ArrayList<String>(departments.size());
		for (Department d : departments) {
			deptKeys.add(d.getKey());
		}
		String sql = " SELECT DISTINCT "
				+ " bci.COURSE_TITLE classtitle, "
				+ " bci.DEPT_NAME department, "
				+ " bci.CATALOG_DESCRIPTION description, "
				+ " bci.CATALOG_ID catalogid "
				+ " FROM BSPACE_COURSE_INFO_VW bci "
				+ " WHERE bci.DEPT_NAME IN ( :departments ) "
				+ "   AND TERM_YR = 2012 AND TERM_CD = 'D'" // TODO parameterize year and term when UI presents that choice
				+ " ORDER BY department, catalogid ";
		SqlParameterSource params = new MapSqlParameterSource().addValue("departments", deptKeys);
		return campusQueryRunner.query(sql, params, new BeanPropertyRowMapper<ClassPage>(ClassPage.class));
	}
}
