package edu.berkeley.calcentral.system;

import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.daos.ClassPagesDao;
import edu.berkeley.calcentral.daos.ClassPagesLocalDataDao;
import edu.berkeley.calcentral.services.CanvasProxy;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Iterator;

@Service
public class CanvasCourseIDFetcher {

	private static final String CANVAS_COURSE_PATH = Urls.CANVAS_ACCOUNT_PATH +
			"/courses?per_page=100";

	private static final String CANVAS_SECTIONS_PATH = "/courses/COURSEID/sections?per_page=100";

	private static final Logger LOGGER = Logger.getLogger(CanvasCourseIDFetcher.class);

	@Autowired
	CanvasProxy proxy;

	@Autowired
	ClassPagesLocalDataDao dao;

	private ObjectMapper mapper = new ObjectMapper();

	public void fetch() throws IOException {
		String response = proxy.doAdminMethod(HttpMethod.GET, CANVAS_COURSE_PATH).getEntity().toString();

		// TODO process link headers in response to handle more than 100 courses
		JsonNode json = mapper.readValue(response, JsonNode.class);
		LOGGER.debug("All courses response JSON: " + json.toString());

		Iterator<JsonNode> courses = json.getElements();
		while (courses.hasNext()) {
			JsonNode course = courses.next();
			if (course.get("sis_course_id") != null) {
				String sisCourseID = course.get("sis_course_id").getTextValue();
				String canvasCourseID = String.valueOf(course.get("id").getIntValue());

				if (sisCourseID != null && canvasCourseID != null) {

					LOGGER.info("Getting section list for cal course " + sisCourseID + "; canvasCourseID " + canvasCourseID);
					String url = CANVAS_SECTIONS_PATH.replaceAll("COURSEID", canvasCourseID);
					JsonNode sectionsJSON = mapper.readValue(
							proxy.doAdminMethod(HttpMethod.GET, url).getEntity().toString(), JsonNode.class);
					LOGGER.debug("Sections JSON = " + sectionsJSON.toString());
					Iterator<JsonNode> sectionIterator = sectionsJSON.getElements();

					while (sectionIterator.hasNext()) {
						JsonNode section = sectionIterator.next();
						String sisSectionID = section.get("sis_section_id").getTextValue();
						// converting form of ccn from the CSV files into the form used in internal storage
						if (sisSectionID != null) {
							String[] idParts = ClassPagesDao.getSectionIDParts(sisSectionID);
							String calcentralClassPageID = idParts[0] + idParts[1] + idParts[2];
							LOGGER.info("Updating classPage " + calcentralClassPageID + " to set canvasCourseID = " + canvasCourseID);
							dao.updateCanvasId(calcentralClassPageID, canvasCourseID);
						}
					}
				}
			}
		}

		LOGGER.info("Done fetching canvas course IDs");
	}

	public static void main(String args[]) {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/applicationContext-service.xml");
		CanvasCourseIDFetcher canvasLoader = applicationContext.getBean(CanvasCourseIDFetcher.class);

		try {
			canvasLoader.fetch();
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

	}

}
