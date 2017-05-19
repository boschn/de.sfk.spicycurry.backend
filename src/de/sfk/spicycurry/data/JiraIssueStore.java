/**
 * 
 */
package de.sfk.spicycurry.data;

import java.io.Closeable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.polarion.alm.ws.client.types.tracker.WorkItem;

import de.sfk.spicycurry.Globals;
import de.sfk.spicycurry.Setting;
import net.rcarz.jiraclient.Issue;

/**
 * @author boris.schneider
 *
 */
public class JiraIssueStore implements Closeable, IStore<JiraIssue>  {

	private static final String JIRA_FEATURE_PROJECTID = "MIBSERIELH";
	private static final String JIRA_FEATURE_TYPE = "Feature";
	private static final String JIRA_FEATURE_QUERY = "project=" + JIRA_FEATURE_PROJECTID + " AND issuetype=" + JIRA_FEATURE_TYPE ;

	public static final String PROPERTY_JIRA_FEATURE_PROJECTID = "Feature.JIRA.ProjectID";
	public static final String PROPERTY_JIRA_FEATURE_TYPE = "Feature.JIRA.IssueType";
	public static final String PROPERTY_JIRA_FEATURE_QUERY = "Feature.JIRA.Query";
	
	// singleton
	public final static JiraIssueStore db = new JiraIssueStore();

	// general issue store
	private ConcurrentHashMap<String, JiraIssue> issues = new ConcurrentHashMap<String,JiraIssue>();
	// specific store by feature-id (since this is 1:m use bucket)
	private ConcurrentHashMap<String, ArrayList<JiraIssueFeature>> featuresById = new ConcurrentHashMap<String, ArrayList<JiraIssueFeature>>(); //byURI
	// Persistor
	private IPersistor persistor = null;
	//  helpers
	private JiraIssueLoader loader = null;
	private boolean isInitialized = false;
	// Logger
	private Logger logger = LogManager.getLogger(JiraIssueStore.class);
			
