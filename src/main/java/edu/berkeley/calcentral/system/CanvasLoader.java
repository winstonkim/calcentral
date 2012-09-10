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
package edu.berkeley.calcentral.system;

import edu.berkeley.calcentral.services.CanvasProxy;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;

public class CanvasLoader {
	private static final Logger LOGGER = Logger.getLogger(CanvasLoader.class);
	ApplicationContext applicationContext;
	CanvasProxy canvasProxy;
	protected DataSource campusDataSource;
	protected DataSource h2CampusDataSource;

	public CanvasLoader(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		LOGGER.warn("Got applicationContext = " + applicationContext);
		canvasProxy = applicationContext.getBean(CanvasProxy.class);
		LOGGER.warn("Got canvasProxy = " + canvasProxy + " with accountID = " + canvasProxy.getAccountId());
		campusDataSource = applicationContext.getBean("campusDataSource", DataSource.class);
		h2CampusDataSource = applicationContext.getBean("h2CampusDataSource", DataSource.class);
		LOGGER.warn("Got campusDataSource = " + campusDataSource);
	}

	public static void main(String args[]) {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/applicationContext-service.xml");
		CanvasLoader canvasLoader = new CanvasLoader(applicationContext);
	}
}
