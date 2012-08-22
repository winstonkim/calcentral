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

import edu.berkeley.calcentral.domain.WidgetData;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WidgetDataService {

	// TODO replace this with persistence to postgres
	private Map<String, List<WidgetData>> widgets = new HashMap<String, List<WidgetData>>();

	public void save(WidgetData widgetData) {
		List<WidgetData> thisUsersWidgets = widgets.get(widgetData.getUid());
		if (thisUsersWidgets == null) {
			thisUsersWidgets = new ArrayList<WidgetData>();
		}
		thisUsersWidgets.add(widgetData);
		widgets.put(widgetData.getUid(), thisUsersWidgets);
	}

	public List<WidgetData> getAllForUser(String userID) {
		return widgets.get(userID);
	}

	public WidgetData get(String userID, String widgetID) {
		// TODO wow, this is inefficient! a db will be much better!
		List<WidgetData> thisUsersWidgets = widgets.get(userID);
		if (thisUsersWidgets == null) {
			return null;
		}
		for (WidgetData widgetData : thisUsersWidgets) {
			if (widgetData.getWidgetID().equals(widgetID)) {
				return widgetData;
			}
		}
		return null;
	}

	public void delete(String userID, String widgetID) {
		List<WidgetData> thisUsersWidgets = widgets.get(userID);
		if (thisUsersWidgets == null) {
			return;
		}
		WidgetData widgetToRemove = null;
		for (WidgetData widgetData : thisUsersWidgets) {
			if (widgetData.getWidgetID().equals(widgetID)) {
				widgetToRemove = widgetData;
			}
		}
		if ( widgetToRemove != null ) {
			thisUsersWidgets.remove(widgetToRemove);
		}
	}

}
