/**
 * requirement
 */
package de.sfk.spicycurry.data;

import java.util.*;
import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.persistence.*;
import javax.persistence.CollectionTable;

import org.eclipse.persistence.annotations.*;

import de.sfk.spicycurry.Globals;

/**
 * requirement is an structured object to describe a feature
 * 
 * @author boris.schneider
 *
 */


@Entity(name="Requirement")
@Inheritance(strategy=InheritanceType.JOINED)
public class Requirement extends Bean implements Visitable ,Serializable{

	@Transient
	private static final long serialVersionUID = 1L;
	
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
	private String customerStatus;
	
	@Column(nullable=true, length = 1024)
	private String customerType;
	
	@Column(nullable=true)
	private Calendar customerUpdatedOn;
	
	@Column(nullable=true, length = 1024)
	private String updateType;
	
	@Column(nullable=true, length = 1024)
	private String customerReqType;
	
	@Column(nullable=true, length = 1024)
	private String customerRequirementTitle;
	
	@Column(nullable=false, length = 5120)
	private String uri;
	
	@Column(nullable=true, length = 1024)
	private String category;
	
	@Column(nullable=true, length = 1024)
	private String status;
	
	@Column(nullable=false)
	private boolean tracking; // true if the requirement is a tracking requirement / linked to a feature
	
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
	@CollectionTable(name ="Attachments", joinColumns = {@JoinColumn(name = "requirement_id")})
	private List<Attachment> attachments = new ArrayList<Attachment>();
	
	@ElementCollection
	private List<String> attachmentIds = new ArrayList<String>();
	
	@ElementCollection
	private List<String> hyperlinks = new ArrayList<String>();
	
	@ElementCollection
	private List<String> markets = new ArrayList<String>();
	@ElementCollection
	private List<String> brands = new ArrayList<String>();
	@ElementCollection
	private List<String> customerMarkets = new ArrayList<String>();
	@ElementCollection
	private List<String> customerBrands = new ArrayList<String>();
	
	@Transient
	// @ManyToMany (cascade= CascadeType.MERGE)
	// @JoinColumn(name = "requirement_id", nullable=true)
	private HashMap<String, Requirement> subRequirements = null;
	
	@ElementCollection
	private List<String> linkedDerivedPolarionURIs = new ArrayList<String>();
	@ElementCollection
	private List<String> linkedPolarionURIs = new ArrayList<String>();
	
	@OneToMany (fetch = FetchType.LAZY,cascade= CascadeType.MERGE)
	@JoinColumn(name = "feature_id", nullable=true)
	private Set<Feature> features = new HashSet<Feature>();
	
	@ElementCollection
	private List<String> featureIds = new ArrayList<String>();
	
