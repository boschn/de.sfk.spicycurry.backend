/**
 * 
 */
package de.sfk.spicycurry.data;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

/**
 * Jira Comments
 * @author boris.schneider
 *
 */
@Embeddable
public class JiraIssueComment {

	@Transient
	private static final long serialVersionUID = 1L;
	
	@Column(name="comment_username", length = 1024)
	private String username;
	@Column(name="comment_user", columnDefinition ="CLOB")
	private String description;
	@Column(name="comment_createdOn")
	private Calendar createdOn;
	
	/**
	 * ctor
	 */
	public JiraIssueComment(){
		super();
	}
	
	/**
	 * ctor
	 * @param name
	 * @param comment
	 * @param createdDate
	 */
	public JiraIssueComment(String name, String comment, Date createdDate) {
		super();
		this.username = name;
		this.description = comment;
		Calendar aDate = new GregorianCalendar();
		aDate.setTime(createdDate);
		this.createdOn = aDate;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return username;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.username = user;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the createdOn
	 */
	public Calendar getCreatedOn() {
		return createdOn;
	}

	/**
	 * @param createdOn the createdOn to set
	 */
	public void setCreatedOn(Calendar createdOn) {
		this.createdOn = createdOn;
	}

	
	
}
