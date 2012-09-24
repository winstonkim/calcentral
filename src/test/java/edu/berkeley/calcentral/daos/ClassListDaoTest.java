package edu.berkeley.calcentral.daos;

import edu.berkeley.calcentral.DatabaseAwareTest;
import edu.berkeley.calcentral.domain.ClassPage;
import edu.berkeley.calcentral.domain.College;
import edu.berkeley.calcentral.domain.Department;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class ClassListDaoTest extends DatabaseAwareTest {

	private static final Log LOGGER = LogFactory.getLog(ClassListDaoTest.class);

	@Autowired
	private ClassListDao dao;

	@Test
	public void getAllColleges() throws Exception {
		List<College> colleges = dao.getAllColleges(10);
		assertEquals(10, colleges.size());
		assertNotNull(colleges.get(1).getSlug());
	}

	@Test
	public void getCollege() throws Exception {
		College college = dao.getCollege(1);
		assertNotNull(college);
	}

	@Test
	public void getAllDepartments() throws Exception {
		List<Department> all = dao.getAllDepartments(10);
		assertEquals(10, all.size());
		assertNotNull(all.get(0).getKey());
	}

	@Test
	public void getDepartments() throws Exception {
		List<Department> departments = dao.getDepartments(1);
		assertTrue(departments.size() > 1);
		assertEquals("AHMA", departments.get(0).getKey());
	}

	@Test
	public void getClasses() throws Exception {
		List<Department> departments = dao.getDepartments(1);
		List<ClassPage> classes = dao.getClasses(departments);
		assertNotNull(classes);
		assertTrue(classes.size() > 0);
		assertTrue(classes.get(0).getClassId().length() > 0);
	}

	@Test
	public void getDepartment() throws Exception {
		Department dept = dao.getDepartment(57, 2);
		assertNotNull(dept);
	}

	@Test
	public void getAllClassIDs() throws Exception {
		List<Map<String, Object>> classIDs = dao.getAllClassIDs(10);
		assertEquals(10, classIDs.size());
	}

}
