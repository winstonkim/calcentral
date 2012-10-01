package edu.berkeley.calcentral.daos;

import edu.berkeley.calcentral.DatabaseAwareTest;
import edu.berkeley.calcentral.domain.ClassPage;
import edu.berkeley.calcentral.domain.ClassPageCourseInfo;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

public class ClassPagesLocalDataDaoTest extends DatabaseAwareTest {

	@Autowired
	private ClassPagesLocalDataDao dao;

	@Test(expected = EmptyResultDataAccessException.class)
	public void getNonexistent() throws Exception {
		ClassPage page = new ClassPage();
		page.setClassId("nonexisting class page");
		page.setCourseinfo(new ClassPageCourseInfo());
		dao.mergeLocalData(page);
	}

	@Test
	public void mergeLocalDataNoCanvasCourseID() throws Exception {
		ClassPage page = new ClassPage();
		page.setClassId("2012D07058");
		page.setCourseinfo(new ClassPageCourseInfo());
		dao.mergeLocalData(page);
		assertNotNull(page.getCourseinfo().getWebcastId());
		assertEquals("", page.getCourseinfo().getCanvasCourseId());
	}

	@Test
	public void updateLocalData() throws Exception {
		ClassPage page = new ClassPage();
		page.setClassId("2012D07058");
		page.setCourseinfo(new ClassPageCourseInfo());
		page.getCourseinfo().setWebcastId("abcdef");
		page.getCourseinfo().setCanvasCourseId("canvas12345");

		dao.updateLocalData(page);

		ClassPage updatedPage = new ClassPage();
		updatedPage.setClassId("2012D07058");
		updatedPage.setCourseinfo(new ClassPageCourseInfo());
		dao.mergeLocalData(updatedPage);
		assertEquals("abcdef", updatedPage.getCourseinfo().getWebcastId());
		assertEquals("canvas12345", updatedPage.getCourseinfo().getCanvasCourseId());

		// before decode, webcastUrl should be null
		assertNull(updatedPage.getCourseinfo().getWebcastUrl());
		// after decode, webcastUrl should have a value
		updatedPage.getCourseinfo().decodeAll();
		assertEquals("http://gdata.youtube.com/feeds/api/playlists/abcdef?v=2&alt=json&max-results=50",
				updatedPage.getCourseinfo().getWebcastUrl());

	}
}
