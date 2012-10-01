package edu.berkeley.calcentral.system;

import edu.berkeley.calcentral.DatabaseAwareTest;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClientException;

public class CanvasCourseIDFetcherTest extends DatabaseAwareTest {

	private static final Logger LOGGER = Logger.getLogger(CanvasCourseIDFetcherTest.class);

	@Autowired
	private CanvasCourseIDFetcher fetcher;

	@Test
	public void fetch() throws Exception {
		try {
			fetcher.fetch();
		} catch (RestClientException e) {
			LOGGER.error("Got a RestClientException, is canvas server properly configured or unavailable?", e);
		}
	}
}
