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
import java.util.Map;

@Repository
public class ClassListDao extends BaseDao {

	public List<College> getAllColleges(int limit) {
		String sql = "SELECT id, slug, title_prefix, title, cssclass " +
				"FROM calcentral_classtree_colleges " +
				"LIMIT :limit";
		SqlParameterSource params = new MapSqlParameterSource("limit", limit);
		return queryRunner.query(sql, params, new BeanPropertyRowMapper<College>(College.class));
	}

	public College getCollege(int id) {
		String sql = "SELECT id, slug, title_prefix, title, cssclass " +
				"FROM calcentral_classtree_colleges WHERE id = :id";
		SqlParameterSource params = new MapSqlParameterSource("id", id);
		return queryRunner.queryForObject(sql, params, new BeanPropertyRowMapper<College>(College.class));
	}

	public List<Department> getAllDepartments(int limit) {
		String sql = "SELECT d.dept_key AS key, d.title, d.id, d.college_id collegeID " +
				"FROM calcentral_classtree_colleges c, calcentral_classtree_departments d " +
				"WHERE c.id = d.college_id " +
				"ORDER BY d.college_id, d.dept_key " +
				"LIMIT :limit";
		SqlParameterSource params = new MapSqlParameterSource("limit", limit);
		return queryRunner.query(sql, params, new BeanPropertyRowMapper<Department>(Department.class));
	}

	public Department getDepartment(int departmentID, int collegeID) {
		String sql = "SELECT dept_key AS key, title, id, college_id collegeID " +
				"FROM calcentral_classtree_departments " +
				"WHERE id = :departmentID AND college_id = :collegeID " +
				"ORDER BY key";
		SqlParameterSource params = new MapSqlParameterSource("departmentID", departmentID)
				.addValue("collegeID", collegeID);
		return queryRunner.queryForObject(sql, params, new BeanPropertyRowMapper<Department>(Department.class));
	}

	public List<Department> getDepartments(int collegeID) {
		String sql = "SELECT dept_key AS key, title, id, college_id collegeID " +
				"FROM calcentral_classtree_departments " +
				"WHERE college_id = :college_id " +
				"ORDER BY key";
		SqlParameterSource params = new MapSqlParameterSource("college_id", collegeID);
		return queryRunner.query(sql, params, new BeanPropertyRowMapper<Department>(Department.class));
	}

	public List<ClassPage> getClasses(List<Department> departments) {
		List<String> deptKeys = new ArrayList<String>(departments.size());
		for (Department d : departments) {
			deptKeys.add(d.getKey());
		}
		String sql = " SELECT * FROM ( "
				+ "   SELECT DISTINCT "
				+ "   bci.TERM_YR || bci.TERM_CD || bci.COURSE_CNTL_NUM classid, "
				+ "   bci.COURSE_TITLE classtitle, "
				+ "   bci.DEPT_NAME department, "
				+ "   bci.CATALOG_DESCRIPTION description, "
				+ "   bci.CATALOG_ID catalogid, "
				+ "   bci.CATALOG_ROOT, bci.CATALOG_PREFIX, bci.CATALOG_SUFFIX_1, bci.CATALOG_SUFFIX_2 "
				+ "   FROM BSPACE_COURSE_INFO_VW bci "
				+ "   WHERE bci.DEPT_NAME IN ( :departments ) "
				+ "     AND TERM_YR = 2012 AND TERM_CD = 'D'" // TODO parameterize year and term when UI presents that choice
				+ "     AND bci.PRIMARY_SECONDARY_CD = :primary "
				+ "     AND bci.INSTRUCTION_FORMAT = :format "
				+ "   ORDER BY department, bci.CATALOG_ROOT, bci.CATALOG_PREFIX, bci.CATALOG_SUFFIX_1, bci.CATALOG_SUFFIX_2 "
				+ ") WHERE ROWNUM <= 30 "; // TODO parameterize pagination when UI needs it
		SqlParameterSource params = new MapSqlParameterSource("departments", deptKeys)
				.addValue("primary", "P")
				.addValue("format", "LEC");
		return campusQueryRunner.query(sql, params, new BeanPropertyRowMapper<ClassPage>(ClassPage.class));
	}

	public List<Map<String, Object>> getAllClassIDs(int limit) {
		MapSqlParameterSource params = new MapSqlParameterSource("limit", limit);
		return campusQueryRunner.queryForList(
				"SELECT bci.TERM_YR || bci.TERM_CD || bci.COURSE_CNTL_NUM classid, " +
						"bci.CATALOG_ROOT, bci.CATALOG_PREFIX, bci.CATALOG_SUFFIX_1, bci.CATALOG_SUFFIX_2 " +
						"FROM BSPACE_COURSE_INFO_VW bci " +
						"WHERE TERM_YR = 2012 AND TERM_CD = 'D' AND PRIMARY_SECONDARY_CD = 'P' AND INSTRUCTION_FORMAT = 'LEC' " +
						"AND ROWNUM <= :limit " +
						"ORDER BY bci.DEPT_NAME, bci.CATALOG_ROOT, bci.CATALOG_PREFIX, bci.CATALOG_SUFFIX_1, bci.CATALOG_SUFFIX_2 ",
				params);
	}

}
