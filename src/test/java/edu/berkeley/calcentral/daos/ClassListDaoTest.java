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

public class ClassListDaoTest extends DatabaseAwareTest {

	private static final Log LOGGER = LogFactory.getLog(ClassListDaoTest.class);

	@Autowired
	private ClassListDao dao;

	@Test
	public void getCollege() throws Exception {
		College college = dao.getCollege("graduateschoolofeducation");
		assertNotNull(college);
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
	}

}
