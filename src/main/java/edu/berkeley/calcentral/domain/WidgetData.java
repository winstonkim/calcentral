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

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Widget Data.
 *
 */
@XmlRootElement
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
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
}
