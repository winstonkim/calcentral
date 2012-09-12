package edu.berkeley.calcentral.domain;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Department {

	private String key;

	private String title;

	private int id;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Department{" +
				"key='" + key + '\'' +
				", title='" + title + '\'' +
				", id=" + id +
				'}';
	}
}