	/**
	 * constructor
	 */
	protected JiraIssueStore() {
		super();
		// set the defaults
		Setting.Default.get(PROPERTY_JIRA_FEATURE_PROJECTID, JIRA_FEATURE_PROJECTID);
		Setting.Default.get(PROPERTY_JIRA_FEATURE_TYPE, JIRA_FEATURE_TYPE);
		Setting.Default.get(PROPERTY_JIRA_FEATURE_QUERY, JIRA_FEATURE_QUERY);
	}
	/**
	 * return the default work item loader
	 * @return the loader
	 */
	public JiraIssueLoader getLoader() {
		return getIssueLoader(JiraParameter.Default.getBaseUrl(), 
						 JiraParameter.Default.getUserName(),
						 JiraParameter.Default.getPassWord());
	}
	/**
	 * return the work item loader associated with the store or create one
	 * @param baseUrl
	 * @param userName
	 * @param passWord
	 * @return
	 */
	public JiraIssueLoader getIssueLoader(String baseUrl, String userName, String passWord) {
		try {
			if (loader == null) loader = JiraIssueLoader.singleton;
			if (!loader.isInitialized()){
				loader.beginSession(baseUrl,userName,passWord);
				
			}
			return loader;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.catching(e);
			return null;
		}
	}
	/**
	 * @param loader the loader to set
	 */
	@SuppressWarnings("unused")
	private void setJiraIssueLoader(JiraIssueLoader loader) {
		this.loader = loader;
	}
	/**
	 * @return the persistor
	 */
	public synchronized IPersistor getPersistor() {
		if (persistor==null) persistor = new EclipseLinkPersistor("JiraIssueStore");
		return persistor;
	}
	/**
	 * @param persistor the persistor to set
	 */
	public synchronized void setPersistor(IPersistor persistor) {
		this.persistor = persistor;
	}
	/**
	 * @return the isInitialized
	 */
	public synchronized boolean isInitialized() {
		return isInitialized;
	}
	/**
	 * @param isInitialized the isInitialized to set
	 */
	private synchronized void setInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
	}
	/**
	 * adds a jira issue as feature issue
	 * @param issue
	 * @return Feature added or null
	 */
	public JiraIssueFeature add(Issue issue, boolean force){
		if (!isInitialized()) this.open();
		
		JiraIssueFeature aFeature = (JiraIssueFeature) this.issues.get(issue.getKey());
		aFeature = (JiraIssueFeature) this.getLoader().convertToIssueFeature(issue, aFeature);
		return addFeature(aFeature, true);
	}
	
	/**
	 * add a feature to the store
	 * @param feature
	 * @return
	 */
	public JiraIssueFeature addFeature(JiraIssueFeature feature, boolean force) {
		if (!isInitialized()) this.open();
		
		if (force || !this.has(feature)) {
			// save the issue
			issues.put(feature.getId(), feature);
			// save the feature issues by feature id
			if (feature.getFeatureId()!= null){
				ArrayList<JiraIssueFeature> bucket = new ArrayList<JiraIssueFeature>();
				if (featuresById.containsKey(feature.getFeatureId())){
					bucket = featuresById.get(feature.getFeatureId()); 
				}
				if (!bucket.contains(feature)) bucket.add(feature);
				featuresById.put(feature.getFeatureId(), bucket);
			}
			return feature;
		}
		return null;
	}
	
	
	/**
	 * returns the number of entries
	 * @return long
	 */
	public long count()
	{
		if (!isInitialized()) this.open();
		return issues.size();
	}
	/**
	 * returns the number of entries
	 * @return long
	 */
	public long countFeatures()
	{
		if (!isInitialized()) this.open();
		return featuresById.size();
	}
	/**
	 * has the store the issue 
	 * @param id
	 * @return true or false
	 */
	public boolean has(JiraIssue issue){
		if (!isInitialized()) this.open();
		if (issues.containsKey(issue.getId())) return true;
		return false;
	}
	
	/**
	 * has the store the feature 
	 * @param id
	 * @return true or false
	 */
	public boolean has(JiraIssueFeature feature){
		if (!isInitialized()) this.open();
		if (featuresById.containsKey(feature.getId())) return true;
		return false;
	}
	
	/**
	 * has the store the feature by id ?
	 * @param id
	 * @return true or false
	 */
	public boolean has(String id){
		if (!isInitialized()) this.open();
		if (issues.containsKey(id)) {
			return true;
		}
		return false;
	}
	/**
	 * has the store the feature by FeatureId
	 * @param id
	 * @return true or false
	 */
	public boolean hasFeatureId(String FeatureId){
		if (!isInitialized()) this.open();
		if (featuresById.containsKey(FeatureId)) {
			return true;
		}
		return false;
	}
	/**
	 * returns a FEATURE by Id or null
	 * @param id
	 * @return Feature or null
	 */
	public JiraIssue getIssueById(String id){
		if (!isInitialized()) this.open();
		if (issues.containsKey(id)) {
			return (JiraIssue) issues.get(id);
		}
		return null;
	}
	/**
	 * returns a FEATURE by feature or empty bucket
	 * @param id
	 * @return Feature or null
	 */
	public ArrayList<JiraIssueFeature> getByFeatureId(String Id){
		if (!isInitialized()) this.open();
		if (hasFeatureId(Id)) {
			return featuresById.get(Id);
		}
		return new ArrayList<JiraIssueFeature>();
	}
	/**
	 * load - fill the store from Polarion with default values
	 * @return true if successfull
	 */
	public boolean loadAllFeatures()
	{
		
		
		return loadFeatures(JiraParameter.Default.getBaseUrl(), 
				JiraParameter.Default.getUserName(),
				JiraParameter.Default.getPassWord(),
				    null, null);
	}
	/**
	 * load - fill the store from Polarion with default values
	 * @return true if successfull
	 */
	public boolean loadAllFeatures(Temporal changedSince)
	{
		return loadFeatures(JiraParameter.Default.getBaseUrl(), 
				JiraParameter.Default.getUserName(),
				JiraParameter.Default.getPassWord(),
				    null, changedSince);
	}

	/**
	 * load from polarion the Feature with the id
	 * 
	 * @param id
	 * @return true if successfull
	 */
	public boolean loadJira(String id)
	{
		if (!isInitialized()) this.open();
		
		return loadFeatures(JiraParameter.Default.getBaseUrl(), 
				JiraParameter.Default.getUserName(),
				JiraParameter.Default.getPassWord(),
				    id, null);
	}
	/**
	 * load from polarion the id
	 * 
	 * @param baseUrl
	 * @param userName
	 * @param passWord
	 * @param key
	 * @return true if successfull
	 */
	private boolean loadFeatures(String baseUrl, String userName, String passWord, String key, Temporal changeDate)
	{
		if (!isInitialized()) this.open();
		
		long i=0;
		try {
			JiraIssueLoader aLoader = this.getIssueLoader(baseUrl, userName, passWord);
			// query

			Issue[] aList;
			String aQry;
			
			aQry = Setting.Default.get(PROPERTY_JIRA_FEATURE_QUERY);
			// load by KA-WI-ID
			if (key !=null) aQry = aQry + " AND key="+ key ;
			if (changeDate != null) {
				SimpleDateFormat aFormatter = new SimpleDateFormat("yyyy-MM-dd");
				aQry = aQry + " AND updated>='" + aFormatter.format(Date.from((Instant) changeDate)) + "'";
			}
			// run the query 
			aList =  aLoader.getIssuesByQuery(aQry);
			
			// fill
			for(Issue anIssue: aList)
			{
				// convert to requirement
				
					JiraIssueFeature aFeature = this.add(anIssue, true);
					if (aFeature != null) {
						try {
							aFeature.persist();
							i++;
							
						} catch (Exception e) {
							logger.debug(e.getMessage());
						}
					}else
						if (logger.isDebugEnabled()) 
							logger.debug(anIssue.getId() + " unable to convert to feature");
			}
			logger.info(i + " features persisted");
			return true;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getLocalizedMessage());
			if (logger.isDebugEnabled()) logger.catching(e);
		}
		
		// rollback & close  
		loader.close();
		return false;
	}

	/**
	 * close all sessions
	 */
	public synchronized void close(){
		loader.close();
	}
	/**
	 * open persistence
	 */
	private synchronized void open() {
		ArrayList<JiraIssueFeature> bucket;
		
		try {
		Query aQueryFeature =
				getPersistor().getEm().createQuery("select f from JiraIssueFeature f");
			aQueryFeature.setLockMode(LockModeType.NONE);
	        List<JiraIssueFeature> theFeatureResults = aQueryFeature.getResultList();
	        for (JiraIssueFeature f : theFeatureResults) {
	        	f.setLoaded(); // set loaded
	        	issues.put(f.getId(), f); // also add to issues
	        	// add to features by feature id
	        	if (!featuresById.containsKey(f.getFeatureId())) bucket = new ArrayList<JiraIssueFeature>() ;
	        	else bucket = featuresById.get(f.getFeatureId());
	        	featuresById.put(f.getFeatureId(), bucket);
	        }
	        this.setInitialized(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			 logger.info(e.getMessage());
			 return;
		} 
	        
	    logger.info("JiraIssueStore has retrieved " + featuresById.size() + " feature issues from persistence");
	}
	/**
	 * get the set of keys
	 * @return
	 */
	public Set<String> keySet(){
		if (!isInitialized()) this.open();
		
		return issues.keySet();
	}
	/**
	 * gets the collection of all features
	 * @return
	 */
	public Collection<JiraIssue> all(){
		if (!isInitialized()) this.open();
		
		return issues.values();
	}
	/** 
	 * persist them all
	 * @return
	 */
	public synchronized boolean  persist(){
		if (!isInitialized()) this.open();
		
		long i = 0;
		
		for (Bean aBean: issues.values()){
			try {
				// persistor.begin();
				aBean.persist();
				i++;
				// persistor.commit();
				if (i % 1000 == 0)
					System.out.print('.');
			} catch (Exception e) {
			
				if (logger.isDebugEnabled())
					logger.debug(e.getMessage());
				else if (logger.isErrorEnabled())
						logger.error(e.getStackTrace());

			}
		}
		logger.info(i + " issues persisted");
		return true;
	}
	
	@Override
	public JiraIssue getById(String id) {
		if (!isInitialized()) this.open();
		return issues.get(id);
	}

	public ArrayList<JiraIssueFeature> getFeaturesById(String id) {
		if (!isInitialized()) this.open();
		return featuresById.get(id);
	}
	@Override
	public JiraIssue add(JiraIssue o, boolean force) {
		if (!isInitialized()) this.open();
		// TODO Auto-generated method stub
		return null;
	}

}
