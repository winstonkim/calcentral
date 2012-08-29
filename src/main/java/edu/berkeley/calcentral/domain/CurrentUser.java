package edu.berkeley.calcentral.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@XmlRootElement
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class CurrentUser {

	public CurrentUser() {}

	public CurrentUser(UserData user) {
		if (user != null) {
			this.user = user;
		}
	}

	private UserData user = new UserData();

	public void setUserData(UserData userData) {
		this.user = userData;
	}

	public List<WidgetData> getWidgetData() {
		return user.getWidgetData();
	}


	public void setWidgetData(List<WidgetData> widgetData) {
		user.setWidgetData(widgetData);
	}

	public String getLastName() {
		return user.getLastName();
	}

	public void setLastName(String lastName) {
		user.setLastName(lastName);
	}

	public String getFirstName() {
		return user.getFirstName();
	}

	public void setFirstName(String firstName) {
		user.setFirstName(firstName);
	}

	public String getUid() {
		return user.getUid();
	}

	public void setUid(String uid) {
		user.setUid(uid);
	}

}
