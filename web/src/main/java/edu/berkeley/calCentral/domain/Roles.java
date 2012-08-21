package edu.berkeley.calcentral.domain;

public class Roles {
    private String role;
    
    private String description;
    
    private boolean isActive;

    /**
     * @return the role
     */
    public final String getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public final void setRole(String role) {
        this.role = role;
    }

    /**
     * @return the description
     */
    public final String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public final void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the isActive
     */
    public final boolean isActive() {
        return isActive;
    }

    /**
     * @param isActive the isActive to set
     */
    public final void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}
