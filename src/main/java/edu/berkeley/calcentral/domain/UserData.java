package edu.berkeley.calcentral.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@XmlRootElement
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class UserData {

	public UserData() {}

	private List<WidgetData> widgetData;

	private CalCentralUser user = new CalCentralUser();


	public void setUser(CalCentralUser user) {
		this.user = user;
	}


	public List<WidgetData> getWidgetData() {
		return widgetData;
	}


	public void setWidgetData(List<WidgetData> widgetData) {
		this.widgetData = widgetData;
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
