/*
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
 */
package edu.berkeley.calcentral.daos;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class OAuth2Dao extends BaseDao {
	public String getToken(String uid, String appId) {
		String sql = "SELECT token " +
				"FROM calcentral_oauth2 " +
				"WHERE uid = :uid AND appId = :appId";
		MapSqlParameterSource params = new MapSqlParameterSource("uid", uid).addValue("appId", appId);
		String token;
		try {
			token = queryRunner.queryForObject(sql, params, String.class);
		} catch (EmptyResultDataAccessException e) {
			token = null;
		}
		return token;
	}

	public boolean isOAuthGranted(String uid, String appId) {
		return (getToken(uid, appId) != null);
	}

	public void update(String uid, String appId, String token) {
		String sql = "UPDATE calcentral_oauth2 SET (token) VALUES (:token) " +
				"WHERE uid = :uid AND appId = :appId";
		MapSqlParameterSource params = new MapSqlParameterSource("uid", uid).
				addValue("appId", appId).addValue("token", token);
		queryRunner.update(sql, params);
	}

	public void insert(String uid, String appId, String token) {
		String sql = "INSERT INTO calcentral_oauth2 (uid, appId, token) VALUES (:uid, :appId, :token)";
		MapSqlParameterSource params = new MapSqlParameterSource("uid", uid).
				addValue("appId", appId).addValue("token", token);
		queryRunner.update(sql, params);
	}

	public void delete(String uid, String appId) {
		String sql = "DELETE FROM calcentral_oauth2 " +
				"WHERE uid = :uid AND appId = :appId";
		MapSqlParameterSource params = new MapSqlParameterSource("uid", uid).addValue("appId", appId);
		queryRunner.update(sql, params);
	}

	public void deleteAllUserData(String uid) {
		String sql = "DELETE FROM calcentral_oauth2 " +
				"WHERE uid = :uid";
		MapSqlParameterSource params = new MapSqlParameterSource("uid", uid);
		queryRunner.update(sql, params);
	}

}
