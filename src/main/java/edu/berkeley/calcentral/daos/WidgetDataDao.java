/**
 * UserServiceDao.java
 * Copyright (c) 2012 The Regents of the University of California
 */
package edu.berkeley.calcentral.daos;

import com.google.common.collect.Maps;
import edu.berkeley.calcentral.domain.WidgetData;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class WidgetDataDao extends BaseDao {

	public final void saveWidgetData(WidgetData widgetData) {
		Map<String, String> params = Maps.newHashMap();
		params.put("uid", widgetData.getUid());
		params.put("widgetId", widgetData.getWidgetID());
		params.put("data", widgetData.getData());
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
	}

	public final void deleteWidgetData(String userId, String widgetId) {
		Map<String, String> params = Maps.newHashMap();
		params.put("uid", userId);
		params.put("widgetId", widgetId);
		String delete = "DELETE FROM calcentral_widgetdata wd "
				+ " WHERE wd.uid = :uid AND wd.widgetId = :widgetId";
		queryRunner.update(delete, params);
	}

	public void deleteAllWidgetData(String userID) {
		Map<String, String> params = Maps.newHashMap();
		params.put("uid", userID);
		String deleteAll = "DELETE FROM calcentral_widgetdata wd "
				+ " WHERE wd.uid = :uid";
		queryRunner.update(deleteAll, params);
	}

	public final WidgetData getWidgetData(String userId, String widgetId) {
		Map<String, String> params = Maps.newHashMap();
		params.put("uid", userId);
		params.put("widgetId", widgetId);
		WidgetData result;
		try {
			String get = "SELECT wd.uid, wd.widgetId, wd.data "
					+ " FROM calcentral_widgetdata wd "
					+ " WHERE wd.uid = :uid"
					+ "   AND wd.widgetId = :widgetId";
			result = queryRunner.queryForObject(get, params, new BeanPropertyRowMapper<WidgetData>(WidgetData.class));
		} catch (EmptyResultDataAccessException e) {
			result = null;
		}
		return result;
	}

	public final List<WidgetData> getAllWidgetData(String userId) {
		Map<String, String> params = Maps.newHashMap();
		params.put("uid", userId);
		String getAllForUser = "SELECT wd.uid, wd.widgetId, wd.data "
				+ " FROM calcentral_widgetdata wd "
				+ " WHERE wd.uid = :uid";
		List<WidgetData> result = queryRunner.query(getAllForUser, params, new BeanPropertyRowMapper<WidgetData>(WidgetData.class));
		if (result.isEmpty()) {
			return null;
		}
		return result;
	}

}
