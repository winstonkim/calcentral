package edu.berkeley.calcentral.services;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import edu.berkeley.calcentral.DatabaseAwareTest;
import edu.berkeley.calcentral.daos.OAuth2Dao;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class GoogleCredentialStoreTest extends DatabaseAwareTest {

	@Autowired
	private GoogleCredentialStore credentialStore;

	@Autowired
	private OAuth2Dao dao;

	private String user;

	@Before
	public void setup() {
		user = "user" + randomString();
	}

	@Test
	public void credentialStore() throws IOException {
		Credential credential = new Credential.Builder(
				BearerToken.authorizationHeaderAccessMethod()).
				setJsonFactory(new JacksonFactory()).
				setTransport(new NetHttpTransport()).
				setClientAuthentication(new Credential(BearerToken.authorizationHeaderAccessMethod())).
				setTokenServerUrl(new GenericUrl("http://localhost:8080/foo")).
				build();
		credential.setAccessToken("abcdef");
		credential.setRefreshToken("refresher");
		credential.setExpirationTimeMilliseconds(12345L);

		assertFalse(credentialStore.load(user, credential));

		credentialStore.store(user, credential);
		String token = dao.getToken(user, GoogleCredentialStore.GOOGLE_APP_ID);
		assertEquals("abcdef", token);

		assertTrue(credentialStore.load(user, credential));
		assertEquals("abcdef", credential.getAccessToken());
		assertEquals("refresher", credential.getRefreshToken());
		assertEquals(12345L, credential.getExpirationTimeMilliseconds().longValue());

		dao.update(user, GoogleCredentialStore.GOOGLE_APP_ID, "newtoken", "newrefreshtoken", 5678);
		assertTrue(credentialStore.load(user, credential));
		assertEquals("newtoken", credential.getAccessToken());
		assertEquals("newrefreshtoken", credential.getRefreshToken());
		assertEquals(5678L, credential.getExpirationTimeMilliseconds().longValue());

		credentialStore.delete(user, credential);
		assertNull(dao.getToken(user, GoogleCredentialStore.GOOGLE_APP_ID));
		assertFalse(credentialStore.load(user, credential));
	}

}