	/**
	 * constructor
	 */
	public Requirement() {
		super(Globals.Persistor);
	}
	/**
	 * constructor
	 * @param id
	 */
	public Requirement(String id)
	{
		super(Globals.Persistor);
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
	public List<String> getBrands() {
		return brands;
	}
	/**
	 * @param brands the brands to set
	 */
	public void setBrands(List<String> brands) {
		this.brands = brands;
		setChanged(true);
	}
	/**
	 * @return Collection of Requirements - lazy load from Requirementsstore
	 */
	public Collection<Requirement> getSubRequirements() {
		if (subRequirements == null) RequirementStore.db.getSubRequirements(this);
		return subRequirements.values();
	}
	/**
	 * @return Collection of Requirements - lazy load from Requirementsstore
	 */
	public Collection<Requirement> loadSubRequirements() {
		if (subRequirements == null){ 
			RequirementStore.db.loadPolarionSubRequirements(this, null);
		}
		return subRequirements.values();
	}
	/**
	 * @return Collection of Requirements
	 */
	public void clearSubRequirements() {
		
		if (this.subRequirements == null)
				 this.subRequirements= new HashMap<String, Requirement>();
		else this.subRequirements.clear();
	}
	/**
	 * @param descendants the descendants to set
	 */
	@SuppressWarnings("unused")
	private void setRequirements(HashMap<String, Requirement> descendants) {
		this.subRequirements = descendants;
	}
	/**
	 * add sub requirement
	 * @param req
	 */
	public void addRequirement(Requirement req) {
		// Initialize
		if (this.subRequirements == null) this.clearSubRequirements();
		
		if (!this.subRequirements.containsKey(req.getId())) {
			// add the child
			this.subRequirements.put(req.getId(), req);
			// add the top level features from me to the child
			for (Feature aFeature: this.getFeatures()) {
				req.addFeature(aFeature);
				setChanged(true);
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
	 * add upwards polarion link uri
	 * @param req
	 */
	public void addLinkedPolarionURI(String id) {
				
		if (!this.linkedPolarionURIs.contains(id)) {
			// add the child
			this.linkedPolarionURIs.add(id);
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
		if (this.featureIds == null) { this.featureIds = new ArrayList<String>();setChanged(true);}
		if (!this.featureIds.contains(feature.getId())) this.featureIds.add(feature.getId());
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
	 * add a hyperlink
	 * @param req
	 */
	public void addHyperlink(String id) {
				
		if (!this.hyperlinks.contains(id)) {
			// add the child
			this.hyperlinks.add(id);
			setChanged(true);
		}
	}
	/**
	 * add a brand
	 * @param brand
	 */
	public void addBrand(String brand) {
		if (!this.brands.contains(brand)) {this.brands.add(brand);setChanged(true);}
	}
	/**
	 * add a customer brand
	 * @param market
	 */
	public void addCustomerBrand(String brand) {
		if (!this.customerBrands.contains(brand)) {this.customerBrands.add(brand);setChanged(true);}
	}
	/**
	 * add a market
	 * @param market
	 */
	public void addMarket(String market) {
		if (!this.markets.contains(market)) {this.markets.add(market);setChanged(true);}
	}
	/**
	 * add a customer market
	 * @param market
	 */
	public void addCustomerMarket(String market) {
		if (!this.customerMarkets.contains(market)) {this.customerMarkets.add(market);setChanged(true);}
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
	 * @return the customerstatus
	 */
	public String getCustomerStatus() {
		return customerStatus;
	}
	/**
	 * @param customerstatus the customerstatus to set
	 */
	public void setCustomerStatus(String customerstatus) {
		this.customerStatus = customerstatus;
		setChanged(true);
	}
	/**
	 * @return the customerType
	 */
	public String getCustomerType() {
		return customerType;
	}
	/**
	 * @param customerType the customerType to set
	 */
	public void setCustomerType(String customerType) {
		this.customerType = customerType;
		setChanged(true);
	}
	/**
	 * @return the customerReqType
	 */
	public String getCustomerReqType() {
		return customerReqType;
	}
	/**
	 * @param customerReqType the customerReqType to set
	 */
	public void setCustomerReqType(String customerReqType) {
		this.customerReqType = customerReqType;
		setChanged(true);
	}
	/**
	 * @return the updateType
	 */
	public String getUpdateType() {
		return updateType;
	}
	/**
	 * @param updateType the updateType to set
	 */
	public void setUpdateType(String updateType) {
		this.updateType = updateType;
		setChanged(true);
	}
	/**
	 * @return the outline
	 */
	public boolean isOutline() {
		if (this.customerReqType != null && this.customerReqType.equalsIgnoreCase("heading")) return true;
		return false;
	}
	/**
	 * @return the outline
	 */
	public boolean isInformation() {
		if (this.customerReqType != null && this.customerReqType.equalsIgnoreCase("information")) return true;
		return false;
	}
	
	/**
	 * @return the customerUpdatedOn
	 */
	public Calendar getCustomerUpdatedOn() {
		return customerUpdatedOn;
	}
	/**
	 * @param customerUpdatedOn the customerUpdatedOn to set
	 */
	public void setCustomerUpdatedOn(Calendar customerUpdatedOn) {
		this.customerUpdatedOn = customerUpdatedOn;
		setChanged(true);
	}
	/**
	 * @return the customerRequirementTitle
	 */
	public String getCustomerRequirementTitle() {
		return customerRequirementTitle;
	}
	/**
	 * @param customerRequirementTitle the customerRequirementTitle to set
	 */
	public void setCustomerRequirementTitle(String customerRequirementTitle) {
		this.customerRequirementTitle = customerRequirementTitle;
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
