package edu.berkeley.calcentral.services;

import com.google.common.collect.ImmutableList;
import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.daos.ClassListDao;
import edu.berkeley.calcentral.domain.ClassPage;
import edu.berkeley.calcentral.domain.College;
import edu.berkeley.calcentral.domain.Department;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.resteasy.annotations.cache.Cache;
import org.jboss.resteasy.spi.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Path(Urls.CLASS_LIST)
public class ClassListService {

	private static final Log LOGGER = LogFactory.getLog(ClassPagesService.class);

	@Autowired
	private ClassListDao dao;

	/**
	 * Get all the courses in a college, with some extra college and departmental metadata.
	 *
	 * @param collegeSlug The college slug.
	 * @return JSON data:
	 *         <pre>
	 *         	{
	 *         		college : {@link edu.berkeley.calcentral.domain.User},
	 *         		departments : array of {@link edu.berkeley.calcentral.domain.Department},
	 *         	  classes : array of {@link edu.berkeley.calcentral.domain.ClassPage}
	 *         	}
	 *         	</pre>
	 */
	@Cache(maxAge = 24 * 60 * 60) // cache for 24 hrs
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{collegeslug}")
	public Map<String, Object> getCollege(@PathParam(value = "collegeslug") String collegeSlug) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			College college = dao.getCollege(collegeSlug);
			LOGGER.info("Got college: " + college);
			result.put("college", college);

			List<Department> departments = dao.getDepartments(college.getId());
			result.put("departments", departments);

			List<ClassPage> classes = dao.getClasses(departments);
			result.put("classes", classes);

		} catch (EmptyResultDataAccessException e) {
			throw new NotFoundException("College " + collegeSlug + " not found");
		}
		return result;
	}

	/**
	 * Get all the courses in a particular department, with some extra college and departmental metadata.
	 *
	 * @param collegeSlug The college slug.
	 * @param department The department to fetch.
	 * @return JSON data:
	 *         <pre>
	 *         	{
	 *         		college : {@link edu.berkeley.calcentral.domain.User},
	 *         		departments : array of {@link edu.berkeley.calcentral.domain.Department},
	 *         	  classes : array of {@link edu.berkeley.calcentral.domain.ClassPage}
	 *         	}
	 *         	</pre>
	 */
	@Cache(maxAge = 24 * 60 * 60) // cache for 24 hrs
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{collegeslug}/{department}")
	public Map<String, Object> getDepartment(@PathParam(value = "collegeslug") String collegeSlug,
	                                         @PathParam(value = "department") String department) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			College college = dao.getCollege(collegeSlug);
			LOGGER.info("Got college: " + college);
			result.put("college", college);

			List<Department> allDepartments = dao.getDepartments(college.getId());
			result.put("departments", allDepartments);

			Department thisDepartment = dao.getDepartment(department);
			List<ClassPage> classes = dao.getClasses(ImmutableList.of(thisDepartment));
			result.put("classes", classes);

		} catch (EmptyResultDataAccessException e) {
			throw new NotFoundException("College " + collegeSlug + " not found");
		}
		return result;
	}
}
