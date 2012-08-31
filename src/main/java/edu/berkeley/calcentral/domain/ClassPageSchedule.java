package edu.berkeley.calcentral.domain;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties({"misc_room", "misc_building_name", "misc_weekdays"})
public class ClassPageSchedule {
	private String coords;
	private String location;
	private String time;
	private String weekdays;
	private String current_sem;
	
	//fields to ignore
	private String misc_room;
	private String misc_building_name;
	private char[] misc_weekdays;
	
	public String getCoords() {
		return coords;
	}
	public String getLocation() {
		return location;
	}
	public String getTime() {
		return time;
	}
	public String getWeekdays() {
		return weekdays;
	}
	public String getCurrent_sem() {
		return current_sem;
	}
	public void setCoords(String coords) {
		this.coords = coords;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public void setWeekdays(String weekdays) {
		this.weekdays = weekdays;
	}
	public void setCurrent_sem(String current_sem) {
		this.current_sem = current_sem;
	}
	public String getMisc_room() {
		return misc_room;
	}
	public String getMisc_building_name() {
		return misc_building_name;
	}
	public void setMisc_room(String misc_room) {
		this.misc_room = misc_room;
	}
	public void setMisc_building_name(String misc_building_name) {
		this.misc_building_name = misc_building_name;
	}
	public char[] getMisc_weekdays() {
		return misc_weekdays;
	}
	public void setMisc_weekdays(char[] misc_weekdays) {
		this.misc_weekdays = misc_weekdays;
	}
}
