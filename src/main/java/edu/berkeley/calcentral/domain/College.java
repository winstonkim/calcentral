package edu.berkeley.calcentral.domain;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class College {

	private int id;

	private String slug;

	private String titlePrefix;

	private String title;

	private String cssClass;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getTitlePrefix() {
		return titlePrefix;
	}

	public void setTitlePrefix(String titlePrefix) {
		this.titlePrefix = titlePrefix;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	@Override
	public String toString() {
		return "College{" +
				"id=" + id +
				", slug='" + slug + '\'' +
				", titlePrefix='" + titlePrefix + '\'' +
				", title='" + title + '\'' +
				", cssClass='" + cssClass + '\'' +
				'}';
	}
}
