/**
 * UserServiceDao.java
 * Copyright (c) 2012 The Regents of the University of California
 */
package edu.berkeley.calcentral.daos;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;

import edu.berkeley.calcentral.domain.WidgetData;

@Repository
public class WidgetDataDao {
    @Autowired
    private DataSource dataSource;

    public final void saveWidgetData(WidgetData widgetData) {
        Map<String, String> params = Maps.newHashMap();
        params.put("uid", widgetData.getUid());
        params.put("widgetId", widgetData.getWidgetID());
        params.put("data", widgetData.getData());
        NamedParameterJdbcTemplate queryRunner = new NamedParameterJdbcTemplate(dataSource);
        queryRunner.update(SqlQueries.saveWidgetData, params);
    }

    public final void deleteWidgetData(String userId, String widgetId) {
        Map<String, String> params = Maps.newHashMap();
        params.put("uid", userId);
        params.put("widgetId", widgetId);
        NamedParameterJdbcTemplate queryRunner = new NamedParameterJdbcTemplate(dataSource);
        queryRunner.update(SqlQueries.delete, params);
    }
    
    public final WidgetData getWidgetData(String userId, String widgetId) {
        Map<String, String> params = Maps.newHashMap();
        params.put("uid", userId);
        params.put("widgetId", widgetId);
        NamedParameterJdbcTemplate queryRunner = new NamedParameterJdbcTemplate(dataSource);
        WidgetData result = null;
        try {
            result = queryRunner.queryForObject(SqlQueries.get, params, new BeanPropertyRowMapper<WidgetData>(WidgetData.class));
        } catch (EmptyResultDataAccessException e) {
            result = null;
        }
        return result;
    }
    
    public final List<WidgetData> getAllWidgetData(String userId) {
        Map<String, String> params = Maps.newHashMap();
        params.put("uid", userId);
        NamedParameterJdbcTemplate queryRunner = new NamedParameterJdbcTemplate(dataSource);
        List<WidgetData> result = queryRunner.query(SqlQueries.getAllForUser, params, new BeanPropertyRowMapper<WidgetData>(WidgetData.class));
        if (result.isEmpty()) {
            return null;
        }
        return result;
    }
    
    private static class SqlQueries {
        static String getAllForUser = 
                "   SELECT wd.uid, wd.widgetId, wd.data "
                        + " FROM calcentral_widgetdata wd "
                        + " WHERE wd.uid = :uid";

        static String get = 
                "   SELECT wd.uid, wd.widgetId, wd.data "
                        + " FROM calcentral_widgetdata wd "
                        + " WHERE wd.uid = :uid"
                        + "   AND wd.widgetId = :widgetId";

        static String saveWidgetData = 
                "   WITH upsert as "
                        + " (UPDATE calcentral_widgetdata wd "
                        + "  SET data = :data "
                        + "  WHERE wd.uid = :uid AND wd.widgetId = :widgetId RETURNING wd.*), "
                        + " entries (uid, widgetId, data) as (values (:uid, :widgetId, :data)) "
                        + " INSERT INTO calcentral_widgetdata (uid, widgetId, data) "
                        + " SELECT uid, widgetId, data FROM entries "
                        + " WHERE NOT EXISTS (SELECT 1 FROM upsert up "
                        + " WHERE up.uid = entries.uid AND up.widgetId = entries.widgetId)";
        
        static String delete = 
                " DELETE FROM calcentral_widgetdata wd "
                + " WHERE wd.uid = :uid AND wd.widgetId = :widgetId";
    }
}
