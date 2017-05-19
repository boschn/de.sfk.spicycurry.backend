/**
 * 
 */
package de.sfk.spicycurry.data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import de.sfk.spicycurry.Globals;

/**
 * persistable specification object
 * 
 * @author boris.schneider
 *
 */
@Entity(name="Specification")
@Inheritance(strategy=InheritanceType.JOINED)
public class Specification extends Bean implements Visitable ,Serializable{

	@Transient
	private static final long serialVersionUID = 2L;
	
	/**
	 * fields
	 */
	@Id
	@Column(name="requirement_id", nullable=false, length = 1024)
	private String id;
	
	@Column(nullable=true, length = 1024)
	private String sourceModule;
	
	@Column(nullable=true, length = 1024)
	private String sourceID;
	
	@Column(nullable=true, length = 1024)
	private String title;
	
	@Column(nullable=true, columnDefinition ="CLOB")
	private String description;
	
	@Column(nullable=true, length = 1024)
	private String customerReqId;
	
	@Column(nullable=true, length = 1024)
	private String specificationType;
	
	@Column(nullable=true, length = 1024)
	private String functionality;
	
	@Column(nullable=false, length = 5120)
	private String uri;
	
	@Column(nullable=true, length = 1024)
	private String category;
	
	@Column(nullable=true, length = 1024)
	private String testability;
	
	@Column(nullable=true, length = 1024)
	private String testAssignee;
	
	@Column(nullable=true, length = 1024)
	private String testWorkgroup;
	
	@Column(nullable=true, length = 1024)
	private String testFeasibility;
	
	
	@Column(nullable=true, length = 1024)
	private String status;
	
	@OneToOne(cascade={CascadeType.MERGE})
	@JoinColumn(name = "feature_id", nullable=true)
	private Feature feature = null;
	
	@Column(nullable=false)
	private boolean accepted; // true if the customer accepted responsibility
	
	@Column(nullable=false)
	private boolean responsible; // true if responsible
		
	private Calendar createdOn;
	private Calendar updatedOn;
	
	@Column(nullable=true, length = 1024)
	private String author;
	
	@Column(nullable=true, length = 1024)
	private String assignee;
	
	@Version
	private Timestamp lastUpdate;
	
	@ElementCollection
	@CollectionTable(name ="Attachments", joinColumns = {@JoinColumn(name = "id")})
	private List<Attachment> attachments = new ArrayList<Attachment>();
	
	@ElementCollection 
		private List<String> attachmentIds = new ArrayList<String>();
	
	
	@ElementCollection
	private List<String> markets = new ArrayList<String>();
	@ElementCollection
	private List<String> brands = new ArrayList<String>();
	@ElementCollection
	private List<String> supplier = new ArrayList<String>();
	@ElementCollection
	private List<String> projectnames = new ArrayList<String>();
	
	@Transient
	// @ManyToMany (cascade= CascadeType.MERGE)
	// @JoinColumn(name = "requirement_id", nullable=true)
	private HashMap<String, Specification> subSpecifications = null;
	
	@ElementCollection
	private List<String> linkedDerivedPolarionURIs = new ArrayList<String>();
	@ElementCollection
	private List<String> linkedPolarionURIs = new ArrayList<String>();
	
	@OneToMany (fetch = FetchType.LAZY,cascade= CascadeType.MERGE)
	@JoinColumn(name = "feature_id", nullable=true)
	private Set<Feature> features = new HashSet<Feature>();
	
	@ElementCollection
	private List<String> featureIds = new ArrayList<String>();
	
