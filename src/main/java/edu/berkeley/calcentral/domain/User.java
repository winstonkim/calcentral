package edu.berkeley.calcentral.domain;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;

/**
 * A CalCentral user, containing basic information that we can save in our local database.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class User {

	private String uid;

	private String preferredName;

	private String link;

	private String profileImageLink;

	private Timestamp firstLogin;

	//LDAP Fields
	private String title;

	private String website;

	private String address;

	private String city;

	private String state;

	private String publicEmail;

	public User() {
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getPreferredName() {
		return preferredName;
	}

	public void setPreferredName(String preferredName) {
		this.preferredName = preferredName;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Timestamp getFirstLogin() {
		return firstLogin;
	}

	public void setFirstLogin(Timestamp firstLogin) {
		this.firstLogin = firstLogin;
	}

	public String getProfileImageLink() {
		return profileImageLink;
	}

	public void setProfileImageLink(String profileImageLink) {
		this.profileImageLink = profileImageLink;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getPublicEmail() {
		return publicEmail;
	}

	public void setPublicEmail(String publicEmail) {
		this.publicEmail = publicEmail;
	}

	@Override
	public String toString() {
		return "User{" +
				"uid='" + uid + '\'' +
				", preferredName='" + preferredName + '\'' +
				", link='" + link + '\'' +
				", profileImageLink='" + profileImageLink + '\'' +
				", firstLogin=" + firstLogin +
				", title='" + title + '\'' +
				", website='" + website + '\'' +
				", address='" + address + '\'' +
				", city='" + city + '\'' +
				", state='" + state + '\'' +
				", publicEmail='" + publicEmail + '\'' +
				'}';
	}
}
