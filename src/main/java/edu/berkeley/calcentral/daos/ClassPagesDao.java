package edu.berkeley.calcentral.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;

import edu.berkeley.calcentral.domain.ClassPage;
import edu.berkeley.calcentral.domain.ClassPageCourseInfo;
import edu.berkeley.calcentral.domain.ClassPageInstructor;
import edu.berkeley.calcentral.domain.ClassPageSchedule;
import edu.berkeley.calcentral.domain.ClassPageSection;

@Repository
public class ClassPagesDao extends BaseDao {
	
	public ClassPage getBaseClassPage(int year, String term, String courseID) {
		Map<String, Object> params = setupParams(year, term, courseID);
		ClassPage classPageResult = campusQueryRunner.queryForObject(SqlQueries.rootInfo, params, new BeanPropertyRowMapper<ClassPage>(ClassPage.class));
		return classPageResult;
	}
	
	public ClassPageCourseInfo getCourseInfo(int year, String term, String courseID) {
		Map<String, Object> params = setupParams(year, term, courseID);
		params.put("format", "LEC");
		
		ClassPageCourseInfo courseInfo = campusQueryRunner.queryForObject(SqlQueries.courseInfo, params, new BeanPropertyRowMapper<ClassPageCourseInfo>(ClassPageCourseInfo.class));
		return courseInfo;
	}
	
	public List<ClassPageInstructor> getCourseInstructors(int year, String term, String courseID) {
		Map<String, Object> params = setupParams(year, term, courseID);
		List<ClassPageInstructor> classPageInstructors = campusQueryRunner.query(SqlQueries.instructors, params, 
				new BeanPropertyRowMapper<ClassPageInstructor>(ClassPageInstructor.class));
		return classPageInstructors;
	}
	
	public List<ClassPageSchedule> getCourseSchedules(int year, String term, String courseID) {
		Map<String, Object> params = setupParams(year, term, courseID);
		List<ClassPageSchedule> classPageSchedules = campusQueryRunner.query(SqlQueries.schedule, params, 
				new BeanPropertyRowMapper<ClassPageSchedule>(ClassPageSchedule.class));
		return classPageSchedules;
	}
	
	public List<ClassPageSection> getCourseSections(int year, String term, String deptName, String catalogId) {
		Map<String, Object> params = setupParams(year, term, "");
		params.put("deptName", deptName);
		params.put("catalogId", catalogId);
		List<ClassPageSection> classPageSections = campusQueryRunner.query(SqlQueries.sections, params, sectionMapper);
		return classPageSections;
	}
	
