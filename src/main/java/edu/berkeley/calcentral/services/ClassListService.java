package edu.berkeley.calcentral.services;

import com.google.common.collect.ImmutableList;
import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.daos.ClassListDao;
import edu.berkeley.calcentral.domain.ClassPage;
import edu.berkeley.calcentral.domain.College;
import edu.berkeley.calcentral.domain.Department;
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

	@Autowired
	private ClassListDao dao;

	/**
	 * Get all the courses in a college, with some extra college and departmental metadata.
	 *
	 * @param id The college id.
	 * @return JSON data:
	 *         <pre>
	 *                         	{
	 *                         		college : {@link edu.berkeley.calcentral.domain.User},
	 *                         		departments : array of {@link edu.berkeley.calcentral.domain.Department},
	 *                         	  classes : array of {@link edu.berkeley.calcentral.domain.ClassPage}
	 *                         	}
	 *                         	</pre>
	 */
	@Cache(maxAge = 24 * 60 * 60) // cache for 24 hrs
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{id}")
	public Map<String, Object> getCollege(@PathParam(value = "id") int id) {
		Map<String, Object> result = new HashMap<String, Object>();
		College college;
		List<Department> departments;
		List<ClassPage> classes;
		try {
			college = dao.getCollege(id);
			result.put("college", college);
		} catch (EmptyResultDataAccessException e) {
			throw new NotFoundException("College " + id + " not found");
		}
		try {
			departments = dao.getDepartments(college.getId());
			result.put("departments", departments);
		} catch (EmptyResultDataAccessException e) {
			throw new NotFoundException("List of departments for college " + college.getSlug() + " not found");
		}
		try {
			classes = dao.getClasses(departments);
			result.put("classes", classes);
		} catch (EmptyResultDataAccessException e) {
			throw new NotFoundException("Classes for " + id + " could not be found");
		}
		return result;
	}

	/**
	 * Get all the courses in a particular department, with some extra college and departmental metadata.
	 *
	 * @param collegeid The college ID.
	 * @param departmentid  The department ID.
	 * @return JSON data:
	 *  <pre>
   *  {
   *    college : {@link edu.berkeley.calcentral.domain.User},
   *    departments : array of {@link edu.berkeley.calcentral.domain.Department},
   *    classes : array of {@link edu.berkeley.calcentral.domain.ClassPage}
   *  }
	 *  </pre>
	 */
	@Cache(maxAge = 24 * 60 * 60) // cache for 24 hrs
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{collegeid}/{departmentid}")
	public Map<String, Object> getDepartment(@PathParam(value = "collegeid") int collegeid,
	                                         @PathParam(value = "departmentid") int departmentid) {
		Map<String, Object> result = new HashMap<String, Object>();
		College college;
		List<Department> allDepartments;
		Department thisDepartment;
		List<ClassPage> classes;
		try {
			college = dao.getCollege(collegeid);
			result.put("college", college);
		} catch (EmptyResultDataAccessException e) {
			throw new NotFoundException("College " + collegeid + " not found");
		}
		try {
			allDepartments = dao.getDepartments(college.getId());
			result.put("departments", allDepartments);
		} catch (EmptyResultDataAccessException e) {
			throw new NotFoundException("List of departments for " + collegeid + " not found");
		}
		try {
			thisDepartment = dao.getDepartment(departmentid, college.getId());
		} catch (EmptyResultDataAccessException e) {
			throw new NotFoundException("Department " + departmentid + " not found");
		}
		try {
			classes = dao.getClasses(ImmutableList.of(thisDepartment));
			result.put("classes", classes);
		} catch (EmptyResultDataAccessException e) {
			throw new NotFoundException("Classes for " + collegeid + " in department " + departmentid + " not found");
		}
		return result;
	}
}
