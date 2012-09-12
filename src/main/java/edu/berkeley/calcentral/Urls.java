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

package edu.berkeley.calcentral;

public interface Urls {

	String API = "/api";

	String USERS = API + "/user";

	String CURRENT_USER = API + "/currentUser";

	String SPECIFIC_USER = USERS + "/{" + Params.USER_ID + "}";

	String WIDGET_DATA = SPECIFIC_USER + "/widgetData";

	String GIT_INFO = API + "/gitInfo";

	String DASHBOARD = "/dashboard";

	String PROFILE = "/profile";

	String CLASS_PAGES = API + "/classPages";

	String CLASS_LIST = API + "/classList";

	String BSPACE_FAVORITES = API + "/bspacefavorites";

	String BSPACE_FAVORITES_UNREAD = BSPACE_FAVORITES + "/unread";

	String CANVAS = API + "/canvas";

	String CANVAS_ACCOUNT_PATH = "/accounts/{" + Params.CANVAS_ACCOUNT_ID + "}";

}
