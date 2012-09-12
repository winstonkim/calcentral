package edu.berkeley.calcentral.domain;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties({"misc_schedule"})
public class ClassPageSection {
	private String coursenum;
	private String enrolled_cur;
	private String enrolled_max;
	private String location;
	private String coords;
	private String note;
	private String section;
	private String time;
	private String weekdays;
	private String waitlist;
	private String midterm_datetime;
	private String midterm_location;
	private String midterm_coords;
	private String midterm_note;
	private String final_datetime;
	private String final_location;
	private String final_coords;
	private String final_note;
	private String restrictions;
	private List<ClassPageInstructor> section_instructors;
	
	private ClassPageSchedule misc_schedule;
	
	
	public String getEnrolled_cur() {
		return enrolled_cur;
	}
	public String getEnrolled_max() {
		return enrolled_max;
	}
	public String getLocation() {
		if (misc_schedule != null) {
			return misc_schedule.getLocation();
		}
		return location;
	}
	public String getCoords() {
		return coords;
	}
	public String getNote() {
		return note;
	}
	public String getSection() {
		return section;
	}
	public String getTime() { 
		return time;
	}
	public String getWaitlist() {
		return waitlist;
	}
	public String getMidterm_datetime() {
		return midterm_datetime;
	}
	public String getMidterm_location() {
		return midterm_location;
	}
	public String getMidterm_coords() {
		return midterm_coords;
	}
	public String getMidterm_note() {
		return midterm_note;
	}
	public String getFinal_datetime() {
		return final_datetime;
	}
	public String getFinal_location() {
		return final_location;
	}
	public String getFinal_coords() {
		return final_coords;
	}
	public String getFinal_note() {
		return final_note;
	}
	public String getRestrictions() {
		return restrictions;
	}
	public List<ClassPageInstructor> getSection_instructors() {
		return section_instructors;
	}
	
	public void setEnrolled_cur(String enrolled_cur) {
		this.enrolled_cur = enrolled_cur;
	}
	public void setEnrolled_max(String enrolled_max) {
		this.enrolled_max = enrolled_max;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public void setCoords(String coords) {
		this.coords = coords;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public void setSection(String section) {
		this.section = section;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public void setWaitlist(String waitlist) {
		this.waitlist = waitlist;
	}
	public void setMidterm_datetime(String midterm_datetime) {
		this.midterm_datetime = midterm_datetime;
	}
	public void setMidterm_location(String midterm_location) {
		this.midterm_location = midterm_location;
	}
	public void setMidterm_coords(String midterm_coords) {
		this.midterm_coords = midterm_coords;
	}
	public void setMidterm_note(String midterm_note) {
		this.midterm_note = midterm_note;
	}
	public void setFinal_datetime(String final_datetime) {
		this.final_datetime = final_datetime;
	}
	public void setFinal_location(String final_location) {
		this.final_location = final_location;
	}
	public void setFinal_coords(String final_coords) {
		this.final_coords = final_coords;
	}
	public void setFinal_note(String final_note) {
		this.final_note = final_note;
	}
	public void setRestrictions(String restrictions) {
		this.restrictions = restrictions;
	}
	public void setSection_instructors(List<ClassPageInstructor> section_instructors) {
		this.section_instructors = section_instructors;
	}
	public String getCoursenum() {
		return coursenum;
	}
	public void setCoursenum(String coursenum) {
		this.coursenum = coursenum;
	}
	public ClassPageSchedule getMisc_schedule() {
		return misc_schedule;
	}
	public void setMisc_schedule(ClassPageSchedule misc_schedule) {
		this.misc_schedule = misc_schedule;
	}
	public String getWeekdays() {
		if (misc_schedule != null) {
			return misc_schedule.getWeekdays();
		}
		return weekdays;
	}
	public void setWeekdays(String weekdays) {
		this.weekdays = weekdays;
	}

	@Override
	public String toString() {
		return "ClassPageSection{" +
				"coursenum='" + coursenum + '\'' +
				", enrolled_cur='" + enrolled_cur + '\'' +
				", enrolled_max='" + enrolled_max + '\'' +
				", location='" + location + '\'' +
				", coords='" + coords + '\'' +
				", note='" + note + '\'' +
				", section='" + section + '\'' +
				", time='" + time + '\'' +
				", weekdays='" + weekdays + '\'' +
				", waitlist='" + waitlist + '\'' +
				", midterm_datetime='" + midterm_datetime + '\'' +
				", midterm_location='" + midterm_location + '\'' +
				", midterm_coords='" + midterm_coords + '\'' +
				", midterm_note='" + midterm_note + '\'' +
				", final_datetime='" + final_datetime + '\'' +
				", final_location='" + final_location + '\'' +
				", final_coords='" + final_coords + '\'' +
				", final_note='" + final_note + '\'' +
				", restrictions='" + restrictions + '\'' +
				", section_instructors=" + section_instructors +
				", misc_schedule=" + misc_schedule +
				'}';
	}
}
