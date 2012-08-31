package edu.berkeley.calcentral.services;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.daos.BaseDao;
import edu.berkeley.calcentral.domain.ClassPage;
import edu.berkeley.calcentral.domain.ClassPageCourseInfo;
import edu.berkeley.calcentral.domain.ClassPageInstructor;

@Service
@Path(Urls.CLASS_PAGES)
public class ClassPagesService extends BaseDao {
	
	private static final Log LOGGER = LogFactory.getLog(ClassPagesService.class);

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{ccc}")
	public ClassPage getClassInfo(@PathParam("ccc") String ccc) {
		//break the crappy smashed up url.
		String year = ccc.substring(0, 4);
		String term = ccc.substring(4, 5);
		String courseID = ccc.substring(5);
		return fetchClassInfo(year, term, courseID);
	}


	private ClassPage fetchClassInfo(String year, String term, String courseID) {
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

		
		ClassPage classPageResult = campusQueryRunner.queryForObject(SqlQueries.rootInfo, params, new BeanPropertyRowMapper<ClassPage>(ClassPage.class));
		ClassPageCourseInfo courseInfo = campusQueryRunner.queryForObject(SqlQueries.courseInfo, params, new BeanPropertyRowMapper<ClassPageCourseInfo>(ClassPageCourseInfo.class));
		cleanupCourseInfo(courseInfo);
		classPageResult.setCourseinfo(courseInfo);

		List<ClassPageInstructor> classPageInstructors = campusQueryRunner.query(SqlQueries.instructors, params, 
				new BeanPropertyRowMapper<ClassPageInstructor>(ClassPageInstructor.class));
		for (ClassPageInstructor instructor : classPageInstructors) {
			applyEmailDisclosure(instructor);
		}
		classPageResult.setInstructors(classPageInstructors);
		
		return classPageResult;
	}

	public void applyEmailDisclosure(ClassPageInstructor instructor) {
		if (instructor.getMisc_email_disclosure().equalsIgnoreCase("N")) {
			instructor.setEmail("");
		}
	}
	
	private void cleanupCourseInfo(ClassPageCourseInfo courseInfo) {
		gradingDecode(courseInfo);
		termDecode(courseInfo);
		courseNumDecode(courseInfo);
		unitsDecode(courseInfo);
	}


	private void gradingDecode(ClassPageCourseInfo courseInfo) {
		String grading = courseInfo.getGrading();
		Map<String, String> gradingDict = Maps.newHashMap();
		gradingDict.put("PF", "PASSED/NOT PASSED");
		gradingDict.put("SU", "SATISFACTORY/UNSATISFACTORY");
		String gradingLookup = gradingDict.get(grading);
		if (gradingLookup == null) {
			gradingLookup = "Letter Grade";
		}
		courseInfo.setGrading(gradingLookup);
		return;
	}

	private void termDecode(ClassPageCourseInfo courseInfo) {
		String term = courseInfo.getTerm();
		Map<String, String> termDict = Maps.newHashMap();
		termDict.put("B", "Spring");
		termDict.put("C", "Summer");
		termDict.put("D", "Fall");
		String termLookup = termDict.get(term);
		if (termLookup == null) {
			termLookup = "Letter Grade";
		}
		courseInfo.setTerm(termLookup);
		return;
	}

	/** 
	 * This is going to handwave some of the other security implications related to SCHEDULE_PRINT_CD for the time being. 
	 * See CLC-13 for details.
	 */
	private void courseNumDecode(ClassPageCourseInfo courseInfo) {
		String printcd = courseInfo.getMisc_scheduleprintcd();
		Map<String, String> printCdDict = Maps.newHashMap();
		printCdDict.put("H", "SEE NOTE");
		printCdDict.put("G", "SEE DEPT");
		printCdDict.put("E", "SEE DEPT");
		printCdDict.put("D", "");
		printCdDict.put("C", "NONE");
		printCdDict.put("B", "TO BE ARRANGED");
		String courseNumValue = printCdDict.get(printcd);
		if (courseNumValue == null) {
			courseNumValue = String.valueOf(courseInfo.getCoursenum());
		}
		courseInfo.setCoursenum(courseNumValue);
		return;
	}

	private void unitsDecode(ClassPageCourseInfo courseInfo) {
		String fixedUnit = courseInfo.getMisc_fixedunit();
		if (!fixedUnit.equalsIgnoreCase("0.0")) {
			courseInfo.setUnits(fixedUnit);
			return;
		}

		String lowerRangeUnit = courseInfo.getMisc_lowerRangeUnit();
		String upperRangeUnit = courseInfo.getMisc_upperRangeUnit();
		Map<String, String> variableUnitCdDict = Maps.newHashMap();
		variableUnitCdDict.put("F", lowerRangeUnit);
		variableUnitCdDict.put("R", lowerRangeUnit + " - " + upperRangeUnit);
		variableUnitCdDict.put("E", lowerRangeUnit + " or " + upperRangeUnit);
		String variableUnitCd = courseInfo.getMisc_variableUnitCd();
		String variableUnitValue = variableUnitCdDict.get(variableUnitCd);
		if (variableUnitValue == null) {
			variableUnitValue = "0";
		}
		courseInfo.setUnits(variableUnitValue);
		return;
	}

