package edu.berkeley.calcentral.system;

import edu.berkeley.calcentral.Urls;
import edu.berkeley.calcentral.daos.BaseDao;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
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

	@SuppressWarnings("UnusedDeclaration")
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Scheduled(fixedRate = 1000 * 60 * 60 * 24) // every 24hrs
	public void warmup() throws InterruptedException {
		if (enabled) {
			Thread.sleep(15000); // so rest of server can start
			warm(10000);
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
				if (errorCount > 10) {
					LOGGER.error("Got more than 10 http errors, aborting cache warmup");
					break;
				}
			}
			Thread.sleep(100L); // so we don't overload ourselves warming cache
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

		MapSqlParameterSource params = new MapSqlParameterSource("limit", limit);

		List<Map<String, Object>> collegeSlugs = queryRunner.queryForList(
				"SELECT id " +
						"FROM calcentral_classtree_colleges " +
						"ORDER BY id " +
						"LIMIT :limit ", params);
		LOGGER.debug("Found " + collegeSlugs.size() + " colleges");
		for (Map<String, Object> college : collegeSlugs) {
			urls.add(URL_CLASSLIST + "/" + college.get("id"));
		}

		List<Map<String, Object>> depts = queryRunner.queryForList(
				"SELECT c.id, d.id deptid " +
						"FROM calcentral_classtree_colleges c, calcentral_classtree_departments d " +
						"WHERE c.id = d.college_id " +
						"ORDER BY d.college_id, d.dept_key " +
						"LIMIT :limit", params);
		LOGGER.debug("Found " + depts.size() + " departments");
		for (Map<String, Object> dept : depts) {
			urls.add(URL_CLASSLIST + "/" + dept.get("id") + "/" + dept.get("deptid"));
		}

		List<Map<String, Object>> courses = campusQueryRunner.queryForList(
				"SELECT bci.TERM_YR || bci.TERM_CD || bci.COURSE_CNTL_NUM classid " +
						"FROM BSPACE_COURSE_INFO_VW bci " +
						"WHERE TERM_YR = 2012 AND TERM_CD = 'D' AND INSTRUCTION_FORMAT = 'LEC' " +
						"AND ROWNUM <= :limit " +
						"ORDER BY bci.DEPT_NAME", params);
		LOGGER.debug("Found " + courses.size() + " courses");
		for (Map<String, Object> course : courses) {
			urls.add(URL_CLASSPAGE + "/" + course.get("classid"));
		}

		return urls;
	}
}
