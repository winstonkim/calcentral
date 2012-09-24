package edu.berkeley.calcentral.system;

import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.daos.BaseDao;
import edu.berkeley.calcentral.daos.ClassListDao;
import edu.berkeley.calcentral.domain.College;
import edu.berkeley.calcentral.domain.Department;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CacheWarmer extends BaseDao {

	private static final Logger LOGGER = Logger.getLogger(CacheWarmer.class);

	private static final String URL_LOCALHOST = "http://localhost:8080";

	private static final String URL_CLASSLIST = URL_LOCALHOST + Urls.CLASS_LIST;

	private static final String URL_CLASSPAGE = URL_LOCALHOST + Urls.CLASS_PAGES;

	boolean enabled = true;

	@Autowired
	private ClassListDao classListDao;

	@SuppressWarnings("UnusedDeclaration")
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Scheduled(fixedRate = 1000 * 60 * 60 * 24) // every 24hrs
	public void warmup() throws InterruptedException {
		if (enabled) {
			Thread.sleep(15000); // so rest of server can start
			warm(50000);
		} else {
			LOGGER.warn("CacheWarmer is disabled");
		}
	}

	void warm(int limit) throws InterruptedException {
		Telemetry telemetry = new Telemetry(CacheWarmer.class, "warm(" + limit + ")");
		RestTemplate restTemplate = new RestTemplate();
		List<String> urls = buildUrlList(limit);
		int current = 0;
		int errorCount = 0;
		LOGGER.warn("Starting to warm " + urls.size() + " urls");
		for (String url : urls) {
			try {
				restTemplate.getForObject(url, String.class);
			} catch (RestClientException e) {
				LOGGER.error("Error getting " + url, e);
				errorCount++;
				Thread.sleep(500L); // sleep a little longer after errors
				if (errorCount > 10) {
					LOGGER.error("Got more than 10 http errors, aborting cache warmup");
					break;
				}
			}
			Thread.sleep(250L); // so we don't overload ourselves warming cache
			current++;
			if (current % 100 == 0) {
				LOGGER.debug("Warmed " + current + " urls of " + urls.size() + " total");
			}
		}
		telemetry.end();
		LOGGER.warn("Finished warming " + current + " of " + urls.size() + " total urls with "
				+ errorCount + " errors in " + telemetry.getTime() / 1000 + "s");
	}

	List<String> buildUrlList(int limit) {
		List<String> urls = new ArrayList<String>(5000);
		urls.add(URL_LOCALHOST + "/index.jsp");
		urls.add(URL_LOCALHOST + "/colleges-and-schools.jsp");

		List<College> colleges = classListDao.getAllColleges(limit);
		LOGGER.debug("Found " + colleges.size() + " colleges");
		for (College college : colleges) {
			urls.add(URL_CLASSLIST + "/" + college.getId());
		}

		List<Department> depts = classListDao.getAllDepartments(limit);
		LOGGER.debug("Found " + depts.size() + " departments");
		for (Department dept : depts) {
			urls.add(URL_CLASSLIST + "/" + dept.getCollegeID() + "/" + dept.getId());
		}

		List<Map<String, Object>> courses = classListDao.getAllClassIDs(limit);
		LOGGER.debug("Found " + courses.size() + " courses");
		for (Map<String, Object> course : courses) {
			urls.add(URL_CLASSPAGE + "/" + course.get("classid"));
		}

		return urls;
	}
}
