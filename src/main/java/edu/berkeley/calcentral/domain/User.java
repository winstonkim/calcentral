package edu.berkeley.calcentral.domain;

import com.google.common.collect.Lists;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

/**
 * A CalCentral user, containing basic information that we can save in our local database.
 */
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class User implements UserDetails {

	private String uid;

	private String preferredName;

	private String link;

	private Timestamp firstLogin;

	private List<SimpleGrantedAuthority> authorities = Lists.newArrayList();

	public User() {
		//Only one role for now to make @PreAuth simple and easy.
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
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

	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public String getPassword() {
		return null;
	}

	public String getUsername() {
		return uid;
	}

	public boolean isAccountNonExpired() {
		return true;
	}

	public boolean isAccountNonLocked() {
		return true;
	}

	public boolean isCredentialsNonExpired() {
		return true;
	}

	public boolean isEnabled() {
		return true;
	}

	@Override
	public String toString() {
		return "User{" +
				"uid='" + uid + '\'' +
				", preferredName='" + preferredName + '\'' +
				", link='" + link + '\'' +
				", firstLogin=" + firstLogin +
				'}';
	}

}
