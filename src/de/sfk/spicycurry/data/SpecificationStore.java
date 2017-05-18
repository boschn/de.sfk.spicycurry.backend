/**
 * 
 */
package de.sfk.spicycurry.data;

import java.io.Closeable;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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

/**
 * Specifcation Store for storing persistable specifications
 * 
 * @author boris.schneider
 *
 */
public class SpecificationStore implements Closeable {

	private static final String POLARION_PROJECTID = "1011";
	private static final String POLARION_TYPE = "requirement";
	private static final String POLARION_SPECIFICATION_QUERY = "project.id:" + POLARION_PROJECTID + " AND type: " + POLARION_TYPE;
	
	public static final String PROPERTY_POLARION_PROJECTID = "Specification.Polarion.ProjectID";
	public static final String PROPERTY_POLARION_TYPE = "Specification.Polarion.WorkItemType";
	public static final String PROPERTY_POLARION_Specification_QUERY = "Specification.Polarion.Query";
	
	// class singleton
	public static SpecificationStore db = new SpecificationStore();

	// store
	private ConcurrentHashMap<String, Specification> specifications = new ConcurrentHashMap<String,Specification>(); // byId
	private ConcurrentHashMap<String, Specification> specificationsByUri = new ConcurrentHashMap<String, Specification>(); //byURI
	private IPersistor persistor = Globals.Persistor;
	
	// polarion helpers
	private PolarionWorkItemLoader loader = null;
		

	// Logger
	private Logger logger = LogManager.getLogger(SpecificationStore.class);
	
