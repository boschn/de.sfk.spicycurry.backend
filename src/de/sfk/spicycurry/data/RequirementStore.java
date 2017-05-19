/**
 * 
 */
package de.sfk.spicycurry.data;

/**
 * @author boris.schneider
 *
 */

import javax.persistence.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.*;
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
public class RequirementStore implements Closeable, IStore<Requirement> {

		private static final String POLARION_PROJECTID = "1010";
		private static final String POLARION_TYPE = "ple";
		private static final String POLARION_REQUIREMENT_QUERY = "project.id:" + POLARION_PROJECTID + " AND type: " + POLARION_TYPE;
		public static final String PROPERTY_POLARION_PROJECTID = "Requirement.Polarion.ProjectID";
		public static final String PROPERTY_POLARION_TYPE = "Requirement.Polarion.WorkItemType";
		public static final String PROPERTY_POLARION_REQUIREMENT_QUERY = "Requirement.Polarion.Query";
		
		// class
		public final static RequirementStore db = new RequirementStore();
	
		// 
		private boolean isInitialized = false;
		// store
		private ConcurrentHashMap<String, Requirement> requirements = new ConcurrentHashMap<String,Requirement>(); // byId
		private ConcurrentHashMap<String, Requirement> requirementsByUri = new ConcurrentHashMap<String, Requirement>(); //byURI
		
		// persistor
		private IPersistor persistor = null; // lazy
		
		// polarion helpers
		private PolarionWorkItemLoader loader = null;

		// Logger
		private Logger logger = LogManager.getLogger(RequirementStore.class);
		
