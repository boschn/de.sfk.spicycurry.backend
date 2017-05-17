/**
 * 
 */
package de.sfk.spicycurry.data;

import java.util.*;
import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.*;

import de.sfk.spicycurry.Globals;
/**
 * @author boris.schneider
 *
 */

@Entity(name="Feature")
@Inheritance(strategy=InheritanceType.JOINED)
public class Feature extends Bean implements Serializable, Visitable {

	@Transient
	private static final long serialVersionUID = 4L;
	
	@Id
	@Column(name="feature_id", length=1024)
	private String id = null;
	
	@Column(nullable=true, length = 1024)
	private String jiraLink = null;
	@Column(nullable=true, length = 1024)
	private String title = null;
	@Column(nullable=true, length = 1024)
	private String category = null;
	@Column(nullable=false)
	private boolean accepted; // true if the customer accepted responsibility
	@Column(nullable=false)
	private boolean responsible; // true if responsible
	@Column(nullable=true, length = 1024)
	private String status;
	@Column(nullable=true, columnDefinition ="CLOB")
	private String description;
	
	private Calendar createdOn;
	private Calendar updatedOn;
	
	@OneToOne(cascade={CascadeType.PERSIST})
	@JoinColumn(name = "requirement_id")
	private Requirement requirement = null;
	
	@OneToMany(fetch = FetchType.LAZY,cascade={CascadeType.REFRESH})
	@JoinColumn(name = "feature_id", nullable=true)
	private List<JiraIssueFeature> jiraissues = new ArrayList<JiraIssueFeature>();
	
	@Version
	private Timestamp lastUpdate;
	
	/**
	 * constructor
	 */
	public Feature() {
		super(Globals.Persistor);
	}
	public Feature(String id) {
		super(Globals.Persistor);
		this.id = id;
	}
	public Feature(Requirement requirement) {
		super(Globals.Persistor);
		this.id = requirement.getCustomerRequirementId(); // KA-WI-ID is the requirement
		this.setRequirement	(requirement);
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
	private void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the requirement
	 */
	public Requirement getRequirement() {
		return requirement;
	}
	/**
	 * @param requirement the requirement to set
	 */
	private void setRequirement(Requirement requirement) {
		this.requirement = requirement;
		convertFromRequirement(requirement);
		requirement.addFeature(this);
	}
	/**
	 * set the internal variables
	 * @param requirement
	 */
	private void convertFromRequirement(Requirement requirement) {
		// take over the nulled values
		if (category == null) 
			this.setCategory(requirement.getCategory());
		if (status == null) 
			this.setStatus(requirement.getStatus()); 
		if (title == null) 
			this.setTitle(requirement.getTitle()); 
		if (description == null) 
			this.setDescription(requirement.getDescription()); 
		if (createdOn == null) 
			this.setCreatedOn(requirement.getCreatedOn()); 
		if (updatedOn == null) 
			this.setUpdatedOn(requirement.getUpdatedOn()); 
		
	}
	/**
	 * add sub requirement and add the feature to it
	 * @param req
	 */
	public void addSubRequirement(Requirement req) {
		requirement.addRequirement(req);
		req.addFeature(this);
	}
	/**
	 * @return Collection of Sub Requirements 
	 */
	public Collection<Requirement> getSubRequirements() {
		return requirement.getSubRequirements();
	}
	/**
	 * @return Collection of Sub Requirements 
	 */
	public Collection<Requirement> loadSubRequirements() {
		return requirement.loadSubRequirements();
	}
	/**
	 * visitor-pattern visit by accept 
	 */
	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
	
	/**
	 * return true if feature
	 * @return
	 */
	@Basic
	public boolean isFeature() {
		return true;
	}
	/**
	 * @return the jiraLink
	 */
	public String getJiraLink() {
		return jiraLink;
	}
	/**
	 * @param jiraLink the jiraLink to set
	 */
	public void setJiraLink(String jiraLink) {
		this.jiraLink = jiraLink;
		setChanged(true);
	}
	/**
	 * @return polarion uri of the requirement
	 */
	public String getPolarionUri() {
		return requirement.getPolarionUri();
	}
	/**
	 * @return the hash code
	 */
	@Override
	public int hashCode()
	{
		return this.id.hashCode();
	}
	/**
	 * override equals if 
	 * @param req
	 * @return
	 */
	public boolean equals(Requirement req)
	{
		return req.getId().equalsIgnoreCase(this.getId());
	}
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof Requirement || o.getClass().isAssignableFrom(Requirement.class)) 
			return this.equals((Requirement) o);
		
