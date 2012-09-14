/**
 * UserServiceDao.java
 * Copyright (c) 2012 The Regents of the University of California
 */
package edu.berkeley.calcentral.daos;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class WidgetDataDao extends BaseDao {
	
	private ObjectMapper mapper = new ObjectMapper();
	
	private static final Log LOGGER = LogFactory.getLog(WidgetDataDao.class);

	public final Map<String, Object> saveWidgetData(String userId, String widgetId, String serializedData) throws IOException {
		
		//check to make sure jsonData is valid json before attempting to store.
		JsonNode dataNode = null;
		if (serializedData != null && !serializedData.isEmpty()) {
			dataNode = mapper.readValue(serializedData, JsonNode.class);
		}

		MapSqlParameterSource params = new MapSqlParameterSource("uid", userId)
			.addValue("widgetId", widgetId)
			.addValue("data", serializedData);
		String saveWidgetData = "WITH upsert as "
				+ " (UPDATE calcentral_widgetdata wd "
				+ "  SET data = :data "
				+ "  WHERE wd.uid = :uid AND wd.widgetId = :widgetId RETURNING wd.*), "
				+ " entries (uid, widgetId, data) as (values (:uid, :widgetId, :data)) "
				+ " INSERT INTO calcentral_widgetdata (uid, widgetId, data) "
				+ " SELECT uid, widgetId, data FROM entries "
				+ " WHERE NOT EXISTS (SELECT 1 FROM upsert up "
				+ " WHERE up.uid = entries.uid AND up.widgetId = entries.widgetId)";
		queryRunner.update(saveWidgetData, params);
		Map<String, Object> response = Maps.newHashMap();
		response.putAll(params.getValues());
		response.put("data", dataNode);
		return response;
	}

	public final void deleteWidgetData(String userId, String widgetId) {
		MapSqlParameterSource params = new MapSqlParameterSource("uid", userId)
			.addValue("widgetId", widgetId);
		String delete = "DELETE FROM calcentral_widgetdata wd "
				+ " WHERE wd.uid = :uid AND wd.widgetId = :widgetId";
		queryRunner.update(delete, params);
	}

	public void deleteAllWidgetData(String userID) {
		MapSqlParameterSource params = new MapSqlParameterSource("uid", userID);
		String deleteAll = "DELETE FROM calcentral_widgetdata wd "
				+ " WHERE wd.uid = :uid";
		queryRunner.update(deleteAll, params);
	}

	public final Map<String, Object> getWidgetData(String userId, String widgetId) {
		Map<String, String> params = Maps.newHashMap();
		params.put("uid", userId);
		params.put("widgetId", widgetId);
		Map<String, Object> result;
		try {
			String get = "SELECT wd.uid, wd.widgetId, wd.data "
					+ " FROM calcentral_widgetdata wd "
					+ " WHERE wd.uid = :uid"
					+ "   AND wd.widgetId = :widgetId";
			result = queryRunner.queryForObject(get, params, new WidgetDataMapper());
		} catch (EmptyResultDataAccessException e) {
			result = null;
		}
		return result;
	}

	public final List<Map<String, Object>> getAllWidgetData(String userId) {
		Map<String, String> params = Maps.newHashMap();
		params.put("uid", userId);
		String getAllForUser = "SELECT wd.uid, wd.widgetId, wd.data "
				+ " FROM calcentral_widgetdata wd "
				+ " WHERE wd.uid = :uid";
		List<Map<String, Object>> result = queryRunner.query(getAllForUser, params, new WidgetDataMapper());
		if (result.isEmpty()) {
			return null;
		}
		return result;
	}
	
	private class WidgetDataMapper implements RowMapper<Map<String, Object>> {
		public Map<String, Object> mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			Map<String, Object> rowResult = new ColumnMapRowMapper().mapRow(rs, rowNum);
			String data = (String) rowResult.get("DATA");
			JsonNode dataNode = null;
			try {
				if (data != null) {
					dataNode = mapper.readValue(data, JsonNode.class);
				}
			} catch (Exception e) {
				LOGGER.error("Error deserializing data", e);
				dataNode =  mapper.createObjectNode();
			}
			
			Map<String, Object> contents =  Maps.newHashMap();
			contents.put("uid", (String) rowResult.get("UID"));
			contents.put("widgetID", (String) rowResult.get("WIDGETID"));
			contents.put("data", dataNode); 
			return ImmutableMap.<String, Object>of("widgetData", contents);
		}
	}

}