		/**
		 * constructor
		 */
		protected RequirementStore() {
			super();
			
			
			// set the defaults
			Setting.Default.get(PROPERTY_POLARION_PROJECTID, POLARION_PROJECTID);
			Setting.Default.get(PROPERTY_POLARION_TYPE, POLARION_TYPE);
			Setting.Default.get(PROPERTY_POLARION_REQUIREMENT_QUERY, POLARION_REQUIREMENT_QUERY);
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
		 * return the work item loader associated with the store or create one
		 * @param baseUrl
		 * @param userName
		 * @param passWord
		 * @return
		 */
		public PolarionWorkItemLoader getPolarionLoader(String baseUrl, String userName, String passWord) {
			try {
				if (loader == null)	loader = PolarionWorkItemLoader.singleton;
				if (!loader.isInitialized()){
					loader.beginSession(baseUrl,userName,passWord);
					loader.setFields(PolarionParameter.getPLEFieldNames());
				}
				return loader;
			} catch (Exception e) {
				logger.catching(e);
				return null;
			}
		}
		/**
		 * @return the persistor
		 */
		public synchronized IPersistor getPersistor() {
			if (persistor==null) persistor = new EclipseLinkPersistor("RequirementStore");
			return persistor;
		}
		/**
		 * @param persistor the persistor to set
		 */
		public synchronized void setPersistor(IPersistor persistor) {
			this.persistor = persistor;
		}
		/**
		 * @param loader the loader to set
		 */
		@SuppressWarnings("unused")
		private void setPolarionLoader(PolarionWorkItemLoader loader) {
			this.loader = loader;
		}
		
		/**
		 * adds a work item as requirement
		 * @param item
		 * @return Feature added or null
		 */
		protected Requirement add(WorkItem item, boolean force){
			// open persistence
			if (!this.isInitialized()) this.open();

			Requirement aRequirement = null;			
			if (this.hasUri(item.getUri())) aRequirement = this.getByUri(item.getUri());
			aRequirement = this.getLoader().convertToRequirement(item, aRequirement);
			return add(aRequirement, force);
		}
		/**
		 * returns the number of entries
		 * @return long
		 */
		public long count()
		{
			return requirements.size();
		}
		/**
		 * add a feature to the store
		 * @param feature
		 * @return
		 */
		public Requirement add(Requirement requirement, boolean force) {
			if (force || !this.has(requirement)) {
				requirements.put(requirement.getId(), requirement);
				if (!this.hasUri(requirement))
					this.requirementsByUri.put(requirement.getPolarionUri(), requirement);
				return requirement;
			}
			return null;
		}
		/**
		 * has the store the feature 
		 * @param id
		 * @return true or false
		 */
		public boolean has(Feature feature){
			if (requirements.containsKey(feature.getId())) return true;
			return false;
		}
		/**
		 * has the store the requirement 
		 * @param id
		 * @return true or false
		 */
		public boolean has(Requirement requirement){
			if (requirements.containsKey(requirement.getId())) return true;
			return false;
		}
		/**
		 * has the store the requirement 
		 * @param id
		 * @return true or false
		 */
		public boolean hasUri(Requirement requirement){
			if (requirementsByUri.containsKey(requirement.getPolarionUri())) return true;
			return false;
		}
		/**
		 * has the store the requirement 
		 * @param id
		 * @return true or false
		 */
		public boolean hasUri(String uri){
			if (requirementsByUri.containsKey(uri)) return true;
			return false;
		}
		/**
		 * has the store the feature by id ?
		 * @param id
		 * @return true or false
		 */
		public boolean has(String id){
			if (requirements.containsKey(id)) {
				return true;
			}
			return false;
		}
		/**
		 * returns a requirement by Id or null
		 * @param id
		 * @return Feature or null
		 */
		public Requirement getById(String id){
			if (requirements.containsKey(id)) {
				return requirements.get(id);
			}
			return null;
		}
		/**
		 * returns a requirement from the store by uri or null if not found
		 * 
		 * @param id
		 * @return Feature or null
		 */
		public Requirement getByUri(String uri){
			if (requirementsByUri.containsKey(uri)) {
				return requirementsByUri.get(uri);
			}
			return null;
		}
		/**
		 * load - load the workitem with the standard setting and persist
		 * 
		 * @param id of the workitem
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
		 * load all workitems from polarion with standard user and persist
		 * 
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
		 * load all workitems from polarion with standard user and persist
		 * 
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
		 * load from polarion the workitem and persist it in store
		 * 
		 * @param baseUrl polarion base url
		 * @param userName polarion access username
		 * @param passWord polarion password
		 * @param id of the workitem or null to load ALL query
		 * @return
		 */
		private boolean loadPolarion(String baseUrl, String userName, String passWord, String id, Temporal changeDate)
		{
			long i=0;
			try {
				PolarionWorkItemLoader aLoader = this.getPolarionLoader(baseUrl, userName, passWord);
				// query
				String[] aList;
				String aQry;
				
				aQry = Setting.Default.get(PROPERTY_POLARION_REQUIREMENT_QUERY);
				if (id != null) aQry = aQry + " AND id:"+ id;
				if (changeDate != null) {
					SimpleDateFormat aFormatter = new SimpleDateFormat("yyyyMMdd");
					aQry = aQry + " AND updated:[" + aFormatter.format(Date.from((Instant) changeDate)) + " TO 30000000]";
				}
				
				// run the query
				aList = aLoader.getWorkItemsUriByQuery(aQry, null);
				logger.info("from polarion " + aList.length + " uris retrieved");
				
				
				// fill
				for(String anUri: aList)
				{
					WorkItem anItem = aLoader.getWorkItemByUri(anUri);
					// convert to requirement
					if (anItem != null){
						Requirement aRequirement = 	this.add(anItem, true);
						if (aRequirement != null) {
							i++;
							if (i % 1000 == 0)
								System.out.print('.');
						
						}else
							if (logger.isDebugEnabled()) 
								logger.debug(anItem.getUri() + " unable to convert to requirement");
						
					} else
						if (logger.isDebugEnabled()) 
							logger.debug(anUri + " unable to obtain work item from polarion");
				}
				 
				
				logger.info(i + " requirements loaded from polarion");
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
		public void close(){
			loader.close();
		}
		/** 
		 * persist them all
		 * @return
		 */
		public synchronized boolean persist(){
			long i = 0;
			
			for (Bean aBean: requirements.values()){
				try {
					// persistor.begin();
					aBean.persist();
					i++;
					// persistor.commit();
					if (i % 1000 == 0)
						System.out.print('.');
				} catch (Exception e) {
					logger.debug(e.getMessage());
				}
			}
			
			return true;
		}
		/**
		 * open persistence
		 */
		private synchronized void open() {
			try {

					long i =0;
					Query aQueryRequirement =
							getPersistor().getEm().createQuery("select r from Requirement r");
					aQueryRequirement.setLockMode(LockModeType.NONE);
					@SuppressWarnings("unchecked")
					List<Requirement> theRequirementResults = aQueryRequirement.getResultList();
				    for (Requirement r : theRequirementResults) {
				    		r.setLoaded();
				        	requirements.put(r.getId(), r);
				        	requirementsByUri.put(r.getPolarionUri(), r);
				        	i++;
				    }
				    this.setInitialized(true);
				    
				} catch (Exception e) {
					// TODO Auto-generated catch block
					 logger.info(e.getLocalizedMessage());
					 return;
				} 
			
		     logger.info("has retrieved " + requirements.size() + " entries from persistence");
		     
		     
		}
		
		/**
		 * load recursive the requirements of a feature (such as workitem) from polarion live load
		 */
		protected List<Requirement> loadPolarionSubRequirements(Requirement noderequirement, WorkItem nodeitem)
		{
			PolarionWorkItemLoader aLoader = this.getLoader();
			ArrayList<Requirement> aList = new ArrayList<Requirement>();
			
			// get the work item
			if (nodeitem == null) 
				nodeitem = aLoader.getWorkItemByUri(noderequirement.getPolarionUri());
			
			// clear and mark we have read it
			noderequirement.clearSubRequirements();
			
			// get the properities
			String polarionProjectId = Setting.Default.get(PROPERTY_POLARION_PROJECTID, POLARION_PROJECTID);
			String polarionType = Setting.Default.get(PROPERTY_POLARION_TYPE, POLARION_TYPE);
			
			for(WorkItem aChildWorkItem: aLoader.getDerived(nodeitem, polarionType, polarionProjectId))
			{
				// convert the child item to a requirement
				Requirement aChildRequirement = (Requirement) this.getLoader().convertToRequirement(aChildWorkItem, null);
				
				// add it to the global requirements -> if not there recursion dive
				if (!requirements.containsKey(aChildRequirement.getId())) {
					// store
					requirements.put(aChildRequirement.getId(), aChildRequirement);
					// deep dive -> if already in store then it must have been already resolved
					loadPolarionSubRequirements(aChildRequirement,aChildWorkItem);
				} else aChildRequirement = requirements.get(aChildRequirement.getId()) ;
				
				// add child to the node requirement
				noderequirement.addRequirement(aChildRequirement);
				// add the requirement to the list
				aList.add(aChildRequirement);
			}
			return aList;
		}
		/**
		 * return recursively the requirements of a requirement (if loaded)
		 */
		protected List<Requirement> getSubRequirements(Requirement noderequirement)
		{
			
			ArrayList<Requirement> aList = new ArrayList<Requirement>();
			
			// clear and mark we have read it
			noderequirement.clearSubRequirements();
			String[] theUris = noderequirement.getDerivedPolarionURIs();
			for(String anUri: theUris )
			{
				// convert the child item to a requirement
				Requirement aChildRequirement = this.getByUri(anUri);
				if (aChildRequirement != null) {
					// add child to the node requirement
					noderequirement.addRequirement(aChildRequirement);
					// add the requirement to the list
					aList.add(aChildRequirement);
				} else
					if (logger.isDebugEnabled()){ 
						logger.info("'" + anUri + "' is not loaded in requirements");
					}
			}
			return aList;
		}
		
		/**
		 * get the set of keys
		 * @return
		 */
		public Set<String> keySet(){
			return requirements.keySet();
		}
		/**
		 * gets the collection of all features
		 * @return
		 */
		public Collection<Requirement> all(){
			return requirements.values();
		}
}
