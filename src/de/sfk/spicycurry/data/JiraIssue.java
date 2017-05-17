/**
 * 
 */
package de.sfk.spicycurry.data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import de.sfk.spicycurry.Globals;
import net.rcarz.jiraclient.Status;

/**
 * abstract Issue object
 *  
 * @author boris.schneider
 *
 */
@Entity
@Table(name="JiraIssues")
@DiscriminatorColumn(name="JiraIssueType")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class JiraIssue extends Bean {
	@Transient
	private static final long serialVersionUID = 1L;

	/**
	 * fields
	 */
	@Id
	@Column(name="jiraissue_id", nullable=false, length = 1024)
	private String id;
	
	@Column(nullable=true)
	private Calendar DueDate;

	@Column(nullable=true, columnDefinition ="CLOB")
	private String description;
	
	@Column(nullable=true, columnDefinition ="CLOB")
	private String summary;
	
	@Column(nullable=true, length = 1024)
	private String status;
	
	@Column(nullable=true, length = 2048)
	private String url;
	
	@Column(nullable=true, length = 1024)
	private String assignee;
	
	@ElementCollection
	private List<String> labels = new ArrayList<String>();
	
	@Column(nullable=true, length = 1024)
	private String parentIssueId;
	
	@ElementCollection
	private List<String> subIssueIds = new ArrayList<String>();
	
	private Calendar createdOn;
	private Calendar updatedOn;
	
	@Version
	private Timestamp lastUpdate;

	
	
	/**
	 * ctor
	 * @param persistor
	 */
	public JiraIssue() {
		super(Globals.Persistor);
	}
	public JiraIssue(String Id) {
		super(Globals.Persistor);
		setId(Id);
	}
	public JiraIssue(String Id, IPersistor persistor) {
		super(persistor);
		setId(Id);
	}
	protected JiraIssue(IPersistor persistor) {
		super(persistor);
		
	}
	/**
	 * add a label
	 * @param label
	 */
	public void addLabel(String label) {
		if (!this.labels.contains(label)) {this.labels.add(label);setChanged(true);}
	}
	/**
	 * @return the labels
	 */
	public List<String> getLabels() {
		return labels;
	}
	/**
	 * @param the labels to set
	 */
	public void setLabels(List<String> labels) {
		this.labels = labels;
		setChanged(true);
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	protected void setId(String id) {
		this.id = id;
		setChanged(true);
	}
	/**
	 * @return the dueDate
	 */
	public Calendar getDueDate() {
		return DueDate;
	}
	/**
	 * @param dueDate the dueDate to set
	 */
	public void setDueDate(Calendar dueDate) {
		DueDate = dueDate;
		setChanged(true);
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
		setChanged(true);
	}
	/**
	 * @return the summary
	 */
	public String getSummary() {
		return summary;
	}
	/**
	 * @param summary the summary to set
	 */
	public void setSummary(String summary) {
		this.summary = summary;
		setChanged(true);
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
		setChanged(true);
	}
	/**
	 * @return the updatedOn
	 */
	public Calendar getUpdatedOn() {
		return updatedOn;
	}
	/**
	 * @param updatedOn the updatedOn to set
	 */
	public void setUpdatedOn(Calendar updatedOn) {
		this.updatedOn = updatedOn;
		setChanged(true);
	}
	public String getStatus() {
		return this.status;
	}
	public void setStatus(String status) {

		this.status=status;
		setChanged(true);
		
	}
	public String geturl() {
		return this.url;
	}
	public void setUrl(String url) {
		this.url=url;
		setChanged(true);
		
	}
	public String getAssignee(){
		return this.assignee;
	}
	public void setAssignee(String name) {
		this.assignee=name;
		setChanged(true);
		
	}
	/**
	 * @return the parentIssueId
	 */
	public String getParentIssueId() {
		return parentIssueId;
	}
	/**
	 * @param parentIssueId the parentIssueId to set
	 */
	public void setParentIssueId(String parentIssueId) {
		this.parentIssueId = parentIssueId;
	}
	/**
	 * @return the subIssueIds
	 */
	public List<String> getSubIssueIds() {
		return subIssueIds;
	}
	/**
	 * @param subIssueIds the subIssueIds to set
	 */
	public void setSubIssueIds(List<String> subIssueIds) {
		this.subIssueIds = subIssueIds;
	}
	public void addSubIssueId(String key) {
		if (!this.subIssueIds.contains(key)) {this.subIssueIds.add(key);setChanged(true);}
		
	}
}
