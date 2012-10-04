package edu.berkeley.calcentral.domain;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
@JsonIgnoreProperties({"misc_deptname"})
public class ClassPage {
	private String classid;
	private String info_last_updated;
	private ClassPageCourseInfo courseinfo; 
	private String classtitle;
	private String department;
	private String catalogid;
	private String description;
	private List<ClassPageInstructor> instructors;
	private List<ClassPageSchedule> schedule; 
	private List<ClassPageSection> sections;
	private String deptId;
	private String collegeId;
	private String collegeName;

	@JsonIgnore
	private String misc_deptname;

	public String getClassId() {
		return classid;
	}
	public String getInfo_last_updated() {
		return info_last_updated;
	}
	public ClassPageCourseInfo getCourseinfo() {
		return courseinfo;
	}
	public String getClasstitle() {
		return classtitle;
	}
	public String getDepartment() {
		return department;
	}
	public String getCatalogid() {
		return catalogid;
	}
	public String getDescription() {
		return description;
	}
	public List<ClassPageInstructor> getInstructors() {
		return instructors;
	}
	public List<ClassPageSchedule> getSchedule() {
		return schedule;
	}
	public List<ClassPageSection> getSections() {
		return sections;
	}
	public void setClassId(String classId) {
		this.classid = classId;
	}
	public void setInfo_last_updated(String info_last_updated) {
		this.info_last_updated = info_last_updated;
	}
	public void setCourseinfo(ClassPageCourseInfo courseinfo) {
		this.courseinfo = courseinfo;
	}
	public void setClasstitle(String classtitle) {
		this.classtitle = classtitle;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public void setCatalogid(String catalogid) {
		this.catalogid = catalogid;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setInstructors(List<ClassPageInstructor> instructors) {
		this.instructors = instructors;
	}
	public void setSchedule(List<ClassPageSchedule> schedule) {
		this.schedule = schedule;
	}
	public void setSections(List<ClassPageSection> sections) {
		this.sections = sections;
	}

	public String getMisc_deptname() {
		return misc_deptname;
	}

	public void setMisc_deptname(String misc_deptname) {
		this.misc_deptname = misc_deptname;
	}

	public String getDeptId() {
		return deptId;
	}

	public void setDeptId(String deptId) {
		this.deptId = deptId;
	}

	public String getCollegeId() {
		return collegeId;
	}

	public void setCollegeId(String collegeId) {
		this.collegeId = collegeId;
	}

	public String getCollegeName() {
		return collegeName;
	}

	public void setCollegeName(String collegeName) {
		this.collegeName = collegeName;
	}

	@Override
	public String toString() {
		return "ClassPage{" +
				"classid='" + classid + '\'' +
				", info_last_updated='" + info_last_updated + '\'' +
				", courseinfo=" + courseinfo +
				", classtitle='" + classtitle + '\'' +
				", department='" + department + '\'' +
				", catalogid='" + catalogid + '\'' +
				", description='" + description + '\'' +
				", instructors=" + instructors +
				", schedule=" + schedule +
				", sections=" + sections +
				", deptId=" + deptId +
				", collegeId=" + collegeId +
				", collegeName='" + collegeName + '\'' +
				", misc_deptname='" + misc_deptname + '\'' +
				'}';
	}
}
