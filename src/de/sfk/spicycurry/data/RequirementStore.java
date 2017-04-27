/**
 * 
 */
package de.sfk.spicycurry.data;

/**
 * @author boris.schneider
 *
 */

import javax.persistence.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Closeable;
import java.util.*;

import com.polarion.alm.ws.client.types.tracker.WorkItem;

/**
 * class stores the features 
 * 
 * use load to load it initially from Polarion
 */
public class RequirementStore implements Closeable {

		private static final String POLARION_PROJECTID = "1010";
		private static final String POLARION_TYPE = "ple";
		private static final String POLARION_REQUIREMENT_QUERY = "project.id:" + POLARION_PROJECTID + " AND type: " + POLARION_TYPE;
		public static final String PROPERTY_POLARION_PROJECTID = "Requirement.Polarion.ProjectID";
		public static final String PROPERTY_POLARION_TYPE = "Requirement.Polarion.WorkItemType";
		public static final String PROPERTY_POLARION_REQUIREMENT_QUERY = "Requirement.Polarion.Query";
		
		// class
		public static RequirementStore db = new RequirementStore();
	
		// store
		private HashMap<String, Requirement> requirements = new HashMap<String,Requirement>(); // byId
		private HashMap<String, Requirement> requirementsByUri = new HashMap<String, Requirement>(); //byURI
		private IPersistor persistor = Globals.Persistor;
		
		// polarion helpers
		private WorkItemPolarionLoader loader = null;
			

		// Logger
		private Log log = LogFactory.getLog(RequirementStore.class);
		
		/**
		 * constructor
		 */
		public RequirementStore() {
			super();
			
			// open persistence
			this.open();
			
			// set the defaults
			Setting.Default.get(PROPERTY_POLARION_PROJECTID, POLARION_PROJECTID);
			Setting.Default.get(PROPERTY_POLARION_TYPE, POLARION_TYPE);
			Setting.Default.get(PROPERTY_POLARION_REQUIREMENT_QUERY, POLARION_REQUIREMENT_QUERY);
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
				if (loader == null)	loader = WorkItemPolarionLoader.singleton;
				if (!loader.isInitialized()){
					loader.beginSession(baseUrl,userName,passWord);
					loader.setFields(PolarionParameter.getPLEFieldNames());
				}
				return loader;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		 * adds a work item as requirement
		 * @param item
		 * @return Feature added or null
		 */
		protected Requirement add(WorkItem item, boolean force){
						
			Requirement aRequirement = (Requirement) this.getLoader().convertToRequirement(item, null);
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
				if (!this.hasByUri(requirement))
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
		public boolean hasByUri(Requirement requirement){
			if (requirementsByUri.containsKey(requirement.getPolarionUri())) return true;
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
		 * returns a requirement by Id or null
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
		 * load - fille the store from Polarion with default values
		 * @return
		 */
		public boolean loadPolarion(String id)
		{
			return loadAllPolarion(PolarionParameter.Default.getBaseUrl(), 
					    PolarionParameter.Default.getUserName(),
					    PolarionParameter.Default.getPassWord(),
					    id);
		}
		public boolean loadAllPolarion()
		{
			return loadAllPolarion(PolarionParameter.Default.getBaseUrl(), 
					    PolarionParameter.Default.getUserName(),
					    PolarionParameter.Default.getPassWord(),
					    null);
		}
		/**
		 * load - fill store from Polarion
		 */
		private boolean loadAllPolarion(String baseUrl, String userName, String passWord, String id)
		{
			long i=0;
			try {
				WorkItemPolarionLoader aLoader = this.getPolarionLoader(baseUrl, userName, passWord);
				// query
				String[] aList;
				String aQry;
				
				aQry = Setting.Default.get(PROPERTY_POLARION_REQUIREMENT_QUERY);
				if (id != null) aQry = aQry + " AND id:"+ id;
				
				// run the query
				aList = aLoader.getWorkItemsUriByQuery(aQry, null);
				log.info("from polarion " + aList.length + " uris retrieved");
				persistor.begin();
				
				// fill
				for(String anUri: aList)
				{
					WorkItem anItem = aLoader.getWorkItemByUri(anUri);
					// convert to requirement
					if (anItem != null){
						Requirement aRequirement = 	this.add(anItem, true);
						if (aRequirement != null) {
							persistor.persist(aRequirement);
							// commit
							persistor.commit();
							i++;
							if (i % 1000 == 0) System.out.print('.');
						}else
							if (log.isDebugEnabled()) log.debug(anItem.getUri() + " unable to convert to requirement");
					} if (log.isDebugEnabled()) log.debug(anUri + " unable to obtain work item from polarion");
				}
				 
				
				log.info(i + " requirements persisted");
				return true;
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error(e.getMessage(), e);
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

				long i =0;
				Query aQueryRequirement =
						persistor.getEm().createQuery("select r from Requirement r");
				@SuppressWarnings("unchecked")
				List<Requirement> theRequirementResults = aQueryRequirement.getResultList();
			    for (Requirement r : theRequirementResults) {
			        	this.add(r, false);
			        	i++;
			        	if (i % 1000 == 0) System.out.print(".");
			    }
			     System.out.println();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					 log.info(e.getLocalizedMessage());
					 return;
				} 
			
		     log.info("has retrieved " + requirements.size() + " entries from persistence");
		}
		
		/**
		 * load recursive the requirements of a feature (such as workitem) from polarion live load
		 */
		protected List<Requirement> loadPolarionSubRequirements(Requirement noderequirement, WorkItem nodeitem)
		{
			WorkItemPolarionLoader aLoader = this.getLoader();
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
					if (log.isDebugEnabled()){ 
						log.info("'" + anUri + "' is not loaded in requirements");
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
