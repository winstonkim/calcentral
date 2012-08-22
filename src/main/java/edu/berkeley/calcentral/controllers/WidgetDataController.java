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

package edu.berkeley.calcentral.controllers;

import edu.berkeley.calcentral.RESTConstants;
import edu.berkeley.calcentral.domain.WidgetData;
import edu.berkeley.calcentral.services.WidgetDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Controller
@Path(WidgetDataController.PATH)
public class WidgetDataController {

	public static final String PATH = RESTConstants.PATH_USER + "/{userID}/widgetData";

	@Autowired
	private WidgetDataService widgetDataService;

	/**
	 * Get all the widget data for a particular user.
	 *
	 * @param userID The user to retrieve the widget data from
	 * @return Array of Widget data
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public List<WidgetData> getAllForUser(@PathParam(RESTConstants.PARAM_USER_ID) String userID) {
		return widgetDataService.getAllForUser(userID);
	}

	/**
	 * Get a particular widget's data.
	 *
	 * @param userID   The user to retrieve the widget data from
	 * @param widgetID The ID of the widget to get
	 * @return A single piece of Widget data
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{widgetID}")
	public WidgetData get(@PathParam(RESTConstants.PARAM_USER_ID) String userID,
												@PathParam("widgetID") String widgetID) {
		return widgetDataService.get(userID, widgetID);
	}

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
	@Path("{widgetID}")
	public WidgetData save(@PathParam(RESTConstants.PARAM_USER_ID) String userID,
												 @PathParam("widgetID") String widgetID,
												 @FormParam(RESTConstants.PARAM_DATA) String jsonData) {
		WidgetData widgetData = new WidgetData(userID, widgetID, jsonData);
		widgetDataService.save(widgetData);
		return widgetData;
	}

	/**
	 * Delete a widget.
	 *
	 * @param userID   The user ID
	 * @param widgetID The widget ID
	 */
	@DELETE
	@Path("{widgetID}")
	public void delete(@PathParam(RESTConstants.PARAM_USER_ID) String userID,
										 @PathParam("widgetID") String widgetID) {
		widgetDataService.delete(userID, widgetID);
	}

}
