package edu.berkeley.calcentral.domain;

import com.google.common.collect.Maps;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.jboss.util.Strings;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
@JsonIgnoreProperties({"misc_scheduleprintcd", "misc_lowerRangeUnit", "misc_upperRangeUnit", 
	"misc_variableUnitCd", "misc_fixedunit", "misc_deptname"})
public class ClassPageCourseInfo {
	private String title;
	private String catalogid;
	private String format;
	private String grading;
	private String prereqs;
	private String requirements;
	private String term;
	private String semesters_offered;
	private String year;
	private String department;
	private String coursenum;
	private String units;
	private String webcastId;
	private String webcastUrl;
	private String canvasCourseId;

	//hidden values used for parsing
	private String misc_scheduleprintcd;
	private String misc_lowerRangeUnit;
	private String misc_upperRangeUnit;
	private String misc_variableUnitCd;
	private String misc_fixedunit;
	private String misc_deptname;
	
	public void decodeAll() {
		gradingDecode();
		termDecode();
		courseNumDecode();
		unitsDecode();
		webcastUrlDecode();
	}
	
	private void gradingDecode() {
		Map<String, String> gradingDict = Maps.newHashMap();
		gradingDict.put("PF", "PASSED/NOT PASSED");
		gradingDict.put("SU", "SATISFACTORY/UNSATISFACTORY");
		String gradingLookup = gradingDict.get(grading);
		if (gradingLookup == null) {
			gradingLookup = "Letter Grade";
		}
		grading = gradingLookup;
		return;
	}

	private void termDecode() {
		Map<String, String> termDict = Maps.newHashMap();
		termDict.put("B", "Spring");
		termDict.put("C", "Summer");
		termDict.put("D", "Fall");
		String termLookup = termDict.get(term);
		if (termLookup == null) {
			termLookup = "Letter Grade";
		}
		term = termLookup;
		return;
	}

	/** 
	 * This is going to handwave some of the other security implications related to SCHEDULE_PRINT_CD for the time being. 
	 * See CLC-13 for details.
	 */
	private void courseNumDecode() {
		Map<String, String> printCdDict = Maps.newHashMap();
		printCdDict.put("H", "SEE NOTE");
		printCdDict.put("G", "SEE DEPT");
		printCdDict.put("E", "SEE DEPT");
		printCdDict.put("D", "");
		printCdDict.put("C", "NONE");
		printCdDict.put("B", "TO BE ARRANGED");
		String courseNumValue = printCdDict.get(misc_scheduleprintcd);
		if (courseNumValue != null) {
			coursenum = courseNumValue;
		}
		return;
	}

	private void unitsDecode() {
		if (misc_fixedunit != null && !misc_fixedunit.equalsIgnoreCase("0.0")) {
			units = misc_fixedunit;
			return;
		}

		Map<String, String> variableUnitCdDict = Maps.newHashMap();
		variableUnitCdDict.put("F", misc_lowerRangeUnit);
		variableUnitCdDict.put("R", misc_lowerRangeUnit + " - " + misc_upperRangeUnit);
		variableUnitCdDict.put("E", misc_lowerRangeUnit + " or " + misc_upperRangeUnit);
		String variableUnitValue = variableUnitCdDict.get(misc_variableUnitCd);
		if (variableUnitValue == null) {
			variableUnitValue = "0";
		}
		units = variableUnitValue;
		return;
	}

	private void webcastUrlDecode() {
		if (Strings.hasLength(webcastId)) {
			webcastUrl = "http://gdata.youtube.com/feeds/api/playlists/" + webcastId + "?v=2&alt=json&max-results=50";
		} else {
			webcastUrl = "";
		}
	}

