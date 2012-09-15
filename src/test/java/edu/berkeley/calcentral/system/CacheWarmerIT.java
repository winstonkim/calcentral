package edu.berkeley.calcentral.system;

import edu.berkeley.calcentral.DatabaseAwareTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class CacheWarmerIT extends DatabaseAwareTest {

	@Autowired
	private CacheWarmer warmer;

	@Test
	public void buildUrlList() throws Exception {
		List<String> urls = warmer.buildUrlList(20);
		assertTrue(urls.size() > 0);
	}

	@Test
	public void warm() throws Exception {
		warmer.warm(20);
	}

}