	private RowMapper<ClassPageSection> sectionMapper = new RowMapper<ClassPageSection>() {
		private BeanPropertyRowMapper<ClassPageSection> baseSectionMapper = new BeanPropertyRowMapper<ClassPageSection>(ClassPageSection.class);
		private BeanPropertyRowMapper<ClassPageSchedule> scheduleMapper = new BeanPropertyRowMapper<ClassPageSchedule>(ClassPageSchedule.class);
		
		public ClassPageSection mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			ClassPageSection classPageSection = baseSectionMapper.mapRow(rs, rowNum);
			ClassPageSchedule sectionSchedule = scheduleMapper.mapRow(rs, rowNum);
			sectionSchedule.decodeAll();
			classPageSection.setMisc_schedule(sectionSchedule);
			return classPageSection;
		}
	};
	
	private Map<String, Object> setupParams(int year, String term, String courseID) {
		Map<String, Object> params = Maps.newHashMap();
		params.put("year", year);
		params.put("term", term);
		params.put("courseID", courseID);
		return params;
	}
	
	
	private static class SqlQueries {
		static String rootInfo = " SELECT " 
				+ " bci.TERM_YR || bci.TERM_CD || bci.COURSE_CNTL_NUM classid, " //ignoring course control num permissions issues for now.
				+ " '' info_last_updated, "
				+ " bci.COURSE_TITLE classtitle, "
				+ " bci.CATALOG_DESCRIPTION description "
				+ " FROM BSPACE_COURSE_INFO_VW bci "
				+ " WHERE bci.TERM_YR = :year AND bci.TERM_CD = :term AND bci.COURSE_CNTL_NUM = :courseID";
		
		static String courseInfo = " SELECT "
				+ " bci.COURSE_TITLE title, " 
				+ " bci.INSTRUCTION_FORMAT format,"
				+ " bci.CRED_CD grading, "
				+ " '' prereqs, "
				+ " '' requirements, "
				+ " bci.term_cd term, "
				+ " '' semesters_offered, "
				+ " bci.term_yr year, "
				+ " bci.DEPT_DESCRIPTION department, "
				+ " bci.DEPT_NAME misc_deptname, "
				+ " bci.SCHEDULE_PRINT_CD misc_scheduleprintcd, "
				+ " bci.COURSE_CNTL_NUM coursenum, " 
				+ " bci.FIXED_UNIT misc_fixedunit, "
				+ " bci.LOWER_RANGE_UNIT misc_lowerRangeUnit, "
				+ " bci.UPPER_RANGE_UNIT misc_upperRangeUnit, "
				+ " bci.VARIABLE_UNIT_CD misc_variableUnitCd, "
				+ " '' units, "
				+ " bci.TERM_YR || bci.TERM_CD || bci.COURSE_CNTL_NUM classid, " //ignoring course control num permissions issues for now.
				+ " bci.COURSE_TITLE classtitle, "
				+ " bci.CATALOG_DESCRIPTION description, "
				+ " bci.CATALOG_ID misc_catalogid "
				+ " FROM BSPACE_COURSE_INFO_VW bci "
				+ " WHERE bci.TERM_YR = :year AND bci.TERM_CD = :term AND bci.COURSE_CNTL_NUM = :courseID and bci.INSTRUCTION_FORMAT = :format";

		//There's some ambiguous logic related to this. Not worrying about edge cases for the time being.
		//disregarding the online schedule of classes rules for now.
		static String schedule = " SELECT "
				+ " '' coords, "
				+ " trim(nvl(bcs.ROOM_NUMBER, '')) misc_room, "
				+ " trim(nvl(bcs.BUILDING_NAME, '')) misc_building_name, "
				+ " '' location, "
				+ " bcs.meeting_start_time || bcs.meeting_start_time_ampm_flag || '-' || bcs.meeting_end_time || bcs.meeting_end_time_ampm_flag time, "
				+ " bcs.MEETING_DAYS misc_weekdays, " 
				+ " '' weekdays, "
				+ " '' current_sem "
				+ " FROM BSPACE_CLASS_SCHEDULE_VW bcs "
				+ " WHERE (bcs.TERM_YR = :year AND bcs.TERM_CD = :term AND bcs.COURSE_CNTL_NUM = :courseID"
				+ "   AND (trim(bcs.MEETING_DAYS) != 'UNSCHED' AND trim(bcs.BUILDING_NAME) != 'NO FACILITY')) ";

		static String instructors = " SELECT "
				+ "   bpi.email_address email, "
				+ "   bpi.ldap_uid id, "
				+ "   bpi.person_name name, " 
				+ "   bpi.email_disclos_cd misc_email_disclosure,"
				+ "   '' office, " 
				+ "   '' phone, " 
				+ "   '' img, " 
				+ "   '' title, " 
				+ "   '' url " 
				+ " FROM BSPACE_COURSE_INSTRUCTOR_VW bci "
				+ " JOIN BSPACE_PERSON_INFO_VW bpi on "
				+ "   (bpi.ldap_uid = bci.instructor_ldap_uid) "
				+ " WHERE bci.TERM_YR = :year AND bci.TERM_CD = :term AND bci.COURSE_CNTL_NUM = :courseID";
		
		static String sections = " SELECT "
				+ "   bci.COURSE_CNTL_NUM coursenum, "
				+ "   '' enrolled_cur, " //TODO: going to have to do some aggregation on BSPACE_CLASS_ROSTER
				+ "   bci.ENROLL_LIMIT enrolled_max, "
				+ " trim(nvl(bcs.ROOM_NUMBER, '')) misc_room, "
				+ " trim(nvl(bcs.BUILDING_NAME, '')) misc_building_name, "
				+ "   '' location, "
				+ "   '' coords, "
				+ "   '' note, "	//TODO, ask jon.
				+ "   bci.SECTION_NUM section, "
				+ " bcs.meeting_start_time || bcs.meeting_start_time_ampm_flag || '-' || bcs.meeting_end_time || bcs.meeting_end_time_ampm_flag time, "
				+ " bcs.MEETING_DAYS misc_weekdays, " 
				+ " '' weekdays, "
				+ " '' waitlist, " //TODO, with aggregation
				+ " '' midterm_datetime, "
				+ " '' midterm_location, "
				+ " '' midterm_coords, "
				+ " '' midterm_note, "
				+ " '' final_datetime, "
				+ " '' final_location, "
				+ " '' final_coords, "
				+ " '' final_note, "
				+ " '' restrictions "
				+ " FROM BSPACE_COURSE_INFO_VW bci "
				+ " JOIN BSPACE_CLASS_SCHEDULE_VW bcs on "
				+ "   (bcs.TERM_YR = bci.TERM_YR AND bcs.TERM_CD = bci.TERM_CD AND bcs.COURSE_CNTL_NUM = bci.COURSE_CNTL_NUM "
				+ "   AND (trim(bcs.MEETING_DAYS) != 'UNSCHED' AND trim(bcs.BUILDING_NAME) != 'NO FACILITY')) "
				+ " WHERE bci.TERM_YR = :year AND bci.TERM_CD = :term "
				+ "   AND bci.DEPT_NAME = :deptName AND bci.CATALOG_ID = :catalogId";
	}
}
