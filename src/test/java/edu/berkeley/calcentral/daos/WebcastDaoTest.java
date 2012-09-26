package edu.berkeley.calcentral.daos;

import edu.berkeley.calcentral.DatabaseAwareTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

public class WebcastDaoTest extends DatabaseAwareTest {

	@Autowired
	private WebcastDao dao;

	@Test
	public void get() throws Exception {
		String webcastID = dao.getWebcastId("2012D07058");
		assertNotNull(webcastID);
	}

	@Test(expected = EmptyResultDataAccessException.class)
	public void getNonexistent() throws Exception {
		dao.getWebcastId("nonexistent class page id");
	}

}