	@ElementCollection
	private List<String> hyperLinks = new ArrayList<String>();
	/**
	 * constructor
	 */
	public Specification() {
		super(SpecificationStore.db);
	}
	/**
	 * constructor
	 * @param id
	 */
	public Specification(String id)
	{
		super(SpecificationStore.db);
		this.setId(id);
		
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
		setChanged(true);
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
		return sourceModule;
	}
	/**
	 * @param sourceModule the sourceModule to set
	 */
	public void setSourceModule(String sourceModule) {
		this.sourceModule = sourceModule;
		setChanged(true);
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
	 * @return the testWorkgroup
	 */
	public String getTestWorkgroup() {
		return testWorkgroup;
	}
	/**
	 * @return the testFeasibility
	 */
	public String getTestFeasibility() {
		return testFeasibility;
	}
	/**
	 * @param testFeasibility the testFeasibility to set
	 */
	public void setTestFeasibility(String testFeasibility) {
		this.testFeasibility = testFeasibility;
		setChanged(true);
	}
	/**
	 * @param testWorkgroup the testWorkgroup to set
	 */
	public void setTestWorkgroup(String testWorkgroup) {
		this.testWorkgroup = testWorkgroup;
		setChanged(true);
	}
	/**
	 * @return the testAssignee
	 */
	public String getTestAssignee() {
		return testAssignee;
	}
	/**
	 * @param testAssignee the testAssignee to set
	 */
	public void setTestAssignee(String testAssignee) {
		this.testAssignee = testAssignee;
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
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}
	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
		setChanged(true);
	}
	/**
	 * @return the markets
	 */
	public List<String> getMarkets() {
		return markets;
	}
	/**
	 * @param markets the markets to set
	 */
	public void setMarkets(List<String> markets) {
		this.markets = markets;
		setChanged(true);
	}
	/**
	 * @return the brands
	 */
	public List<String> getSuppliers() {
		return supplier;
	}
	/**
	 * @return the testability
	 */
	public String getTestability() {
		return testability;
	}
	/**
	 * @param testability the testability to set
	 */
	public void setTestability(String testability) {
		this.testability = testability;
		setChanged(true);
	}
	/**
	 * @return the brands
	 */
	public List<String> getBrands() {
		return brands;
	}
	/**
	 * @return the the list of projectnames
	 */
	public List<String> getProjectnames() {
		return projectnames;
	}
	/**
	 * @param brands the brands to set
	 */
	public void setBrands(List<String> brands) {
		this.brands = brands;
		setChanged(true);
	}
	/**
	 * @return Collection of Requirements - lazy load from SpecificationStore
	 */
	public Collection<Specification> getSubSpecifications() {
		if (subSpecifications == null) SpecificationStore.db.getSubSpecifications(this);
		return subSpecifications.values();
	}
	/**
	 * @return Collection of Requirements - lazy load from SpecificationStore
	 */
	public Collection<Specification> loadSubSpecifications() {
		if (subSpecifications == null){ 
			SpecificationStore.db.loadPolarionSubSpecifications(this, null);
		}
		return subSpecifications.values();
	}
	/**
	 * @return Collection of Requirements
	 */
	public void clearSubSpecifications() {
		
		if (this.subSpecifications == null)
				 this.subSpecifications= new HashMap<String, Specification>();
		else this.subSpecifications.clear();
	}
	/**
	 * @param descendants the descendants to set
	 */
	@SuppressWarnings("unused")
	private void setSpecifications(HashMap<String, Specification> descendants) {
		this.subSpecifications = descendants;
	}
	/**
	 * add sub specification
	 * @param specification
	 */
	public void addSpecification(Specification specification) {
		// Initialize
		if (this.subSpecifications == null) this.clearSubSpecifications();
		
		if (!this.subSpecifications.containsKey(specification.getId())) {
			// add the child
			this.subSpecifications.put(specification.getId(), specification);
			setChanged(true);
			// add the top level features from me to the child
			for (Feature aFeature: this.getFeatures()) {
				specification.addFeature(aFeature);
			}
		}
	}
	/**
	 * returns the derived polarion URIs
	 * @return String[]
	 */
	public String[] getDerivedPolarionURIs(){
		if (linkedDerivedPolarionURIs != null) return  linkedDerivedPolarionURIs.toArray(new String[linkedDerivedPolarionURIs.size()]);
		return (new String[0]);
	}
	/**
	 * returns an array of string ids
	 * @return String[]
	 */
	public String[] getLinkedUpPolarionURIs(){
		if (linkedPolarionURIs != null)  return linkedPolarionURIs.toArray(new String[linkedPolarionURIs.size()]);
		return (new String[0]);
	}
	/**
	 * clear sub polarion URI 
	 */
	public void clearDerivedPolarionURIs(){
		linkedDerivedPolarionURIs.clear();
		setChanged(true);
	}
	/**
	 * clear upwards polarion URI
	 */
	public void clearLinkedUpPolarionURIs(){
		linkedPolarionURIs.clear();
		setChanged(true);
	}
	/**
	 * add upwards polarion URI
	 * @param req
	 */
	public void addDerivedPolarionURI(String id) {
				
		if (!this.linkedDerivedPolarionURIs.contains(id)) {
			// add the child
			this.linkedDerivedPolarionURIs.add(id);
			setChanged(true);
		}
	}
	/**
	 * add a hyperlink
	 * @param req
	 */
	public void addHyperlink(String id) {
				
		if (!this.hyperLinks.contains(id)) {
			// add the child
			this.hyperLinks.add(id);
			setChanged(true);
		}
	}
	/**
	 * add upwards polarion link uri
	 * @param req
	 */
	public void addLinkedPolarionURI(String id) {
				
		if (!this.linkedPolarionURIs.contains(id)) {
			// add the child
			this.linkedPolarionURIs.add(id);
			setChanged(true);
		}
	}
	/**
	 * @return the features
	 */
	public Set<Feature> getFeatures() {
		if (features == null) { 
			features = new HashSet<Feature>();
			if (this.featureIds != null)
				for (String anId: this.featureIds) 
					if (FeatureStore.db.has(anId)) 
						this.addFeature(FeatureStore.db.getById(anId));
		}
		return features;
	}
	/**
	 * @param features the features to set
	 */
	@SuppressWarnings("unused")
	private final void setFeatures(Set<Feature> features) {
		this.features = features;
		setChanged(true);
	}
	/**
	 * add top level requirement
	 * @param req
	 */
	public void addFeature(Feature feature) {
		if (features == null) features = new HashSet<Feature>();
		if (!this.features.contains(feature)) this.features.add(feature);
		if (this.featureIds == null) this.featureIds = new ArrayList<String>();
		if (!this.featureIds.contains(feature.getId())) {this.featureIds.add(feature.getId());setChanged(true);}
	}
	/**
	 * add the feature id without adding the feature by itself
	 * @param id
	 */
	public void addFeatureID(String id){
		if (this.featureIds == null) this.featureIds = new ArrayList<String>();
		if (!this.featureIds.contains(id)) {this.featureIds.add(id);setChanged(true);}
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
	 * @return the assignee
	 */
	public String getAssignee() {
		return assignee;
	}
	/**
	 * @param assignee the assignee to set
	 */
	public void setAssignee(String assignee) {
		this.assignee = assignee;
		setChanged(true);
	}
	/**
	 * @return the customerreqid
	 */
	public String getCustomerRequirementId() {
		return customerReqId;
	}
	/**
	 * @param customerreqid the customerreqid to set
	 */
	public void setCustomerRequirementId(String customerreqid) {
		this.customerReqId = customerreqid;
		setChanged(true);
	}
	/**
	 * add a attachment
	 * @param attachment
	 */
	public void addAttachment(String id, String url, String filename, String uri) {
		Attachment anAttachment = new Attachment(id, url, filename, uri);
		if (attachments == null) attachments = new ArrayList<Attachment>();
		if (!this.attachments.contains(attachments)) this.attachments.add(anAttachment);
		if (this.attachmentIds == null) this.attachmentIds = new ArrayList<String>();
		if (!this.attachmentIds.contains(anAttachment.getId())) {this.attachmentIds.add(anAttachment.getId());setChanged(true);}
	}
	/**
	 * add a supplier
	 * @param supplier
	 */
	public void addSupplier(String supplier) {
		if (!this.supplier.contains(supplier)) {this.supplier.add(supplier);setChanged(true);}
	}
	
	/**
	 * add a brand
	 * @param brand
	 */
	public void addBrand(String brand) {
		if (!this.brands.contains(brand)) {this.brands.add(brand);setChanged(true);}
	}
	
	/**
	 * add a projectname
	 * @param projectname
	 */
	public void addProjectname(String projectname) {
		if (!this.projectnames.contains(projectname)) {this.projectnames.add(projectname);setChanged(true);}
	}
	/**
	 * add a market
	 * @param market
	 */
	public void addMarket(String market) {
		if (!this.markets.contains(market)) {this.markets.add(market);setChanged(true);}
	}
	
	/**
	 * @return the uri
	 */
	public String getPolarionUri() {
		return uri;
	}
	/**
	 * @param uri the uri to set
	 */
	public void setPolarionUri(String uri) {
		this.uri = uri;
		setChanged(true);
	}
	/**
	 * @return the AttachmentIds
	 */
	public List<String> getAttachmentIds() {
		return attachmentIds;
	}
	/**
	 * @param attachmentIds to set
	 */
	private void setAttachmentIds(List<String> attachmentIds) {
		this.attachmentIds = attachmentIds;
		setChanged(true);
	}
	/**
	 * @return the featureIds
	 */
	public List<String> getFeatureIds() {
		return featureIds;
	}
	/**
	 * @param featureIds the featureIds to set
	 */
	private void setFeatureIds(List<String> featureIds) {
		this.featureIds = featureIds;
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
		return sourceID;
	}
	/**
	 * @param sourceID the sourceID to set
	 */
	public void setSourceID(String sourceID) {
		this.sourceID = sourceID;
		setChanged(true);
	}
	
	/**
	 * @return the customerReqType
	 */
	public String getFunctionality() {
		return functionality;
	}
	/**
	 * @param customerReqType the customerReqType to set
	 */
	public void setFunctionality(String functionality) {
		this.functionality = functionality;
		setChanged(true);
	}
	/**
	 * @return the specificationType
	 */
	public String getSpecificationType() {
		return specificationType;
	}
	/**
	 * @param specificationType
	 */
	public void setSpecificationType(String type) {
		this.specificationType = type;
		setChanged(true);
	}
	
	/**
	 * visitor-pattern visit by accept 
	 */
	@Override
	public void accept(Visitor v) {
		v.visit(this);
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
	/**
	 * return true if feature
	 * @return
	 */
	@Basic
	public boolean isFeature() {
		return false;
	}
}

