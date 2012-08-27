package edu.berkeley.calcentral.domain;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A simple CalCentral user, containing basic information.  
 */
@XmlRootElement
public class CalCentralUser {

    /** Wonderful identifier from CAS. */
	private String uid;

	/** OPTIONAL: basic last name i'm assuming we might want. **/
	private String lastName;

	/** OPTIONAL: basic first name i'm assuming we might want. **/
	private String firstName;

	/** Need at least one boolean to enable disable users **/ 
	private boolean activeFlag;

	public CalCentralUser() {}
	
	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the activeFlag
	 */
	public boolean getActiveFlag() {
		return activeFlag;
	}

	/**
	 * @param activeFlag the activeFlag to set
	 */
	public final void setActiveFlag(boolean activeFlag) {
		this.activeFlag = activeFlag;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
}
