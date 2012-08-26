package edu.berkeley.calcentral.domain;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Properties;

@XmlRootElement
@Component
public class GitInfo {

	@Resource(name = "gitProperties")
	private Properties properties;

	String branch;

	String commitId;

	String commitIdAbbrev;

	String buildUserName;

	String buildUserEmail;

	String buildTime;

	String commitUserName;

	String commitUserEmail;

	String commitMessageFull;

	String commitMessageShort;

	String commitTime;

	public GitInfo() {
		// required by framework
	}

	@PostConstruct
	public void init() {
		this.branch = properties.getProperty("git.branch");
		this.commitId = properties.getProperty("git.commit.id");
		this.buildUserName = properties.getProperty("git.build.user.name");
		this.buildUserEmail = properties.getProperty("git.build.user.email");
		this.buildTime = properties.getProperty("git.build.time");
		this.commitUserName = properties.getProperty("git.commit.user.name");
		this.commitUserEmail = properties.getProperty("git.commit.user.email");
		this.commitMessageShort = properties.getProperty("git.commit.message.short");
		this.commitMessageFull = properties.getProperty("git.commit.message.full");
		this.commitTime = properties.getProperty("git.commit.time");
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getCommitId() {
		return commitId;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

	public String getCommitIdAbbrev() {
		return commitIdAbbrev;
	}

	public void setCommitIdAbbrev(String commitIdAbbrev) {
		this.commitIdAbbrev = commitIdAbbrev;
	}

	public String getBuildUserName() {
		return buildUserName;
	}

	public void setBuildUserName(String buildUserName) {
		this.buildUserName = buildUserName;
	}

	public String getBuildUserEmail() {
		return buildUserEmail;
	}

	public void setBuildUserEmail(String buildUserEmail) {
		this.buildUserEmail = buildUserEmail;
	}

	public String getBuildTime() {
		return buildTime;
	}

	public void setBuildTime(String buildTime) {
		this.buildTime = buildTime;
	}

	public String getCommitUserName() {
		return commitUserName;
	}

	public void setCommitUserName(String commitUserName) {
		this.commitUserName = commitUserName;
	}

	public String getCommitUserEmail() {
		return commitUserEmail;
	}

	public void setCommitUserEmail(String commitUserEmail) {
		this.commitUserEmail = commitUserEmail;
	}

	public String getCommitMessageFull() {
		return commitMessageFull;
	}

	public void setCommitMessageFull(String commitMessageFull) {
		this.commitMessageFull = commitMessageFull;
	}

	public String getCommitMessageShort() {
		return commitMessageShort;
	}

	public void setCommitMessageShort(String commitMessageShort) {
		this.commitMessageShort = commitMessageShort;
	}

	public String getCommitTime() {
		return commitTime;
	}

	public void setCommitTime(String commitTime) {
		this.commitTime = commitTime;
	}
}
