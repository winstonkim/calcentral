package edu.berkeley.calcentral.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ClassPage {
	private String classid;
	private String info_last_updated;
	private ClassPageCourseInfo courseinfo; 
	private String classtitle;
	private String department;
	private String description;
	private List<ClassPageInstructor> instructors;
	private List<ClassPageSchedule> schedule; 
	private List<ClassPageSection> sections;

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
}
