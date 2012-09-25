package edu.berkeley.calcentral.daos;

import edu.berkeley.calcentral.DatabaseAwareTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class OAuth2DaoTest extends DatabaseAwareTest {

	@Autowired
	private OAuth2Dao dao;

	private String user;

	private String app;

	@Before
	public void setup() {
		user = "user" + randomString();
		app = "app" + randomString();
	}

	@Test
	public void getToken() throws Exception {
		assertNull(dao.getToken(user, app));
	}

	@Test
	public void isOAuthGranted() throws Exception {
		assertFalse(dao.isOAuthGranted(user, app));
		dao.insert(user, app, "mytoken", "myrefresh", 12345);
		assertTrue(dao.isOAuthGranted(user, app));
	}

	@Test
	public void update() throws Exception {
		dao.insert(user, app, "mytoken", "myrefresh", 12345);
		String token = dao.getToken(user, app);
		assertEquals("mytoken", token);
		dao.update(user, app, "updatedtoken", "updatedrefresh", 5678);
		String updatedToken = dao.getToken(user, app);
		assertEquals("updatedtoken", updatedToken);
	}

	@Test
	public void insert() throws Exception {
		dao.insert(user, app, "mytoken", "myrefresh", 12345);
		String token = dao.getToken(user, app);
		assertEquals("mytoken", token);
	}

	@Test
	public void delete() throws Exception {
		dao.insert(user, app, "mytoken", "myrefresh", 12345);
		dao.delete(user, app);
		assertNull(dao.getToken(user, app));
	}

	@Test
	public void deleteAllUserData() throws Exception {
		String otherApp = "otherApp" + randomString();
		dao.insert(user, app, "mytoken", "myrefresh", 12345);
		dao.insert(user, otherApp, "mytoken", "myrefresh", 12345);
		dao.deleteAllUserData(user);
		assertNull(dao.getToken(user, app));
		assertNull(dao.getToken(user, otherApp));
	}

}
