package edu.berkeley.calcentral.domain;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties({"misc_scheduleprintcd", "misc_lowerRangeUnit", "misc_upperRangeUnit", "misc_variableUnitCd", "misc_fixedunit" })
public class ClassPageCourseInfo {
	private String title;
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
	
	//hidden values used for parsing
	private String misc_scheduleprintcd;
	private String misc_lowerRangeUnit;
	private String misc_upperRangeUnit;
	private String misc_variableUnitCd;
	private String misc_fixedunit;
	
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
}