	public String getTitle() {
		return title;
	}
	public String getFormat() {
		return format;
	}
	public String getGrading() {
		return grading;
	}
	public String getPrereqs() {
		return prereqs;
	}
	public String getRequirements() {
		return requirements;
	}
	public String getTerm() {
		return term;
	}
	public String getSemesters_offered() {
		return semesters_offered;
	}
	public String getYear() {
		return year;
	}
	public String getDepartment() {
		return department;
	}
	public String getCoursenum() {
		return coursenum;
	}
	public String getUnits() {
		return units;
	}
	public String getWebcastId() {
		return webcastId;
	}
	public String getWebcastUrl() {
		return webcastUrl;
	}
	public String getCanvasCourseId() {
		return canvasCourseId;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public void setGrading(String grading) {
		this.grading = grading;
	}
	public void setPrereqs(String prereqs) {
		this.prereqs = prereqs;
	}
	public void setRequirements(String requirements) {
		this.requirements = requirements;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public void setSemesters_offered(String semesters_offered) {
		this.semesters_offered = semesters_offered;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public void setCoursenum(String coursenum) {
		this.coursenum = coursenum;
	}
	public void setUnits(String units) {
		this.units = units;
	}
	public String getMisc_scheduleprintcd() {
		return misc_scheduleprintcd;
	}
	public String getMisc_lowerRangeUnit() {
		return misc_lowerRangeUnit;
	}
	public String getMisc_upperRangeUnit() {
		return misc_upperRangeUnit;
	}
	public String getMisc_variableUnitCd() {
		return misc_variableUnitCd;
	}
	public void setMisc_scheduleprintcd(String misc_scheduleprintcd) {
		this.misc_scheduleprintcd = misc_scheduleprintcd;
	}
	public void setMisc_lowerRangeUnit(String misc_lowerRangeUnit) {
		this.misc_lowerRangeUnit = misc_lowerRangeUnit;
	}
	public void setMisc_upperRangeUnit(String misc_upperRangeUnit) {
		this.misc_upperRangeUnit = misc_upperRangeUnit;
	}
	public void setMisc_variableUnitCd(String misc_variableUnitCd) {
		this.misc_variableUnitCd = misc_variableUnitCd;
	}
	public String getMisc_fixedunit() {
		return misc_fixedunit;
	}
	public void setMisc_fixedunit(String misc_fixedunit) {
		this.misc_fixedunit = misc_fixedunit;
	}

	public String getCatalogid() {
		return catalogid;
	}

	public void setCatalogid(String catalogid) {
		this.catalogid = catalogid;
	}

	public String getMisc_deptname() {
		return misc_deptname;
	}

	public void setMisc_deptname(String misc_deptname) {
		this.misc_deptname = misc_deptname;
	}
	public void setWebcastId(String webcastId) {
		this.webcastId = webcastId;
	}
	public void setWebcastUrl(String webcastUrl) {
		this.webcastUrl = webcastUrl;
	}
	public void setCanvasCourseId(String canvasCourseId) {
		this.canvasCourseId = canvasCourseId;
	}

	@Override
	public String toString() {
		return "ClassPageCourseInfo{" +
				"title='" + title + '\'' +
				", catalogid='" + catalogid + '\'' +
				", format='" + format + '\'' +
				", grading='" + grading + '\'' +
				", prereqs='" + prereqs + '\'' +
				", requirements='" + requirements + '\'' +
				", term='" + term + '\'' +
				", semesters_offered='" + semesters_offered + '\'' +
				", year='" + year + '\'' +
				", department='" + department + '\'' +
				", coursenum='" + coursenum + '\'' +
				", units='" + units + '\'' +
				", webcastUrl=" + webcastUrl +
				", canvasCourseId=" + canvasCourseId +
				", misc_scheduleprintcd='" + misc_scheduleprintcd + '\'' +
				", misc_lowerRangeUnit='" + misc_lowerRangeUnit + '\'' +
				", misc_upperRangeUnit='" + misc_upperRangeUnit + '\'' +
				", misc_variableUnitCd='" + misc_variableUnitCd + '\'' +
				", misc_fixedunit='" + misc_fixedunit + '\'' +
				", misc_deptname='" + misc_deptname + '\'' +
				'}';
	}
}