	/**
	 * constructor
	 */
	public SpecificationStore() {
		super();
		
		// open persistence
		this.open();
		
		// set the defaults
		Setting.Default.get(PROPERTY_POLARION_PROJECTID, POLARION_PROJECTID);
		Setting.Default.get(PROPERTY_POLARION_TYPE, POLARION_TYPE);
		Setting.Default.get(PROPERTY_POLARION_Specification_QUERY, POLARION_SPECIFICATION_QUERY);
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
	 * @param loader the loader to set
	 */
	@SuppressWarnings("unused")
	private void setPolarionLoader(PolarionWorkItemLoader loader) {
		this.loader = loader;
	}
	
	/**
	 * adds a work item as Specification
	 * @param item
	 * @return Feature added or null
	 */
	protected Specification add(WorkItem item, boolean force){
		Specification aSpecification = null;			
		if (this.hasUri(item.getUri())) aSpecification = this.getByUri(item.getUri());
		aSpecification = this.getLoader().convertToSpecification(item, aSpecification);
		return add(aSpecification, force);
	}
	/**
	 * returns the number of entries
	 * @return long
	 */
	public long count()
	{
		return specifications.size();
	}
	/**
	 * add a feature to the store
	 * @param feature
	 * @return
	 */
	public Specification add(Specification Specification, boolean force) {
		if (force || !this.has(Specification)) {
			specifications.put(Specification.getId(), Specification);
			if (!this.hasUri(Specification))
				this.specificationsByUri.put(Specification.getPolarionUri(), Specification);
			return Specification;
		}
		return null;
	}
	/**
	 * has the store the feature 
	 * @param id
	 * @return true or false
	 */
	public boolean has(Feature feature){
		if (specifications.containsKey(feature.getId())) return true;
		return false;
	}
	/**
	 * has the store the Specification 
	 * @param id
	 * @return true or false
	 */
	public boolean has(Specification Specification){
		if (specifications.containsKey(Specification.getId())) return true;
		return false;
	}
	/**
	 * has the store the Specification 
	 * @param id
	 * @return true or false
	 */
	public boolean hasUri(Specification Specification){
		if (specificationsByUri.containsKey(Specification.getPolarionUri())) return true;
		return false;
	}
	/**
	 * has the store the Specification 
	 * @param id
	 * @return true or false
	 */
	public boolean hasUri(String uri){
		if (specificationsByUri.containsKey(uri)) return true;
		return false;
	}
	/**
	 * has the store the feature by id ?
	 * @param id
	 * @return true or false
	 */
	public boolean has(String id){
		if (specifications.containsKey(id)) {
			return true;
		}
		return false;
	}
	/**
	 * returns a Specification by Id or null
	 * @param id
	 * @return Feature or null
	 */
	public Specification getById(String id){
		if (specifications.containsKey(id)) {
			return specifications.get(id);
		}
		return null;
	}
	/**
	 * returns a Specification from the store by uri or null if not found
	 * 
	 * @param id
	 * @return Feature or null
	 */
	public Specification getByUri(String uri){
		if (specificationsByUri.containsKey(uri)) {
			return specificationsByUri.get(uri);
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
			
			aQry = Setting.Default.get(PROPERTY_POLARION_Specification_QUERY);
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
				// convert to Specification
				if (anItem != null){
					Specification aSpecification = 	this.add(anItem, true);
					if (aSpecification != null) {
						i++;
						if (i % 1000 == 0)	System.out.print('.');

/*						
						try {
							persistor.begin();
							persistor.persist(aSpecification);
							persistor.commit();
							
						} catch (Exception e) {
							
							if (logger.isDebugEnabled())
								logger.debug(e.getMessage());
							else if (logger.isErrorEnabled())
									logger.error(e.getStackTrace());

						}
*/
					}else
						if (logger.isDebugEnabled()) 
							logger.debug(anItem.getUri() + " unable to convert to Specification");
					
				} else
					if (logger.isDebugEnabled()) 
						logger.debug(anUri + " unable to obtain work item from polarion");
			}
			 
			
			logger.info(i + " specifications persisted");
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
	 * open persistence
	 */
	private synchronized void open() {
		try {

			long i =0;
			Query aQuerySpecification =
					persistor.getEm().createQuery("select r from Specification r");
			aQuerySpecification.setLockMode(LockModeType.NONE);
			@SuppressWarnings("unchecked")
			List<Specification> theSpecificationResults = aQuerySpecification.getResultList();
		    for (Specification r : theSpecificationResults) {
		    		r.setLoaded();
		        	this.add(r, false);
		        	i++;
		    }
		     System.out.println();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				 logger.info(e.getLocalizedMessage());
				 return;
			} 
		
	     logger.info("has retrieved " + specifications.size() + " entries from persistence");
	}
	
	/**
	 * load recursive the specifications of a feature (such as workitem) from polarion live load
	 */
	protected List<Specification> loadPolarionSubSpecifications(Specification nodeSpecification, WorkItem nodeitem)
	{
		PolarionWorkItemLoader aLoader = this.getLoader();
		ArrayList<Specification> aList = new ArrayList<Specification>();
		
		// get the work item
		if (nodeitem == null) 
			nodeitem = aLoader.getWorkItemByUri(nodeSpecification.getPolarionUri());
		
		// clear and mark we have read it
		nodeSpecification.clearSubSpecifications();
		
		// get the properities
		String polarionProjectId = Setting.Default.get(PROPERTY_POLARION_PROJECTID, POLARION_PROJECTID);
		String polarionType = Setting.Default.get(PROPERTY_POLARION_TYPE, POLARION_TYPE);
		
		for(WorkItem aChildWorkItem: aLoader.getDerived(nodeitem, polarionType, polarionProjectId))
		{
			// convert the child item to a Specification
			Specification aChildSpecification = (Specification) this.getLoader().convertToSpecification(aChildWorkItem, null);
			
			// add it to the global specifications -> if not there recursion dive
			if (!specifications.containsKey(aChildSpecification.getId())) {
				// store
				specifications.put(aChildSpecification.getId(), aChildSpecification);
				// deep dive -> if already in store then it must have been already resolved
				loadPolarionSubSpecifications(aChildSpecification,aChildWorkItem);
			} else aChildSpecification = specifications.get(aChildSpecification.getId()) ;
			
			// add child to the node Specification
			nodeSpecification.addSpecification(aChildSpecification);
			// add the Specification to the list
			aList.add(aChildSpecification);
		}
		return aList;
	}
	/**
	 * return recursively the specifications of a Specification (if loaded)
	 */
	protected List<Specification> getSubSpecifications(Specification nodeSpecification)
	{
		
		ArrayList<Specification> aList = new ArrayList<Specification>();
		
		// clear and mark we have read it
		nodeSpecification.clearSubSpecifications();
		String[] theUris = nodeSpecification.getDerivedPolarionURIs();
		for(String anUri: theUris )
		{
			// convert the child item to a Specification
			Specification aChildSpecification = this.getByUri(anUri);
			if (aChildSpecification != null) {
				// add child to the node Specification
				nodeSpecification.addSpecification(aChildSpecification);
				// add the Specification to the list
				aList.add(aChildSpecification);
			} else
				if (logger.isDebugEnabled()){ 
					logger.info("'" + anUri + "' is not loaded in specifications");
				}
		}
		return aList;
	}
	
	/**
	 * get the set of keys
	 * @return
	 */
	public Set<String> keySet(){
		return specifications.keySet();
	}
	/**
	 * gets the collection of all features
	 * @return
	 */
	public Collection<Specification> all(){
		return specifications.values();
	}

	/** 
	 * persist them all
	 * @return
	 */
	public synchronized boolean persist(){
		long i = 0;
		
		for (Bean aBean: specifications.values()){
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
		logger.info(i + " specifications persisted");
		return true;
	}
}
