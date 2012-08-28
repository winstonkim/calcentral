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

import edu.berkeley.calcentral.Params;
import edu.berkeley.calcentral.Urls;
import org.jboss.resteasy.annotations.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;

import edu.berkeley.calcentral.daos.WidgetDataDao;
import edu.berkeley.calcentral.domain.WidgetData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Service
@Path(Urls.WIDGET_DATA)
public class WidgetDataService {

	@Autowired
	private WidgetDataDao widgetDataDao;

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
	public List<WidgetData> getAllForUser(@PathParam(Params.USER_ID) String userID) {
		return widgetDataDao.getAllWidgetData(userID);
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
	public WidgetData get(@PathParam(Params.USER_ID) String userID,
												@PathParam(Params.WIDGET_ID) String widgetID) {
		return widgetDataDao.getWidgetData(userID, widgetID);
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
