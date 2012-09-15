package edu.berkeley.calcentral.system;

import edu.berkeley.calcentral.DatabaseAwareTest;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class CacheWarmerTest extends DatabaseAwareTest {

	private static final Logger LOGGER = Logger.getLogger(CacheWarmerTest.class);

	@Autowired
	private CacheWarmer warmer;

	@Test
	public void buildUrlList() throws Exception {
		List<String> urls = warmer.buildUrlList(20);
		LOGGER.info(urls);
	}

}
