package edu.berkeley.calcentral.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;
import edu.berkeley.calcentral.daos.BaseDao;
import edu.berkeley.calcentral.daos.OAuth2Dao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class GoogleCredentialStore extends BaseDao implements CredentialStore {

	public static final String GOOGLE_APP_ID = "google";

	@Autowired
	private OAuth2Dao dao;

	public boolean load(String userId, final Credential credential) throws IOException {
		String sql = "SELECT token, refreshToken, expirationTime " +
				"FROM calcentral_oauth2 " +
				"WHERE uid = :uid AND appId = :appId";
		MapSqlParameterSource params = new MapSqlParameterSource("uid", userId).
				addValue("appId", GOOGLE_APP_ID);
		try {
			queryRunner.queryForObject(sql, params, new RowMapper<Object>() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					credential.setAccessToken(rs.getString("token"));
					credential.setRefreshToken(rs.getString("refreshToken"));
					credential.setExpirationTimeMilliseconds(rs.getLong("expirationTime"));
					return "1";
				}
			});
		} catch (IncorrectResultSizeDataAccessException e) {
			return false;
		}
		return true;
	}

	public void store(String userId, Credential credential) throws IOException {
		if (dao.getToken(userId, GOOGLE_APP_ID) != null) {
			dao.update(userId, GOOGLE_APP_ID, credential.getAccessToken(),
					credential.getRefreshToken(), credential.getExpirationTimeMilliseconds());
		} else {
			dao.insert(userId, GOOGLE_APP_ID, credential.getAccessToken(),
					credential.getRefreshToken(), credential.getExpirationTimeMilliseconds());
		}
	}

	public void delete(String userId, Credential credential) throws IOException {
		dao.delete(userId, GOOGLE_APP_ID);
	}
}
