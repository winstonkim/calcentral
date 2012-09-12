package edu.berkeley.calcentral.services;

import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.daos.ClassListDao;
import edu.berkeley.calcentral.domain.ClassPage;
import edu.berkeley.calcentral.domain.College;
import edu.berkeley.calcentral.domain.Department;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

	/*
	/api/classList/collegeoflettersscienceartshumanities => everything in L&S
	/api/classList/collegeoflettersscienceartshumanities/Arabic => just Arabic
	 */
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

}
