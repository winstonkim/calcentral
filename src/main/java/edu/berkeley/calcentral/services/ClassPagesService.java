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
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.daos.BaseDao;
import edu.berkeley.calcentral.domain.ClassPage;
import edu.berkeley.calcentral.domain.ClassPageCourseInfo;
import edu.berkeley.calcentral.domain.ClassPageInstructor;
import edu.berkeley.calcentral.domain.ClassPageSchedule;

@Service
@Path(Urls.CLASS_PAGES)
public class ClassPagesService extends BaseDao {
	
	private static final Log LOGGER = LogFactory.getLog(ClassPagesService.class);

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{ccc}")
	public Map<String, Object> getClassInfoMap(@PathParam("ccc") String ccc) {
		
		ClassPage classPage;
		Map<String, Object> returnObject = Maps.newHashMap();
		try {
			classPage= getClassInfo(ccc);
			returnObject.put("classid", classPage.getClassId());
			returnObject.put("info_last_updated", classPage.getInfo_last_updated());
			returnObject.put("courseinfo", classPage.getCourseinfo());
			returnObject.put("classtitle", classPage.getClasstitle());
			returnObject.put("description", classPage.getDescription());
			returnObject.put("instructors", classPage.getInstructors());
			returnObject.put("schedule", classPage.getSchedule());
		} catch (Exception e) {
			//should probably do something with the header response code.
			LOGGER.error(Throwables.getRootCause(e).getMessage());
		}
		return returnObject;
	}
	
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
		params.put("format", "LEC");
		
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
		
		List<ClassPageSchedule> classPageSchedules = campusQueryRunner.query(SqlQueries.schedule, params, 
				new BeanPropertyRowMapper<ClassPageSchedule>(ClassPageSchedule.class));
		for (ClassPageSchedule schedule : classPageSchedules) {
			cleanupScheduleInfo(schedule);
		}
		classPageResult.setSchedule(classPageSchedules);
		
		
		
		return classPageResult;
	}

	private void cleanupScheduleInfo(ClassPageSchedule schedule) {
		locationDecode(schedule);
		weekdaysDecode(schedule);
	}

	private void weekdaysDecode(ClassPageSchedule schedule) {
		char[] unfilteredWeekdays = schedule.getMisc_weekdays();
		List<String> weekdays = Lists.newArrayList();
		Map<Character, String> weekdayDict = Maps.newHashMap();
		weekdayDict.put('M', "Mon");
		weekdayDict.put('W', "Wed");
		weekdayDict.put('F', "Fri");
		
		//because some genius decided not to differentiate between tues and thurs, sunday and saturdays...
		int index = 0;
		for (char possibleDay : unfilteredWeekdays) {
			String translatedDay = "";
			if (possibleDay == 'S' && index == 0) {
				translatedDay = "Sun";
			} else if (possibleDay == 'S' && index == 6) {
				translatedDay = "Sat";
			} else if (possibleDay == 'T' && index == 2) {
				translatedDay = "Tues";
			} else if (possibleDay == 'T' && index == 4) {
				translatedDay = "Thurs";
			} else {
				translatedDay = Strings.nullToEmpty(weekdayDict.get(possibleDay));
			}
			
			if (!translatedDay.isEmpty()) {
				weekdays.add(translatedDay);
			}
			index++;
		}
		
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < weekdays.size(); i++) {
			if (i == 0) {
				sb.append(weekdays.get(i));
			} else if (i == weekdays.size()-1) {
				sb.append(" and ").append(weekdays.get(i));
			} else {
				sb.append(", ").append(weekdays.get(i));
			}
		}
		
		schedule.setWeekdays(sb.toString());
	}

	private void locationDecode(ClassPageSchedule schedule) {
		String room = schedule.getMisc_room();
		//attempts to cleanup the data....
		try {
			room = Integer.toString(Integer.parseInt(room));
		} catch (NumberFormatException e) {
			//dont' touch the ugly formatted room.
		}
		
		String buildingName = schedule.getMisc_building_name();
		
		if (buildingName.equalsIgnoreCase("No Facility")) {
			buildingName = "";
		}
		
		if (!buildingName.isEmpty() && !room.isEmpty()) {
			schedule.setLocation(room + " " + buildingName);
		}
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
				+ " WHERE bci.TERM_YR = :year AND bci.TERM_CD = :term AND bci.COURSE_CNTL_NUM = :courseID and bci.INSTRUCTION_FORMAT = :format";

		//There's some ambiguous logic related to this. Not worrying about edge cases for the time being.
		//disregarding the online schedule of classes rules for now.
		static String schedule = " SELECT "
				+ " '' coords, "
				+ " trim(nvl(bcs.ROOM_NUMBER, '')) misc_room, "
				+ " trim(nvl(bcs.BUILDING_NAME, '')) misc_building_name, "
				+ " '' location, "
				+ " bcs.meeting_start_time || bcs.meeting_start_time_ampm_flag || '-' || bcs.meeting_end_time || bcs.meeting_end_time_ampm_flag time, "
				+ " bcs.MEETING_DAYS misc_weekdays, " //disaster to decode...
				+ " '' weekdays, "
				+ " '' current_sem "
				+ " FROM BSPACE_CLASS_SCHEDULE_VW bcs "
				+ " WHERE (bcs.TERM_YR = :year AND bcs.TERM_CD = :term AND bcs.COURSE_CNTL_NUM = :courseID"
				+ "   AND (trim(bcs.MEETING_DAYS) != 'UNSCHED' AND trim(bcs.BUILDING_NAME) != 'NO FACILITY')) ";

		static String instructors = " SELECT "
				+ "    bpi.email_address email, "
				+ "    bpi.ldap_uid id, "
				+ "    bpi.person_name name, " //this can probably be pulled in to go against our preferred name instead.
				+ "    bpi.email_disclos_cd misc_email_disclosure,"
				+ "    '' office, " 
				+ "    '' phone, " 
				+ "    '' img, " 
				+ "    '' title, " 
				+ "    '' url " 
				+ " FROM BSPACE_COURSE_INSTRUCTOR_VW bci "
				+ " JOIN BSPACE_PERSON_INFO_VW bpi on "
				+ "   (bpi.ldap_uid = bci.instructor_ldap_uid) "
				+ " WHERE bci.TERM_YR = :year AND bci.TERM_CD = :term AND bci.COURSE_CNTL_NUM = :courseID";

		static String sections = "";
	}
}
