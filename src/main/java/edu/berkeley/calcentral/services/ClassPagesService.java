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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.daos.ClassPagesDao;
import edu.berkeley.calcentral.domain.ClassPage;
import edu.berkeley.calcentral.domain.ClassPageCourseInfo;
import edu.berkeley.calcentral.domain.ClassPageInstructor;
import edu.berkeley.calcentral.domain.ClassPageSchedule;
import edu.berkeley.calcentral.domain.ClassPageSection;

@Service
@Path(Urls.CLASS_PAGES)
public class ClassPagesService {
	
	private static final Log LOGGER = LogFactory.getLog(ClassPagesService.class);

	@Autowired
	private ClassPagesDao classPagesDao;
	
	/**
	 * Exposed REST endpoint for fetching classes
	 * 
	 * @param ccc concatenated tuple of term_yr, term_cd, and course_catalogue_code
	 * @return course info in an json object, or empty json object on errors
	 */
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
			returnObject.put("sections", classPage.getSections());
		} catch (Exception e) {
			//TODO: Change this to use whatever final exception handling scheme, instead of swallowing the exception.
			LOGGER.error(Throwables.getStackTraceAsString(e));
		}
		return returnObject;
	}
	
	public ClassPage getClassInfo(String ccc) {
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
		
		int yearInt = Integer.parseInt(year);
		ClassPage classPageResult = classPagesDao.getBaseClassPage(yearInt, term, courseID);
		ClassPageCourseInfo courseInfo = classPagesDao.getCourseInfo(yearInt, term, courseID);
		courseInfo.decodeAll();
		classPageResult.setCourseinfo(courseInfo);

		List<ClassPageInstructor> classPageInstructors = classPagesDao.getCourseInstructors(yearInt, term, courseID);
		for (ClassPageInstructor instructor : classPageInstructors) {
			instructor.emailDisclosureDecode();
		}
		classPageResult.setInstructors(classPageInstructors);
		
		List<ClassPageSchedule> classPageSchedules = classPagesDao.getCourseSchedules(yearInt, term, courseID);
		for (ClassPageSchedule schedule : classPageSchedules) {
			schedule.decodeAll();
		}
		classPageResult.setSchedule(classPageSchedules);
		
		String deptName = courseInfo.getMisc_deptname();
		String catalogId = courseInfo.getMisc_catalogid();
		List<ClassPageSection> classPageSections = classPagesDao.getCourseSections(yearInt, term, deptName, catalogId);
		//TODO: can probably use predicates here, to get a list of sections we want to keep.
		//TODO: another query on all the applicable sections to figure out list of instructors.
		classPageResult.setSections(classPageSections);
		
		return classPageResult;
	}

	
}
