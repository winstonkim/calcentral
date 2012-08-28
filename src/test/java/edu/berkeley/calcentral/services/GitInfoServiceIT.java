package edu.berkeley.calcentral.services;

import edu.berkeley.calcentral.IntegrationTest;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.junit.Test;

import java.io.IOException;

public class GitInfoServiceIT extends IntegrationTest {

	@Override
	protected void setup() {
		// no-op
	}

	@Test
	public void get() throws IOException, JSONException {
		GetMethod get = doGet("/api/gitInfo");
		assertResponse(HttpResponseCodes.SC_OK, get);
		JSONObject json = toJSONObject(get);
		assertNotNull(json.getJSONObject("gitInfo"));
	}

}
