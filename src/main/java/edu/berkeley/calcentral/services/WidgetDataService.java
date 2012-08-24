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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.berkeley.calcentral.domain.WidgetData;

@Service
public class WidgetDataService {

	// TODO replace this with persistence to postgres
	//add another key, widgetID to mash on.
	private Map<String, Map<String, WidgetData>> widgets = new HashMap<String, Map<String, WidgetData>>();

	public void save(WidgetData widgetData) {
		Map<String, WidgetData> thisUsersWidgets = widgets.get(widgetData.getUid());
		if (thisUsersWidgets == null) {
			thisUsersWidgets = Maps.newHashMap();
		}
		
		thisUsersWidgets.put(widgetData.getWidgetID(), widgetData);
		widgets.put(widgetData.getUid(), thisUsersWidgets);
	}

	public List<WidgetData> getAllForUser(String userID) {
		//flatten the hashmap into a list.
		Map<String, WidgetData> widgetDataMap  = widgets.get(userID);
		if (widgetDataMap == null) {
			return null;
		}
		List<WidgetData> returnWidgetData = Lists.newArrayList();
		
		for (Map.Entry<String, WidgetData> widgetDataEntry : widgetDataMap.entrySet()) {
			returnWidgetData.add(widgetDataEntry.getValue());
		}
		
		return returnWidgetData;
	}

	public WidgetData get(String userID, String widgetID) {
		// TODO wow, this is inefficient! a db will be much better!
		Map<String, WidgetData> thisUsersWidgets = widgets.get(userID);
		if (thisUsersWidgets == null) {
			return null;
		}
		
		return thisUsersWidgets.get(widgetID);
	}

	public void delete(String userID, String widgetID) {
		Map<String, WidgetData> thisUsersWidgets = widgets.get(userID);
		if (thisUsersWidgets == null) {
			return;
		}
		thisUsersWidgets.remove(widgetID);
	}

}
