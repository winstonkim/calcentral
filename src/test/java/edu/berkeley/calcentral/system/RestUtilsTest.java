package edu.berkeley.calcentral.system;

import edu.berkeley.calcentral.BaseTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;

public class RestUtilsTest extends BaseTest {

	@Mock
	HttpServletRequest request;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void regularQueryString() throws Exception {
		Mockito.when(request.getQueryString()).thenReturn("a=1&b=2");
		assertEquals("/foo/bar?a=1&b=2", RestUtils.pathPlusQueryString("/foo/bar", request));
	}

	@Test
	public void encodedQueryString() {
		Mockito.when(request.getQueryString()).thenReturn("encoded=%2Fsome%2Fpath");
		assertEquals("/foo/bar?encoded=/some/path", RestUtils.pathPlusQueryString("/foo/bar", request));
	}

	@Test
	public void noQueryString() {
		Mockito.when(request.getQueryString()).thenReturn(null);
		assertEquals("/foo/bar", RestUtils.pathPlusQueryString("/foo/bar", request));
	}

}
