package edu.berkeley.calcentral.domain;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
	
	
	public void decodeAll() {
		locationDecode();
		weekdaysDecode();
	}

	private void weekdaysDecode() {
		List<String> weekdaysList = Lists.newArrayList();
		if (misc_weekdays != null) {
			Map<Character, String> weekdayDict = Maps.newHashMap();
			weekdayDict.put('M', "Mon");
			weekdayDict.put('W', "Wed");
			weekdayDict.put('F', "Fri");

			//because some genius decided not to differentiate between tues and thurs, sunday and saturdays...
			int index = 0;
			for (char possibleDay : misc_weekdays) {
				String translatedDay = "";
				if (possibleDay == 'S' && index == 0) {
					translatedDay = "Sun";
				} else if (possibleDay == 'S' && index == 6) {
					translatedDay = "Sat";
				} else if (possibleDay == 'T' && index == 2) {
					translatedDay = "Tues";
				} else if (possibleDay == 'T' && index == 4) {
					translatedDay = "Thurs";
				} else {
					translatedDay = Strings.nullToEmpty(weekdayDict.get(possibleDay));
				}

				if (!translatedDay.isEmpty()) {
					weekdaysList.add(translatedDay);
				}
				index++;
			}
		}

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < weekdaysList.size(); i++) {
			if (i == 0) {
				sb.append(weekdaysList.get(i));
			} else if (i == weekdaysList.size()-1) {
				sb.append(" and ").append(weekdaysList.get(i));
			} else {
				sb.append(", ").append(weekdaysList.get(i));
			}
		}
		
		weekdays = sb.toString();
	}

	private void locationDecode() {
		//attempts to cleanup the data....
		misc_room = Strings.nullToEmpty(misc_room);
		misc_building_name = Strings.nullToEmpty(misc_building_name);
		
		try {
			misc_room = Integer.toString(Integer.parseInt(misc_room));
		} catch (NumberFormatException e) {
			//dont' touch the ugly formatted room.
		}
		
		if (misc_building_name.equalsIgnoreCase("No Facility")) {
			misc_building_name = "";
		}
		
		if (!misc_building_name.isEmpty() && !misc_room.isEmpty()) {
			location = misc_room + " " + misc_building_name;
		}
	}
	
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

	@Override
	public String toString() {
		return "ClassPageSchedule{" +
				"coords='" + coords + '\'' +
				", location='" + location + '\'' +
				", time='" + time + '\'' +
				", weekdays='" + weekdays + '\'' +
				", current_sem='" + current_sem + '\'' +
				", misc_room='" + misc_room + '\'' +
				", misc_building_name='" + misc_building_name + '\'' +
				", misc_weekdays=" + misc_weekdays +
				'}';
	}
}
