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
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import de.sfk.spicycurry.Globals;

/**
 * feature issue object
 * @author boris.schneider
 *
 */
@Entity
@DiscriminatorValue(value="Feature")
public class JiraIssueFeature extends JiraIssue {

	@Transient
	private static final long serialVersionUID = 4L;
	
	/**
	 * fields
	 */
	
	@Column(nullable=true, length = 1024)
	private String type;
	
	@Column(nullable=true)
	private Calendar SOP1EU;
	
	@Column(nullable=true)
	private Calendar SOP1EUBaseline;
	
	@Column(nullable=true, length = 1024)
	private String sourceModule;
	
	@Column(nullable=true, length = 1024)
	private String workgroup;
	
	@Column(nullable=true, length = 1024)
	private String marketType;
	
	@Column(nullable=true, length = 1024)
	private String completeness;
	
	@Column(nullable=true, length = 1024)
	private String schedulingStatus;

	@Column(nullable=true, length = 1024)
	private String schedulingComments;
	
	@Column(nullable=true, length = 1014)
	private String scheduleType;

	@Column(nullable=true)
	private Calendar suggestedDate;
	
	// a jira issue belongs to one feature
	@OneToOne(fetch = FetchType.LAZY,cascade={CascadeType.MERGE})
	@JoinColumn(name = "feature_id", nullable=true)
	private Feature feature = null;
	
	@Column(nullable=true, length = 1024)
	private String featureId = null;
	
	@ElementCollection
	private List<String> markets = new ArrayList<String>();
	
	@ElementCollection
	private List<String> brands = new ArrayList<String>();

	
	/**
	 * ctor
	 * @param persistor
	 */
	public JiraIssueFeature() {
		super(JiraIssueStore.db.getPersistor());
	}
	public JiraIssueFeature(String Id) {
		super(JiraIssueStore.db.getPersistor());
		setId(Id);
	}
	public JiraIssueFeature(String Id, IPersistor persistor) {
		super(persistor);
		setId(Id);
	}
	
	protected JiraIssueFeature(IPersistor persistor) {
		super(persistor);
	}

	/**
	 * add a brand
	 * @param brand
	 */
	public void addBrand(String brand) {
		if (!this.brands.contains(brand)) {this.brands.add(brand);setChanged(true);}
	}
	
	/**
	 * add a market
	 * @param market
	 */
	public void addMarket(String market) {
		if (!this.markets.contains(market)) {this.markets.add(market);setChanged(true);}
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
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
		setChanged(true);
	}
	/**
	 * @return the sOP1EU
	 */
	public Calendar getSOP1EU() {
		return SOP1EU;
	}
	/**
	 * @param sOP1EU the sOP1EU to set
	 */
	public void setSOP1EU(Calendar sOP1EU) {
		SOP1EU = sOP1EU;
		setChanged(true);
	}
	/**
	 * @return the sOP1EUBaseline
	 */
	public Calendar getSOP1EUBaseline() {
		return SOP1EUBaseline;
	}
	/**
	 * @param sOP1EUBaseline the sOP1EUBaseline to set
	 */
	public void setSOP1EUBaseline(Calendar sOP1EUBaseline) {
		SOP1EUBaseline = sOP1EUBaseline;
		setChanged(true);
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
	 * @return the workgroup
	 */
	public String getWorkgroup() {
		return workgroup;
	}
	/**
	 * @param workgroup the workgroup to set
	 */
	public void setWorkgroup(String workgroup) {
		this.workgroup = workgroup;
		setChanged(true);
	}
	/**
	 * @return the marketType
	 */
	public String getMarketType() {
		return marketType;
	}
	/**
	 * @param marketType the marketType to set
	 */
	public void setMarketType(String marketType) {
		this.marketType = marketType;
		setChanged(true);
	}
	/**
	 * @return the completeness
	 */
	public String getCompleteness() {
		return completeness;
	}
	/**
	 * @param completeness the completeness to set
	 */
	public void setCompleteness(String completeness) {
		this.completeness = completeness;
		setChanged(true);
	}
	/**
	 * @return the schedulingStatus
	 */
	public String getSchedulingStatus() {
		return schedulingStatus;
	}
	/**
	 * @param schedulingStatus the schedulingStatus to set
	 */
	public void setSchedulingStatus(String schedulingStatus) {
		this.schedulingStatus = schedulingStatus;
		setChanged(true);
	}
	/**
	 * @return the schedulingComments
	 */
	public String getSchedulingComments() {
		return schedulingComments;
	}
	/**
	 * @param schedulingComments the schedulingComments to set
	 */
	public void setSchedulingComments(String schedulingComments) {
		this.schedulingComments = schedulingComments;
		setChanged(true);
	}
	/**
	 * @return the feature
	 */
	public Feature getFeature() {
		if (feature == null && featureId != null) 
			feature = FeatureStore.db.getById(featureId);
		return feature;
	}
	/**
	 * @param feature the feature to set
	 */
	public void setFeature(Feature feature) {
		this.feature = feature;
		setFeatureId(feature.getId());
		setChanged(true);
	}
	/**
	 * returns the id of the linked feature
	 * @return id or null
	 */
	public String getFeatureId() {
		if (this.getFeature()!= null) return this.getFeature().getId();
		else if(this.featureId != null) return this.featureId;
		else return null;
	}
	public void setFeatureId(String id) {
		if (id==null) return;
		this.featureId = id;
		if (FeatureStore.db.has(id)) feature = FeatureStore.db.getById(id);
		else feature = null;
		setChanged(true);
	}
	/**
	 * type of schedule in issue
	 * @return
	 */
	public String getScheduleType() {
		return this.scheduleType;
		
	}
	/**
	 * set the type of schedule
	 * @param type
	 */
	public void setScheduleType(String type) {
		this.scheduleType=type;
		setChanged(true);
		
	}
	/**
	 * set the suggested Date
	 * @param date
	 */
	public void setSuggestedDate(Calendar date) {
		this.suggestedDate=date;
		setChanged(true);
	}
	public Calendar getSuggestedDate(){
		return this.suggestedDate;
	}
	/**
	 * set the markets to one market
	 * @param aValue
	 */
	public void setMarket(String aValue) {
		markets.clear();
		markets.add(aValue);
		setChanged(true);
		
	}
	/**
	 * set the markets to one market
	 * @param aValue
	 */
	public void setBrand(String aValue) {
		brands.clear();
		brands.add(aValue);
		setChanged(true);
		
	}
}
