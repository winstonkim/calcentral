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

package edu.berkeley.calcentral.services;

import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.annotations.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.berkeley.calcentral.Params;
import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.daos.WidgetDataDao;
import edu.berkeley.calcentral.domain.WidgetData;

@Service
@Path(Urls.WIDGET_DATA)
public class WidgetDataService {

	@Autowired
	private WidgetDataDao widgetDataDao;

	private static final Log LOGGER = LogFactory.getLog(WidgetDataService.class);
	
	/**
	 * Save widget data. Updates an existing widget or creates a new one if none exists.
	 *
	 * @param userID   The user to save widget data on
	 * @param widgetID the ID of the widget to save.
	 * @param jsonData A String representation of the JSON data to save.
	 * @return The created Widget data.
	 */
	@POST
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/{" + Params.WIDGET_ID + "}")
	public WidgetData save(@PathParam(Params.USER_ID) String userID,
												 @PathParam(Params.WIDGET_ID) String widgetID,
												 @FormParam(Params.DATA) String jsonData) {
		WidgetData widgetData = new WidgetData(userID, widgetID, jsonData);
		//sanity check
		if (Strings.nullToEmpty(widgetData.getUid()).isEmpty() ||
				Strings.nullToEmpty(widgetData.getWidgetID()).isEmpty()) {
			return null;
		}
		widgetDataDao.saveWidgetData(widgetData);
		return widgetData;
	}

	/**
	 * Get all the widget data for a particular user.
	 *
	 * @param userID The user to retrieve the widget data from
	 * @return Array of Widget data
	 */
	@Cache(mustRevalidate = true)
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public List<Map<String, Object>> getAllForUser(@PathParam(Params.USER_ID) String userID) {
		List<WidgetData> allWidgetData =  widgetDataDao.getAllWidgetData(userID);
		List<Map<String, Object>> response = Lists.newArrayList();
		if (allWidgetData == null) {
			return null;
		}
		for(WidgetData widgetData : allWidgetData) {
			Map<String, Object> singleWidgetData = convertBeanResponse(widgetData);
			if (singleWidgetData != null) {
				response.add(singleWidgetData);
			}
		}
		return response;
			
	}

	/**
	 * Get a particular widget's data.
	 *
	 * @param userID   The user to retrieve the widget data from
	 * @param widgetID The ID of the widget to get
	 * @return A single piece of Widget data
	 */
	@Cache(mustRevalidate = true)
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/{" + Params.WIDGET_ID + "}")
	public Map<String, Object> get(@PathParam(Params.USER_ID) String userID,
												@PathParam(Params.WIDGET_ID) String widgetID) {
		WidgetData responseBean = widgetDataDao.getWidgetData(userID, widgetID);
		return convertBeanResponse(responseBean);
	}
	
	/**
	 * Deals with the problem of data being stored as beans. Extracts the data string and
	 * converts result into json.
	 * 
	 * @param dataBean widgetData bean.
	 * @return map with data converted into json.
	 */
	private Map<String, Object> convertBeanResponse(WidgetData dataBean) {
		Map<String, Object> response = Maps.newHashMap();
		if (dataBean == null || dataBean.getUid() == null || dataBean.getWidgetID() == null) {
			return null;
		}
		response.put("uid", dataBean.getUid());
		response.put("widgetID", dataBean.getWidgetID());
		ObjectMapper mapper = new ObjectMapper();
		JsonNode dataNode = null;
		try {
			dataNode = mapper.readValue(dataBean.getData(), JsonNode.class);
			response.put("data", dataNode);
		} catch (Exception e) {
			LOGGER.error("Malformed JSON", e);
			response.put("data", "");
		}
		Map<String, Object> wrapper = Maps.newHashMap();
		wrapper.put("widgetData", response);
		return wrapper;
	}

	/**
	 * Delete a widget.
	 *
	 * @param userID   The user ID
	 * @param widgetID The widget ID
	 */
	@DELETE
	@Path("/{" + Params.WIDGET_ID + "}")
	public void delete(@PathParam(Params.USER_ID) String userID,
										 @PathParam(Params.WIDGET_ID) String widgetID) {
		widgetDataDao.deleteWidgetData(userID, widgetID);
	}

	/**
	 * Delete all widget data for a user.
	 *
	 * @param userID The user ID
	 */
	@DELETE
	public void deleteAll(@PathParam(Params.USER_ID) String userID) {
		widgetDataDao.deleteAllWidgetData(userID);
	}

}
