package edu.berkeley.calcentral.domain;

import org.codehaus.jackson.annotate.JsonIgnore;

public class ClassPageInstructor {
	private String email;
	private String id;
	private String name;
	private String office;
	private String phone;
	private String img;
	private String title;
	private String url;
	
	@JsonIgnore
	private String misc_email_disclosure;
	
	public String getEmail() {
		return email;
	}
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getOffice() {
		return office;
	}
	public String getPhone() {
		return phone;
	}
	public String getImg() {
		return img;
	}
	public String getTitle() {
		return title;
	}
	public String getUrl() {
		return url;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setOffice(String office) {
		this.office = office;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public void setImg(String img) {
		this.img = img;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getMisc_email_disclosure() {
		return misc_email_disclosure;
	}
	public void setMisc_email_disclosure(String misc_email_disclosure) {
		this.misc_email_disclosure = misc_email_disclosure;
	}
	
}
