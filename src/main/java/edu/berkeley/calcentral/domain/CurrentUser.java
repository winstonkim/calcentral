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
			this.loggedIn = true;
		}
	}

	private UserData user = new UserData();

	public void setUserData(UserData userData) {
		this.user = userData;
	}

	private boolean loggedIn;

	public boolean isLoggedIn() {
		return loggedIn;
	}


	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}


	public List<WidgetData> getWidgetData() {
		return user.getWidgetData();
	}


	public void setWidgetData(List<WidgetData> widgetData) {
		user.setWidgetData(widgetData);
	}

	/**
	 * @return the lastName
	 */
	 public String getLastName() {
		return user.getLastName();
	}

	/**
	 * @param lastName the lastName to set
	 */
	 public void setLastName(String lastName) {
		 user.setLastName(lastName);
	 }

	 /**
	  * @return the firstName
	  */
	 public String getFirstName() {
		 return user.getFirstName();
	 }

	 /**
	  * @param firstName the firstName to set
	  */
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
