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

package edu.berkeley.calcentral.domain;

import javax.ws.rs.FormParam;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WidgetData {

	private String uid;

	private String widgetID;

	private String data;

	public WidgetData() {
		// required by xml framework
	}

	public WidgetData(String uid, String widgetID, String data) {
		this.uid = uid;
		this.widgetID = widgetID;
		this.data = data;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getWidgetID() {
		return widgetID;
	}

	@FormParam("widgetID")
	public void setWidgetID(String widgetID) {
		this.widgetID = widgetID;
	}

	public String getData() {
		return data;
	}

	@FormParam("data")
	public void setData(String data) {
		this.data = data;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		WidgetData that = (WidgetData) o;

		if (data != null ? !data.equals(that.data) : that.data != null) return false;
		if (uid != null ? !uid.equals(that.uid) : that.uid != null) return false;
		if (widgetID != null ? !widgetID.equals(that.widgetID) : that.widgetID != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = uid != null ? uid.hashCode() : 0;
		result = 31 * result + (widgetID != null ? widgetID.hashCode() : 0);
		result = 31 * result + (data != null ? data.hashCode() : 0);
		return result;
	}
}