	private static class SqlQueries {
		
		static String rootInfo = " SELECT " 
				+ " bci.TERM_YR || bci.TERM_CD || bci.COURSE_CNTL_NUM classid, " //ignoring course control num permissions issues for now.
				+ " bci.COURSE_TITLE classtitle, "
				+ " bci.CATALOG_DESCRIPTION description "
				+ " FROM BSPACE_COURSE_INFO_VW bci "
				+ " WHERE bci.TERM_YR = :year AND bci.TERM_CD = :term AND bci.COURSE_CNTL_NUM = :courseID";
		
		static String courseInfo = " SELECT "
				+ " bci.COURSE_TITLE classtitle, " 
				+ " bci.INSTRUCTION_FORMAT format,"
				+ " bci.CRED_CD grading, "
				+ " '' prereqs, "
				+ " '' requirements, "
				+ " bci.term_cd term, "
				+ " '' semesters_offered, "
				+ " bci.term_yr year, "
				+ " bci.DEPT_DESCRIPTION department, "
				+ " bci.DEPT_NAME deptname, "
				+ " bci.SCHEDULE_PRINT_CD misc_scheduleprintcd, "
				+ " bci.COURSE_CNTL_NUM coursenum, " 
				+ " bci.FIXED_UNIT misc_fixedunit, "
				+ " bci.LOWER_RANGE_UNIT misc_lowerRangeUnit, "
				+ " bci.UPPER_RANGE_UNIT misc_upperRangeUnit, "
				+ " bci.VARIABLE_UNIT_CD misc_variableUnitCd, "
				+ " '' units, "
				+ " bci.TERM_YR || bci.TERM_CD || bci.COURSE_CNTL_NUM classid, " //ignoring course control num permissions issues for now.
				+ " bci.COURSE_TITLE classtitle, "
				+ " bci.CATALOG_DESCRIPTION description "
				+ " FROM BSPACE_COURSE_INFO_VW bci "
				+ " WHERE bci.TERM_YR = :year AND bci.TERM_CD = :term AND bci.COURSE_CNTL_NUM = :courseID";

		//TODO: filtered out the crappy data. Figure out the fields later.
		static String schedule = " SELECT "
				+ "		bcs.* "
				+ " FROM BSPACE_CLASS_SCHEDULE_VW bcs "
				+ " WHERE (bcs.TERM_YR = :year AND bcs.TERM_CD = :term AND bcs.COURSE_CNTL_NUM = :courseID"
				+ "   AND (trim(bcs.MEETING_DAYS) != 'UNSCHED' AND trim(bcs.BUILDING_NAME) != 'NO FACILITY')) ";
//		+ " LEFT JOIN BSPACE_CLASS_SCHEDULE_VW bcs on "
//		+ "   (bcs.term_yr = bci.term_yr AND bcs.term_cd = bci.term_cd AND bci.COURSE_CNTL_NUM = bcs.COURSE_CNTL_NUM) "
//		+ " '' coords, "
//		+ " trim(decode(bcs.room_number, null, '', bcs.room_number || ' ' || decode(bcs.building_name, null, '', 'NO FACILITY', '', bcs.BUILDING_NAME))) location, "
//		+ " bcs.meeting_start_time || bcs.meeting_start_time_ampm_flag || '-' || bcs.meeting_end_time || bcs.meeting_end_time_ampm_flag time, "
//		+ " bcs.MEETING_DAYS weekends " //disaster to decode...

		static String instructors = " SELECT "
				+ "    bpi.email_address email, "
				+ "    bpi.ldap_uid id, "
				+ "    bpi.person_name name, " //TODO: this can probably be pulled in to go against our perferred name instead.
				+ "    bpi.email_disclos_cd misc_email_disclosure,"
				+ "    '' office, " //not availble for now
				+ "    '' phone, " //not availble for now
				+ "    '' img, " //not availble for now
				+ "    '' title, " //not availble for now
				+ "    '' url " //not availble for now
				+ " FROM BSPACE_COURSE_INSTRUCTOR_VW bci "
				+ " JOIN BSPACE_PERSON_INFO_VW bpi on "
				+ "   (bpi.ldap_uid = bci.instructor_ldap_uid) "
				+ " WHERE bci.TERM_YR = :year AND bci.TERM_CD = :term AND bci.COURSE_CNTL_NUM = :courseID";

		static String sections = "";
	}
}
