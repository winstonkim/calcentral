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
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

@Service
public class CanvasLoader {
	private static final Logger LOGGER = Logger.getLogger(CanvasLoader.class);
	private static final String CANVAS_IMPORT_PATH = Urls.CANVAS_ACCOUNT_PATH +
			"/sis_imports.json?import_type=instructure_csv";
	private static final String WORKING_DIR = "target/canvasload/";
	private static final String ENROLLMENTS_CSV_FILE = WORKING_DIR + "enrollments.csv";
	private static final String USERS_CSV_FILE = WORKING_DIR + "users.csv";
	@Autowired
	CanvasProxy canvasProxy;
	@Autowired
	@Qualifier("campusDataSource")
	DataSource campusDataSource;
	@Autowired
	EnrollmentCSVGenerator enrollmentCSVGenerator;
	@Autowired
	UserCSVGenerator userCSVGenerator;

	public CanvasLoader() {
	}
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
			loadCsvResource(new ClassPathResource("/canvas/" + csvName), false);
		}
	}

	public void loadCampusData() throws IOException {
		Resource enrollmentsStarter = new ClassPathResource("/canvas/static-enrollments.csv");
		FileUtils.copyInputStreamToFile(enrollmentsStarter.getInputStream(), new File(ENROLLMENTS_CSV_FILE));
		Resource usersStarter = new ClassPathResource("/canvas/static-users.csv");
		FileUtils.copyInputStreamToFile(usersStarter.getInputStream(), new File(USERS_CSV_FILE));
		enrollmentCSVGenerator.generate(ENROLLMENTS_CSV_FILE);
		userCSVGenerator.generate(ENROLLMENTS_CSV_FILE, USERS_CSV_FILE);
		loadCsvResource(new FileSystemResource(USERS_CSV_FILE), false);
		loadCsvResource(new FileSystemResource(ENROLLMENTS_CSV_FILE), true);
	}

	private String loadCsvResource(Resource csvResource, boolean replaceAll) {
		MultiValueMap<String, Object> data = new LinkedMultiValueMap<String, Object>();
		if (replaceAll) {
			// TODO Canvas is unable to parse the request with these additional parameters.
//			data.add("batch_mode", "1");
//			data.add("batch_mode_term_id", "sis_term_id:2012-D");
		}
		data.add("attachment", csvResource);
		String response = canvasProxy.doAdminMethod(HttpMethod.POST, CANVAS_IMPORT_PATH, data).getEntity().toString();
		LOGGER.info("Imported " + csvResource + "; got " + response);
		return response;
	}

	public static void main(String args[]) {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/applicationContext-service.xml");
		CanvasLoader canvasLoader = applicationContext.getBean(CanvasLoader.class);
		canvasLoader.loadStaticData();
		try {
			canvasLoader.loadCampusData();
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
}
