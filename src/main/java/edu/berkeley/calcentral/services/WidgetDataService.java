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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;

import edu.berkeley.calcentral.daos.WidgetDataDao;
import edu.berkeley.calcentral.domain.WidgetData;

@Service
public class WidgetDataService {

	@Autowired
	private WidgetDataDao widgetDataDao;
	
	public void save(WidgetData widgetData) {
	    //sanity check
	    if (Strings.nullToEmpty(widgetData.getUid()).isEmpty() ||
	            Strings.nullToEmpty(widgetData.getWidgetID()).isEmpty()) {
	        return;
	    }

		widgetDataDao.saveWidgetData(widgetData);
	}

	public List<WidgetData> getAllForUser(String userID) {
	    List<WidgetData> widgetData = widgetDataDao.getAllWidgetData(userID);
	    return widgetData;
	}

	public WidgetData get(String userID, String widgetID) {
	    WidgetData returnData = widgetDataDao.getWidgetData(userID, widgetID);
	    return returnData;
	}

	public void delete(String userID, String widgetID) {
	    widgetDataDao.deleteWidgetData(userID, widgetID);
	}

    public void deleteAll(String userID) {
        widgetDataDao.deleteAllWidgetData(userID);
    }

}