		return false;
	}
	/**
	 * @return the sourceModule
	 */
	public String getSourceModule() {
		return requirement.getSourceModule();
	}
	/**
	 * @param sourceModule the sourceModule to set
	 */
	public void setSourceModule(String sourceModule) {
		this.requirement.setSourceModule(sourceModule);
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
		setChanged(true);
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param text the description to set
	 */
	public void setDescription(String text) {
		this.description = text;
		setChanged(true);
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
		setChanged(true);
	}
	/**
	 * @return the accepted
	 */
	public boolean isAccepted() {
		return accepted;
	}
	/**
	 * @param accepted the accepted to set
	 */
	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
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
		
	/**
	 * @return the responsible
	 */
	public boolean isResponsible() {
		return responsible;
	}
	/**
	 * @param responsible the responsible to set
	 */
	public void setResponsible(boolean responsible) {
		this.responsible = responsible;
		setChanged(true);
	}
	
	/**
	 * @return the customerreqid
	 */
	public String getCustomerRequirementId() {
		return requirement.getCustomerRequirementId();
	}
	/**
	 * @param customerreqid the customerreqid to set
	 */
	public void setCustomerRequirementId(String customerreqid) {
		this.requirement.setCustomerRequirementId(customerreqid);
		setChanged(true);
	}
	
	/**
	 * @param uri the uri to set
	 */
	public void setPolarionUri(String uri) {
		this.requirement.setPolarionUri(uri);
		setChanged(true);
	}
	
	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}
	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
		setChanged(true);
	}
	/**
	 * @return the sourceID
	 */
	public String getSourceID() {
		return this.requirement.getSourceID();
	}
	/**
	 * @param sourceID the sourceID to set
	 */
	public void setSourceID(String sourceID) {
		this.requirement.setSourceID(sourceID);
	}
	/**
	 * @return the customerstatus
	 */
	public String getCustomerStatus() {
		return this.requirement.getCustomerStatus();
	}
	/**
	 * @param customerstatus the customerstatus to set
	 */
	public void setCustomerStatus(String customerstatus) {
		this.requirement.setCustomerStatus(customerstatus);
	}
	/**
	 * @return the customerType
	 */
	public String getCustomerType() {
		return this.requirement.getCustomerType();
	}
	/**
	 * @param customerType the customerType to set
	 */
	public void setCustomerType(String customerType) {
		this.requirement.setCustomerType(customerType);
	}
	/**
	 * @return the customerReqType
	 */
	public String getCustomerReqType() {
		return requirement.getCustomerReqType();
	}
	/**
	 * @param customerReqType the customerReqType to set
	 */
	public void setCustomerReqType(String customerReqType) {
		this.requirement.setCustomerReqType(customerReqType);
	}
	
	/**
	 * @return the customerUpdatedOn
	 */
	public Calendar getCustomerUpdatedOn() {
		return this.requirement.getCustomerUpdatedOn();
	}
	/**
	 * @param customerUpdatedOn the customerUpdatedOn to set
	 */
	public void setCustomerUpdatedOn(Calendar customerUpdatedOn) {
		this.requirement.setCustomerUpdatedOn(customerUpdatedOn);
	}
	/**
	 * @return the customerRequirementTitle
	 */
	public String getCustomerRequirementTitle() {
		return requirement.getCustomerRequirementTitle();
	}
	/**
	 * @param customerRequirementTitle the customerRequirementTitle to set
	 */
	public void setCustomerRequirementTitle(String customerRequirementTitle) {
		this.requirement.setCustomerRequirementTitle (customerRequirementTitle);
	}
	
	/*
	 * the string representation
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		return this.getClass()+ " [" + this.getId() + "," + this.getTitle() + "]"; 
	}
	
	
}
