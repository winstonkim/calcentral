package edu.berkeley.calcentral.services;

import edu.berkeley.calcentral.DatabaseAwareTest;
import org.jboss.resteasy.spi.NotFoundException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class ClassListServiceTest extends DatabaseAwareTest {

	@Autowired
	private ClassListService service;

	@Test
	public void getCollege() throws Exception {
		Map<String, Object> result = service.getCollege(1);
		assertNotNull(result);
		assertNotNull(result.get("college"));
		assertNotNull(result.get("departments"));
		assertNotNull(result.get("classes"));
	}

	@Test
	public void getDepartment() throws Exception {
		Map<String, Object> result = service.getDepartment(2, 53);
		assertNotNull(result);
		assertNotNull(result.get("college"));
		assertNotNull(result.get("departments"));
		assertNotNull(result.get("classes"));
	}

	@Test(expected = NotFoundException.class)
	public void getNonexistentCollege() {
		service.getCollege(972);
	}

	@Test(expected = NotFoundException.class)
	public void getNonexistentDept() {
		service.getDepartment(2, 9723);
	}

}
