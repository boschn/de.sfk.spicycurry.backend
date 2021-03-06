/**
 * 
 */
package de.sfk.spicycurry.data;


import javax.persistence.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.Closeable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.polarion.alm.ws.client.types.tracker.WorkItem;

import de.sfk.spicycurry.Globals;
import de.sfk.spicycurry.Setting;

/**
 * class stores the features 
 * 
 * use load to load it initially from Polarion
 */
public class FeatureStore implements Closeable, IStore<Feature> {

		private static final String POLARION_PROJECTID = "1010";
		private static final String POLARION_TYPE = "ple";
		private static final String POLARION_FEATURE_QUERY = "project.id:" + POLARION_PROJECTID + " AND type:" + POLARION_TYPE + " AND rif_FT_Tracking_Feature:true";
	
		public static final String PROPERTY_POLARION_PROJECTID = "Feature.Polarion.ProjectID";
		public static final String PROPERTY_POLARION_TYPE = "Feature.Polarion.WorkItemType";
		public static final String PROPERTY_POLARION_FEATURE_QUERY = "Feature.Polarion.Query";

		private static final String JIRA_PROJECTID = "MIBSERIELH";
		private static final String JIRA_TYPE = "Feature";
		private static final String JIRA_FEATURE_QUERY = "project=" + JIRA_PROJECTID + " AND issuetype:" + JIRA_TYPE ;
	
		public static final String PROPERTY_JIRA_PROJECTID = "Feature.JIRA.ProjectID";
		public static final String PROPERTY_JIRA_TYPE = "Feature.JIRA.IssueType";
		public static final String PROPERTY_JIRA_FEATURE_QUERY = "Feature.JIRA.Query";
		
		// singleton
		public final static FeatureStore db = new FeatureStore();
	
		// store
		private ConcurrentHashMap<String, Feature> features = new ConcurrentHashMap<String,Feature>();
		private ConcurrentHashMap<String, Feature> featuresByUri = new ConcurrentHashMap<String, Feature>(); //byURI
		
		// Persistor
		private IPersistor persistor = null;
		// polarion helpers
		private PolarionWorkItemLoader loader = null;
		private boolean isInitialized = false;
		
		// Logger
		private Logger logger = LogManager.getLogger(FeatureStore.class);
				
