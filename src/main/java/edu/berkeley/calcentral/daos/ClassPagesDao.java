package edu.berkeley.calcentral.daos;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.Produces;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import edu.berkeley.calcentral.Urls;

@Service
@Repository
@Path(Urls.CLASS_PAGES)
public class ClassPagesDao extends BaseDao {

	//Would prefer there to be two different ways of lookup up class info, one by "class ids" and one with data already split out.
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{year}/{term}/{courseID}")
	public List<Map<String, Object>>  foo(@PathParam("year") String year,
			@PathParam("term") String term,
			@PathParam("courseID") String courseID) {
		//sanity check
		if (Strings.nullToEmpty(year).isEmpty()
				|| Strings.nullToEmpty(term).isEmpty()
				|| Strings.nullToEmpty(courseID).isEmpty()) {
			return null;
		}
		Map<String, Object> params = Maps.newHashMap();
		params.put("year", Integer.parseInt(year));
		params.put("term", term);
		params.put("courseID", courseID);

		List<Map<String, Object>> result = campusQueryRunner.queryForList(SqlQueries.courseInfo, params);
		
		
		return result;
	}
	
	private static class SqlQueries {
		static String courseInfo = 
				//courseinfo bean
				" SELECT bci.COURSE_TITLE classtitle, " 
				+ " bci.INSTRUCTION_FORMAT format,"
				+ " decode(bci.CRED_CD, 'PF', 'PASSED/NOT PASSED', 'SU', 'SATISFACTORY/UNSATISFACTORY', 'Letter Grade') grading, "
				+ " '' prereqs, "
				+ " '' requirements, "
				+ " decode(bci.term_cd, 'B', 'Fall', 'C', 'Summer', 'D', 'Spring' ,'') term, "
				+ " '' semesters_offered, "
				+ " bci.term_yr year, "
				+ " bci.DEPT_DESCRIPTION department, "
				+ " bci.DEPT_NAME deptname, "
				+ " decode(bci.SCHEDULE_PRINT_CD, 'H', 'SEE NOTE', "
				+ "                               'G', 'SEE DEPT', "
				+ "                               'E', 'SEE DEPT', "
				+ "                               'D', '', "
				+ "                               'C', 'NONE', "
				+ "                               'B', 'TO BE ARRANGED', "
				+ "                                bci.COURSE_CNTL_NUM) coursenum, " 
				/*   this is going to handwave some of the other security implications related to SCHEDULE_PRINT_CD for the time being. See CLC-13 for details.*/
				+ " decode(bci.FIXED_UNIT, 0, "
				+ "		decode(bci.VARIABLE_UNIT_CD, 'F', bci.LOWER_RANGE_UNIT, "
				+ " 								 'R', bci.LOWER_RANGE_UNIT || ' - ' || bci.UPPER_RANGE_UNIT, "
				+ "                                  'E', bci.LOWER_RANGE_UNIT || ' or ' || bci.UPPER_RANGE_UNIT, '0'), bci.fixed_unit) units, "
				//rootbean
				+ " bci.TERM_YR || bci.TERM_CD || bci.COURSE_CNTL_NUM classid, " //
				+ " bci.COURSE_TITLE classtitle, "
				+ " bci.CATALOG_DESCRIPTION description, "
				//schedule bean
				+ " '' coords, "
				+ " trim(decode(bcs.room_number, null, '', bcs.room_number || ' ' || decode(bcs.building_name, null, '', 'NO FACILITY', '', bcs.BUILDING_NAME))) location, "
				+ " bcs.meeting_start_time || bcs.meeting_start_time_ampm_flag || '-' || bcs.meeting_end_time || bcs.meeting_end_time_ampm_flag time, "
				+ " bcs.MEETING_DAYS weekends " //disaster to decode...
				+ " FROM BSPACE_COURSE_INFO_VW bci "
				+ " LEFT JOIN BSPACE_CLASS_SCHEDULE_VW bcs on "
				+ "   (bcs.term_yr = bci.term_yr AND bcs.term_cd = bci.term_cd AND bci.COURSE_CNTL_NUM = bcs.COURSE_CNTL_NUM) "
				+ " WHERE bci.TERM_YR = :year AND bci.TERM_CD = :term AND bci.COURSE_CNTL_NUM = :courseID";
		
		static String schedule = "";
		
		static String instructors = "";
		
		static String sections = "";
	}
}
