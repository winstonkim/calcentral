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

import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.services.CanvasProxy;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.sql.DataSource;

public class CanvasLoader {
	private static final Logger LOGGER = Logger.getLogger(CanvasLoader.class);
	private static final String CANVAS_IMPORT_PATH = Urls.CANVAS_ACCOUNT_PATH +
			"/sis_imports.json?import_type=instructure_csv";
	private CanvasProxy canvasProxy;
	private DataSource campusDataSource;

	public CanvasLoader(ApplicationContext applicationContext) {
		LOGGER.warn("Got applicationContext = " + applicationContext);
		canvasProxy = applicationContext.getBean(CanvasProxy.class);
		LOGGER.warn("Got canvasProxy = " + canvasProxy + " with accountID = " + canvasProxy.getAccountId());
		campusDataSource = applicationContext.getBean("campusDataSource", DataSource.class);
	}

	public void loadStaticData() {
		final String[] csvNames = {
				"departments.csv",
				"terms.csv",
				"courses.csv",
				"sections.csv"
		};
		for (String csvName : csvNames) {
			Resource csvFile = new ClassPathResource("/canvas/" + csvName);
			MultiValueMap<String, Object> data = new LinkedMultiValueMap<String, Object>();
			data.add("attachment", csvFile);
			String response = canvasProxy.post(CANVAS_IMPORT_PATH, data);
			LOGGER.info("Imported " + csvName + "; got " + response);
		}
	}

	public static void main(String args[]) {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/applicationContext-service.xml");
		CanvasLoader canvasLoader = new CanvasLoader(applicationContext);
		canvasLoader.loadStaticData();
	}
}