		/**
		 * constructor
		 */
		protected FeatureStore() {
			super();
			
					
			// set the defaults
			Setting.Default.get(PROPERTY_POLARION_PROJECTID, POLARION_PROJECTID);
			Setting.Default.get(PROPERTY_POLARION_TYPE, POLARION_TYPE);
			Setting.Default.get(PROPERTY_POLARION_FEATURE_QUERY, POLARION_FEATURE_QUERY);
		}
		/**
		 * return the default work item loader
		 * @return the loader
		 */
		public PolarionWorkItemLoader getLoader() {
			return getPolarionLoader(PolarionParameter.Default.getBaseUrl(), 
							 PolarionParameter.Default.getUserName(),
							 PolarionParameter.Default.getPassWord());
		}
		/**
		 * return the work item loader associated with the store or create one
		 * @param baseUrl
		 * @param userName
		 * @param passWord
		 * @return
		 */
		public PolarionWorkItemLoader getPolarionLoader(String baseUrl, String userName, String passWord) {
			try {
				if (loader == null) loader = PolarionWorkItemLoader.singleton;
				if (!loader.isInitialized()){
					loader.beginSession(baseUrl,userName,passWord);
					loader.setFields(PolarionParameter.getPLEFieldNames());
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
		private void setPolarionLoader(PolarionWorkItemLoader loader) {
			this.loader = loader;
		}
		/**
		 * @return the persistor
		 */
		public synchronized IPersistor getPersistor() {
			if (persistor==null) persistor = new EclipseLinkPersistor("FeatureStore");
			return persistor;
		}
		/**
		 * @param persistor the persistor to set
		 */
		public synchronized void setPersistor(IPersistor persistor) {
			this.persistor = persistor;
		}
		/**
		 * adds a work item as feature
		 * @param item
		 * @return Feature added or null
		 */
		protected Feature add(WorkItem item){
			if (!isInitialized()) this.open();
			
			Feature aFeature = null;
			
			if (this.hasUri(item.getUri()))	
				aFeature = this.getByUri(item.getUri());
			aFeature = (Feature) this.getLoader().convertToFeature(item, aFeature);
			return add(aFeature, true);
		}
		
		/**
		 * add a feature to the store
		 * @param feature
		 * @return
		 */
		public Feature add(Feature feature, boolean force) {
			if (!isInitialized()) this.open();
			if (force || !this.has(feature)) {
				// will be replaced by put
				features.put(feature.getId(), feature);
				if (!this.hasUri(feature.getPolarionUri()))
					featuresByUri.put(feature.getPolarionUri(), feature);
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
			return features.size();
		}
		/**
		 * has the store the feature 
		 * @param id
		 * @return true or false
		 */
		public boolean has(Feature feature){
			if (!isInitialized()) this.open();
			if (features.containsKey(feature.getId())) return true;
			return false;
		}
		
		/**
		 * has the store the feature by id ?
		 * @param id
		 * @return true or false
		 */
		public boolean has(String id){
			if (!isInitialized()) this.open();
			if (features.containsKey(id)) {
				return true;
			}
			return false;
		}
		/**
		 * has the store the feature by Uri
		 * @param id
		 * @return true or false
		 */
		public boolean hasUri(String uri){
			if (!isInitialized()) this.open();
			if (featuresByUri.containsKey(uri)) {
				return true;
			}
			return false;
		}
		/**
		 * returns a FEATURE by Id or null
		 * @param id
		 * @return Feature or null
		 */
		public Feature getById(String id){
			if (!isInitialized()) this.open();
			if (features.containsKey(id)) {
				return features.get(id);
			}
			return null;
		}
		/**
		 * returns a FEATURE by Uri or null
		 * @param id
		 * @return Feature or null
		 */
		public Feature getByUri(String uri){
			if (!isInitialized()) this.open();
			if (featuresByUri.containsKey(uri)) {
				return featuresByUri.get(uri);
			}
			return null;
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
		 * load - fill the store from Polarion with default values
		 * @return true if successfull
		 */
		public boolean loadAllPolarion()
		{
			return loadPolarion(PolarionParameter.Default.getBaseUrl(), 
					    PolarionParameter.Default.getUserName(),
					    PolarionParameter.Default.getPassWord(),
					    null, null);
		}
		/**
		 * load - fill the store from Polarion with default values
		 * @return true if successfull
		 */
		public boolean loadAllPolarion(Temporal changedSince)
		{
			return loadPolarion(PolarionParameter.Default.getBaseUrl(), 
					    PolarionParameter.Default.getUserName(),
					    PolarionParameter.Default.getPassWord(),
					    null, changedSince);
		}

		/**
		 * load from polarion the Feature with the id
		 * 
		 * @param id
		 * @return true if successfull
		 */
		public boolean loadPolarion(String id)
		{
			return loadPolarion(PolarionParameter.Default.getBaseUrl(), 
					    PolarionParameter.Default.getUserName(),
					    PolarionParameter.Default.getPassWord(),
					    id, null);
		}
		/**
		 * load from polarion the id
		 * 
		 * @param baseUrl
		 * @param userName
		 * @param passWord
		 * @param id
		 * @return true if successfull
		 */
		private boolean loadPolarion(String baseUrl, String userName, String passWord, String id, Temporal changeDate)
		{
			if (!isInitialized()) this.open();
			
			long i=0;
			try {
				PolarionWorkItemLoader aLoader = this.getPolarionLoader(baseUrl, userName, passWord);
				// query

				String[] aList;
				String aQry;
				
				aQry = Setting.Default.get(PROPERTY_POLARION_FEATURE_QUERY);
				// load by KA-WI-ID
				if (id !=null) aQry = aQry + " AND idKA:"+ id ;
				if (changeDate != null) {
					SimpleDateFormat aFormatter = new SimpleDateFormat("yyyyMMdd");
					aQry = aQry + " AND updated:[" + aFormatter.format(Date.from((Instant) changeDate)) + " TO 30000000]";
				}
				// run the query 
				aList =  aLoader.getWorkItemsUriByQuery(aQry,null);
				logger.info("from polarion " + aList.length + " uris retrieved for qry " );
				
				
				// fill
				for(String anUri: aList)
				{
					WorkItem anItem = aLoader.getWorkItemByUri(anUri);
					// convert to requirement
					if (anItem != null){
						Feature aFeature = 	this.add(anItem);
						if (aFeature != null) {
							try {
								aFeature.persist();
								i++;
								if (i % 100 == 0) System.out.print('.');
								
							} catch (Exception e) {
								logger.debug(e.getMessage());
							}
						}else
							
							if (logger.isDebugEnabled()) 
								logger.debug(anItem.getUri() + " unable to convert to feature");
						
					} else if (logger.isDebugEnabled()) 
						logger.debug(anUri + " unable to obtain work item from polarion");
				}
				logger.info(i + " features persisted");
				return true;
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(e.getMessage(), e);
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

			try {
			Query aQueryFeature =
					getPersistor().getEm().createQuery("select f from Feature f");
				aQueryFeature.setLockMode(LockModeType.NONE);
		        List<Feature> theFeatureResults = aQueryFeature.getResultList();
		        for (Feature f : theFeatureResults) {
		        	f.setLoaded();
		        	features.put(f.getId(), f);
		        	featuresByUri.put(f.getPolarionUri(),f);
		        }
		        this.setInitialized(true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				 logger.info(e.getLocalizedMessage());
				 return;
			} 
		        
		    logger.info("FeatureStore has retrieved " + features.size() + " entries from persistence");
		}
		/**
		 * get the set of keys
		 * @return
		 */
		public Set<String> keySet(){
			if (!isInitialized()) this.open();
			return features.keySet();
		}
		/**
		 * gets the collection of all features
		 * @return
		 */
		public Collection<Feature> all(){
			if (!isInitialized()) this.open();
			return features.values();
		}
		/** 
		 * persist them all
		 * @return
		 */
		public synchronized boolean  persist(){
			if (!isInitialized()) this.open();
			
			long i = 0;
			
			for (Bean aBean: features.values()){
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
			logger.info(i + " features persisted");
			return true;
		}

}
