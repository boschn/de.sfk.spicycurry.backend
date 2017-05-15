/**
 * 
 */
package de.sfk.spicycurry.data;


import javax.persistence.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.Closeable;
import java.text.SimpleDateFormat;
import java.util.*;
import com.polarion.alm.ws.client.types.tracker.WorkItem;

import de.sfk.spicycurry.Setting;

/**
 * class stores the features 
 * 
 * use load to load it initially from Polarion
 */
public class FeatureStore implements Closeable {

		private static final String POLARION_PROJECTID = "1010";
		private static final String POLARION_TYPE = "ple";
		private static final String POLARION_FEATURE_QUERY = "project.id:" + POLARION_PROJECTID + " AND type:" + POLARION_TYPE + " AND rif_FT_Tracking_Feature:true";
	
		public static final String PROPERTY_POLARION_PROJECTID = "Feature.Polarion.ProjectID";
		public static final String PROPERTY_POLARION_TYPE = "Feature.Polarion.WorkItemType";
		public static final String PROPERTY_POLARION_FEATURE_QUERY = "Feature.Polarion.Query";

		// singleton
		public static FeatureStore db = new FeatureStore();
	
		// store
		private HashMap<String, Feature> features = new HashMap<String,Feature>();
		private HashMap<String, Feature> featuresByUri = new HashMap<String, Feature>(); //byURI
		private IPersistor persistor = Globals.Persistor;
		// polarion helpers
		private WorkItemPolarionLoader loader = null;
		
		// Logger
		private Logger logger = LogManager.getLogger(FeatureStore.class);
				
		/**
		 * constructor
		 */
		public FeatureStore() {
			super();
			
			// open persistence
			this.open();
			
			// set the defaults
			Setting.Default.get(PROPERTY_POLARION_PROJECTID, POLARION_PROJECTID);
			Setting.Default.get(PROPERTY_POLARION_TYPE, POLARION_TYPE);
			Setting.Default.get(PROPERTY_POLARION_FEATURE_QUERY, POLARION_FEATURE_QUERY);
		}
		/**
		 * return the default work item loader
		 * @return the loader
		 */
		public WorkItemPolarionLoader getLoader() {
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
		public WorkItemPolarionLoader getPolarionLoader(String baseUrl, String userName, String passWord) {
			try {
				if (loader == null) loader = WorkItemPolarionLoader.singleton;
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
		private void setPolarionLoader(WorkItemPolarionLoader loader) {
			this.loader = loader;
		}
		/**
		 * adds a work item as feature
		 * @param item
		 * @return Feature added or null
		 */
		protected Feature add(WorkItem item){
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
			return features.size();
		}
		/**
		 * has the store the feature 
		 * @param id
		 * @return true or false
		 */
		public boolean has(Feature feature){
			if (features.containsKey(feature.getId())) return true;
			return false;
		}
		
		/**
		 * has the store the feature by id ?
		 * @param id
		 * @return true or false
		 */
		public boolean has(String id){
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
			if (featuresByUri.containsKey(uri)) {
				return featuresByUri.get(uri);
			}
			return null;
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
		public boolean loadAllPolarion(Date changeDate)
		{
			return loadPolarion(PolarionParameter.Default.getBaseUrl(), 
					    PolarionParameter.Default.getUserName(),
					    PolarionParameter.Default.getPassWord(),
					    null, changeDate);
		}

		/**
		 * load from polarion the Feature with the id
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
		private boolean loadPolarion(String baseUrl, String userName, String passWord, String id, Date changeDate)
		{
			long i=0;
			try {
				WorkItemPolarionLoader aLoader = this.getPolarionLoader(baseUrl, userName, passWord);
				// query

				String[] aList;
				String aQry;
				
				aQry = Setting.Default.get(PROPERTY_POLARION_FEATURE_QUERY);
				if (id !=null) aQry = aQry + " AND id:"+ id ;
				if (changeDate != null) {
					SimpleDateFormat aFormatter = new SimpleDateFormat("yyyyMMdd");
					aQry = aQry + " AND updated:[" + aFormatter.format(changeDate) + " TO 30000000]";
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
								persistor.begin();
								persistor.persist(aFeature);
								persistor.commit();
								i++;
								if (i % 100 == 0) System.out.print('.');
								
							} catch (Exception e) {
								if (logger.isErrorEnabled()) 
									logger.catching(e);
								else logger.debug(e.getMessage());
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
			persistor.rollback();
			loader.close();
			return false;
		}

		/**
		 * close all sessions
		 */
		public void close(){
			loader.close();
		}
		/**
		 * open persistence
		 */
		private void open() {

			try {
			Query aQueryFeature =
					persistor.getEm().createQuery("select f from Feature f");
		        List<Feature> theFeatureResults = aQueryFeature.getResultList();
		        for (Feature f : theFeatureResults) {
		        	this.add(f, false);
		        }
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
			return features.keySet();
		}
		/**
		 * gets the collection of all features
		 * @return
		 */
		public Collection<Feature> all(){
			return features.values();
		}
}
